package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

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

        //Cue with events nodes
        Map<Idea, List<Idea>> bestMatches = new HashMap<>();
        List<Idea> cueEvents = cue.getEventNodes();
        for (Idea eventNode : cueEvents) {
            Idea eventContent = getNodeContent(eventNode);
            Idea eventCategory = (Idea) eventContent.getValue();

            if (!eventContent.getL().isEmpty()) {
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

                    List<Idea> eventBestMatches = new ArrayList<>();
                    int numMatchedProperties = 1;
                    for (Idea eventMem : events) {
                        int c = 0;
                        if (getNodeContent(epGraph.getChildrenWithLink(eventMem, "Initial").get(0)) == bestInitialProperty)
                            c++;
                        if (getNodeContent(epGraph.getChildrenWithLink(eventMem, "Final").get(0)) == bestFinalProperty)
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
                    bestMatches.put(eventNode, eventBestMatches);
                }
            }
        }

        if (bestMatches.size() == 1){
            List<Idea> bestMemEvents = bestMatches.get(cueEvents.get(0));
            if (bestMemEvents.size() == 1){

            }
        }
        //For each event
        //Get properties nodes
        //Propagate activation from properties in EPLTM
        //Order event nodes with higher activation
        //Search for events that have same start and end properties
        //Add to a map (cue->memory)
        //Check time relations

        synchronized (recallMO) {
            recallMO.setI(new GraphIdea(new Idea("Failed")));
        }
    }
}
