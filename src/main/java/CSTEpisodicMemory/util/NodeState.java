package CSTEpisodicMemory.util;

import br.unicamp.cst.representation.idea.Idea;

public class NodeState {

    public Idea idea;
    public double eval = 0;

    public NodeState(Idea idea, double eval) {
        this.idea = idea;
        this.eval = eval;
    }

    @Override
    public String toString() {
        return "Node{" +
                "idea=" + idea.getName() +
                ", eval=" + eval +
                '}';
    }
}
