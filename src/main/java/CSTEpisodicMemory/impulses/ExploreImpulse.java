package CSTEpisodicMemory.impulses;

import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

public class ExploreImpulse extends Codelet {

    private Memory jewelsMO;
    private Memory innerMO;
    private Memory roomMO;
    private Memory impulsesMO;

    private List<Idea> roomCategories;
    private String impulseCat = "Explore";

    public ExploreImpulse(List<Idea> roomCategories) {
        this.roomCategories = roomCategories;
    }

    @Override
    public void accessMemoryObjects() {
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.innerMO = (MemoryObject) getInput("INNER");
        this.roomMO = (MemoryObject) getInput("ROOM");
        this.impulsesMO = (MemoryObject) getOutput("IMPULSES");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea jewels = (Idea) jewelsMO.getI();
        Idea impulses = (Idea) impulsesMO.getI();
        Idea inner = (Idea) innerMO.getI();

        int numJewels = jewels.getL().size();
        if (numJewels == 0){
            boolean foundPrevImpulse = false;
            for (Idea impulse : impulses.getL()){
                if (impulse.getValue().equals(this.impulseCat)){
                    foundPrevImpulse = true;
                    Vector2D dest = new Vector2D(
                            (float) impulse.get("Self.Position.X").getValue(),
                            (float) impulse.get("Self.Position.Y").getValue());
                    Vector2D curr = new Vector2D(
                            (float) inner.get("Position.X").getValue(),
                            (float) inner.get("Position.Y").getValue());
                    if (dest.sub(curr).magnitude() < 0.15) {
                        removeSatisfiedImpulses();
                        Vector2D newDest = chooseLocation();
                        impulses.add(createImpulse(newDest, 0.1));
                    }
                }
            }
            if (!foundPrevImpulse){
                Vector2D dest = chooseLocation();
                impulses.add(createImpulse(dest, 0.1));
            }
        } else {
            removeSatisfiedImpulses();
        }
    }

    private Vector2D chooseLocation() {
        return null;
    }

    private void removeSatisfiedImpulses() {
        Idea impulses = (Idea) impulsesMO.getI();
        Idea remove = null;
        for (Idea impulse : impulses.getL()){
            if (impulse.getValue().equals(this.impulseCat))
                remove = impulse;
        }
        impulses.getL().remove(remove);
    }

    private Idea createImpulse(Vector2D pos, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Episode", 0);
        Idea state = new Idea("State", null, "Timestamp", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        Idea position = new Idea("Position", null, "Property", 0);
        position.add(new Idea("X", pos.getX(), "QualityDimension", 0));
        position.add(new Idea("Y", pos.getY(), "QualityDimension", 0));
        self.add(position);
        state.add(self);
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }
}
