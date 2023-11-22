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

    private int bufferSize = 10;
    private boolean checkPerception = false;

    public BufferCodelet() {
        this.name = "BufferCodelet";
    }

    public BufferCodelet(int size) {
        this.name = "BufferCodelet";
        bufferSize = size;
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
        synchronized (bufferMO) {
            Idea buffer = (Idea) bufferMO.getI();
            if (buffer.getL().size() > bufferSize)
                buffer.getL().remove(0);
            Idea currTimestep = new Idea("", currTime, "Timestep", 1);
            //System.out.println("-----------------------");
            //System.out.println(IdeaHelper.csvPrint(buffer).replace('\n',' '));
            //PrintWriter out;
            //try {
            //    out = new PrintWriter(bufferMO.getName());
            //    String csv = IdeaHelper.csvPrint(buffer, 6);
            //    out.println(csv);
            //    out.close();
            //} catch (FileNotFoundException e) {
            //    throw new RuntimeException(e);
            //}
            synchronized (inputsMO) {
                for (Memory input : inputsMO) {
                    Idea content = (Idea) input.getI();
                    if (content != null) {
                        if (checkPerception && !content.getName().equals("Self")) {
                            if (content.get("Novelty") != null && content.get("Occupation") != null) {
                                currTimestep.add(IdeaHelper.cloneIdea(content));
                            }else {
                                for (Idea sub : content.getL()) {
                                    if (sub.get("Novelty") != null && sub.get("Occupation") != null) {
                                        currTimestep.add(IdeaHelper.cloneIdea(sub));
                                    }
                                }
                            }
                        } else {
                            currTimestep.add(IdeaHelper.cloneIdea(content));
                        }
                    }
                }
                buffer.add(currTimestep);
            }
        }
    }

    public void setCheckPerception(boolean checkPerception) {
        this.checkPerception = checkPerception;
    }
}
