package CSTEpisodicMemory.perception;

import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WallDetector extends Codelet {
    private Memory visionMO;
    private Memory currentWallsMO;

    public WallDetector(){
        this.name = "WallDetector";
    }

    @Override
    public void accessMemoryObjects() {
        synchronized(this) {
            this.visionMO=(MemoryObject)this.getInput("VISION");
        }
        this.currentWallsMO=(MemoryObject)this.getOutput("WALLS");

    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Idea> known;
        synchronized (visionMO) {
            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            known = Collections.synchronizedList(((Idea) currentWallsMO.getI()).getL());
            synchronized(vision) {
                for (Thing t : vision) {
                    boolean found = false;
                    synchronized(known) {
                        CopyOnWriteArrayList<Idea> myknown = new CopyOnWriteArrayList<>(known);
                        for (Idea e : myknown)
                            if (t.getId() == ((int) e.get("ID").getValue())) {
                                found = true;
                                break;
                            }
                        if (!found && t.isBrick()) known.add(constructWallIdea(t));
                    }

                }
            }
        }
    }

    private Idea constructWallIdea(Thing t) {

        Idea wallIdea = new Idea("Wall", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        wallIdea.add(posIdea);
        wallIdea.add(new Idea("Color", t.getColor(), "Property", 1));
        return wallIdea;
    }
}
