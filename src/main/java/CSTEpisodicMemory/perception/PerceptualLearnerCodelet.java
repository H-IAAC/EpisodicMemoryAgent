package CSTEpisodicMemory.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PerceptualLearnerCodelet extends Codelet {

    private Memory inputMO;
    private Memory outputMO;
    private String inputName;
    private String outputName;

    public PerceptualLearnerCodelet(String inputName_, String outputName_) {
        inputName = inputName_;
        outputName = outputName_;
        this.name = "PErceptualLearner_"+inputName_;
    }

    @Override
    public void accessMemoryObjects() {
        inputMO = getInput(inputName);
        outputMO = getOutput(outputName);
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (inputMO) {
            synchronized (outputMO) {
                Idea outputIdea = (Idea) outputMO.getI();

                CopyOnWriteArrayList<Idea> inputObjs = new CopyOnWriteArrayList<>(((Idea) inputMO.getI()).getL());
                List<Idea> known = Collections.synchronizedList(outputIdea.getL());
                if (!inputObjs.isEmpty()) {
                    for (Idea obj : inputObjs) {
                        boolean found = false;
                        for (Idea know : known) {
                            if (know.get("ID").getValue().equals(obj.get("ID").getValue())) {
                                found = true;
                                break;
                            }
                        }
                        if (obj.get("Grid_Place") != null)
                            if (!found) {
                                known.add(obj);
                            }
                    }
                }
            }
        }
    }
}