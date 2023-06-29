package CSTEpisodicMemory.motor;

import CSTEpisodicMemory.util.IdeaHelper;
import WS3DCoppelia.model.Agent;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.representation.idea.Idea;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class HandsActuatorCodelet extends Codelet {

    private MemoryContainer handsMO;

    private List<String> avaiableActions = Arrays.asList("Collect", "Eat", "Deliver");

    private Agent creature;

    public HandsActuatorCodelet(Agent creature) {
        this.creature = creature;
    }

    @Override
    public void accessMemoryObjects() {
        this.handsMO = (MemoryContainer) getInput("HANDS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea action = (Idea) handsMO.getI();
        if (action != null) {
            //System.out.println(IdeaHelper.fullPrint(action));
            String command = (String) action.getValue();
            if (command != null) {
                if (avaiableActions.contains(command)) {
                    if (command.equals("Collect")) {
                        int jewelID = (int) action.get("Jewel_ID").getValue();
                        creature.sackIt(jewelID);
                    }
                    if (command.equals("Eat")){
                        int foodID = (int) action.get("Food_ID").getValue();
                        creature.eatIt(foodID);
                    }
                }
            }
        }

    }
}
