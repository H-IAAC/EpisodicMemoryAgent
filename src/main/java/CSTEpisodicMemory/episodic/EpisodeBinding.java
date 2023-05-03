package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

public class EpisodeBinding extends Codelet {

    private Memory eventsMO;
    private MemoryContainer impulsesMO;
    private Memory roomMO;
    private Memory storyMO;

    private static final int intervalThreshold = 200;

    @Override
    public void accessMemoryObjects() {
        this.eventsMO = (MemoryObject) getInput("EVENTS");
        this.impulsesMO = (MemoryContainer) getInput("IMPULSES");
        this.roomMO = (MemoryObject) getInput("ROOM");
        this.storyMO = (MemoryObject) getOutput("STORY");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea eventsIdea = (Idea) eventsMO.getI();
        Idea impulseIdea = (Idea) impulsesMO.getI();
        GraphIdea story = (GraphIdea) storyMO.getI();

        System.out.println(story);

        for (Idea event : eventsIdea.getL()){
            if (! story.hasNodeContent(event)){
                List<Idea> otherNodes = new ArrayList<Idea>(story.getNodes());
                story.insertNode(event);

                Long start1 = 0L, end1 = 0L, start2 = 0L, end2 = 0L;
                for (Idea step : event.getL()){
                    if ((int) step.getValue() == 1)
                        start1 = (Long) step.get("TimeStamp").getValue();
                    else
                        end1 = (Long) step.get("TimeStamp").getValue();
                }

                for (Idea node : otherNodes){
                    Idea nodeContent = node.getL().stream()
                            .filter(e->!e.getName().equals("Coordinate"))
                            .findFirst()
                            .orElse(null);

                    for (Idea step : nodeContent.getL()) {
                        if ((int) step.getValue() == 1)
                            start2 = (Long) step.get("TimeStamp").getValue();
                        else
                            end2 = (Long) step.get("TimeStamp").getValue();
                    }

                    String relation = temporalRelation(start1, end1, start2, end2);
                    if (!relation.equals("")){
                        story.insetLink(event, nodeContent, relation);
                    }
                    relation = temporalRelation(start2, end2, start1, end1);
                    if (!relation.equals("")){
                        story.insetLink(nodeContent, event, relation);
                    }
                }
            }
        }
    }

    public static String temporalRelation(long start1, long end1, long start2, long end2){
        //before
        if (start2 - end1 >= intervalThreshold && start2 - end1 <= 5* intervalThreshold)
            return "Before";
        //meets
        if (Math.abs(start2 - end1) <= intervalThreshold)
            return "Meet";
        //overlaps
        if (start2 - start1 >= intervalThreshold && end1 - start2 >= intervalThreshold)
            return "Overlap";
        //starts
        if (Math.abs(start1 - start2) <= intervalThreshold && end2 - end1 >= intervalThreshold)
            return "Start";
        //during
        if (start1 - start2 >= intervalThreshold && end2 - end1 >= intervalThreshold)
            return "During";
        //finishes
        if (start1 - start2 >= intervalThreshold && Math.abs(end1 - end2) <= intervalThreshold)
            return "Finish";
        //equals
        if (Math.abs(start1 - start2) <= intervalThreshold && Math.abs(end1 -end2) <= intervalThreshold)
            return "Equal";
        return "";
    }
}
