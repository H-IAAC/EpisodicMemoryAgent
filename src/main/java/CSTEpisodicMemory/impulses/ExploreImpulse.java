package CSTEpisodicMemory.impulses;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;

public class ExploreImpulse extends Codelet {

    private Memory jewelsMO;
    private Memory innerMO;
    private Memory roomMO;
    private Memory roomsCategoriesMO;
    private MemoryContainer impulsesMO;
    private Memory locationsMO;
    private Memory epltMO;
    private Memory extra;

    private final String impulseCat = "Explore";

    @Override
    public void accessMemoryObjects() {
        this.jewelsMO = (MemoryObject) getInput("KNOWN_JEWELS");
        this.innerMO = (MemoryObject) getInput("INNER");
        this.roomMO = (MemoryObject) getInput("ROOM");
        this.roomsCategoriesMO = (MemoryObject) getInput("ROOM_CATEGORIES");
        this.impulsesMO = (MemoryContainer) getOutput("IMPULSES");
        this.locationsMO = (MemoryObject) getInput("LOCATION");
        this.epltMO = (MemoryObject) getInput("EPLTM");
        this.extra = (Memory) getOutput("extra");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea jewels = (Idea) jewelsMO.getI();
        Idea inner = (Idea) innerMO.getI();

        int numJewels = jewels.getL().size();
        if (numJewels == 0){
            synchronized (impulsesMO) {
                Idea impulse = (Idea) impulsesMO.getI(this.impulseCat);
                if (impulse != null) {
                    Vector2D dest = new Vector2D(
                            (float) impulse.get("State.Self.Position.X").getValue(),
                            (float) impulse.get("State.Self.Position.Y").getValue());
                    Vector2D curr = new Vector2D(
                            (float) inner.get("Position.X").getValue(),
                            (float) inner.get("Position.Y").getValue());
                    if (dest.sub(curr).magnitude() < 0.55) {
                        //removeSatisfiedImpulses();
                        Idea newDest = chooseLocation();
                        impulsesMO.setI(createImpulse(newDest, 0.1), 0.1, this.impulseCat);
                    }
                } else {
                    Idea dest = chooseLocation();
                    impulsesMO.setI(createImpulse(dest, 0.1), 0.1, this.impulseCat);
                }
            }
        } else {
            removeSatisfiedImpulses();
        }
    }

    private Idea chooseLocation() {
        Idea choosenLoc = null;
        //List known locations
        List<Idea> locations = (List<Idea>) locationsMO.getI();

        //Sample a location based on reward value
        List<Double> weigths = new LinkedList<>();
        Idea selected = null;
        if(!locations.isEmpty()) {
            synchronized (locations) {
                double total = 0d;
                for (Idea catLoc : locations){
                    double r = (double) catLoc.get("Reward").getValue();
                    total += r;
                    weigths.add(total);
                }
                //5% chance of choosing a random, possibly unexplored, location
                double rndChance = Math.exp(-locations.size()/10.0) + 0.05;
                double rnd = new Random().nextDouble() * total*(1+rndChance);
                //System.out.println(weigths);
                //System.out.println(rnd);
                total = 0;
                for (Idea catLoc : locations){
                    double r = (double) catLoc.get("Reward").getValue();
                    total += r;
                    if (rnd < total) {
                        selected = catLoc;
                        break;
                    }
                }
            }
        }

        boolean isInRoom = false;
        while (!isInRoom) {
            if (selected != null) {
                choosenLoc = selected.getInstance();
            } else {
                choosenLoc = new Idea("Position", null, "Property", 0);
                float x = 10 * new Random().nextFloat();
                float y = 8 * new Random().nextFloat();
                choosenLoc.add(new Idea("X", x, "QualityDimension", 0));
                choosenLoc.add(new Idea("Y", y, "QualityDimension", 0));
            }

            synchronized (roomMO){
                List<Idea> roomCategories = (List<Idea>) roomsCategoriesMO.getI();

                Idea currentRoom = ((Idea) roomMO.getI()).get("Location");
                if (currentRoom != null){
                    currentRoom = (Idea) currentRoom.getValue();
                    for (Idea room : roomCategories) {
                        if (room.membership(choosenLoc) > 0.8 && room.get("Adjacent").getL().contains(currentRoom))
                            isInRoom = true;
                    }
                } else {
                    for (Idea room : roomCategories) {
                        if (room.membership(choosenLoc) > 0.8)
                            isInRoom = true;
                    }
                }
            }
        }

        //planTrajectory(choosenLoc);

        return choosenLoc;
    }

    private void removeSatisfiedImpulses() {
        List<Memory> impulsesMemories = impulsesMO.getAllMemories();
        Memory remove = null;
        synchronized (impulsesMO) {
            for (Memory impulseMem : impulsesMemories) {
                Idea impulse = (Idea) impulseMem.getI();
                if (impulse.getValue().equals(this.impulseCat))
                    remove = impulseMem;
            }
            if (remove != null)
                impulsesMemories.remove(remove);
        }
    }

    private Idea createImpulse(Idea position, double desirability) {
        Idea impulse = new Idea("Impulse", this.impulseCat, "Goal", 0);
        Idea state = new Idea("State", null, "Timestamp", 0);
        Idea self = new Idea("Self", null, "AbstractObject", 1);
        self.add(position);
        state.add(self);
        state.add(new Idea("Desire", desirability, "Property", 1));
        impulse.add(state);
        return impulse;
    }

    private void planTrajectory(Idea choosenLoc) {
        List<Idea> locations = (List<Idea>) locationsMO.getI();
        if (!locations.isEmpty()) {
            GraphIdea epltmGraph = new GraphIdea((GraphIdea) epltMO.getI());

            Idea currPos = ((Idea) innerMO.getI()).clone().get("Position");

            Idea bestTargetLoc = null;
            double bestTargetMem = 0;
            Idea bestStartLoc = null;
            double bestStartMem = 0;
            for (Idea loc : locations) {
                double currPosMem = loc.membership(currPos);
                double targetPosMem = loc.membership(choosenLoc);

                if (currPosMem > bestStartMem) {
                    bestStartLoc = loc;
                    bestStartMem = currPosMem;
                }
                if (targetPosMem > bestTargetMem) {
                    bestTargetLoc = loc;
                    bestTargetMem = targetPosMem;
                }
            }

            epltmGraph.setNodeActivation(bestTargetLoc, 1);
            epltmGraph.propagateActivations(Arrays.asList("Before","Overlap","Meet","Start","During","Finish","Equal","SpatialContext","Next","Begin","End"),
                    Arrays.asList("Before","Overlap","Meet","Start","During","Finish","Equal","SpatialContext","End","Begin","Next"));
            List<Idea> locationNodes = epltmGraph.getLocationNodes();
            locationNodes.sort(Comparator.comparing(idea -> ((Double) idea.get("Activation").getValue())));

            extra.setI(epltmGraph.getLocationNodes());

            for (Idea ll : locationNodes){
                System.out.println((double)ll.get("Activation").getValue());
            }
        }

    }
}
