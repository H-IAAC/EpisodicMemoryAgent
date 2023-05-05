package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

public class TimelineBufferCodelet extends Codelet {

    private List<String> inputMemoriesNames;
    private List<Memory> inputsMO;
    private Memory bufferMO;

    public TimelineBufferCodelet(List<String> inputs){
        this.inputMemoriesNames = inputs;
    }

    @Override
    public void accessMemoryObjects() {
        inputsMO = new ArrayList<>();
        for (String input : inputMemoriesNames){
            Object mo = getInput(input);
            if (mo instanceof MemoryObject)
                inputsMO.add((MemoryObject) mo);
            else if (mo instanceof MemoryContainer)
                inputsMO.add((MemoryContainer) mo);
            else
                inputsMO.add((Memory) mo);
        }
        bufferMO = (MemoryObject) getOutput("BUFFER");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        long currTime = System.currentTimeMillis();
        Idea buffer = (Idea) bufferMO.getI();
        if (buffer.getL().size() > 100)
            buffer.getL().remove(0);
        Idea currTimestep = new Idea("", currTime, "Timestep", 1);
        //System.out.println("-----------------------");
        //System.out.println(IdeaHelper.csvPrint(buffer).replace('\n',' '));
        for (Memory input : inputsMO){
            Idea content = (Idea) input.getI();
            if (content != null) {
                        currTimestep.add(content.clone());
            }
        }
        buffer.add(currTimestep);
    }
}
