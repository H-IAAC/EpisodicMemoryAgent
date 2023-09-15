package CSTEpisodicMemory.perception;

import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

public class FoodDetector extends Codelet {

    private Memory foodMO;
    private Memory visionMO;

    @Override
    public void accessMemoryObjects() {
        foodMO = (MemoryObject) getOutput("FOOD");
        visionMO = (MemoryObject) this.getInput("VISION");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        List<Identifiable> vision = (List<Identifiable>) visionMO.getI();
        Idea foods = (Idea) foodMO.getI();
        synchronized (vision){
            synchronized (foods) {
                for (Identifiable obj : vision) {
                    if (obj instanceof Thing) {
                        Thing t = (Thing) obj;
                        boolean found = false;
                        for (Idea known : foods.getL()) {
                            if (t.getId() == ((int) known.get("ID").getValue()))
                                found = true;
                        }

                        if (!found && t.isFood()) {
                            foods.add(constructFoodIdea(t));
                        }
                    }
                }
            }
        }
        synchronized (foodMO){
            foodMO.setI(foods);
        }
    }

    public static Idea constructFoodIdea(Thing t) {

        Idea foodIdea = new Idea("Food", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        foodIdea.add(posIdea);
        foodIdea.add(new Idea("Color", t.getColor(), "Property", 1));
        foodIdea.add(new Idea("ID", t.getId(), "Property", 1));
        return foodIdea;
    }
}
