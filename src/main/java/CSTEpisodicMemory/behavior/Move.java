package CSTEpisodicMemory.behavior;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;

public class Move extends Codelet {

    private MemoryContainer legsMO;
    private MemoryContainer impulseMO;
    private Memory locationsMO;
    private Memory epltMO;
    private Memory innerMO;
    private Memory roomMO;
    private Memory extra;

    private Idea impulse = null;
    private Idea lastImpulse;
    private List<Idea> plan = null;
    private List<Idea> highPlan = null;
    private List<Idea> locations;
    private GraphIdea epltmGraph;
    private Idea currPos;

    public Move() {
        this.name = "MoveBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (MemoryContainer) getInput("IMPULSES");
        synchronized (impulseMO) {
            Idea impulse_ = (Idea) impulseMO.getI();
            if (impulse_ != null) {
                if (this.impulse == null) {
                    this.impulse = IdeaHelper.cloneIdea(impulse_);
                } else {
                    if (this.impulseMO.getAllMemories().stream().map(m -> (Idea) m.getI()).noneMatch(o -> IdeaHelper.match(o, impulse)) ||
                            (double) this.impulse.get("State.Desire").getValue() < (double) impulse_.get("State.Desire").getValue()) {
                        this.impulse = IdeaHelper.cloneIdea(impulse_);
                    }
                }
            }
        }
        this.legsMO = (MemoryContainer) getOutput("LEGS");
        this.locationsMO = (MemoryObject) getInput("LOCATION");
        this.epltMO = (MemoryObject) getInput("EPLTM");
        this.innerMO = (MemoryObject) getInput("INNER");
        this.roomMO = (MemoryObject) getInput("ROOM");
        this.extra = (Memory) getOutput("extra");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (impulse != null) {
            if (impulse.get("State.Self.Position") != null && innerMO.getI() != null) {
                currPos = IdeaHelper.cloneIdea((Idea) innerMO.getI()).get("Position");
                if (!IdeaHelper.match(impulse, lastImpulse)) {
                    locations = (List<Idea>) locationsMO.getI();
                    epltmGraph = new GraphIdea((GraphIdea) epltMO.getI());

                    planTrajectory(impulse.get("State.Self.Position"));
                    lastImpulse = impulse;
                }
                Idea action = nextPlanAction();
                //float px = (float) impulse.get("State.Self.Position.X").getValue();
                //float py = (float) impulse.get("State.Self.Position.Y").getValue();
                //Idea action = new Idea("Action", "Move", "Episode", 0);
                //action.add(new Idea("X", px));
                //action.add(new Idea("Y", py));
                legsMO.setI(action, (double) impulse.get("State.Desire").getValue(), this.name);
            } else {
                legsMO.setI(null, 0.0, this.name);
            }
        }
    }

    private void planTrajectory(Idea dest) {
        Idea room;
        synchronized (roomMO) {
            room = (Idea) roomMO.getI();
        }

        if (room != null) {
            room = (Idea) room.get("Location").getValue();
            Idea destRoom = (Idea) dest.getValue();

            if (room != destRoom) {
                highPlan = path(room, destRoom);
            }
        }
    }

    private List<Idea> path(Idea room, Idea destRoom) {
        Map<Idea, Double> distMap = new HashMap<>();
        distMap.put(room, 1.0d);
        distMap = makeDistMap(room, distMap);
        System.out.println(distMap);
        return null;
    }

    private Map<Idea, Double> makeDistMap(Idea room, Map<Idea, Double> distMap) {
        List<Idea> adjacents = room.get("Adjacent").getL();
        double roomValue = distMap.get(room);

        for (Idea adjacent : adjacents) {
            double adjValue = distMap.getOrDefault(adjacent, 0d);
            if (adjValue < roomValue * 0.9) {
                distMap.put(adjacent, roomValue * 0.9);
            }
        }
        for (Idea adjacent : adjacents) {
            double adjValue = distMap.getOrDefault(adjacent, 0d);
            if (adjValue == roomValue * 0.9) {
                distMap = makeDistMap(adjacent, distMap);
            }
        }
        return distMap;
    }
    //private void planTrajectory(Idea choosenLoc) {
    //    if (locations.size() >= 4) {
    //        Idea bestTargetLoc = null;
    //        double bestTargetMem = 0;
    //        Idea bestStartLoc = null;
    //        double bestStartMem = 0;
    //        for (Idea loc : locations) {
    //            double currPosMem = loc.membership(currPos);
    //            double targetPosMem = loc.membership(choosenLoc);

    //            if (currPosMem > bestStartMem) {
    //                bestStartLoc = loc;
    //                bestStartMem = currPosMem;
    //            }
    //            if (targetPosMem > bestTargetMem) {
    //                bestTargetLoc = loc;
    //                bestTargetMem = targetPosMem;
    //            }
    //        }

    //        epltmGraph.setNodeActivation(bestTargetLoc, 1);
    //        epltmGraph.propagateActivations(Arrays.asList("Before", "Overlap", "Meet", "Start", "During", "Finish", "Equal", "Position", "Next"),
    //                Arrays.asList("Before", "Overlap", "Meet", "Start", "During", "Finish", "Equal", "Position", "Next"));
    //        List<Idea> locationNodes = epltmGraph.getLocationNodes();
    //        locationNodes.sort(Comparator.comparing(idea -> ((Double) idea.get("Activation").getValue())));

    //        //try {
    //        //    PrintWriter out = new PrintWriter("./locations");
    //        //    Idea tt = new Idea("ttt", null);
    //        //    tt.setL(epltmGraph.getLocationNodes());
    //        //    String csv = IdeaHelper.csvPrint(tt, 6);
    //        //    out.println(csv);
    //        //    out.close();
    //        //} catch (FileNotFoundException e) {
    //        //    throw new RuntimeException(e);
    //        //}
    //        //try {
    //        //    PrintWriter out = new PrintWriter("./epltm");
    //        //    String csv = IdeaHelper.csvPrint(epltmGraph.graph, 4);
    //        //    out.println(csv);
    //        //    out.close();
    //        //} catch (FileNotFoundException e) {
    //        //    throw new RuntimeException(e);
    //        //}
    //        //try {
    //        //    PrintWriter out = new PrintWriter("./graph");
    //        //    StringBuilder a = new StringBuilder();
    //        //    StringBuilder b = new StringBuilder();
    //        //    for (Idea n : epltmGraph.graph.getL()) {
    //        //        String name = n.get("Content").getL().get(0).getName();
    //        //        a.append("\n").append(name);
    //        //        Map<String, List<Idea>> ll = epltmGraph.getSuccesors(n);
    //        //        for (List<Idea> lll : ll.values()) {
    //        //            for (Idea llll : lll) {
    //        //                b.append("\n").append(name).append(" ").append(llll.get("Content").getL().get(0).getName());
    //        //            }
    //        //        }
    //        //    }
    //        //    out.println(a.append(b).toString());
    //        //    out.close();
    //        //} catch (FileNotFoundException e) {
    //        //    throw new RuntimeException(e);
    //        //}

    //        extra.setI(epltmGraph.getLocationNodes());
    //        plan = locationNodes;
    //        int firstIdx = plan.indexOf(epltmGraph.getNodeFromContent(bestStartLoc));
    //        //System.out.println("Start Idx " + firstIdx);
    //        if (firstIdx > 0) {
    //            plan.subList(0, firstIdx).clear();
    //        }
    //    }
    //}

    private Idea nextPlanAction() {
        if (plan != null && !plan.isEmpty()) {

            Idea nextMoveLoc = GraphIdea.getNodeContent(plan.get(0));
            double mm = nextMoveLoc.membership(currPos);
            if (mm == 1) {
                plan.remove(0);
                //System.out.println("----");
                //System.out.println(mm);
                //System.out.println(IdeaHelper.fullPrint(nextMoveLoc));
                //System.out.println(plan);
            }

            Idea action = new Idea("Action", "Move", "Action", 1);
            action.add(new Idea("X", nextMoveLoc.get("centerX").getValue()));
            action.add(new Idea("Y", nextMoveLoc.get("centerY").getValue()));
            return action;
        }

        float px = (float) impulse.get("State.Self.Position.X").getValue();
        float py = (float) impulse.get("State.Self.Position.Y").getValue();
        Idea action = new Idea("Action", "Move", "Action", 1);
        action.add(new Idea("X", px));
        action.add(new Idea("Y", py));
        return action;
    }
}
