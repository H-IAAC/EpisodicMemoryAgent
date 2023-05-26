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

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class EpisodicGistExtraction extends Codelet {

    private Memory episodesMO;
    private Memory epLTM;
    private Memory locationMO;

    private Idea locCatAcomodate;
    private Idea newLocCategoryGenerator;
    private Idea prevEp = null;
    private Idea prevLastEvent = null;

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

            Map<Idea, Idea> instanceNodeToMemoryNode = new HashMap<>();

            //Extract locations
            List<Idea> epLocations = story.getLocationNodes();
            for (Idea loc : epLocations) {
                if (memLocations.size() == 0) {
                    Idea newLocCat = newLocCategoryGenerator.exec0(getNodeContent(loc));
                    Idea firstLoc = epLTMGraph.insertLocationNode(newLocCat);
                    memLocations.add(newLocCat);
                    instanceNodeToMemoryNode.put(loc, firstLoc);
                } else {

                    Idea bestFitCategory = memLocations.get(0);
                    double bestFitMembership = 0;
                    for (Idea cat : memLocations) {
                        double catMembership = cat.membership(getNodeContent(loc));
                        if (catMembership > bestFitMembership) {
                            bestFitCategory = cat;
                            bestFitMembership = catMembership;
                        }
                    }

                    if (bestFitMembership >= 0.8) {
                        locCatAcomodate.getL().clear();
                        locCatAcomodate.add(getNodeContent(loc));
                        locCatAcomodate.exec0(bestFitCategory);
                    } else {
                        Idea newLocCat = newLocCategoryGenerator.exec0(getNodeContent(loc));
                        Idea newLocNode = epLTMGraph.insertLocationNode(newLocCat);
                        memLocations.add(newLocCat);
                        bestFitCategory = newLocCat;
                    }

                    instanceNodeToMemoryNode.put(loc, bestFitCategory);
                }
            }

            Idea startEvent = null;
            Idea endEvent = null;
            //Copy events and impulses to memory and add links
            for (Idea eventNode : story.getEventNodes()) {
                Idea eventContent = getNodeContent(eventNode);
                Idea LTEventContent = new Idea(eventContent.getName(), eventContent.getValue(), "Episode", 1);
                LTEventContent.add(new Idea("Start", eventContent.getL().get(0).get("TimeStamp").getValue(), "TimeStep", 1));
                LTEventContent.add(new Idea("End", eventContent.getL().get(1).get("TimeStamp").getValue(), "TimeStep", 1));
                Idea LTEventNode = epLTMGraph.insertEventNode(LTEventContent);
                instanceNodeToMemoryNode.put(eventNode, LTEventNode);

                Idea eventsSelfObject = eventContent.getL().get(1).get("Self");
                if (eventsSelfObject != null){
                    Idea LTEventsSelfObject = new Idea("Self", null, "Object", 1);
                    if (eventContent.getValue().equals("Rotate")){
                        eventsSelfObject.get("Pitch");
                    }
                }

                if (startEvent == null) {
                    startEvent = LTEventNode;
                } else {
                    long bestStart = (long) getNodeContent(startEvent).get("Start").getValue();
                    long currStart = (long) eventContent.getL().get(0)
                            .get("TimeStamp").getValue();
                    if (currStart < bestStart)
                        startEvent = LTEventNode;
                }

                if (endEvent == null){
                    endEvent = LTEventNode;
                } else {
                    long bestEnd = (long) getNodeContent(startEvent).get("End").getValue();
                    long currEnd = (long) eventContent.getL().get(1)
                            .get("TimeStamp").getValue();
                    if (currEnd > bestEnd)
                        endEvent = LTEventNode;
                }
            }
            for (Idea impulseNode : story.getContextNodes()){
                Idea LTContextNode = epLTMGraph.insertContextNode(getNodeContent(impulseNode).clone());
                instanceNodeToMemoryNode.put(impulseNode, LTContextNode);
            }

            //Clone links to LTM
            for (Idea eventNode : story.getEventNodes()) {
                Idea eventMemoryNode = instanceNodeToMemoryNode.get(eventNode);
                Map<String, List<Idea>> linksOut = story.getSuccesors(eventNode);
                for (String linkType : linksOut.keySet()){
                    for (Idea node : linksOut.get(linkType)){
                        Idea linkedMemoryNode = instanceNodeToMemoryNode.get(node);
                        epLTMGraph.insetLink(eventMemoryNode, linkedMemoryNode, linkType);
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
                prevLastEvent = endEvent;
            } else {
                epLTMGraph.insetLink(prevEp, ep, "Next");
                epLTMGraph.insetLink(prevLastEvent, startEvent, "Next");
                prevEp = ep;
                prevLastEvent = endEvent;
            }

            stories.getL().remove(oldestEpisode);
            synchronized (locationMO) {
                locationMO.setI(memLocations);
            }
        }
    }
}
