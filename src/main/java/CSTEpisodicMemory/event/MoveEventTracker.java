package CSTEpisodicMemory.event;

import CSTEpisodicMemory.entity.EventTracker;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.List;

public class MoveEventTracker extends EventTracker {

    private boolean debug = false;

    public MoveEventTracker(){
        super("INNER", "EVENTS");
        this.name = "MoveEventTracker";
        this.setEventCategoryName("Move");
        this.setBufferSize(2);
        this.setBufferStepSize(4);
    }

    public MoveEventTracker(boolean debug){
        super("INNER", "EVENTS");
        this.name = "MoveEventTracker";
        this.debug = debug;
        this.setEventCategoryName("Move");
        this.setBufferSize(2);
        this.setBufferStepSize(4);
    }

    @Override
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
        if (pointA.sub(pointB).magnitude() > 0.05) {
            Vector2D prevDirVector = pointB.sub(pointA).normalize();
            Vector2D currDirVector = pointC.sub(pointB).normalize();
            return prevDirVector.angle(currDirVector) < 0.01;
        }
        return false;
    }

    @Override
    public Idea extractRelevant(Idea i){
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(i.get("Position").clone());
        self.add(i.get("Step").clone());
        return self;
    }

}
