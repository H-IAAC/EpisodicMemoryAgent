package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BufferCodelet extends Codelet {

    private List<Memory> inputsMO;
    private Memory bufferMO;

    public BufferCodelet(){
    }

    @Override
    public void accessMemoryObjects() {
        inputsMO = getInputs();
        bufferMO = (MemoryObject) getOutputs().get(0);
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        long currTime = System.currentTimeMillis();
        Idea buffer = (Idea) bufferMO.getI();
        if (buffer.getL().size() > 500)
            buffer.getL().remove(0);
        Idea currTimestep = new Idea("", currTime, "Timestep", 1);
        //System.out.println("-----------------------");
        //System.out.println(IdeaHelper.csvPrint(buffer).replace('\n',' '));
        PrintWriter out;
        try {
            out = new PrintWriter(bufferMO.getName());
            String csv = IdeaHelper.csvPrint(buffer, 6);
            out.println(csv);
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (Memory input : inputsMO){
            Idea content = (Idea) input.getI();
            if (content != null) {
                currTimestep.add(content.clone());
            }
        }
        buffer.add(currTimestep);
    }
}
