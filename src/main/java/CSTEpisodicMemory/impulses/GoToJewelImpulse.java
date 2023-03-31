package CSTEpisodicMemory.impulses;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class GoToJewelImpulse extends Codelet {

    private Memory innerSenseMO;
    private Memory leafletMO;
    private Memory jewelsMO;
    private Memory impulsesMO;
    private Idea inner;
    private Idea leaflets;
    private Idea jewels;
    private Idea impulses;

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) getInput("INNER");
        this.inner = (Idea) innerSenseMO.getI();
        this.leafletMO = (MemoryObject) getInput("LEAFLETS");
        this.leaflets = (Idea) leafletMO.getI();
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.jewels = (Idea) jewelsMO.getI();
        this.impulsesMO = (MemoryObject) getOutput("IMPULSES");
        this.impulses = (Idea) impulsesMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        int numJewels = jewels.getL().size();
        if (numJewels > 0){
            for (Idea jewel : jewels.getL()){
                if (canReduceDrive(jewel)){
                    Idea impulse = createImpulse(jewel);
                    addIfNotPresent(impulse);
                }
            }
        }
        System.out.println(fullPrint(jewels));
        System.out.println(fullPrint(impulses));
    }

    private boolean canReduceDrive(Idea jewel) {
        for (Idea leaflet : leaflets.getL()){
            for (Idea jewelColor : leaflet.getL()){
                if (jewelColor.get("Remained") != null)
                    if ((int) jewelColor.get("Remained").getValue() > 0 && jewelColor.getName().equals(jewel.getValue()))
                        return true;
            }
        }
        return false;
    }

    private Idea createImpulse(Idea jewel) {
        Idea impulse = new Idea("Impulse", null, "Episode", 0);
        Idea state = new Idea("State", null, "Timestamp", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(jewel.get("Position").clone());
        state.add(self);
        state.add(jewel.get("ID").clone());
        impulse.add(state);
        return impulse;
    }

    public void addIfNotPresent(Idea idea){
        for (Idea present : impulses.getL()){
            if ((int) present.get("State.ID").getValue() == (int) idea.get("State.ID").getValue())
                return;
        }
        impulses.add(idea);
    }

}