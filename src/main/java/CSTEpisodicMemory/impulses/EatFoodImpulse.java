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

public class EatFoodImpulse extends Codelet {

    private Memory foodMO;
    private Memory innerSenseMO;
    private ImpulseMemory impulseMO;

    private final double minDesire = 0.9;
    private final double maxDesire = 1.0;
    private final String impulseCat = "Eat";

    public EatFoodImpulse(){
        this.name = "EatFoodImpulse";
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.foodMO = (MemoryObject) getInput("KNOWN_FOODS");
        this.impulseMO = (ImpulseMemory) getOutput("IMPULSES");

    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        removeSatisfiedImpulses();

        synchronized (foodMO) {
            Idea foods = (Idea) foodMO.getI();
            int numFoods = foods.getL().size();
            if (numFoods > 0) {
                for (Idea food : foods.getL()) {
                    double desirability = calculateDesirability(food);
                    if (desirability > -1.0) {
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
    }


    private void removeSatisfiedImpulses() {
        List<Memory> toRemove = new ArrayList<>();
        Idea foods = (Idea) foodMO.getI();
        List<Integer> foodID = foods.getL().stream().map(e-> (int) e.get("ID").getValue()).collect(Collectors.toList());
        List<Memory> impulsesMemories = impulseMO.getAllMemories();
        synchronized (impulseMO) {
            for (Memory impulseMem : impulsesMemories){
                Idea impulse = (Idea) impulseMem.getI();
                if (impulse.getValue().equals(this.impulseCat)){
                    if (!foodID.contains((int) impulse.get("State.Food.ID").getValue())){
                        toRemove.add(impulseMem);
                    }
                }
            }
            if(!toRemove.isEmpty())
                System.out.println("REMOVING FOOD IMPULSE");
            impulsesMemories.removeAll(toRemove);
        }
    }

    private double calculateDesirability(Idea food) {
        double maxDesire = -1.0;
        Idea inner = (Idea) innerSenseMO.getI();
        Vector2D selfPos = new Vector2D(
                (double) inner.get("Position.X").getValue(),
                (double) inner.get("Position.Y").getValue());
        Vector2D foodPos = new Vector2D(
                (double) food.get("Position.X").getValue(),
                (double) food.get("Position.Y").getValue());
        if (selfPos.sub(foodPos).magnitude() < 0.45)
            maxDesire = 1.0;
        return maxDesire;
    }

    private Idea createImpulse(Idea food, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Goal", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea stateFood = new Idea("Food", food.getValue(), "AbstractObject", 1);
        stateFood.add(food.get("ID").clone());
        stateFood.add(new Idea("Condition", "Consumed", "Property", 1));
        state.add(stateFood);
        state.add(food.get("ID").clone());
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }

    public void addIfNotPresent(Idea idea){
        synchronized (impulseMO) {
            impulseMO.setI(idea,
                    (double) idea.get("State.Desire").getValue(),
                    this.impulseCat + idea.get("State.ID").getValue());
        }
    }

    public void removeIfPresent(Idea food){
        synchronized (impulseMO) {
            impulseMO.setI(food,
                    -1.0,
                    this.impulseCat + food.get("State.ID").getValue());
        }
    }
}
