package CSTEpisodicMemory.perception;

import CSTEpisodicMemory.core.representation.GridLocation;
import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FoodDetector extends Codelet {

    private Memory foodMO;
    private Memory visionMO;
    private Memory knownFoodMO;

    private Idea detectedRoom = null;

    public FoodDetector(){
        this.name = "FoodDetector";
    }

    @Override
    public void accessMemoryObjects() {
        foodMO = (MemoryObject) getOutput("FOOD");
        visionMO = (MemoryObject) this.getInput("VISION");
        knownFoodMO = (MemoryObject) this.getInput("KNOWN_FOODS");
        MemoryObject roomMO = (MemoryObject) this.getInput("ROOM");
        if (roomMO != null)
            detectedRoom = (Idea) roomMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (visionMO) {
            synchronized (foodMO) {
                CopyOnWriteArrayList<Identifiable> vision = new CopyOnWriteArrayList((List<Identifiable>) visionMO.getI());
                Idea foods = (Idea) foodMO.getI();
                synchronized (vision) {
                    synchronized (foods) {
                        foods.setL(new ArrayList<>());
                        for (Identifiable obj : vision) {
                            if (obj instanceof Thing) {
                                Thing t = (Thing) obj;
                                if (t.isFood()) {
                                    foods.add(constructFoodIdea(t));
                                }
                            }
                        }
                    }
                }
                foodMO.setI(foods);
            }
        }
    }

    public Idea constructFoodIdea(Thing t) {

        Idea foodIdea = new Idea("Food", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        foodIdea.add(posIdea);
        Idea color = new Idea("Color", t.getTypeName().split("_")[0], "Property", 1);
        color.add(new Idea("R", t.getColor().get(0), "QualityDimension", 1));
        color.add(new Idea("G", t.getColor().get(1), "QualityDimension", 1));
        color.add(new Idea("B", t.getColor().get(2), "QualityDimension", 1));
        foodIdea.add(color);
        foodIdea.add(new Idea("ID", t.getId(), "Property", 1));
        if (detectedRoom != null) {
            if (detectedRoom.get("Location") != null) {
                Idea room = (Idea) detectedRoom.get("Location").getValue();
                double px = t.getPos().get(0) - (double) room.get("center.x").getValue();
                double py = t.getPos().get(1) - (double) room.get("center.y").getValue();
                Idea occupation = new Idea("Occupation", null, "Aggregate", 1);
                Idea gridPlace = GridLocation.getInstance().locateHCCIdea(px, py);
                occupation.add(gridPlace);
                foodIdea.add(occupation);
            }
        }
        synchronized (knownFoodMO){
            Idea outputIdea = (Idea) knownFoodMO.getI();
            List<Idea> known = Collections.synchronizedList(outputIdea.getL());
            for (Idea know : known) {
                if (know.get("ID").getValue().equals(t.getId())) {
                    foodIdea.add(know.get("Novelty"));
                    break;
                }
            }
        }
        return foodIdea;
    }
}
