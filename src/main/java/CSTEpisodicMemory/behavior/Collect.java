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

    private Memory impulseMO;
    private Memory jewelsMO;
    private MemoryContainer handsMO;

    private Idea impulses;
    private Idea jewels;

    public Collect() {
        this.name = "CollectBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (Memory) getInput("IMPULSES");
        this.impulses = (Idea) impulseMO.getI();
        this.handsMO = (MemoryContainer) getOutput("HANDS");
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        System.out.println(fullPrint(impulses));
        Idea mostIntenseImpulse = null;
        for (Idea impulse : impulses.getL()){
            if (impulse.get("State.Jewel") != null){
                if (impulse.get("State.Jewel.Condition").getValue().equals("In Bag")) {
                    if (mostIntenseImpulse == null) {
                        mostIntenseImpulse = impulse;
                    } else {
                        double maxDesire = (double) mostIntenseImpulse.get("State.Desire").getValue();
                        double checkDesire = (double) impulse.get("State.Desire").getValue();
                        if (checkDesire > maxDesire) {
                            mostIntenseImpulse = impulse;
                        }
                    }
                }
            }
        }
        if (mostIntenseImpulse != null){
            Idea action = new Idea("Action", "Collect", "Episode", 0);
            action.add(new Idea("Jewel_ID", mostIntenseImpulse.get("State.Jewel.ID").getValue()));
            handsMO.setI(action, (double) mostIntenseImpulse.get("State.Desire").getValue(), this.name);
            removeFromMemory((int) mostIntenseImpulse.get("State.Jewel.ID").getValue());
        } else {
            handsMO.setI(null, 0.0, this.name);
        }
    }

    private void removeFromMemory(int id) {
        List<Idea> modifiedL = new ArrayList<>();
        for (Idea jewel : jewels.getL()){
            if (((int) jewel.get("ID").getValue()) != id){
                modifiedL.add(jewel.clone());
            }
        }
        jewels.setL(modifiedL);
    }
}
