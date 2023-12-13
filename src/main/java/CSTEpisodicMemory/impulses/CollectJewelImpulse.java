package CSTEpisodicMemory.impulses;

import CSTEpisodicMemory.core.codelets.ImpulseMemory;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectJewelImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory jewelsMO;
    private ImpulseMemory impulsesMO;
    private Idea inner;
    private Idea jewels;

    private final double minDesire = 0.9;
    private final double maxDesire = 1.0;
    private final String impulseCat = "Collect";

    public CollectJewelImpulse(){
        this.name = "CollectJewelImpulse";
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.inner = (Idea) innerSenseMO.getI();
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.impulsesMO = (ImpulseMemory) getOutput("IMPULSES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        removeSatisfiedImpulses();

        int numJewels = jewels.getL().size();
        if (numJewels > 0){
            synchronized (jewelsMO){
            for (Idea jewel : jewels.getL()) {
                double desirability = calculateDesirability(jewel);
                if (desirability > -1.0) {
                    desirability = desirability * (maxDesire - minDesire) + minDesire;
                    Idea impulse = createImpulse(jewel, desirability);
                    addIfNotPresent(impulse);
                } else {
                    Idea impulse = createImpulse(jewel, -1);
                    removeIfPresent(impulse);
                }
            }
            }
        }
    }

    private void removeSatisfiedImpulses() {
        List<Memory> toRemove = new ArrayList<>();
        List<Integer> jewelsID = jewels.getL().stream().map(e-> (int) e.get("ID").getValue()).collect(Collectors.toList());
        List<Memory> impulsesMemories = impulsesMO.getAllMemories();
        synchronized (impulsesMO) {
            for (Memory impulseMem : impulsesMemories){
                Idea impulse = (Idea) impulseMem.getI();
                if (impulse.getValue().equals(this.impulseCat)){
                    if (!jewelsID.contains((int) impulse.get("State.Jewel.ID").getValue())){
                        toRemove.add(impulseMem);
                    }
                }
            }
            impulsesMemories.removeAll(toRemove);
        }
    }

    private double calculateDesirability(Idea jewel) {
        double maxDesire = -1.0;
        Vector2D selfPos = new Vector2D(
                (double) inner.get("Position.X").getValue(),
                (double) inner.get("Position.Y").getValue());
        Vector2D jewelPos = new Vector2D(
                (double) jewel.get("Position.X").getValue(),
                (double) jewel.get("Position.Y").getValue());
        if (selfPos.sub(jewelPos).magnitude() < 0.45)
            maxDesire = 1.0;
        return maxDesire;
    }

    private Idea createImpulse(Idea jewel, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Goal", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea stateJewel = new Idea("Jewel", jewel.getValue(), "AbstractObject", 1);
        stateJewel.add(jewel.get("ID").clone());
        stateJewel.add(new Idea("Condition", "In Bag", "Property", 1));
        state.add(stateJewel);
        state.add(jewel.get("ID").clone());
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }

    public void addIfNotPresent(Idea idea){
        synchronized (impulsesMO) {
            impulsesMO.setI(idea,
                    (double) idea.get("State.Desire").getValue(),
                    this.impulseCat + idea.get("State.ID").getValue());
        }
    }

    public void removeIfPresent(Idea jewel){
        synchronized (impulsesMO) {
            impulsesMO.setI(jewel,
                    -1.0,
                    this.impulseCat + jewel.get("State.ID").getValue());
        }
    }
}
