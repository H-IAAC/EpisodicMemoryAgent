package CSTEpisodicMemory.event;

import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.List;

public class MoveEventTracker extends Codelet {

    private Memory innerSenseMO;
    private Memory eventsMO;
    private boolean debug = false;
    private int bufferSize = 2;
    private List<Idea> previousSelfIdea = new LinkedList<Idea>();
    private Idea firstSelfIdea;
    private Idea lastSelfIdea;
    private Idea currentInnerSense;
    private int count = 1;

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
        //Initialize event track buffer
        if (previousSelfIdea.size() < this.bufferSize){
            previousSelfIdea.add(currentInnerSense.clone());
        } else {
            //Check if current state is coherent with previous states and event category
            if (belongsToEvent(previousSelfIdea, currentInnerSense)){
                Idea drop = previousSelfIdea.remove(0);
                //Copies start of the event
                if (firstSelfIdea == null) this.firstSelfIdea = drop.clone();
                previousSelfIdea.add(currentInnerSense.clone());
            } else {
                Idea event = constructEventIdea(previousSelfIdea.get(1));
                previousSelfIdea.clear();
                previousSelfIdea.add(currentInnerSense);
                firstSelfIdea = null;
                Idea eventsIdea = (Idea) eventsMO.getI();
                eventsIdea.add(event);
            }
        }
    }

    public boolean belongsToEvent(List<Idea> previous, Idea current){
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
        return prevDirVector.angle(currDirVector) < 0.01;
    }

    private Idea constructEventIdea(Idea lastSelf){
        Idea eventIdea = new Idea("Event" + count++, "Move", "Episode", 1);
        Idea time1 = new Idea("", 1, "TimeStep", 1);
        Idea time2 = new Idea("", 2, "TimeStep", 1);
        time1.add(extractRelevant(firstSelfIdea));
        time2.add(extractRelevant(lastSelf));
        eventIdea.add(time1);
        eventIdea.add(time2);
        return eventIdea;
    }

    private Idea extractRelevant(Idea i){
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(i.get("Position").clone());
        self.add(i.get("Step").clone());
        return self;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize > 0) this.bufferSize = bufferSize;
    }
}
