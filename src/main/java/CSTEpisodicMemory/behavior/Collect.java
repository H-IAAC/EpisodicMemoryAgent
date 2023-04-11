package CSTEpisodicMemory.behavior;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class Collect extends Codelet {

    private MemoryContainer impulseMO;
    private Memory jewelsMO;
    private MemoryContainer handsMO;
    private Memory jewelsCountersMO;

    private Idea impulse;
    private Idea jewels;

    public Collect() {
        this.name = "CollectBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (MemoryContainer) getInput("IMPULSES");
        this.impulse = (Idea) impulseMO.getI();
        this.handsMO = (MemoryContainer) getOutput("HANDS");
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.jewelsCountersMO = (MemoryObject) getOutput("JEWELS_COUNTERS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (impulse != null) {
            if (impulse.get("State.Jewel") != null) {
                if (impulse.get("State.Jewel.Condition").getValue().equals("In Bag")) {
                    Idea action = new Idea("Action", "Collect", "Episode", 0);
                    action.add(new Idea("Jewel_ID", impulse.get("State.Jewel.ID").getValue()));
                    handsMO.setI(action, (double) impulse.get("State.Desire").getValue(), this.name);
                    removeFromMemory((int) impulse.get("State.Jewel.ID").getValue());
                } else {
                    handsMO.setI(null, 0.0, this.name);
                }
            }
        }
    }

    private void removeFromMemory(int id) {
        List<Idea> modifiedL = new ArrayList<>();
        String jewelType = "";
        for (Idea jewel : jewels.getL()){
            if (((int) jewel.get("ID").getValue()) != id){
                modifiedL.add(jewel.clone());
            } else {
                jewelType = (String) jewel.getValue();
            }
        }
        jewels.setL(modifiedL);
        synchronized (jewelsCountersMO){
            Idea jewelsCountersIdea = (Idea) jewelsCountersMO.getI();
            List<Idea> counters = jewelsCountersIdea.getL();
            jewelsCountersIdea.get("Step").setValue((int) jewelsCountersIdea.get("Step").getValue() + 1);
            for (Idea counter : counters){
                if (counter.getName().equalsIgnoreCase(jewelType)){
                    counter.setValue((int) counter.getValue() - 1);
                }
            }
        }
    }
}
