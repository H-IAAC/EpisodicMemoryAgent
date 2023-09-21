package CSTEpisodicMemory.perception;

import CSTEpisodicMemory.core.representation.GridLocation;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridLocatorCodelet extends Codelet {

    private Memory input;
    private Memory context;
    private Idea detectedRoom;
    private Memory output;
    private final GridLocation locator = GridLocation.getInstance();
    private String in;
    private String out;

    public GridLocatorCodelet(String in, String out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void accessMemoryObjects() {
        input = (MemoryObject) getInput(in);
        context = (MemoryObject) getInput("ROOM");
        detectedRoom = (Idea) context.getI();
        output = (MemoryObject) getOutput(out);
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea in = (Idea) input.getI();
        Idea pos = in.get("Position");
        if (pos != null) {
            synchronized (detectedRoom) {
                if (detectedRoom != null && detectedRoom.get("Location") != null) {
                    Idea room = (Idea) detectedRoom.get("Location").getValue();
                    double px = Double.parseDouble(pos.get("X").getValue().toString()) - (double) room.get("center.x").getValue();
                    double py = Double.parseDouble(pos.get("Y").getValue().toString()) - (double) room.get("center.y").getValue();
                    Idea gridPlace = locator.locateHCCIdea(px, py);
                    Idea out = in.clone();
                    out.add(gridPlace);
                    synchronized (output) {
                        output.setI(out);
                    }
                }
            }
        } else {
            List<Idea> subIdea = Collections.synchronizedList(in.getL());
            synchronized (subIdea) {
                if (subIdea.size() > 1) {
                    List<Idea> outL = new ArrayList<>();
                    for (Idea sub : subIdea) {
                        pos = sub.get("Position");
                        if (pos != null && sub.get("Grid_Place") == null) {
                            synchronized (detectedRoom) {
                                if (detectedRoom != null && detectedRoom.get("Location") != null) {
                                    Idea room = (Idea) detectedRoom.get("Location").getValue();
                                    double px = Double.parseDouble(pos.get("X").getValue().toString()) - (double) room.get("center.x").getValue();
                                    double py = Double.parseDouble(pos.get("Y").getValue().toString()) - (double) room.get("center.y").getValue();
                                    Idea gridPlace = locator.locateHCCIdea(px, py);
                                    Idea out = sub.clone();
                                    out.add(gridPlace);
                                    outL.add(out);
                                } else {
                                    outL.add(sub.clone());
                                }
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
