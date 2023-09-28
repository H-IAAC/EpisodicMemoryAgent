package CSTEpisodicMemory.motor;

import WS3DCoppelia.model.Agent;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Arrays;
import java.util.List;

public class LegsActuatorCodelet extends Codelet {

    private MemoryContainer legsMO;

    private final Agent creature;

    private final List<String> avaiableActions = List.of("Move");

    public LegsActuatorCodelet(Agent creature) {
        this.creature = creature;
    }

    @Override
    public void accessMemoryObjects() {
        this.legsMO = (MemoryContainer) getInput("LEGS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (legsMO){
        Idea action = (Idea) legsMO.getI();
        if (action != null) {
            String command = (String) action.getValue();
            if (command != null) {
                if (avaiableActions.contains(command)) {
                    if (command.equals("Move")) {
                        float px = (float) action.get("X").getValue();
                        float py = (float) action.get("Y").getValue();
                        creature.moveTo(px, py);
                    } else if (command.equals("Rotate")) {
                        creature.rotate();
                    } else {
                        creature.stop();
                    }
                }
            }
        }
        }
    }
}
