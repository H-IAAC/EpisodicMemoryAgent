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

public class WallDetector extends Codelet {
    private Memory visionMO;
    private Memory currentWallsMO;
    private Memory knownWallsMO;
    private boolean debug = false;
    private Idea detectedRoom = null;

    public WallDetector() {
        this.name = "WallDetector";
    }

    public WallDetector(boolean debug) {
        this.name = "WallDetector";
        this.debug = debug;
    }

    @Override
    public void accessMemoryObjects() {
        this.visionMO = (MemoryObject) this.getInput("VISION");
        MemoryObject roomMO = (MemoryObject) this.getInput("ROOM");
        if (roomMO != null)
            detectedRoom = (Idea) roomMO.getI();
        this.currentWallsMO = (MemoryObject) this.getOutput("WALLS");
        this.knownWallsMO = (MemoryObject) this.getInput("KNOWN_WALLS");

    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (visionMO) {
            synchronized (currentWallsMO) {
                CopyOnWriteArrayList<Identifiable> vision = new CopyOnWriteArrayList((List<Identifiable>) visionMO.getI());
                Idea wallsIdea = ((Idea) currentWallsMO.getI());
                if (debug) {
                    System.out.println(wallsIdea.toStringFull());
                }
                wallsIdea.setL(new ArrayList<>());
                for (Identifiable obj : vision) {
                    if (obj instanceof Thing) {
                        Thing t = (Thing) obj;
                        if (t.isBrick()) {
                            wallsIdea.add(constructWallIdea(t));
                        }
                    }
                }
                currentWallsMO.setI(wallsIdea);
            }
        }
    }

    private Idea constructWallIdea(Thing t) {

        Idea wallIdea = new Idea("Wall", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        wallIdea.add(posIdea);
        Idea sizeIdea = new Idea("Size", null, "Property", 1);
        sizeIdea.add(new Idea("Width", t.getWidth(), "QualityDimension", 1));
        sizeIdea.add(new Idea("Depth", t.getDepth(), "QualityDimension", 1));
        wallIdea.add(sizeIdea);
        Idea color = new Idea("Color", t.getTypeName().split("_")[0], "Property", 1);
        color.add(new Idea("R", t.getColor().get(0), "QualityDimension", 1));
        color.add(new Idea("G", t.getColor().get(1), "QualityDimension", 1));
        color.add(new Idea("B", t.getColor().get(2), "QualityDimension", 1));
        wallIdea.add(color);
        wallIdea.add(new Idea("ID", t.getId(), "Property", 1));
        if (detectedRoom != null) {
            Idea room = (Idea) detectedRoom.get("Location").getValue();
            double px = t.getPos().get(0) - (double) room.get("center.x").getValue();
            double py = t.getPos().get(1) - (double) room.get("center.y").getValue();
            Idea occupation = new Idea("Occupation", null, "Aggregate", 1);
            int[] minCorner = GridLocation.getInstance().locateHCC(px - t.getWidth() / 2, py - t.getDepth() / 2);
            int[] maxCorner = GridLocation.getInstance().locateHCC(px + t.getWidth() / 2, py + t.getDepth() / 2);
            for (int i = minCorner[0]; i <= maxCorner[0]; i++) {
                for (int j = minCorner[1]; j <= maxCorner[1]; j++) {
                    Idea gridPlace = GridLocation.getInstance().getReferenceGridIdea(i, j);
                    occupation.add(gridPlace);
                }
            }
            wallIdea.add(occupation);
        }
        synchronized (knownWallsMO){
            Idea outputIdea = (Idea) knownWallsMO.getI();
            List<Idea> known = Collections.synchronizedList(outputIdea.getL());
            for (Idea know : known) {
                if (know.get("ID").getValue().equals(t.getId())) {
                    wallIdea.add(know.get("Novelty"));
                    break;
                }
            }
        }
        return wallIdea;
    }
}
