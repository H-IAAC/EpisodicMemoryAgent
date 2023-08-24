package CSTEpisodicMemory.impulses;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoToJewelImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory leafletMO;
    private Memory jewelsMO;
    private MemoryContainer impulsesMO;
    private Idea inner;
    private Idea leaflets;
    private Idea jewels;

    private final double minDesire = 0.7;
    private final double maxDesire = 0.8;
    private final String impulseCat = "GoTo";

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.inner = (Idea) innerSenseMO.getI();
        this.leafletMO = (MemoryObject) getInput("LEAFLETS");
        this.leaflets = (Idea) leafletMO.getI();
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.impulsesMO = (MemoryContainer) getOutput("IMPULSES");
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
                    Idea impulse = createImpulse(jewel, -1);
                    removeIfPresent(impulse);
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
                    if (!jewelsID.contains((int) impulse.get("State.ID").getValue())){
                        toRemove.add(impulseMem);
                    }
                }
            }
            impulsesMemories.removeAll(toRemove);
        }
    }

    private double calculateDesirability(Idea jewel) {
        double maxDesire = -1.0;

        for (Idea leaflet : leaflets.getL()){
            int leafletRemain = 0;
            int leafletNeed = 0;
            boolean necessary = false;
            for (Idea jewelColor : leaflet.getL()){
                if (jewelColor.get("Remained") != null) {
                    leafletRemain += (int) jewelColor.get("Remained").getValue();
                    leafletNeed += (int) jewelColor.get("Need").getValue();
                    if ((int) jewelColor.get("Remained").getValue() > 0 && jewelColor.getName().equals(jewel.getValue())) {
                        necessary = true;
                    }
                }
            }
            if (necessary && (1.0 - leafletRemain / (1.0*leafletNeed)) > maxDesire)
                maxDesire = 1.0 - leafletRemain / (1.0*leafletNeed);
        }
        return maxDesire;
    }

    private Idea createImpulse(Idea jewel, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Goal", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(jewel.get("Position").clone());
        state.add(self);
        state.add(jewel.get("ID").clone());
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }

    @SuppressWarnings({"INFO"})
    public void addIfNotPresent(Idea idea){
        synchronized (impulsesMO) {
            if (impulsesMO.getI(this.impulseCat + idea.get("State.ID").getValue()) == null)
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