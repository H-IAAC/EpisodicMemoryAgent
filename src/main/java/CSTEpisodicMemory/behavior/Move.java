package CSTEpisodicMemory.behavior;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Move extends Codelet {

    private MemoryContainer legsMO;
    private MemoryContainer impulseMO;
    private Memory locationsMO;
    private Memory epltMO;
    private Memory innerMO;
    private Memory extra;

    private Idea impulse = null;
    private Idea lastImpulse;
    private List<Idea> plan = null;
    private List<Idea> locations;
    private GraphIdea epltmGraph;
    private Idea currPos;

    public Move() {
        this.name = "MoveBehaviour";
    }

    @Override
    public void accessMemoryObjects() {
        this.impulseMO = (MemoryContainer) getInput("IMPULSES");
        Idea impulse_ = (Idea) this.impulseMO.getI();
        if (impulse_ != null)
            this.impulse = impulse_.clone();
        this.legsMO = (MemoryContainer) getOutput("LEGS");
        this.locationsMO = (MemoryObject) getInput("LOCATION");
        this.epltMO = (MemoryObject) getInput("EPLTM");
        this.innerMO = (MemoryObject) getInput("INNER");
        this.extra = (Memory) getOutput("extra");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (impulse != null) {
            if (impulse.get("State.Self.Position") != null) {
                currPos = ((Idea) innerMO.getI()).clone().get("Position");
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

    private void planTrajectory(Idea choosenLoc) {
        if (locations.size() >= 4) {
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
            epltmGraph.propagateActivations(Arrays.asList("Before","Overlap","Meet","Start","During","Finish","Equal","SpatialContext","Next"),
                    Arrays.asList("Before","Overlap","Meet","Start","During","Finish","Equal","SpatialContext","Next"));
            List<Idea> locationNodes = epltmGraph.getLocationNodes();
            locationNodes.sort(new Comparator<Idea>() {
                @Override
                public int compare(Idea idea, Idea t1) {
                    return ((Double) idea.get("Activation").getValue()).compareTo((Double) t1.get("Activation").getValue());
                }
            });

            try {
                PrintWriter out = new PrintWriter("./locations");
                Idea tt = new Idea("ttt", null);
                tt.setL(epltmGraph.getLocationNodes());
                String csv = IdeaHelper.csvPrint(tt, 6);
                out.println(csv);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                PrintWriter out = new PrintWriter("./epltm");
                String csv = IdeaHelper.csvPrint(epltmGraph.graph, 4);
                out.println(csv);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                PrintWriter out = new PrintWriter("./graph");
                StringBuilder a = new StringBuilder();
                StringBuilder b = new StringBuilder();
                for (Idea n : epltmGraph.graph.getL()){
                    String name = n.get("Content").getL().get(0).getName();
                    a.append("\n").append(name);
                    Map<String, List<Idea>> ll = epltmGraph.getSuccesors(n);
                    for (List<Idea> lll : ll.values()){
                        for (Idea llll : lll){
                            b.append("\n").append(name).append(" ").append(llll.get("Content").getL().get(0).getName());
                        }
                    }
                }
                out.println(a.append(b).toString());
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            extra.setI(epltmGraph.getLocationNodes());
            plan = locationNodes;
            int firstIdx = plan.indexOf(epltmGraph.getNodeFromContent(bestStartLoc));
            //System.out.println("Start Idx " + firstIdx);
            if (firstIdx > 0) {
                plan.subList(0, firstIdx).clear();
            }
        }
    }

    private Idea nextPlanAction(){
        if (plan != null && plan.size() > 0) {

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
            action.add(new Idea("X", (float) nextMoveLoc.get("centerX").getValue()));
            action.add(new Idea("Y", (float) nextMoveLoc.get("centerY").getValue()));
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
