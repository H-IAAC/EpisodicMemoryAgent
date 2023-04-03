package CSTEpisodicMemory.behavior;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

public class Move extends Codelet {

    private MemoryContainer legsMO;
    private Memory impulseMO;

    private Idea impulses;

    public Move() {
        this.name = "MoveBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (MemoryObject) getInput("IMPULSES");
        this.impulses = (Idea) this.impulseMO.getI();
        this.legsMO = (MemoryContainer) getOutput("LEGS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea mostIntenseImpulse = null;
        for (Idea impulse : impulses.getL()){
            if (impulse.get("State.Self.Position") != null){
                if(mostIntenseImpulse == null){
                    mostIntenseImpulse = impulse;
                } else {
                    double maxDesire = (double) mostIntenseImpulse.get("State.Desire").getValue();
                    double checkDesire = (double) impulse.get("State.Desire").getValue();
                    if (checkDesire > maxDesire){
                        mostIntenseImpulse = impulse;
                    }
                }
            }
        }

        if (mostIntenseImpulse != null) {
            float px = (float) mostIntenseImpulse.get("State.Self.Position.X").getValue();
            float py = (float) mostIntenseImpulse.get("State.Self.Position.Y").getValue();
            Idea action = new Idea("Action", "Move", "Episode", 0);
            action.add(new Idea("X", px));
            action.add(new Idea("Y", py));
            legsMO.setI(action, (double) mostIntenseImpulse.get("State.Desire").getValue(), this.name);
        } else {
            legsMO.setI(null, 0.0, this.name);
        }
    }
}
