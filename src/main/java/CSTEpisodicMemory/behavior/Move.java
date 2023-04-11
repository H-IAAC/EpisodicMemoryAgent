package CSTEpisodicMemory.behavior;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

public class Move extends Codelet {

    private MemoryContainer legsMO;
    private MemoryContainer impulseMO;

    private Idea impulse;

    public Move() {
        this.name = "MoveBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (MemoryContainer) getInput("IMPULSES");
        this.impulse = (Idea) this.impulseMO.getI();
        this.legsMO = (MemoryContainer) getOutput("LEGS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (impulse != null) {
            if (impulse.get("State.Self.Position") != null) {
                float px = (float) impulse.get("State.Self.Position.X").getValue();
                float py = (float) impulse.get("State.Self.Position.Y").getValue();
                Idea action = new Idea("Action", "Move", "Episode", 0);
                action.add(new Idea("X", px));
                action.add(new Idea("Y", py));
                legsMO.setI(action, (double) impulse.get("State.Desire").getValue(), this.name);
            } else {
                legsMO.setI(null, 0.0, this.name);
            }
        }
    }
}
