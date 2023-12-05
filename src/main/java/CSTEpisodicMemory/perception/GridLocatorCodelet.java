package CSTEpisodicMemory.perception;

import CSTEpisodicMemory.core.representation.GridLocation;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridLocatorCodelet extends Codelet {

    private Memory input;
    private Memory context;
    private Memory roomCategories;
    private Idea detectedRoom;
    private Memory output;
    private final GridLocation locator = GridLocation.getInstance();
    private String in;
    private String out;

    public GridLocatorCodelet(String in, String out) {
        this.in = in;
        this.out = out;
        this.name = "GridLocator_" + in;
    }

    @Override
    public void accessMemoryObjects() {
        input = (MemoryObject) getInput(in);
        context = (MemoryObject) getInput("ROOM");
        output = (MemoryObject) getOutput(out);
        roomCategories = (MemoryObject) getInput("ROOM_CATEGORIES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (input) {
            synchronized (context) {
                Idea in = (Idea) input.getI();
                detectedRoom = (Idea) context.getI();
                Idea pos = in.get("Position");
                if (pos != null) {
                    if (detectedRoom != null && detectedRoom.get("Location") != null) {
                        Idea out = setGridPlaceFromPos(pos, in);
                        synchronized (output) {
                            output.setI(out);
                        }
                    }
                } else {
                    List<Idea> subIdea = Collections.synchronizedList(in.getL());
                    if (!subIdea.isEmpty()) {
                        List<Idea> outL = new ArrayList<>();
                        for (Idea sub : subIdea) {
                            pos = sub.get("Position");
                            if (pos != null && sub.get("Occupation") == null) {
                                if (detectedRoom != null && detectedRoom.get("Location") != null) {
                                    Idea out = setGridPlaceFromPos(pos, sub);
                                    outL.add(out);
                                } else {
                                    outL.add(sub.clone());
                                }
                            } else {
                                outL.add(sub.clone());
                            }
                        }
                        synchronized (output) {
                            Idea outIdea = new Idea(in.getName(), in.getValue(), in.getCategory(), in.getScope());
                            outIdea.setL(outL);
                            output.setI(outIdea);
                        }
                    }
                }
            }
        }
    }

    @NotNull
    private Idea setGridPlaceFromPos(Idea pos, Idea in) {
        double x = Double.parseDouble(pos.get("X").getValue().toString());
        double y = Double.parseDouble(pos.get("Y").getValue().toString());
        Idea room = (Idea) detectedRoom.get("Location").getValue();
        double px = x - (double) room.get("center.x").getValue();
        double py = y - (double) room.get("center.y").getValue();

        Idea out = in.clone();
        Idea occupation = new Idea("Occupation", null, "Aggregate", 1);
        Idea size = out.get("Size");
        if (size == null) {
            Idea gridPlace = locator.locateHCCIdea(px, py);
            occupation.add(gridPlace);
        } else {
            double width = (double) size.get("Width").getValue();
            double depth = (double) size.get("Depth").getValue();
            int[] minCorner = locator.locateHCC(px - width / 2, py - depth / 2);
            int[] maxCorner = locator.locateHCC(px + width / 2, py + depth / 2);
            for (int i = minCorner[0]; i <= maxCorner[0]; i++) {
                for (int j = minCorner[1]; j <= maxCorner[1]; j++) {
                    Idea gridPlace = locator.getReferenceGridIdea(i, j);
                    occupation.add(gridPlace);
                }
            }
        }
        out.add(occupation);
        out.get("Position").setValue(room);
        //List<Idea> roomCats = Collections.synchronizedList((List<Idea>) roomCategories.getI());
        //for (Idea cat : roomCats) {
        //    if (cat.membership(pos) > 0) {
        //        out.get("Position").setValue(cat);
        //    }
        //}
        return out;
    }
}
