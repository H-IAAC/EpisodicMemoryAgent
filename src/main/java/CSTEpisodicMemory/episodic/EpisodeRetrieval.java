package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;
import static CSTEpisodicMemory.episodic.EpisodeBinding.temporalRelation;

public class EpisodeRetrieval extends Codelet {

    private Memory cueMO;
    private Memory epltm;
    //private Memory propertiesMO;
    private Memory recallMO;

    @Override
    public void accessMemoryObjects() {
        cueMO = (MemoryObject) getInput("CUE");
        epltm = (MemoryObject) getInput("EPLTM");
        //propertiesMO = (MemoryObject) getInput("PROPERTIES");
        recallMO = (MemoryObject) getOutput("RECALL");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        long sTotal = System.currentTimeMillis();
        GraphIdea cue;
        GraphIdea epGraph;
        GraphIdea recalledEpisode = new GraphIdea(new Idea("Failed"));

        //List<Idea> propertiesCat = new ArrayList<>();
        synchronized (cueMO) {
            cue = new GraphIdea((GraphIdea) cueMO.getI());
        }
        synchronized (epltm) {
            epGraph = (GraphIdea) epltm.getI();

            //synchronized (propertiesMO) {
            //    for (Idea cat : (ArrayList<Idea>) propertiesMO.getI())
            //        propertiesCat.add(IdeaHelper.cloneIdea(cat));
            //}

            List<Idea> eventNodes = new ArrayList<>();

            //Filter by desired context
            List<Idea> cueContext = cue.getObjectNodes();
            for (Idea context : cueContext) {
                List<Idea> similarObjectsMemory = epGraph.getAllNodesWithSimilarContent(getNodeContent(context), 0.68);

                for (Idea similarObject : similarObjectsMemory) {
                    for (Idea spatialLink : epGraph.getPredecessors(similarObject).getOrDefault("Object", new ArrayList<>())) {
                        List<Idea> eventsWithObject = epGraph.getPredecessors(spatialLink).getOrDefault("ObjectContext", new ArrayList<>());
                        eventNodes.addAll(eventsWithObject);
                    }
                }
            }

            if (eventNodes.isEmpty()) eventNodes = epGraph.getEventNodes();

            //Cue with events nodes
            Map<Idea, List<Idea>> bestMatches = new HashMap<>();
            List<Idea> cueEvents = cue.getEventNodes();
            for (Idea eventNode : cueEvents) {
                Idea eventContent = getNodeContent(eventNode);
                Idea eventCategory = (Idea) eventContent.getValue();

                if (eventContent.getL().isEmpty()) {
                    if (eventCategory != null) {
                        List<Idea> sameCatEvents = eventNodes.stream().filter(e -> getNodeContent(e).getValue().equals(eventCategory)).collect(Collectors.toList());
                        if (!cue.getEpisodeNodes().isEmpty()) {
                            if (cue.getPredecessors(eventNode).containsKey("Begin")) {
                                sameCatEvents = sameCatEvents.stream().filter(e -> epGraph.getPredecessors(e).containsKey("Begin")).collect(Collectors.toList());
                            }
                            if (cue.getPredecessors(eventNode).containsKey("End")) {
                                sameCatEvents = sameCatEvents.stream().filter(e -> epGraph.getPredecessors(e).containsKey("End")).collect(Collectors.toList());
                            }
                        }
                        sameCatEvents.sort(Comparator.comparingInt(n -> (int) n.get("Coordinate").getValue()));
                        Collections.reverse(sameCatEvents);
                        bestMatches.put(eventNode, sameCatEvents);
                    }
                } else {
                    epGraph.resetActivations();
                    String observedObject = (String) eventCategory.get("ObservedObject").getValue();

                    Idea initialObjectState = eventContent.getL().get(0).getL().stream()
                            .filter(o -> !o.getName().equals("TimeStamp"))
                            .findFirst().orElse(null);
                    Idea finalObjectState = eventContent.getL().get(1).getL().stream()
                            .filter(o -> !o.getName().equals("TimeStamp"))
                            .findFirst().orElse(null);

                    if (initialObjectState != null && finalObjectState != null) {

                        List<Idea> eventsNodesFilteredByInitialObject = filterEventsWithLinkToObject(epGraph, eventNodes, initialObjectState, "Initial");
                        List<Idea> eventsNodesFilteredByFinalObject = filterEventsWithLinkToObject(epGraph, eventNodes, finalObjectState, "Final");
                        //Idea bestInitialProperty = propertiesCat.stream().max(Comparator.comparingDouble(idea -> idea.membership(initialObjectState))).orElse(null);
                        //if (bestInitialProperty != null && bestInitialProperty.membership(initialObjectState) == 1) {
                        //    Idea node = epGraph.setNodeActivation(bestInitialProperty, 1.0);
                        //    epGraph.propagateActivations(node, new ArrayList<>(), Arrays.asList("Initial", "Final"));
                        //}
                        //Idea bestFinalProperty = propertiesCat.stream().max(Comparator.comparingDouble(idea -> idea.membership(finalObjectState))).orElse(null);
                        //if (bestFinalProperty != null && bestFinalProperty.membership(finalObjectState) == 1) {
                        //    Idea node = epGraph.setNodeActivation(bestFinalProperty, 1.0);
                        //    epGraph.propagateActivations(node, new ArrayList<>(), Arrays.asList("Initial", "Final"));
                        //}

                        //eventNodes.sort(Comparator.comparingDouble(epGraph::getNodeActivation));
                        //Collections.reverse(eventNodes);

                        List<Idea> eventBestMatches = new ArrayList<>();
                        if (eventsNodesFilteredByInitialObject.stream().anyMatch(eventsNodesFilteredByFinalObject::contains)) {
                            eventsNodesFilteredByInitialObject.retainAll(eventsNodesFilteredByFinalObject);
                            eventBestMatches = eventsNodesFilteredByInitialObject;
                        } else {
                            eventBestMatches.addAll(eventsNodesFilteredByInitialObject);
                            eventBestMatches.addAll(eventsNodesFilteredByFinalObject);
                        }
                        //int numMatchedProperties = 1;
                        //for (Idea eventMem : eventNodes) {
                        //    if (epGraph.getNodeActivation(eventMem) > 0) {
                        //        int c = 0;
                        //        if (IdeaHelper.match(getNodeContent(epGraph.getChildrenWithLink(eventMem, "Initial").get(0)), bestInitialProperty))
                        //            c++;
                        //        if (IdeaHelper.match(getNodeContent(epGraph.getChildrenWithLink(eventMem, "Final").get(0)), bestFinalProperty))
                        //            c++;

                        //        if (c == numMatchedProperties) {
                        //            eventBestMatches.add(eventMem);
                        //        }
                        //        if (c > numMatchedProperties) {
                        //            numMatchedProperties = c;
                        //            eventBestMatches = new ArrayList<>();
                        //            eventBestMatches.add(eventMem);
                        //        }
                        //    }
                        //}
                        bestMatches.put(eventNode, eventBestMatches);
                    }
                }
            }

            if (bestMatches.isEmpty()) {
                if (eventNodes.size() < epGraph.getEventNodes().size()) {
                    //Recall episodes from only the objects context
                    epGraph.resetActivations();
                    for (Idea event : eventNodes) {
                        epGraph.setNodeActivation(event, 1.0);
                        epGraph.propagateActivations(event, Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                    }

                    List<Idea> activatedEpisodes = epGraph.getEpisodeNodes().stream()
                            .filter(e->epGraph.getNodeActivation(e) > 0)
                            //.sorted(Comparator.comparingDouble(epGraph::getNodeActivation))
                            .collect(Collectors.toList());

                    GraphIdea recall = new GraphIdea(new Idea("Recall"));
                    for (Idea ep : activatedEpisodes) {
                        GraphIdea episodeGraph = epGraph.getEpisodeSubGraph(ep);
                        recall.addAll(episodeGraph);
                    }
                    recalledEpisode = recall;
                }
            } else if (bestMatches.size() == 1) {
                List<Idea> bestMemEvents = bestMatches.get(cueEvents.get(0));
                if (bestMemEvents.size() == 1) {
                    epGraph.resetActivations();
                    epGraph.setNodeActivation(bestMemEvents.get(0), 1.0);
                    epGraph.propagateActivations(bestMemEvents.get(0), Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                    Idea activatedEpisode = epGraph.getEpisodeNodes().stream().max(Comparator.comparingDouble(epGraph::getNodeActivation)).get();
                    recalledEpisode = epGraph.getEpisodeSubGraph(activatedEpisode);
                }
            } else if (bestMatches.size() > 1) {
                LinkedList<Idea> events = new LinkedList<>(bestMatches.keySet());
                ///int[] p = new int[bestMatches.size()];
                ///Arrays.fill(p, 0);
                Map<Idea, Integer> currCheckPos = bestMatches.keySet().stream().collect(Collectors.toMap(e -> e, e -> 0));
                Map<List<Idea>, Double> validSequenceTimeInterval = new HashMap<>();
                boolean valid;
                int totalCombinations = bestMatches.values().stream().map(List::size).reduce((a, b) -> a * b).get();
                for (int c = 0; c < totalCombinations; c++) {
                    int k = c;
                    for (Idea event : currCheckPos.keySet()) {
                        int totalInPosI = bestMatches.get(event).size();
                        currCheckPos.put(event, k % totalInPosI);
                        k = k / totalInPosI;
                    }
                    /// for (int i = p.length - 1; i >= 0; i--) {
                    ///     int totalInPosI = bestMatches.get(events.get(i)).size();
                    ///     p[i] = k % totalInPosI;
                    ///     k = k / totalInPosI;
                    /// }

                    LinkedList<Idea> recalls = new LinkedList<>();
                    for (Idea event : currCheckPos.keySet()) {
                        recalls.add(bestMatches.get(event).get(currCheckPos.get(event)));
                    }
                    ///for (int i = 0; i < events.size(); i++) {
                    ///    recalls.add(bestMatches.get(events.get(i)).get(p[i]));
                    ///}
                    valid = true;
                    //Check episodes
                    for (Idea ep : cue.getEpisodeNodes()) {
                        List<Idea> beginLink = cue.getChildrenWithLink(ep, "Begin");
                        Idea beginEp = null;
                        if (!beginLink.isEmpty()) {
                            Idea beginCue = beginLink.get(0);
                            beginEp = epGraph.getPredecessors(bestMatches.get(beginCue).get(currCheckPos.get(beginCue))).get("Begin").get(0);
                        }
                        List<Idea> endLink = cue.getChildrenWithLink(ep, "End");
                        Idea endEp = null;
                        if (!endLink.isEmpty()) {
                            Idea endCue = endLink.get(0);
                            endEp = epGraph.getPredecessors(bestMatches.get(endCue).get(currCheckPos.get(endCue))).get("End").get(0);
                        }
                        if (beginEp != endEp) {
                            valid = false;
                            break;
                        }
                    }

                    //Check context
                    //for (Idea context : cueContext){
                    //    Map<String, List<Idea>> links = cue.getPredecessors(context);
                    //    for (Map.Entry link : links.entrySet()){
                    //        for (Idea parent : (List<Idea>) link.getValue()){
                    //            List<Idea> memsContext = epGraph.getChildrenWithLink(bestMatches.get(parent).get(currCheckPos.get(parent)), (String) link.getKey());
                    //            if (memsContext.isEmpty()){
                    //                valid = false;
                    //                break;
                    //            } else {
                    //                Idea memContext = GraphIdea.getNodeContent(memsContext.get(0));
                    //                if (memContext.isCategory()){
                    //                    if (!memContext.getName().equals(context.getValue())){
                    //                        valid = false;
                    //                        break;
                    //                    }
                    //                } else {
                    //                    if (!memContext.getValue().equals(context.getValue())){
                    //                        valid = false;
                    //                        break;
                    //                    }
                    //                }
                    //            }
                    //        }
                    //    }
                    //}

                    if (valid) {
                        for (int i = 0; i < events.size(); i++) {
                            Map<String, List<Idea>> links = cue.getSuccesors(events.get(i));
                            Idea recallA = getNodeContent(recalls.get(i));
                            long startRecallA = getStartTime(recallA);
                            long endRecallA = getEndTime(recallA);
                            for (int j = 0; j < events.size(); j++) {
                                Idea eventB = events.get(j);
                                String cueRelation = links.entrySet().stream()
                                        .filter(e -> e.getValue().contains(eventB))
                                        .map(Map.Entry::getKey)
                                        .findFirst()
                                        .orElse("");
                                Idea recallB = getNodeContent(recalls.get(j));
                                long startRecallB = getStartTime(recallB);
                                long endRecallB = getEndTime(recallB);
                                if (!cueRelation.isEmpty() && !cueRelation.equals(temporalRelation(startRecallA, endRecallA, startRecallB, endRecallB))) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid) {
                        double firstTime = Double.POSITIVE_INFINITY;
                        double lastTime = 0;
                        for (Idea recall : recalls) {
                            double start = getStartTime(getNodeContent(recall));
                            double end = getEndTime(getNodeContent(recall));
                            if (start < firstTime) firstTime = start;
                            if (end > lastTime) lastTime = end;
                        }
                        validSequenceTimeInterval.put(recalls, lastTime - firstTime);
                    }
                }

                if (!validSequenceTimeInterval.isEmpty()) {
                    List<Idea> recalledEvents = validSequenceTimeInterval.entrySet().stream()
                            .min(Map.Entry.comparingByValue()).get().getKey();
                    epGraph.resetActivations();
                    for (Idea event : recalledEvents) {
                        epGraph.setNodeActivation(event, 1.0);
                        epGraph.propagateActivations(event, Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                    }
                    ///for (int i = 0; i < p.length; i++) {
                    ///    epGraph.setNodeActivation(bestMatches.get(events.get(i)).get(p[i]), 1.0);
                    ///}
                    double maxActivation = epGraph.getEpisodeNodes().stream().mapToDouble(epGraph::getNodeActivation).max().getAsDouble();
                    if (maxActivation > 0) {
                        List<Idea> activatedEpisodes = epGraph.getEpisodeNodes().stream().filter(n -> epGraph.getNodeActivation(n) == maxActivation).collect(Collectors.toList());
                        if (activatedEpisodes.size() == 1) {
                            Idea activatedEpisode = activatedEpisodes.get(0);
                            if (!cue.getEpisodeNodes().isEmpty()) {
                                Idea ep = cue.getEpisodeNodes().get(0);
                                Idea cueStarEventNode = cue.getChildrenWithLink(ep, "Begin").get(0);
                                Idea cueEndEventNode = cue.getChildrenWithLink(ep, "End").get(0);
                                Idea recallStartEventNode = epGraph.getChildrenWithLink(activatedEpisode, "Begin").get(0);
                                Idea recallEndEventNode = epGraph.getChildrenWithLink(activatedEpisode, "End").get(0);
                                if (isSameEventCategory(cueStarEventNode, recallStartEventNode) && isSameEventCategory(cueEndEventNode, recallEndEventNode)) {
                                    recalledEpisode = epGraph.getEpisodeSubGraph(activatedEpisode);
                                }

                            } else {
                                recalledEpisode = epGraph.getEpisodeSubGraph(activatedEpisode);
                            }
                        } else if (activatedEpisodes.size() > 1) {
                            GraphIdea recall = new GraphIdea(new Idea("Recall"));
                            for (Idea ep : activatedEpisodes) {
                                GraphIdea episodeGraph = epGraph.getEpisodeSubGraph(ep);
                                recall.addAll(episodeGraph);
                            }
                            recalledEpisode = recall;
                        }
                    }
                }
            }
        }
        //Instantiate Properties
        //Set<Idea> removeCategoriesNodes = new HashSet<>();
        //for (Idea event : recalledEpisode.getEventNodes()) {
        //    Idea initialProperty = recalledEpisode.getChildrenWithLink(event, "Initial").get(0);
        //    Idea finalProperty = recalledEpisode.getChildrenWithLink(event, "Final").get(0);
        //    removeCategoriesNodes.add(initialProperty);
        //    removeCategoriesNodes.add(finalProperty);

        //    Idea initialPropertyInstance = getNodeContent(initialProperty).getInstance();
        //    Idea finalPropertyInstance = getNodeContent(finalProperty).getInstance();

        //    Idea initialPropertyNode = recalledEpisode.insertPropertyNode(initialPropertyInstance);
        //    Idea finalPropertyNode = recalledEpisode.insertPropertyNode(finalPropertyInstance);

        //    recalledEpisode.removeLink(event, initialProperty);
        //    recalledEpisode.removeLink(event, finalProperty);

        //    recalledEpisode.insertLink(event, initialPropertyNode, "Initial");
        //    recalledEpisode.insertLink(event, finalPropertyNode, "Final");
        //}

        Set<Idea> nodesToRemove = new HashSet<>();
        for (Idea event : recalledEpisode.getEventNodes()) {
            for (Idea spatialLink : recalledEpisode.getChildrenWithLink(event, "ObjectContext")) {
                Idea objectNode = recalledEpisode.getChildrenWithLink(spatialLink, "Object").get(0);
                Idea copyObject = getNodeContent(objectNode).getInstance();
                Idea objectOccupation = new Idea("Occupation", null, "Aggregate", 1);
                List<Idea> occupationCells = recalledEpisode.getChildrenWithLink(spatialLink, "GridPlace");
                for (Idea gridCell : occupationCells){
                    objectOccupation.add(getNodeContent(gridCell));
                }
                copyObject.add(objectOccupation);
                Idea copyObjectNode = recalledEpisode.insertObjectNode(copyObject);

                recalledEpisode.insertLink(event, copyObjectNode, "ObjectContext");
                for (Idea gridCell : occupationCells) {
                    recalledEpisode.insertLink(copyObjectNode, gridCell, "GridPlace");
                }
                nodesToRemove.add(spatialLink);
                nodesToRemove.add(objectNode);
            }
        }
        for (Idea node : nodesToRemove){
            recalledEpisode.removeNode(node);
        }

        //for (Idea disconnected : removeCategoriesNodes)
        //  recalledEpisode.removeNode(disconnected);

        synchronized (recallMO) {
            recallMO.setI(recalledEpisode);
        }
    }

    private List<Idea> filterEventsWithLinkToObject(GraphIdea epGraph, List<Idea> eventNodes, Idea initialObjectState, String linkType) {
        List<Idea> allEventsWithLinkToObject = new ArrayList<>();

        Idea objOccupation = initialObjectState.get("Occupation");
        if (objOccupation != null) {
            initialObjectState.getL().remove(objOccupation);
            List<Idea> objNodes = epGraph.getAllNodesWithSimilarContent(initialObjectState, 0.9);
            List<Idea> posNodes = new ArrayList<>();
            for (Idea objGrid : objOccupation.getL()) {
                posNodes.add(epGraph.getNodeFromContent(objGrid));
            }
            for (Idea objNode : objNodes) {
                Idea spatialLinkNode = epGraph.commomParent(objNode, posNodes);
                if (spatialLinkNode != null) {
                    List<Idea> eventWithInitialObject = epGraph.getPredecessors(spatialLinkNode).getOrDefault(linkType, new ArrayList<>());
                    allEventsWithLinkToObject.addAll(eventWithInitialObject);
                }
            }
        } else {
            List<Idea> objNodes = epGraph.getAllNodesWithSimilarContent(initialObjectState);
            for (Idea objNode : objNodes) {
                List<Idea> spatialLinkNodes = epGraph.getPredecessors(objNode).getOrDefault("Object", new ArrayList<>());
                for (Idea spatialLinkNode : spatialLinkNodes) {
                    List<Idea> eventWithInitialObject = epGraph.getPredecessors(spatialLinkNode).getOrDefault(linkType, new ArrayList<>());
                    allEventsWithLinkToObject.addAll(eventWithInitialObject);
                }
            }
        }
        return eventNodes.stream().filter(e -> allEventsWithLinkToObject.contains(e)).collect(Collectors.toList());
    }


    private static long getEndTime(Idea eventA) {
        return (long) eventA.get("End").getValue();
    }

    private static long getStartTime(Idea eventA) {
        return (long) eventA.get("Start").getValue();
    }

    private static boolean isSameEventCategory(Idea nodeA, Idea nodeB) {
        return getNodeContent(nodeA).getValue() == getNodeContent(nodeB).getValue();
    }
}
