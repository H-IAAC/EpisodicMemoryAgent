package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class EpisodicGistExtraction extends Codelet {

    private Memory episodesMO;
    private Memory epLTM;
    private Memory locationMO;
    private Memory propertiesMO;

    private Idea locCatAcomodate;
    private Idea newLocCategoryGenerator;
    private Idea prevEp = null;
    private Idea prevLastEvent = null;
    private Idea trackedPropertiesAssimilateAccommodateHabit;

    public EpisodicGistExtraction(Idea locCatAcomodate, Idea newLocCategoryGenerator, Idea trackedPropertiesAssimilateAccommodateHabit) {
        this.locCatAcomodate = locCatAcomodate;
        this.newLocCategoryGenerator = newLocCategoryGenerator;
        this.trackedPropertiesAssimilateAccommodateHabit = trackedPropertiesAssimilateAccommodateHabit;
    }

    @Override
    public void accessMemoryObjects() {
        episodesMO = (MemoryObject) getInput("STORY");
        epLTM = (MemoryObject) getOutput("EPLTM");
        locationMO = (MemoryObject) getInput("LOCATION");
        propertiesMO = (MemoryObject) getInput("PROPERTIES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {

        GraphIdea epLTMGraph = (GraphIdea) epLTM.getI();
        Idea stories = (Idea) episodesMO.getI();
        List<Idea> memLocations = new ArrayList<>((ArrayList) locationMO.getI());
        List<Idea> propertiesCategories = new ArrayList<>((ArrayList) propertiesMO.getI());

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
                Idea eventCategory = (Idea) eventContent.getValue();
                Idea LTEventContent = new Idea(eventContent.getName(),
                        eventCategory.getName(),
                        "Episode", 1);
                LTEventContent.add(new Idea("Start",
                        eventContent.getL().get(0).get("TimeStamp").getValue(),
                        "TimeStep", 1));
                LTEventContent.add(new Idea("End",
                        eventContent.getL().get(1).get("TimeStamp").getValue(),
                        "TimeStep", 1));
                Idea LTEventNode = epLTMGraph.insertEventNode(LTEventContent);
                instanceNodeToMemoryNode.put(eventNode, LTEventNode);

                Idea catParam = trackedPropertiesAssimilateAccommodateHabit.get("Input_Category");
                catParam.getL().clear();
                catParam.add(eventCategory.get("ObservedObject"));
                catParam.add(eventCategory.get("properties"));
                Idea catss = trackedPropertiesAssimilateAccommodateHabit.get("categories");
                catss.setL(propertiesCategories);

                List<Idea> eventPropertiesCategories = trackedPropertiesAssimilateAccommodateHabit.exec(eventContent);
                Idea propertyStartNode = epLTMGraph.insertNode(eventPropertiesCategories.get(0), "Property");
                Idea propertyEndNode = epLTMGraph.insertNode(eventPropertiesCategories.get(1), "Property");
                epLTMGraph.insertLink(LTEventNode, propertyStartNode, "Initial");
                epLTMGraph.insertLink(LTEventNode, propertyEndNode, "Final");

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
                        epLTMGraph.insertLink(eventMemoryNode, linkedMemoryNode, linkType);
                    }
                }
            }

            //Create an Episode node
            Idea ep = new Idea("Episode" + (int) oldestEpisode.getValue(), (int) oldestEpisode.getValue(), "Episode", 1);
            epLTMGraph.insertEpisodeNode(ep);
            epLTMGraph.insertLink(ep, startEvent, "Begin");
            epLTMGraph.insertLink(ep, endEvent, "End");
            if (prevEp == null){
                prevEp = ep;
                prevLastEvent = endEvent;
            } else {
                epLTMGraph.insertLink(prevEp, ep, "Next");
                epLTMGraph.insertLink(prevLastEvent, startEvent, "Next");
                prevEp = ep;
                prevLastEvent = endEvent;
            }

            stories.getL().remove(oldestEpisode);
            synchronized (locationMO) {
                locationMO.setI(memLocations);
            }
            synchronized (propertiesMO) {
                propertiesMO.setI(propertiesCategories);
            }
        }
    }
}
