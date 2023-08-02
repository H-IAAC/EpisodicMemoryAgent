package CSTEpisodicMemory.impulses;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoToFoodImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory foodMO;
    private MemoryContainer impulsesMO;

    private final double minDesire = 0;
    private final double maxDesire = 1;
    private final String impulseCat = "GoTo";

    @Override
    public void accessMemoryObjects() {
        this.foodMO = (MemoryObject) getInput("FOOD");
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.impulsesMO = (MemoryContainer) getOutput("IMPULSES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        removeSatisfiedImpulses();

        Idea foods = (Idea) foodMO.getI();
        int numFoods = foods.getL().size();
        if (numFoods > 0){
            for (Idea food : foods.getL()){
                double desirability = calculateDesirability(food);
                if (desirability > -1.0){
                    desirability = desirability * (maxDesire - minDesire) + minDesire;
                    Idea impulse = createImpulse(food, desirability);
                    addIfNotPresent(impulse);
                } else {
                    Idea impulse = createImpulse(food, -1);
                    removeIfPresent(impulse);
                }
            }
        }
    }


    private void removeSatisfiedImpulses() {
        List<Memory> toRemove = new ArrayList<>();
        Idea foods = (Idea) foodMO.getI();
        synchronized (foods) {
            List<Integer> foodID = foods.getL().stream().map(e -> (int) e.get("ID").getValue()).collect(Collectors.toList());
            List<Memory> impulsesMemories = impulsesMO.getAllMemories();
            synchronized (impulsesMO) {
                for (Memory impulseMem : impulsesMemories) {
                    Idea impulse = (Idea) impulseMem.getI();
                    if (impulse.getValue().equals(this.impulseCat)) {
                        if (!foodID.contains((int) impulse.get("State.ID").getValue())) {
                            toRemove.add(impulseMem);
                        }
                    }
                }
                impulsesMemories.removeAll(toRemove);
            }
        }
    }

    private double calculateDesirability(Idea food) {
        double desire = 0.;

        Idea innerSense = (Idea) innerSenseMO.getI();
        float energy = (float) innerSense.get("Fuel").getValue();
        if (energy < 800) {
            desire = 0.2;
        }
        if (energy < 400){
            desire = 0.8;
        }
        if (food.getValue().equals("P_FOOD"))
            desire += 0.1;

        return desire;
    }

    private Idea createImpulse(Idea food, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Goal", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(food.get("Position").clone());
        state.add(self);
        state.add(food.get("ID").clone());
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

    public void removeIfPresent(Idea food){
        synchronized (impulsesMO) {
            impulsesMO.setI(food,
                    -1.0,
                    this.impulseCat + food.get("State.ID").getValue());
        }
    }
}
