package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class EpisodicGistExtraction extends Codelet {

    private Memory episodesMO;
    private Memory epLTM;
    private Memory locationMO;

    private Idea locCatAcomodate;
    private Idea newLocCategoryGenerator;
    private Idea prevEp = null;

    public EpisodicGistExtraction(Idea locCatAcomodate, Idea newLocCategoryGenerator) {
        this.locCatAcomodate = locCatAcomodate;
        this.newLocCategoryGenerator = newLocCategoryGenerator;
    }

    @Override
    public void accessMemoryObjects() {
        episodesMO = (MemoryObject) getInput("STORY");
        epLTM = (MemoryObject) getOutput("EPLTM");
        locationMO = (MemoryObject) getInput("LOCATION");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {

        GraphIdea epLTMGraph = (GraphIdea) epLTM.getI();
        Idea stories = (Idea) episodesMO.getI();
        List<Idea> memLocations = new ArrayList<>((ArrayList) locationMO.getI());

        //System.out.println("--------------------");
        //System.out.println(IdeaHelper.csvPrint(epLTMGraph.graph,4).replace("\n"," "));

        //If there is a segmented episode
        if (stories.getL().size() > 1) {
            Idea oldestEpisode = stories.getL().get(0);
            for (Idea ep : stories.getL()) {
                if ((int) ep.getValue() < (int) oldestEpisode.getValue())
                    oldestEpisode = ep;
            }
            GraphIdea story = new GraphIdea(oldestEpisode.get("Story"));

            //Extract locations
            List<Idea> epLocations = story.getLocationNodes();

            Map<Idea, Idea> categoriesOfEpisodeLocations = new HashMap<>();
            for (Idea loc : epLocations) {
                if (memLocations.size() == 0) {
                    Idea newLocCat = newLocCategoryGenerator.exec0(GraphIdea.getNodeContent(loc));
                    Idea firstLoc = epLTMGraph.insertLocationNode(newLocCat);
                    memLocations.add(newLocCat);
                    categoriesOfEpisodeLocations.put(loc, firstLoc);
                } else {

                    Idea bestFitCategory = memLocations.get(0);
                    double bestFitMembership = 0;
                    for (Idea cat : memLocations) {
                        double catMembership = cat.membership(GraphIdea.getNodeContent(loc));
                        if (catMembership > bestFitMembership) {
                            bestFitCategory = cat;
                            bestFitMembership = catMembership;
                        }
                    }

                    if (bestFitMembership >= 0.8) {
                        locCatAcomodate.getL().clear();
                        locCatAcomodate.add(GraphIdea.getNodeContent(loc));
                        locCatAcomodate.exec0(bestFitCategory);
                    } else {
                        Idea newLocCat = newLocCategoryGenerator.exec0(GraphIdea.getNodeContent(loc));
                        Idea newLocNode = epLTMGraph.insertLocationNode(newLocCat);
                        memLocations.add(newLocCat);
                        bestFitCategory = newLocCat;
                    }

                    categoriesOfEpisodeLocations.put(loc, bestFitCategory);
                }
            }

            Idea startEvent = null;
            Idea endEvent = null;
            //Copy events and impulses to memory and add links
            for (Idea eventNode : story.getEventNodes()) {
                Idea eventContent = GraphIdea.getNodeContent(eventNode);
                Idea LTEventNode = epLTMGraph.insertEventNode(GraphIdea.getNodeContent(eventNode));
                if (startEvent == null) {
                    startEvent = LTEventNode;
                } else {
                    long bestStart = (long) GraphIdea.getNodeContent(startEvent).getL().get(0)
                            .get("TimeStamp").getValue();
                    long currStart = (long) eventContent.getL().get(0)
                            .get("TimeStamp").getValue();
                    if (currStart < bestStart)
                        startEvent = LTEventNode;
                }

                if (endEvent == null){
                    endEvent = LTEventNode;
                } else {
                    long bestEnd = (long) GraphIdea.getNodeContent(startEvent).getL().get(1)
                            .get("TimeStamp").getValue();
                    long currEnd = (long) eventContent.getL().get(1)
                            .get("TimeStamp").getValue();
                    if (currEnd > bestEnd)
                        endEvent = LTEventNode;
                }
            }
            for (Idea impulseNode : story.getContextNodes()){
                epLTMGraph.insertContextNode(((Idea) impulseNode.get("Content").getValue()).clone());
            }

            //Clone links to LTM
            for (Idea eventNode : story.getEventNodes()) {
                Map<String, List<Idea>> linksOut = story.getSuccesors(eventNode);
                for (String linkType : linksOut.keySet()){
                    for (Idea node : linksOut.get(linkType)){
                        epLTMGraph.insetLink((Idea) eventNode.get("Content").getValue(),
                                categoriesOfEpisodeLocations.getOrDefault(node, (Idea) node.get("Content").getValue()),
                                linkType);
                    }
                }
            }

            //Create an Episode node
            Idea ep = new Idea("Episode", (int) oldestEpisode.getValue(), "Episode", 1);
            epLTMGraph.insertEpisodeNode(ep);
            epLTMGraph.insetLink(ep, startEvent, "Begin");
            epLTMGraph.insetLink(ep, endEvent, "End");
            if (prevEp == null){
                prevEp = ep;
            } else {
                epLTMGraph.insetLink(prevEp, ep, "Next");
                prevEp = ep;
            }

            stories.getL().remove(oldestEpisode);
            locationMO.setI(memLocations);
        }
    }
}
