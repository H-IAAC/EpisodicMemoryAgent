package CSTEpisodicMemory.behavior;

import CSTEpisodicMemory.core.codelets.ImpulseMemory;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

public class Eat extends Codelet {

    private ImpulseMemory impulseMO;
    private Memory foodMO;
    private MemoryContainer handsMO;

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (ImpulseMemory) getInput("IMPULSES");
        this.handsMO = (MemoryContainer) getOutput("HANDS");
        this.foodMO = (MemoryObject) getInput("KNOWN_FOODS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea impulse = (Idea) impulseMO.getI();
        if (impulse != null) {
            if (impulse.get("State.Food") != null) {
                if (impulse.get("State.Food.Condition").getValue().equals("Consumed")) {
                    Idea action = new Idea("Action", "Eat", "Action", 1);
                    action.add(new Idea("Food_ID", impulse.get("State.Food.ID").getValue()));
                    handsMO.setI(action, (double) impulse.get("State.Desire").getValue(), this.name);
                    removeFromMemory((int) impulse.get("State.Food.ID").getValue());
                } else {
                    handsMO.setI(null, 0.0, this.name);
                }
            }
        }
    }

    private void removeFromMemory(int id) {
        Idea foods = (Idea) foodMO.getI();
        List<Idea> modifiedL = new ArrayList<>();
        //String foodType = "";
        synchronized (foods){
            for (Idea food : foods.getL()){
                if (((int) food.get("ID").getValue()) != id){
                    modifiedL.add(food);
                } //else {
                    //foodType = (String) food.getValue();
                //}
            }
            foods.setL(modifiedL);
        }
        synchronized (foodMO){
            foodMO.setI(foods);
        }
    }
}
