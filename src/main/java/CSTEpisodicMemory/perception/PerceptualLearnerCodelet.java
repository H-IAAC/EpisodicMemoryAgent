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
        this.name = "PerceptualLearner_" + inputName_;
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
                    //Decrement novelty of known itens
                    for (Idea know : known) {
                        double novelty = (double) know.get("Novelty").getValue();
                        know.get("Novelty").setValue(novelty * 0.99);
                    }

                    for (Idea obj : inputObjs) {
                        Idea found = null;
                        if (obj.get("Occupation") != null) {
                            for (Idea know : known) {
                                if (know.get("ID").getValue().equals(obj.get("ID").getValue())) {
                                    found = know;
                                    break;
                                }
                            }
                            if (found == null) {
                                obj.add(new Idea("Novelty", 1d, "Property", 1));
                                known.add(obj);
                            } else {
                                double novelty = (double) found.get("Novelty").getValue();
                                if (novelty <= 0.5)
                                    novelty = 2 * Math.pow(novelty - 0.5, 2) + 0.5;
                                else if (novelty < 0.9)
                                    novelty = -2.5 * Math.pow(novelty - 0.9, 2) + 0.9;
                                found.get("Novelty").setValue(novelty);
                                updateItem(found, obj);
                            }
                        }
                    }
                }
                outputMO.setI(outputIdea);
            }
        }
    }

    private void updateItem(Idea know, Idea obj) {
        for (Idea property : obj.getL()) {
            Idea knowProperty = know.get(property.getName());
            if (knowProperty != null) {
                knowProperty.setL(property.getL());
                knowProperty.setValue(property.getValue());
            } else {
                know.add(property);
            }
        }
    }
}