package CSTEpisodicMemory.perception;

import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Identifiable;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AgentDetector extends Codelet {

    private Memory visionMO;
    private Memory currentAgentsMO;

    public AgentDetector(){
        this.name = "AgentDetector";
    }

    @Override
    public void accessMemoryObjects() {
        visionMO = (MemoryObject) getInput("VISION");
        currentAgentsMO = (MemoryObject) getOutput("AGENTS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (visionMO) {
            synchronized (currentAgentsMO) {
                CopyOnWriteArrayList<Identifiable> vision = new CopyOnWriteArrayList((List<Identifiable>) visionMO.getI());
                Idea agentsIdea = ((Idea) currentAgentsMO.getI());
                agentsIdea.setL(new ArrayList<>());
                agentsIdea.setType(5);
                for (Identifiable obj : vision) {
                    if (obj instanceof Agent) {
                        agentsIdea.add(constructAgentIdea((Agent) obj));
                    }
                }
            }
        }
    }

    private Idea constructAgentIdea(Agent agent) {
        Idea agentIdea = new Idea("Agent" + agent.getId(), "Agent" + agent.getId(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", agent.getPosition().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", agent.getPosition().get(1), "QualityDimension", 1));
        agentIdea.add(posIdea);
        agentIdea.add(new Idea("ID", agent.getId(), "Property", 1));
        Idea color = new Idea("Color", agent.getColorName(), "Property", 1);
        color.add(new Idea("R", agent.getColor().get(0), "QualityDimension", 1));
        color.add(new Idea("G", agent.getColor().get(1), "QualityDimension", 1));
        color.add(new Idea("B", agent.getColor().get(2), "QualityDimension", 1));
        agentIdea.add(color);
        return agentIdea;
    }
}
