package CSTEpisodicMemory.event;

import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MoveEventTracker extends Codelet {

    private Memory innerSenseMO;
    private Memory eventsMO;
    private boolean debug = false;
    private int bufferSize = 2;
    private List<Idea> previousSelfIdea = new LinkedList<Idea>();
    private Idea firstSelfIdea;
    private Idea currentInnerSense;

    public MoveEventTracker(){
        this.name = "JewelDetector";
    }

    public MoveEventTracker(boolean debug){
        this.name = "JewelDetector";
        this.debug = debug;
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO=(MemoryObject)this.getInput("INNER");
        this.currentInnerSense = (Idea) innerSenseMO.getI();
        this.eventsMO=(MemoryObject)this.getOutput("EVENTS");

    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        //Copies start of the event
        if (previousSelfIdea.size() == 0) this.firstSelfIdea = currentInnerSense.clone();

        if (previousSelfIdea.size() < this.bufferSize){
            previousSelfIdea.add(currentInnerSense.clone());
        } else {
            belongsToEvent(previousSelfIdea, currentInnerSense);
        }
    }

    public void belongsToEvent(List<Idea> previous, Idea current){
        Idea a = previous.get(0);
        Idea b = previous.get(1);
        Vector2D pointA = new Vector2D(
                (float) a.get("Position.X").getValue(),
                (float) a.get("Position.Y").getValue());
        Vector2D pointB = new Vector2D(
                (float) b.get("Position.X").getValue(),
                (float) b.get("Position.Y").getValue());
        Vector2D pointC = new Vector2D(
                (float) current.get("Position.X").getValue(),
                (float) current.get("Position.Y").getValue());
        Vector2D prevDirVector = pointB.sub(pointA).normalize();
        Vector2D currDirVector = pointC.sub(pointB).normalize();

    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize > 0) this.bufferSize = bufferSize;
    }
}
