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
    private Memory propertiesMO;
    private Memory recallMO;

    @Override
    public void accessMemoryObjects() {
        cueMO = (MemoryObject) getInput("CUE");
        epltm = (MemoryObject) getInput("EPLTM");
        propertiesMO = (MemoryObject) getInput("PROPERTIES");
        recallMO = (MemoryObject) getOutput("RECALL");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        GraphIdea cue;
        GraphIdea epGraph;
        List<Idea> propertiesCat = new ArrayList<>();
        synchronized (cueMO) {
            cue = new GraphIdea((GraphIdea) cueMO.getI());
        }
        synchronized (epltm) {
            epGraph = new GraphIdea((GraphIdea) epltm.getI());
        }
        synchronized (propertiesMO) {
            for (Idea cat : (ArrayList<Idea>) propertiesMO.getI())
                propertiesCat.add(cat.clone());
        }

        GraphIdea recalledEpisode = new GraphIdea(new Idea("Failed"));

        //Cue with events nodes
        Map<Idea, List<Idea>> bestMatches = new HashMap<>();
        List<Idea> cueEvents = cue.getEventNodes();
        for (Idea eventNode : cueEvents) {
            Idea eventContent = getNodeContent(eventNode);
            Idea eventCategory = (Idea) eventContent.getValue();

            if (eventContent.getL().isEmpty()) {
                if (eventCategory != null) {
                    List<Idea> sameCatEvents = epGraph.getEventNodes().stream().filter(e -> getNodeContent(e).getValue().equals(eventCategory)).collect(Collectors.toList());
                    if (!cue.getEpisodeNodes().isEmpty()){
                        if (cue.getPredecessors(eventNode).containsKey("Begin")){
                            sameCatEvents = sameCatEvents.stream().filter(e -> epGraph.getPredecessors(e).containsKey("Begin")).collect(Collectors.toList());
                        }
                        if (cue.getPredecessors(eventNode).containsKey("End")){
                            sameCatEvents = sameCatEvents.stream().filter(e -> epGraph.getPredecessors(e).containsKey("End")).collect(Collectors.toList());
                        }
                    }
                    sameCatEvents.sort(Comparator.comparingInt(n-> (int) n.get("Coordinate").getValue()));
                    Collections.reverse(sameCatEvents);
                    bestMatches.put(eventNode, sameCatEvents);
                }
            } else {
                epGraph.resetActivations();
                String observedObject = (String) eventCategory.get("ObservedObject").getValue();

                Idea initialObjectState = eventContent.getL().get(0).get(observedObject);
                Idea finalObjectState = eventContent.getL().get(1).get(observedObject);

                if (initialObjectState != null && finalObjectState != null) {
                    Idea bestInitialProperty = propertiesCat.stream().max(Comparator.comparingDouble(idea -> idea.membership(initialObjectState))).orElseGet(null);
                    if (bestInitialProperty != null && bestInitialProperty.membership(initialObjectState) == 1) {
                        epGraph.setNodeActivation(bestInitialProperty, 1.0);
                    }
                    Idea bestFinalProperty = propertiesCat.stream().max(Comparator.comparingDouble(idea -> idea.membership(finalObjectState))).orElseGet(null);
                    if (bestFinalProperty != null && bestFinalProperty.membership(finalObjectState) == 1) {
                        epGraph.setNodeActivation(bestFinalProperty, 1.0);
                    }

                    epGraph.propagateActivations(new ArrayList<>(), Arrays.asList("Initial", "Final"));
                    List<Idea> events = epGraph.getEventNodes();
                    events.sort(Comparator.comparingDouble(epGraph::getNodeActivation));
                    Collections.reverse(events);

                    List<Idea> eventBestMatches = new ArrayList<>();
                    int numMatchedProperties = 1;
                    for (Idea eventMem : events) {
                        if (epGraph.getNodeActivation(eventMem) > 0) {
                            int c = 0;
                            if (IdeaHelper.match(getNodeContent(epGraph.getChildrenWithLink(eventMem, "Initial").get(0)), bestInitialProperty))
                                c++;
                            if (IdeaHelper.match(getNodeContent(epGraph.getChildrenWithLink(eventMem, "Final").get(0)), bestFinalProperty))
                                c++;

                            if (c == numMatchedProperties) {
                                eventBestMatches.add(eventMem);
                            }
                            if (c > numMatchedProperties) {
                                numMatchedProperties = c;
                                eventBestMatches = new ArrayList<>();
                                eventBestMatches.add(eventMem);
                            }
                        }
                    }
                    bestMatches.put(eventNode, eventBestMatches);
                }
            }
        }

        if (bestMatches.size() == 1) {
            List<Idea> bestMemEvents = bestMatches.get(cueEvents.get(0));
            if (bestMemEvents.size() == 1) {
                epGraph.resetActivations();
                epGraph.setNodeActivation(bestMemEvents.get(0), 1.0);
                epGraph.propagateActivations(Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                Idea activatedEpisode = epGraph.getEpisodeNodes().stream().max(Comparator.comparingDouble(epGraph::getNodeActivation)).get();
                recalledEpisode = epGraph.getEpisodeSubGraph(activatedEpisode);
            }
        } else if (bestMatches.size() > 1) {
            LinkedList<Idea> events = new LinkedList<>(bestMatches.keySet());
            ///int[] p = new int[bestMatches.size()];
            ///Arrays.fill(p, 0);
            Map<Idea, Integer> currCheckPos = bestMatches.keySet().stream().collect(Collectors.toMap(e->e,e->0));
            boolean valid = true;
            int totalCombinations = bestMatches.values().stream().map(List::size).reduce((a,b)->a*b).get();
            for (int c = 0; c < totalCombinations; c++) {
                int k = c;
                for (Idea event : currCheckPos.keySet()){
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
                for (Idea event : currCheckPos.keySet()){
                    recalls.add(bestMatches.get(event).get(currCheckPos.get(event)));
                }
                ///for (int i = 0; i < events.size(); i++) {
                ///    recalls.add(bestMatches.get(events.get(i)).get(p[i]));
                ///}
                valid = true;
                //Check episodes
                for (Idea ep : cue.getEpisodeNodes()){
                    List<Idea> beginLink = cue.getChildrenWithLink(ep, "Begin");
                    Idea beginEp = null;
                    if (!beginLink.isEmpty()){
                        Idea beginCue = beginLink.get(0);
                        beginEp = epGraph.getPredecessors(bestMatches.get(beginCue).get(currCheckPos.get(beginCue))).get("Begin").get(0);
                    }
                    List<Idea> endLink = cue.getChildrenWithLink(ep, "End");
                    Idea endEp = null;
                    if (!endLink.isEmpty()){
                        Idea endCue = endLink.get(0);
                        endEp = epGraph.getPredecessors(bestMatches.get(endCue).get(currCheckPos.get(endCue))).get("End").get(0);
                    }
                    if (beginEp != endEp){
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    for (int i = 0; i < events.size(); i++) {
                        Idea eventA = getNodeContent(events.get(i));
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

                if (valid){
                    epGraph.resetActivations();
                    for (Idea event : currCheckPos.keySet()){
                        epGraph.setNodeActivation(bestMatches.get(event).get(currCheckPos.get(event)), 1.0);
                    }
                    ///for (int i = 0; i < p.length; i++) {
                    ///    epGraph.setNodeActivation(bestMatches.get(events.get(i)).get(p[i]), 1.0);
                    ///}
                    epGraph.propagateActivations(Arrays.asList("Before", "Meet", "Overlap", "Start", "During", "Finish", "Equal"), Arrays.asList("Begin", "End"));
                    double maxActivation = epGraph.getEpisodeNodes().stream().mapToDouble(epGraph::getNodeActivation).max().getAsDouble();
                    if (maxActivation > 0) {
                        List<Idea> activatedEpisodes = epGraph.getEpisodeNodes().stream().filter(n->epGraph.getNodeActivation(n) == maxActivation).collect(Collectors.toList());
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
                                    break;
                                }

                            } else {
                                recalledEpisode = epGraph.getEpisodeSubGraph(activatedEpisode);
                                break;
                            }
                        } else if (activatedEpisodes.size() > 1) {
                            GraphIdea recall = new GraphIdea(new Idea("Recall"));
                            for (Idea ep : activatedEpisodes){
                                GraphIdea episodeGraph = epGraph.getEpisodeSubGraph(ep);
                                recall.addAll(episodeGraph);
                            }
                            recalledEpisode = recall;
                            break;
                        }

                    }
                }
            }

        }

        //Instantiate Properties
        Set<Idea> removeCategoriesNodes = new HashSet<>();
        for (Idea event : recalledEpisode.getEventNodes()) {
            Idea initialProperty = recalledEpisode.getChildrenWithLink(event, "Initial").get(0);
            Idea finalProperty = recalledEpisode.getChildrenWithLink(event, "Final").get(0);
            removeCategoriesNodes.add(initialProperty);
            removeCategoriesNodes.add(finalProperty);

            Idea initialPropertyInstance = getNodeContent(initialProperty).getInstance();
            Idea finalPropertyInstance = getNodeContent(finalProperty).getInstance();

            Idea initialPropertyNode = recalledEpisode.insertPropertyNode(initialPropertyInstance);
            Idea finalPropertyNode = recalledEpisode.insertPropertyNode(finalPropertyInstance);

            recalledEpisode.removeLink(event, initialProperty);
            recalledEpisode.removeLink(event, finalProperty);

            recalledEpisode.insertLink(event, initialPropertyNode, "Initial");
            recalledEpisode.insertLink(event, finalPropertyNode, "Final");
        }

        for (Idea disconnected : removeCategoriesNodes)
            recalledEpisode.removeNode(disconnected);

        //For each event
        //Get properties nodes
        //Propagate activation from properties in EPLTM
        //Order event nodes with higher activation
        //Search for events that have same start and end properties
        //Add to a map (cue->memory)
        //Check time relations

        synchronized (recallMO) {
            recallMO.setI(recalledEpisode);
        }
    }


    private static long getEndTime(Idea eventA) {
        return (long) eventA.get("End").getValue();
    }

    private static long getStartTime(Idea eventA) {
        return (long) eventA.get("Start").getValue();
    }

    private static boolean isSameEventCategory(Idea nodeA, Idea nodeB){
        return getNodeContent(nodeA).getValue() == getNodeContent(nodeB).getValue();
    }
}
