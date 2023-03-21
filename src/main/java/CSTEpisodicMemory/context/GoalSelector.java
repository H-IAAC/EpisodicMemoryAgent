package CSTEpisodicMemory.context;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class GoalSelector extends Codelet {

    private Memory innerSenseMO;
    private Memory jewelsMO;
    private Memory wallsMO;
    private Memory goalsMO;

    private Idea currentInnerSense;
    private Idea knownJewels;
    private Idea walls;
    private Idea goals;

    private boolean debug = true;

    public GoalSelector(){
        this.name = "GoalSelector";
    }

    public GoalSelector(boolean debug){
        this.name = "GoalSelector";
        this.debug = debug;
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO=(MemoryObject)this.getInput("INNER");
        this.currentInnerSense = (Idea) innerSenseMO.getI();
        this.jewelsMO=(MemoryObject)this.getInput("KNOWN_JEWELS");
        this.knownJewels = (Idea) jewelsMO.getI();
        this.wallsMO=(MemoryObject)this.getInput("WALLS");
        this.walls = (Idea) wallsMO.getI();
        this.goalsMO=(MemoryObject)this.getOutput("GOALS");
        this.goals = (Idea) goalsMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        //Initialize primary goal to collect all jewels
        if (goals.getL().size() == 0) initializeSuperGoal();

        if (knownJewels.getL().size() == 0){
            setFindJewelGoal();
        } else {
            setPickJewelGoal();
        }
        if (debug) System.out.println(fullPrint(goals));
    }

    private void setPickJewelGoal() {
        List<Idea> subGoalList = goals.get("Goal1.Sub-Goal").getL();
        if (!subGoalList.isEmpty()){
            if ("Goal3".equals(subGoalList.get(0).getName())){
                return;
            }
        }
        Idea subGoal = new Idea("Goal3", "Pick up Jewel", "Episode", 0);
        subGoal.add(new Idea("Sub-Goal", null, "Property", 0));
        Idea parentGoal = new Idea("Parent-Goal", null, "Property", 0);
        parentGoal.add(goals.get("Goal1"));
        subGoal.add(parentGoal);
        goals.get("Goal1.Sub-Goal").setL(new ArrayList<Idea>());
        goals.get("Goal1.Sub-Goal").add(subGoal);
    }

    private void setFindJewelGoal() {
        List<Idea> subGoalList = goals.get("Goal1.Sub-Goal").getL();
        if (!subGoalList.isEmpty()){
            if ("Goal2".equals(subGoalList.get(0).getName())){
                return;
            }
        }
        Idea subGoal = new Idea("Goal2", "Search Jewels", "Episode", 0);
        subGoal.add(new Idea("Sub-Goal", null, "Property", 0));
        Idea parentGoal = new Idea("Parent-Goal", null, "Property", 0);
        parentGoal.add(goals.get("Goal1"));
        subGoal.add(parentGoal);
        goals.get("Goal1.Sub-Goal").setL(new ArrayList<Idea>());
        goals.get("Goal1.Sub-Goal").add(subGoal);
    }

    private void initializeSuperGoal() {
        Idea superGoal = new Idea("Goal1", "Collect all Jewels", "Episode", 0);
        superGoal.add(new Idea("Sub-Goal", null, "Property", 0));
        superGoal.add(new Idea("Parent-Goal", null, "Property", 0));
        goals.add(superGoal);
    }

}
