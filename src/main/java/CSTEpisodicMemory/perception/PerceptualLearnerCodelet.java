package CSTEpisodicMemory.perception;

import WS3DCoppelia.model.Identifiable;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.rmi.MarshalException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PerceptualLearnerCodelet extends Codelet {

    private Memory inputMO;
    private Memory outpurMO;
    private String inputName;
    private String outputName;

    public PerceptualLearnerCodelet(String inputName_, String outputName_) {
        inputName = inputName_;
        outputName = outputName_;
    }

    @Override
    public void accessMemoryObjects() {
        inputMO = getInput(inputName);
        outpurMO = getOutput(outputName);
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea outputIdea = (Idea) outpurMO.getI();

        CopyOnWriteArrayList<Idea> inputObjs = new CopyOnWriteArrayList<>(((Idea) inputMO.getI()).getL());
        List<Idea> known = Collections.synchronizedList(outputIdea.getL());
        if (!inputObjs.isEmpty()) {
            synchronized (inputObjs) {
                for (Idea obj : inputObjs) {
                    boolean found = false;
                    synchronized (known) {
                        for (Idea know : known) {
                            if (know.get("ID").getValue().equals(obj.get("ID").getValue())) {
                                found = true;
                                break;
                            }
                        }
                        if (obj.get("Grid_Place") == null)
                            if (!found) {
                                known.add(obj);
                            }
                    }
                }
            }
        }
    }
}