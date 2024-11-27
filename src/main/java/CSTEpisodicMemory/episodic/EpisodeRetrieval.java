package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;
import static CSTEpisodicMemory.episodic.EpisodeBinding.temporalRelation;

public class EpisodeRetrieval extends Codelet {

    private Memory cueMO;
    private Memory epltm;
    //private Memory propertiesMO;
    private Memory recallMO;
    private long searchCount = 0;
    private long rejectCount = 0;

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
                List<Idea> similarObjectsMemory = epGraph.getAllNodesWithSimilarContent(getNodeContent(context), 0.70);

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
                        GraphIdea episodeGraph = epGraph.getEpisodeSubGraphCopy(ep);
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
                    recalledEpisode = epGraph.getEpisodeSubGraphCopy(activatedEpisode);
                } else {
                    epGraph.resetActivations();
                    for (Idea event : bestMemEvents){
                        epGraph.setNodeActivation(event, 1.0);
                        epGraph.propagateActivations(event, Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                    }
                    List<Idea> activatedEpisodes = epGraph.getEpisodeNodes().stream().filter(a->epGraph.getNodeActivation(a)>0).toList();
                    GraphIdea recall = new GraphIdea(new Idea("Recall"));
                    for (Idea ep : activatedEpisodes) {
                        GraphIdea episodeGraph = epGraph.getEpisodeSubGraphCopy(ep);
                        recall.addAll(episodeGraph);
                    }
                    recalledEpisode = recall;
                }
            } else if (bestMatches.size() > 1) {
                System.out.println("-----");
                long totalCombinations = bestMatches.values().stream()
                        .mapToLong(List::size)
                        .reduce((a, b) -> a * b)
                        .getAsLong();
                System.out.println("Greedy: " + totalCombinations);
                LinkedList<HashMap<String, List<Integer>>> eventsRelations = getEventsRelations(bestMatches, cue);
                List<List<Idea>> validRecalls = search(bestMatches, eventsRelations);
                System.out.println("Optimized: " + searchCount);
                searchCount = 0;
                rejectCount = 0;
                Map<List<Idea>, Double> validSequenceTimeInterval = new HashMap<>();
                //HashMap<Idea, List<Idea>> filteredMatches = filterMatchedEventsByRelations(bestMatches, eventsRelations);
                /*
                Map<Idea, Integer> currCheckPos = bestMatches.keySet().stream().collect(Collectors.toMap(e -> e, e -> 0));
                boolean valid;
                for (long c = 0; c < totalCombinations; c++) {
                    if ((c+1)%500_000 == 0){
                        System.out.printf("%10s / %d\n",
                                Long.toString(c+1),
                                totalCombinations);
                    }
                //for (int c = 0; c < Math.max(totalCombinations,500000); c++) {
                    long k = c;
                    for (Idea event : currCheckPos.keySet()) {
                        int totalInPosI = bestMatches.get(event).size();
                        currCheckPos.put(event, (int) (k % totalInPosI));
                        k = k / totalInPosI;
                    }

                    LinkedList<Idea> recalls = new LinkedList<>();
                    //Idea prevEvent = null;
                    for (Idea event : currCheckPos.keySet()) {
                        Idea currIdeaPos = bestMatches.get(event).get(currCheckPos.get(event));
                        //if (prevEvent != null){
                        //    long prevStart = (long) getNodeContent(prevEvent).get("Start").getValue();
                        //    long currStart = (long) getNodeContent(currIdeaPos).get("Start").getValue();
                        //}
                        recalls.add(currIdeaPos);
                    }
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

                    if (valid) {
                        valid = isValidRelations(eventsRelations, recalls);
                    }

                    if (valid) {

                 */
                for (List<Idea> recalls : validRecalls) {
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
                    //}
                //}

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
                                    recalledEpisode = epGraph.getEpisodeSubGraphCopy(activatedEpisode);
                                }

                            } else {
                                recalledEpisode = epGraph.getEpisodeSubGraphCopy(activatedEpisode);
                            }
                        } else if (activatedEpisodes.size() > 1) {
                            GraphIdea recall = new GraphIdea(new Idea("Recall"));
                            for (Idea ep : activatedEpisodes) {
                                GraphIdea episodeGraph = epGraph.getEpisodeSubGraphCopy(ep);
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
        LinkedList<Idea> recalledEvents = new LinkedList<>(recalledEpisode.getEventNodes());
        String aaa = "";
        for (Idea event : recalledEvents) {
            for (Idea spatialLink : recalledEpisode.getChildrenWithLink(event, "ObjectContext")) {
                List<Idea> occupationCells = new ArrayList<>();
                Idea copyObject = transformSpatialLinkToObject(spatialLink, recalledEpisode, nodesToRemove, occupationCells);
                Idea copyObjectNode = recalledEpisode.insertObjectNode(copyObject);
                for (Idea gridCell : occupationCells) {
                    recalledEpisode.insertLink(copyObjectNode, gridCell, "GridPlace");
                }
                recalledEpisode.insertLink(event, copyObjectNode, "ObjectContext");
            }
            Idea initialSpatialLink = recalledEpisode.getChildrenWithLink(event, "Initial").get(0);
            Idea finalSpatialLink = recalledEpisode.getChildrenWithLink(event, "Final").get(0);
            Idea initialCopyObject = transformSpatialLinkToObject(initialSpatialLink, recalledEpisode, nodesToRemove, new ArrayList<>());
            Idea finalCopyObject = transformSpatialLinkToObject(finalSpatialLink, recalledEpisode, nodesToRemove, new ArrayList<>());
            Idea eventContent = getNodeContent(event);
            aaa += eventContent.getName();
            if(eventContent.get("Start") == null)
                System.out.println(aaa);
            initialCopyObject.add(new Idea("TimeStamp", eventContent.get("Start").getValue(), "TimeStamp", 1));
            eventContent.get("Start").add(initialCopyObject);
            eventContent.get("Start").setValue(null);
            eventContent.get("Start").setName("0");
            finalCopyObject.add(new Idea("TimeStamp", eventContent.get("End").getValue(), "TimeStamp", 1));
            eventContent.get("End").add(finalCopyObject);
            eventContent.get("End").setValue(null);
            eventContent.get("End").setName("1");
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

    private static Idea transformSpatialLinkToObject(Idea spatialLink, GraphIdea recalledEpisode, Set<Idea> nodesToRemove, List<Idea> occupationCells){
        Idea objectNode = recalledEpisode.getChildrenWithLink(spatialLink, "Object").get(0);
        Idea copyObject = getNodeContent(objectNode).getInstance();
        Idea objectOccupation = new Idea("Occupation", null, "Aggregate", 1);
        occupationCells.addAll(recalledEpisode.getChildrenWithLink(spatialLink, "GridPlace"));
        for (Idea gridCell : occupationCells){
            objectOccupation.add(getNodeContent(gridCell));
        }
        copyObject.add(objectOccupation);
        nodesToRemove.add(spatialLink);
        nodesToRemove.add(objectNode);

        return copyObject;
    }

    private HashMap<Idea, List<Idea>> filterMatchedEventsByRelations(Map<Idea, List<Idea>> bestMatches, LinkedList<HashMap<String, List<Integer>>> eventsRelations) {
        LinkedList<HashMap<String, List<Pair<Integer, Integer>>>> filtered = new LinkedList<>();
        List<int[]> test = new ArrayList<>();
        LinkedList<Idea> events = new LinkedList<>(bestMatches.keySet());
        for (int i = 0; i<events.size(); i++){
            List<Idea> matchesA = bestMatches.get(events.get(i));
            HashMap<String, List<Integer>> relations = eventsRelations.get(i);
            for (String cueRelation : relations.keySet()) {
                for (int j : relations.get(cueRelation)) {
                    List<Idea> matchesB = bestMatches.get(events.get(j));
                    for (int k=0; k<matchesA.size(); k++){
                        long startRecallA = getStartTime(matchesA.get(k));
                        long endRecallA = getEndTime(matchesA.get(k));
                        for (int l=0; l<matchesB.size(); l++){
                            long startRecallB = getStartTime(matchesB.get(l));
                            long endRecallB = getEndTime(matchesB.get(l));
                            if (cueRelation.equals(temporalRelation(startRecallA, endRecallA, startRecallB, endRecallB))) {
                                if (test.isEmpty()){
                                    int[] firstTest = new int[events.size()];
                                    Arrays.fill(firstTest, -1);
                                    firstTest[i] = k;
                                    firstTest[j] = l;
                                    test.add(firstTest);
                                } else {
                                    
                                }
                                Pair<Integer, Integer> opt = new Pair<>(k,l);
                                List<Pair<Integer,Integer>> opts = filtered.get(i).getOrDefault(cueRelation, new ArrayList<>());
                                opts.add(opt);
                                filtered.get(i).put(cueRelation, opts);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<List<Idea>> search(Map<Idea, List<Idea>> bestMatches, LinkedList<HashMap<String, List<Integer>>> eventsRelations){
        List<List<Idea>> validRecalls = new ArrayList<>();
        List<List<List<Integer>>> searchGraph = createSearchGraphMatrix(bestMatches);
        List<Idea> events = bestMatches.keySet().stream().toList();
        List<Integer> level1Nodes = searchGraph.get(0).get(0);
        for (int i = level1Nodes.size()-1; i >= 0; i--){
            int level1Node = level1Nodes.get(i);
            List<Integer> currSequence = new ArrayList<>();
            currSequence.add(level1Node);
            LinkedList<Idea> currNodesSequence = new LinkedList<>();
            currNodesSequence.add(bestMatches.get(events.get(0)).get(level1Node));
            recursiveSearch(searchGraph, bestMatches, eventsRelations, currSequence, currNodesSequence, validRecalls);
        }
        return validRecalls;
    }

    private int recursiveSearch(List<List<List<Integer>>> searchGraph, Map<Idea, List<Idea>> bestMatches, LinkedList<HashMap<String, List<Integer>>> eventsRelations, List<Integer> currSequence, LinkedList<Idea> currNodesSequence, List<List<Idea>> validRecalls){
        int level = currSequence.size();
        int exploreNode = currSequence.get(level-1);
        if (level == searchGraph.size()){
            searchCount++;
            if ((searchCount+1) % 10_000_000==0)
                System.out.println((searchCount+1) + " | " + rejectCount);
            //System.out.println(currSequence);
            int[] reject = new int[2];
            if (isValidRelations(eventsRelations, currNodesSequence, reject)){
                validRecalls.add(new ArrayList<>(currNodesSequence));
                System.out.println("Valid");
            } else {
                int rejectLevelSource = Math.min(reject[0], reject[1]);
                int rejectNodeSource = currSequence.get(rejectLevelSource);
                int rejectLevelDest = Math.max(reject[0], reject[1]);
                int rejectNodeDest = currSequence.get(rejectLevelDest);
                //System.out.println(Arrays.toString(reject) + " - " + rejectNodeSource + "|" + rejectNodeDest);
                if (Math.abs(reject[0] - reject[1]) == 1){
                    searchGraph.get(rejectLevelSource+1).get(rejectNodeSource).remove((Object) rejectNodeDest);
                    //System.out.println(searchGraph);
                    rejectCount++;
                    return rejectLevelDest + 1;
                }
            }
            return level;
        }
        List<Idea> events = bestMatches.keySet().stream().toList();
        List<Integer> levelNodes = searchGraph.get(level).get(exploreNode);
        for (int i = levelNodes.size()-1; i >= 0; i--){
            int nextLevelNode = levelNodes.get(i);
            currSequence.add(nextLevelNode);
            currNodesSequence.add(bestMatches.get(events.get(level)).get(nextLevelNode));
            int returnLevel = recursiveSearch(searchGraph, bestMatches, eventsRelations, currSequence, currNodesSequence, validRecalls);
            currSequence.remove(level);
            currNodesSequence.remove(level);
            if (returnLevel <= level)
                return returnLevel;
        }
        return level;
    }

    private static List<List<List<Integer>>> createSearchGraphMatrix(Map<Idea, List<Idea>> bestMatches){
        List<Idea> events = bestMatches.keySet().stream().toList();
        List<List<List<Integer>>> searchGraph = new ArrayList<>();

        List<List<Integer>> level0Connections = new ArrayList<>();
        List<Integer> rootNodeConnections = new ArrayList<>();
        for (int k = 0; k < bestMatches.get(events.get(0)).size(); k++)
            rootNodeConnections.add(k);
        level0Connections.add(rootNodeConnections);
        searchGraph.add(level0Connections);

        for (int i = 0; i < bestMatches.size()-1; i++){
            List<List<Integer>> levelConnections = new ArrayList<>();
            for (int j=0; j < bestMatches.get(events.get(i)).size(); j++){
                List<Integer> nodeConnections = new ArrayList<>();
                for (int k = 0; k < bestMatches.get(events.get(i+1)).size(); k++)
                    nodeConnections.add(k);
                levelConnections.add(nodeConnections);
            }
            searchGraph.add(levelConnections);
        }
        return searchGraph;
    }

    @NotNull
    private static LinkedList<HashMap<String, List<Integer>>> getEventsRelations(Map<Idea, List<Idea>> bestMatches, GraphIdea cue) {
        LinkedList<Idea> events = new LinkedList<>(bestMatches.keySet());
        LinkedList<HashMap<String, List<Integer>>> eventsRelations = new LinkedList<>();
        for (int i = 0; i < events.size(); i++) {
            Map<String, List<Idea>> links = cue.getSuccesors(events.get(i));
            HashMap<String, List<Integer>> eventRelations = new HashMap<>();
            for (int j = 0; j < events.size(); j++) {
                Idea eventB = events.get(j);
                String cueRelation = links.entrySet().stream()
                        .filter(e -> e.getValue().contains(eventB))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("");
                if (!cueRelation.isEmpty()) {
                    List<Integer> relatedEvents = eventRelations.getOrDefault(cueRelation, new ArrayList<>());
                    relatedEvents.add(j);
                    eventRelations.put(cueRelation, relatedEvents);
                }
            }
            eventsRelations.add(eventRelations);
        }
        return eventsRelations;
    }

    private static boolean isValidRelations(LinkedList<HashMap<String, List<Integer>>> eventsRelations, LinkedList<Idea> recalls, int[] reject) {
        boolean valid = true;
        for (int i = 0; i < eventsRelations.size(); i++) {
            HashMap<String, List<Integer>> relations = eventsRelations.get(i);
            if (!relations.isEmpty()) {
                Idea recallA = getNodeContent(recalls.get(i));
                long startRecallA = getStartTime(recallA);
                long endRecallA = getEndTime(recallA);
                for (String cueRelation : relations.keySet()) {
                    for (int j : relations.get(cueRelation)) {
                        Idea recallB = getNodeContent(recalls.get(j));
                        long startRecallB = getStartTime(recallB);
                        long endRecallB = getEndTime(recallB);
                        if (!cueRelation.equals(temporalRelation(startRecallA, endRecallA, startRecallB, endRecallB))) {
                            reject[0] = i;
                            reject[1] = j;
                            valid = false;
                            if (Math.abs(i-j) == 1) {
                                return valid;
                            }
                        }
                    }
                }
            }
        }
        return valid;
    }

    private static boolean isValidRelations(LinkedList<HashMap<String, List<Integer>>> eventsRelations, LinkedList<Idea> recalls) {
        for (int i = 0; i < eventsRelations.size(); i++) {
            HashMap<String, List<Integer>> relations = eventsRelations.get(i);
            if (!relations.isEmpty()) {
                Idea recallA = getNodeContent(recalls.get(i));
                long startRecallA = getStartTime(recallA);
                long endRecallA = getEndTime(recallA);
                for (String cueRelation : relations.keySet()) {
                    for (int j : relations.get(cueRelation)) {
                        Idea recallB = getNodeContent(recalls.get(j));
                        long startRecallB = getStartTime(recallB);
                        long endRecallB = getEndTime(recallB);
                        if (!cueRelation.equals(temporalRelation(startRecallA, endRecallA, startRecallB, endRecallB))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<Idea> filterEventsWithLinkToObject(GraphIdea epGraph, List<Idea> eventNodes, Idea initialObjectState, String linkType) {
        List<Idea> allEventsWithLinkToObject = new ArrayList<>();

        Idea objOccupation = initialObjectState.get("Occupation");
        if (objOccupation != null) {
            initialObjectState.getL().remove(objOccupation);
            List<Idea> objNodes = epGraph.getAllNodesWithSimilarContent(initialObjectState, 0.5);
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
