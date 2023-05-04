package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
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

        //System.out.println(IdeaHelper.csvPrint(story.graph).replace('\n',' '));

        for (Idea event : eventsIdea.getL()){
            if (! story.hasNodeContent(event)){
                List<Idea> otherNodes = new ArrayList<Idea>(story.getNodes());
                story.insertEventNode(event);

                Long start1 = 0L, end1 = 0L, start2 = 0L, end2 = 0L;
                for (Idea step : event.getL()){
                    if ((int) step.getValue() == 1)
                        start1 = (Long) step.get("TimeStamp").getValue();
                    else
                        end1 = (Long) step.get("TimeStamp").getValue();
                }

                Long closestBeforeSink = Long.MAX_VALUE;
                Idea closestBeforeSinkIdea = null;
                Long closestBeforeSource = Long.MAX_VALUE;
                Idea closestBeforeSourceIdea = null;
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
                        if (relation.equals("Before")){
                            if (start2 - end1 < closestBeforeSink) {
                                closestBeforeSink = start2 - end1;
                                closestBeforeSinkIdea = nodeContent;
                            }
                        } else {
                            story.insetLink(event, nodeContent, relation);
                        }
                    }
                    relation = temporalRelation(start2, end2, start1, end1);
                    if (!relation.equals("")){
                        if (relation.equals("Before")){
                            if (start1 - end2 < closestBeforeSource) {
                                closestBeforeSource = start1 - end2;
                                closestBeforeSourceIdea = nodeContent;
                            }
                        } else {
                            story.insetLink(nodeContent, event, relation);
                        }
                    }
                }

                if (closestBeforeSinkIdea != null)
                    story.insetLink(event, closestBeforeSinkIdea, "Before");
                if (closestBeforeSourceIdea != null)
                    story.insetLink(closestBeforeSourceIdea, event, "Before");
            }
        }
    }

    public static String temporalRelation(long start1, long end1, long start2, long end2){
        long a = start1 - start2;
        long b = start1 - end2;
        long c = end1 - start2;
        long d = end1 - end2;
        //meets
        if (-a>= intervalThreshold &&
            -b >= intervalThreshold &&
            Math.abs(c) <= intervalThreshold &&
            -d >= intervalThreshold)
            return "Meet";
        //overlaps
        if (-a>= intervalThreshold &&
            -b >= intervalThreshold &&
            c>= intervalThreshold &&
            -d>= intervalThreshold)
            return "Overlap";
        //starts
        if (Math.abs(a) <= intervalThreshold &&
            -b >= intervalThreshold &&
            c >= intervalThreshold &&
            -d>= intervalThreshold)
            return "Start";
        //during
        if (a >= intervalThreshold &&
            -b >= intervalThreshold &&
            c >= intervalThreshold &&
            -d>= intervalThreshold)
            return "During";
        //finishes
        if (a >= intervalThreshold &&
            -b >= intervalThreshold &&
            c >= intervalThreshold &&
            Math.abs(d) <= intervalThreshold)
            return "Finish";
        //equals
        if (Math.abs(a) <= intervalThreshold && Math.abs(d) <= intervalThreshold)
            return "Equal";
        //before
        if (-c >= intervalThreshold && -c <= 50* intervalThreshold)
            return "Before";
        return "";
    }
}
