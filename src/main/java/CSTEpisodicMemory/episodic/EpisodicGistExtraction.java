package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.categories.ObjectCategory;
import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import br.unicamp.cst.util.viewer.representation.idea.IdeaPanel;
import com.google.gson.Gson;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.implementations.SingleGraph;

import javax.swing.*;
import java.util.*;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class EpisodicGistExtraction extends Codelet {

    private Memory episodesMO;
    private Memory epLTM;
    private Memory locationMO;
    private Memory propertiesMO;

    private final Idea locCatAcomodate;
    private final Idea newLocCategoryGenerator;
    private Idea prevEp = null;
    private Idea prevLastEvent = null;
    private final Idea trackedPropertiesAssimilateAccommodateHabit;
    private int spatialLinkCount = 0;
    private boolean debug = true;
    private long processingStart;
    public static double objectCategoryThreshold = 0.9;

    public EpisodicGistExtraction(Idea locCatAcomodate, Idea newLocCategoryGenerator, Idea trackedPropertiesAssimilateAccommodateHabit) {
        this.name = "GistExtraction";
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

        synchronized (episodesMO){
            synchronized (epLTM) {
                GraphIdea epLTMGraph = (GraphIdea) epLTM.getI();
                Idea stories = (Idea) episodesMO.getI();
                List<Idea> memLocations = new ArrayList<>((ArrayList) locationMO.getI());
                List<Idea> propertiesCategories = new ArrayList<>((ArrayList) propertiesMO.getI());

                //System.out.println("--------------------");
                //System.out.println(IdeaHelper.csvPrint(epLTMGraph.graph,4).replace("\n"," "));

                //If there is a segmented episode
                if (stories.getL().size() > 1) {
                    processingStart = System.currentTimeMillis();
                    Idea oldestEpisode = stories.getL().get(0);
                    for (Idea ep : stories.getL()) {
                        if ((int) ep.getValue() < (int) oldestEpisode.getValue())
                            oldestEpisode = ep;
                    }
                    GraphIdea story = new GraphIdea(oldestEpisode.get("Story"));

                    Map<Idea, Idea> instanceNodeToMemoryNode = new HashMap<>();

                    //Extract locations
                    List<Idea> epLocations = story.getLocationNodes();

            /*for (Idea loc : epLocations) {
                if (memLocations.isEmpty()) {
                    Idea newLocCat = newLocCategoryGenerator.exec(getNodeContent(loc));
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

                    if (bestFitMembership >= 0.95) {
                        locCatAcomodate.getL().clear();
                        locCatAcomodate.add(getNodeContent(loc));
                        locCatAcomodate.exec(bestFitCategory);
                    } else {
                        Idea newLocCat = newLocCategoryGenerator.exec(getNodeContent(loc));
                        Idea newLocNode = epLTMGraph.insertLocationNode(newLocCat);
                        memLocations.add(newLocCat);
                        bestFitCategory = newLocCat;
                    }

                    instanceNodeToMemoryNode.put(loc, bestFitCategory);
                }
            }
             */

                    Idea startEvent = null;
                    Idea endEvent = null;
                    //Copy events and impulses to memory and add links
                    System.out.println("New Events: " + story.getEventNodes().size());
                    for (Idea eventNode : story.getEventNodes()) {
                        Idea eventContent = getNodeContent(eventNode);
                        Idea eventCategory = (Idea) eventContent.getValue();
                        Idea LTEventContent = new Idea(eventContent.getName(),
                                eventCategory,
                                "Episode", 1);
                        LTEventContent.add(new Idea("Start",
                                eventContent.getL().get(0).get("TimeStamp").getValue(),
                                "TimeStep", 1));
                        LTEventContent.add(new Idea("End",
                                eventContent.getL().get(1).get("TimeStamp").getValue(),
                                "TimeStep", 1));
                        Idea LTEventNode = epLTMGraph.insertEventNode(LTEventContent);
                        instanceNodeToMemoryNode.put(eventNode, LTEventNode);

                        //Event Object
                        Optional<Idea> initialObject = eventContent.getL().get(0).getL().stream()
                                .filter(o -> !o.getName().equals("TimeStamp"))
                                .findFirst();
                        Optional<Idea> finalObject = eventContent.getL().get(1).getL().stream()
                                .filter(o -> !o.getName().equals("TimeStamp"))
                                .findFirst();

                        Idea initialNode;
                        if (initialObject.isPresent())
                            initialNode = makeSpatialLink(initialObject.get(), epLTMGraph);
                        else
                            initialNode = epLTMGraph.insertObjectNode(new Idea("Null Object"));
                        Idea finalNode;
                        if (finalObject.isPresent())
                            finalNode = makeSpatialLink(finalObject.get(), epLTMGraph);
                        else
                            finalNode = epLTMGraph.insertObjectNode(new Idea("Null Object"));
                        epLTMGraph.insertLink(LTEventNode, initialNode, "Initial");
                        epLTMGraph.insertLink(LTEventNode, finalNode, "Final");

                        //Get start and end properties
                        //Idea catParam = trackedPropertiesAssimilateAccommodateHabit.get("Input_Category");
                        //catParam.getL().clear();
                        /////catParam.add(eventCategory.get("ObservedObject"));
                        //catParam.add(eventCategory.get("properties"));
                        //Idea catss = trackedPropertiesAssimilateAccommodateHabit.get("categories");
                        //catss.setL(propertiesCategories);

                        //Idea eventPropertiesCategories = trackedPropertiesAssimilateAccommodateHabit.exec(eventContent);
                        //Idea propertyStartNode = epLTMGraph.insertNode((Idea) eventPropertiesCategories.get("0").getValue(), "Property");
                        //Idea propertyEndNode = epLTMGraph.insertNode((Idea) eventPropertiesCategories.get("1").getValue(), "Property");
                        //epLTMGraph.insertLink(LTEventNode, propertyStartNode, "Initial");
                        //epLTMGraph.insertLink(LTEventNode, propertyEndNode, "Final");

                        //Get grid location
                        List<Idea> grids = story.getChildrenWithLink(eventNode, "GridPlace");
                        if (grids.isEmpty()) {
                            JPanel panel = new JPanel();
                            Idea ideaToShow = new Gson().fromJson(new Gson().toJson(story.graph), Idea.class);
                            IdeaPanel ip = new IdeaPanel(story.graph, false);
                            ip.updateTree();
                            ip.expandAllNodes();
                            panel.add(ip);
                            panel.revalidate();
                            panel.setVisible(true);
                        }
                        Idea gridPlace = getNodeContent(grids.get(0));
                        Idea gridNode = epLTMGraph.insertLocationNode(gridPlace);
                        epLTMGraph.insertLink(LTEventNode, gridNode, "GridPlace");

                        if (startEvent == null) {
                            startEvent = LTEventNode;
                        } else {
                            long bestStart = (long) getNodeContent(startEvent).get("Start").getValue();
                            long currStart = (long) eventContent.getL().get(0)
                                    .get("TimeStamp").getValue();
                            if (currStart < bestStart)
                                startEvent = LTEventNode;
                        }

                        if (endEvent == null) {
                            endEvent = LTEventNode;
                        } else {
                            long bestEnd = (long) getNodeContent(startEvent).get("End").getValue();
                            long currEnd = (long) eventContent.getL().get(1)
                                    .get("TimeStamp").getValue();
                            if (currEnd > bestEnd)
                                endEvent = LTEventNode;
                        }
                    }
                    for (Idea contextNode : story.getContextNodes()) {
                        Idea contextContent = getNodeContent(contextNode);
                        Idea LTContextNode = epLTMGraph.insertContextNode(contextContent);
                        insertContextWithSpatialLink(contextNode, story, contextContent, epLTMGraph, LTContextNode, instanceNodeToMemoryNode);
                    }

                    for (Idea objectNode : story.getObjectNodes()) {
                        Idea objectContent = getNodeContent(objectNode);
                        if (objectContent.get("Novelty") != null)
                            if ((double) objectContent.get("Novelty").getValue() >= 0.9) {
                                Idea spatialLinkNode = makeSpatialLink(objectContent, epLTMGraph);
                                instanceNodeToMemoryNode.put(objectNode, spatialLinkNode);
                                //insertContextWithSpatialLink(objectNode, story, objectContent, epLTMGraph, instanceNodeToMemoryNode);
                            }
                    }

                    //Clone links to LTM
                    for (Idea eventNode : story.getEventNodes()) {
                        Idea eventMemoryNode = instanceNodeToMemoryNode.get(eventNode);
                        Map<String, List<Idea>> linksOut = story.getSuccesors(eventNode);
                        for (String linkType : linksOut.keySet()) {
                            for (Idea node : linksOut.get(linkType)) {
                                Idea linkedMemoryNode = instanceNodeToMemoryNode.get(node);
                                if (linkedMemoryNode != null)
                                    epLTMGraph.insertLink(eventMemoryNode, linkedMemoryNode, linkType);
                            }
                        }
                    }

                    //Create an Episode node
                    Idea ep = new Idea("Episode" + (int) oldestEpisode.getValue(), oldestEpisode.getValue(), "Episode", 1);
                    epLTMGraph.insertEpisodeNode(ep);
                    epLTMGraph.insertLink(ep, startEvent, "Begin");
                    epLTMGraph.insertLink(ep, endEvent, "End");
                    if (prevEp != null) {
                        epLTMGraph.insertLink(prevEp, ep, "Next");
                        //epLTMGraph.insertLink(prevLastEvent, startEvent, "Next");
                    }
                    prevEp = ep;
                    prevLastEvent = endEvent;

                    if (debug){
                        evaluateMemory(epLTMGraph, (int) oldestEpisode.getValue());
                    }

                    stories.getL().remove(oldestEpisode);
                }
            }
            //synchronized (locationMO) {
            //    locationMO.setI(memLocations);
            //}
            //synchronized (propertiesMO) {
            //    propertiesMO.setI(propertiesCategories);
            //}
        }
    }

    private void insertContextWithSpatialLink(Idea contextNode, GraphIdea story, Idea contextContent, GraphIdea epLTMGraph, Idea LTContextNode, Map<Idea, Idea> instanceNodeToMemoryNode) {
        List<Idea> contextPos = story.getChildrenWithLink(contextNode, "GridPlace");
        if (!contextPos.isEmpty()){
            List<Idea> LTPosNodes = new ArrayList<>();
            for (Idea pos : contextPos){
                LTPosNodes.add(epLTMGraph.insertLocationNode(pos));
            }
            contextContent.getL().remove(contextContent.get("Occupation"));
            Idea spatialLinkNode = epLTMGraph.commomParent(LTContextNode, LTPosNodes);
            if (spatialLinkNode == null){
                Idea spatialLink = new Idea("SpatialLink" + spatialLinkCount++, null, "Link", 1);
                spatialLinkNode = epLTMGraph.insertContextNode(spatialLink);
                epLTMGraph.insertLink(spatialLinkNode, LTContextNode, "Object");
                for (Idea LTPosNode : LTPosNodes)
                    epLTMGraph.insertLink(spatialLinkNode, LTPosNode, "GridPlace");
            }
            instanceNodeToMemoryNode.put(contextNode, spatialLinkNode);
        } else {
            instanceNodeToMemoryNode.put(contextNode, LTContextNode);
            //System.out.println(getNodeContent(contextNode));
            //JPanel panel = new JPanel();
            //IdeaPanel ip = new IdeaPanel(story.graph, false);
            //ip.updateTree();
            //ip.expandAllNodes();
            //panel.add(ip);
            //panel.revalidate();
            //panel.setVisible(true);
        }
    }

    private Idea makeSpatialLink(Idea objectContent, GraphIdea epLTMGraph) {
        Idea initialNode;
            Idea objOccupation = objectContent.get("Occupation");
            if (objectContent.get("Position") != null)
                objectContent.getL().remove(objectContent.get("Position"));
            if (objectContent.get("TimeStamp") != null)
                objectContent.getL().remove(objectContent.get("TimeStamp"));
            if (objectContent.get("Novelty") != null)
                objectContent.getL().remove(objectContent.get("Novelty"));
            if (objOccupation != null){
                objectContent.getL().remove(objOccupation);
                //Idea objNode = epLTMGraph.insertObjectNode(objectContent);
                Idea objNode = assimilateObject(objectContent, epLTMGraph);
                List<Idea> posNodes = new ArrayList<>();
                for (Idea objGrid : objOccupation.getL()){
                    posNodes.add(epLTMGraph.insertLocationNode(objGrid));
                }
                Idea spatialLinkNode = epLTMGraph.commomParent(objNode, posNodes);
                if (spatialLinkNode == null){
                    Idea spatialLink = new Idea("SpatialLink" + spatialLinkCount++, null, "Link", 1);
                    spatialLinkNode = epLTMGraph.insertContextNode(spatialLink);
                    epLTMGraph.insertLink(spatialLinkNode, objNode, "Object");
                    for (Idea posNode : posNodes)
                        epLTMGraph.insertLink(spatialLinkNode, posNode, "GridPlace");
                }
                initialNode = spatialLinkNode;
            } else {
                initialNode = epLTMGraph.insertObjectNode(objectContent);
            }
        return initialNode;
    }

    private Idea assimilateObject(Idea objectContent, GraphIdea epLTMGraph) {
        double bestMem = 0;
        Idea bestObjCat = null;
        for (Idea objNode : epLTMGraph.getObjectNodes()){
            Idea objCat = getNodeContent(objNode);
            double mem = objCat.membership(objectContent);
            if (mem > bestMem) {
                bestMem = mem;
                bestObjCat = objCat;
            }
        }
        if (bestObjCat != null){
            if (bestMem >= objectCategoryThreshold){
                if (bestMem >= (0.5 + objectCategoryThreshold/2.0)) {
                    ObjectCategory cat = (ObjectCategory) bestObjCat.getValue();
                    cat.insertExamplar(objectContent);
                    bestObjCat.setValue(cat);
                }
                return epLTMGraph.getNodeFromContent(bestObjCat);
            }
        }
        ObjectCategory newObjCatFunc = new ObjectCategory(objectContent);
        Idea newObjCat = new Idea(objectContent.getName(), newObjCatFunc, "AbstractObject", 2);
        return epLTMGraph.insertObjectNode(newObjCat);
    }

    private void evaluateMemory(GraphIdea epLTMGraph, int episodeId) {
        System.out.println("--Armazenamento--");
        //System.out.println("Número de nós: " + epLTMGraph.getNodes().size());
        //System.out.println("Detected Events: " +  epLTMGraph.getEventNodes().size());
        long links = epLTMGraph.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count();
        //System.out.println("Contextos: " + (epLTMGraph.getContextNodes().size() - links));
        //System.out.println("Objetos: " + epLTMGraph.getObjectNodes().size());
        //System.out.println("Links espaciais: " + links);
        //System.out.println("Número de células de grade: " + epLTMGraph.getLocationNodes().size());
        //System.out.println("Episódios: " +  epLTMGraph.getEpisodeNodes().size());
        System.out.println("Episode Gist "+ episodeId + " & " + epLTMGraph.getNodes().size() + " & " + epLTMGraph.getEventNodes().size() + " & " + epLTMGraph.getObjectNodes().size() + " & " + links);

        SingleGraph measureGraph = new SingleGraph("Measure");
        long start = System.currentTimeMillis();
        for (Idea node : epLTMGraph.getNodes()){
            measureGraph.addNode(node.getName()+node.getValue());
        }
        for (Idea node : epLTMGraph.getNodes()){
            String nodeName = node.getName()+node.getValue();
            for (Map.Entry<String,List<Idea>> link : epLTMGraph.getSuccesors(node).entrySet()){
                for (Idea dest : link.getValue()){
                    String destName = dest.getName()+dest.getValue();
                    try {
                        measureGraph.addEdge(nodeName + "_" + destName, nodeName, destName, true);
                    } catch (IdAlreadyInUseException ignored){
                    }
                }
            }
        }

        System.out.println("--Graph Measure---");
        System.out.println("EPLTM Measure " +
                (start-processingStart) + " & "
                        +(System.currentTimeMillis()-start)+ " & "
                +Toolkit.averageDegree(measureGraph)+ " & "
                +Toolkit.density(measureGraph)+ " & "
                +Toolkit.diameter(measureGraph)+ " & "
                +Toolkit.averageClusteringCoefficient(measureGraph));
        //System.out.println("Tempo de Processamento: " + (start-processingStart));
        //System.out.println("Tempo de Avaliação: " + (System.currentTimeMillis()-start));
        //System.out.println("EPLTM grau: " + Toolkit.averageDegree(measureGraph));
        //System.out.println("EPLTM densidade: " + Toolkit.density(measureGraph));
        //System.out.println("EPLTM diâmetro: " + Toolkit.diameter(measureGraph));
        //System.out.println("EPLTM clustering: " + Toolkit.averageClusteringCoefficient(measureGraph));
        System.out.println("------------------");
    }
}
