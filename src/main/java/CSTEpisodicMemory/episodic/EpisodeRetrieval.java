package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.awt.event.MouseWheelEvent;

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
        synchronized (cueMO){
            cue = new GraphIdea((GraphIdea) cueMO.getI());
        }
        synchronized (epltm){
            epGraph = new GraphIdea((GraphIdea) epltm.getI());
        }

        for (Idea eventNode: cue.getEventNodes()){
            Idea eventContent = getNodeContent(eventNode);
            Idea eventCategory = (Idea) eventContent.getValue();

        }
        //For each event
            //Get properties nodes
            //Propagate activation from properties in EPLTM
            //Order event nodes with higher activation
            //Search for events that have same start and end properties
            //Add to a map (cue->memory)
        //Check time relations
        
    }
}
