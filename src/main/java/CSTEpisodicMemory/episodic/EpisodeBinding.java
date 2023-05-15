package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import br.unicamp.cst.representation.idea.IdeaComparator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpisodeBinding extends Codelet {

    private Memory eventsMO;
    private Memory bufferMO;
    private Memory storyMO;

    private static final int intervalThreshold = 200;

    @Override
    public void accessMemoryObjects() {
        this.eventsMO = (MemoryObject) getInput("EVENTS");
        this.bufferMO = (MemoryObject) getInput("BUFFER");
        this.storyMO = (MemoryObject) getOutput("STORY");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea eventsIdea = (Idea) eventsMO.getI();
        Idea timeline = (Idea) bufferMO.getI();
        Idea stories = (Idea) storyMO.getI();

        Idea currentEpisode = stories.getL().get(0);
        for (Idea ep : stories.getL()){
            if((int)ep.getValue() > (int)currentEpisode.getValue())
                currentEpisode = ep;
        }
        GraphIdea story = new GraphIdea(currentEpisode.get("Story"));

        //try {
        //    out = new PrintWriter("filename");
        //    String csv = IdeaHelper.csvPrint(stories, 6);
        //    out.println(csv);
        //} catch (FileNotFoundException e) {
        //    throw new RuntimeException(e);
        //}

        //System.out.println(IdeaHelper.csvPrint(stories, 6).replace("\n", " "));

        List<Idea> segmentedEvents = new ArrayList<>();
        for (Idea event : eventsIdea.getL()){
            if (! story.hasNodeContent(event)){
                long eventEnd = (long) event.getL().get(1).get("TimeStamp").getValue();
                Optional<Idea> context = timeline.getL().stream()
                        .filter(e -> ((long) e.getValue()) <= eventEnd)
                        .max((a, b) -> (int) ((long) a.getValue() - (long) b.getValue()));

                if (context.isPresent() && isSegmentationEvent(story, event, context.get())){
                    segmentedEvents.add(event);
                } else {
                    List<Idea> otherNodes = new ArrayList<Idea>(story.getEventNodes());
                    story.insertEventNode(event);
                    createTemporalRelations(event, otherNodes, story);

                    if (context.isPresent()) {
                        Idea contextIdea = context.get();
                        Idea position = contextIdea.get("Self").get("Position").clone();
                        if (!story.hasNodeContent(position)) {
                            story.insertLocationNode(position);
                        }
                        story.insetLink(event, position, "SpatialContext");

                        Idea impulse = contextIdea.get("Impulse").clone();
                        if (impulse != null) {
                            if (!story.hasNodeContent(impulse)) {
                                story.insertContextNode(impulse);
                            }
                            story.insetLink(event, impulse, "InternalContext");
                        }
                    }
                }
            }
        }

        //Segment Episode
        if (segmentedEvents.size() > 0){
            //Create new episode
            Idea newStory = new Idea("Story", null, "Composition", 1);
            Idea newEpisode = new Idea("Episode", (int)currentEpisode.getValue() +1, "Episode", 1);
            newEpisode.add(newStory);
            stories.add(newEpisode);

            //Clear events buffer
            eventsIdea.setL(segmentedEvents);
        }
    }

    private boolean isSegmentationEvent(GraphIdea story, Idea event, Idea context) {
        Idea impulse = context.get("Impulse").clone();
        if (impulse != null) {
            if (!story.hasNodeContent(impulse)) {
                return !(story.getContextNodes().size() < 1);
            }
        }
        return false;
    }

    private void createTemporalRelations(Idea event, List<Idea> otherNodes, GraphIdea story){
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
            Idea nodeContent = GraphIdea.getNodeContent(node);

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
                        closestBeforeSinkIdea = nodeContent.clone();
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
                        closestBeforeSourceIdea = nodeContent.clone();
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
