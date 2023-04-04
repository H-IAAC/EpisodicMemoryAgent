package CSTEpisodicMemory.impulses;

import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class CollectJewelImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory jewelsMO;
    private Memory impulsesMO;
    private Idea inner;
    private Idea jewels;
    private Idea impulses;

    private double minDesire = 0.9, maxDesire = 1.0;
    private String impulseCat = "Collect";

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.inner = (Idea) innerSenseMO.getI();
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.impulsesMO = (MemoryObject) getOutput("IMPULSES");
        this.impulses = (Idea) impulsesMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        removeSatisfiedImpulses();

        int numJewels = jewels.getL().size();
        if (numJewels > 0){
            for (Idea jewel : jewels.getL()){
                double desirability = calculateDesirability(jewel);
                if (desirability > -1.0){
                    desirability = desirability * (maxDesire - minDesire) + minDesire;
                    Idea impulse = createImpulse(jewel, desirability);
                    addIfNotPresent(impulse);
                } else {
                    removeIfPresent(jewel);
                }
            }
        }
    }

    private void removeSatisfiedImpulses() {
        List<Idea> toRemove = new ArrayList<>();
        List<Integer> jewelsID = jewels.getL().stream().map(e-> (int) e.get("ID").getValue()).toList();
        for (Idea impulse : impulses.getL()){
            if (impulse.getValue().equals(this.impulseCat)){
                if (!jewelsID.contains((int) impulse.get("State.Jewel.ID").getValue())){
                    toRemove.add(impulse);
                }
            }
        }
        synchronized (impulsesMO) {
            this.impulses = (Idea) impulsesMO.getI();
            List<Idea> currL = impulses.getL();
            currL.removeAll(toRemove);
            impulses.setL(currL);
        }
    }

    private double calculateDesirability(Idea jewel) {
        double maxDesire = -1.0;
        Vector2D selfPos = new Vector2D(
                (float) inner.get("Position.X").getValue(),
                (float) inner.get("Position.Y").getValue());
        Vector2D jewelPos = new Vector2D(
                (float) jewel.get("Position.X").getValue(),
                (float) jewel.get("Position.Y").getValue());
        if (selfPos.sub(jewelPos).magnitude() < 0.45)
            maxDesire = 1.0;
        return maxDesire;
    }

    private Idea createImpulse(Idea jewel, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Episode", 0);
        Idea state = new Idea("State", null, "Timestamp", 0);
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
        int presentInIdx = -1;
        for (Idea present : impulses.getL()){
            if ((int) present.get("State.ID").getValue() == (int) idea.get("State.ID").getValue()
                    && present.getValue().equals(this.impulseCat))
                presentInIdx = impulses.getL().indexOf(present); //Ineficient please chnge
        }
        if (presentInIdx != -1){
            impulses.getL().remove(presentInIdx);
        }
        impulses.add(idea);
    }

    public void removeIfPresent(Idea jewel){
        int presentInIdx = -1;
        for (Idea present : impulses.getL()){
            if ((int) present.get("State.ID").getValue() == (int) jewel.get("ID").getValue()
                    && present.getValue().equals(this.impulseCat)){
                presentInIdx = impulses.getL().indexOf(present);
            }
        }
        if (presentInIdx != -1){
            impulses.getL().remove(presentInIdx);
        }
    }
}
