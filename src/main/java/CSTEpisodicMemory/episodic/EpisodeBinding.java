package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EpisodeBinding extends Codelet {

    private Memory eventsMO;
    private Memory contextBufferMO;
    private Memory objectBufferMO;
    private Memory storyMO;
    private Memory contextSegmentMO;
    private long latestSegmentationTime = 0L;

    private static final int intervalThreshold = 200;

    @Override
    public void accessMemoryObjects() {
        this.eventsMO = (MemoryObject) getInput("EVENTS");
        this.contextBufferMO = (MemoryObject) getInput("CONTEXT_BUFFER");
        this.objectBufferMO = (MemoryObject) getInput("PERCEPTUAL_BUFFER");
        this.storyMO = (MemoryObject) getOutput("STORY");
        this.contextSegmentMO = (MemoryObject) getInput("BOUNDARIES");
        this.latestSegmentationTime = getLatestSegmentationTime();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        Idea eventsIdea = (Idea) eventsMO.getI();
        Idea timeline = (Idea) contextBufferMO.getI();
        Idea objectsTimeline = (Idea) objectBufferMO.getI();
        Idea stories = (Idea) storyMO.getI();

        synchronized (stories) {
            synchronized (timeline) {
                synchronized (eventsIdea) {

                    Idea currentEpisode = stories.getL().get(0);
                    for (Idea ep : stories.getL()) {
                        if ((int) ep.getValue() > (int) currentEpisode.getValue())
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
                    for (Idea event : eventsIdea.getL()) {
                        if (!story.hasNodeContent(event)) {
                            long eventEnd = (long) event.getL().get(1).get("TimeStamp").getValue();
                            Optional<Idea> context = timeline.getL().stream()
                                    .filter(e -> ((long) e.getValue()) <= eventEnd)
                                    .max((a, b) -> (int) ((long) a.getValue() - (long) b.getValue()));

                            Optional<Idea> objects = objectsTimeline.getL().stream()
                                    .filter(e -> ((long) e.getValue()) <= eventEnd)
                                    .max((a, b) -> (int) ((long) a.getValue() - (long) b.getValue()));

                            if (context.isPresent() && isSegmentationEvent(story)) {
                                segmentedEvents.add(event);
                            } else {
                                List<Idea> otherNodes = new ArrayList<>(story.getEventNodes());
                                story.insertEventNode(event);
                                createTemporalRelations(event, otherNodes, story);

                                if (context.isPresent()) {
                                    Idea contextIdea = context.get();

                                    //Agent position
                                    Idea position = contextIdea.get("Self").get("Position").clone();
                                    if (!story.hasNodeContent(position)) {
                                        story.insertLocationNode(position);
                                    }
                                    story.insertLink(event, position, "Position");

                                    //Grid place
                                    Idea grid = contextIdea.get("Self").get("Grid_Place");
                                    if (grid != null) {
                                        if (!story.hasNodeContent(grid)) {
                                            story.insertLocationNode(grid);
                                        }
                                        story.insertLink(event, grid, "GridPlace");
                                    }

                                    //Agent environment
                                    Idea room = contextIdea.get("Room");
                                    if (room != null){
                                        Idea roomCat = (Idea) room.get("Location").getValue();
                                        story.insertContextNode(roomCat);
                                        story.insertLink(event, roomCat, "Environment");
                                    }

                                    //Agent Impulse
                                    Idea impulse = contextIdea.get("Impulse");
                                    if (impulse != null) {
                                        impulse = impulse.clone();
                                        if (!story.hasNodeContent(impulse)) {
                                            story.insertContextNode(impulse);
                                        }
                                        story.insertLink(event, impulse, "MotivationalContext");
                                    }

                                    //Context objects
                                    if (objects.isPresent()){
                                        Idea eventObject = event.getL().get(0).getL().stream()
                                                .filter(o->!o.getName().equals("TimeStamp"))
                                                .findFirst()
                                                .orElse(null);

                                        Idea objectsIdea = objects.get();
                                        for (Idea object : objectsIdea.getL()){
                                            Idea pos = object.get("Position");
                                            if (pos != null){
                                                if (!object.getName().equals(eventObject.getName())) {
                                                    story.insertContextNode(object);
                                                    story.insertLocationNode(pos);
                                                    story.insertLink(event, object, "ObjectContext");
                                                    story.insertLink(object, pos, "Position");
                                                }
                                            } else {
                                                for (Idea subObject : object.getL()){
                                                    Idea subPos = subObject.get("Position");
                                                    if (subPos != null) {
                                                        if (!subObject.getName().equals(eventObject.getName())) {
                                                            story.insertContextNode(subObject);
                                                            story.insertLocationNode(subPos);
                                                            story.insertLink(event, subObject, "ObjectContext");
                                                            story.insertLink(subObject, subPos, "Position");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //Segment Episode
                    if (!segmentedEvents.isEmpty()) {
                        //Set contextual drift flag
                        //Create new episode
                        Idea newStory = new Idea("Story", null, "Composition", 1);
                        Idea newEpisode = new Idea("Episode", (int) currentEpisode.getValue() + 1, "Episode", 1);
                        newEpisode.add(newStory);
                        stories.add(newEpisode);

                        //Clear events buffer
                        eventsIdea.setL(segmentedEvents);
                    }
                }
            }
        }
    }

    private boolean isSegmentationEvent(GraphIdea story) {
        Idea firstEvent = story.getEventNodes().stream().min(Comparator.comparingLong(e -> (long) GraphIdea.getNodeContent(e).getL().get(0).get("TimeStamp").getValue())).orElse(null);
        if (firstEvent != null)
            return (long) GraphIdea.getNodeContent(firstEvent).getL().get(0).get("TimeStamp").getValue() < latestSegmentationTime;
        return false;
    }

    private void createTemporalRelations(Idea event, List<Idea> otherNodes, GraphIdea story) {
        Long start1 = 0L, end1 = 0L, start2 = 0L, end2 = 0L;
        for (Idea step : event.getL()) {
            if ((int) step.getValue() == 1)
                start1 = (Long) step.get("TimeStamp").getValue();
            else
                end1 = (Long) step.get("TimeStamp").getValue();
        }

        long closestBeforeSink = Long.MAX_VALUE;
        Idea closestBeforeSinkIdea = null;
        long closestBeforeSource = Long.MAX_VALUE;
        Idea closestBeforeSourceIdea = null;
        for (Idea node : otherNodes) {
            Idea nodeContent = GraphIdea.getNodeContent(node);

            for (Idea step : nodeContent.getL()) {
                if ((int) step.getValue() == 1)
                    start2 = (Long) step.get("TimeStamp").getValue();
                else
                    end2 = (Long) step.get("TimeStamp").getValue();
            }

            String relation = temporalRelation(start1, end1, start2, end2);
            if (!relation.isEmpty()) {
                if (relation.equals("Before")) {
                    if (start2 - end1 < closestBeforeSink) {
                        closestBeforeSink = start2 - end1;
                        closestBeforeSinkIdea = nodeContent.clone();
                    }
                } else {
                    story.insertLink(event, nodeContent, relation);
                }
            }
            relation = temporalRelation(start2, end2, start1, end1);
            if (!relation.isEmpty()) {
                if (relation.equals("Before")) {
                    if (start1 - end2 < closestBeforeSource) {
                        closestBeforeSource = start1 - end2;
                        closestBeforeSourceIdea = nodeContent.clone();
                    }
                } else {
                    story.insertLink(nodeContent, event, relation);
                }
            }
        }

        if (closestBeforeSinkIdea != null)
            story.insertLink(event, closestBeforeSinkIdea, "Before");
        if (closestBeforeSourceIdea != null)
            story.insertLink(closestBeforeSourceIdea, event, "Before");

    }

    private long getLatestSegmentationTime() {
        synchronized (contextSegmentMO) {
            Idea boundaries = (Idea) contextSegmentMO.getI();
            return boundaries.getL()
                    .stream()
                    .mapToLong(b -> b.getValue().equals("Hard") ? (long) b.get("TimeStamp").getValue() : 0L)
                    .max().orElse(0L);
        }
    }

    public static String temporalRelation(long start1, long end1, long start2, long end2) {
        long a = start1 - start2;
        long b = start1 - end2;
        long c = end1 - start2;
        long d = end1 - end2;
        //meets
        if (-a >= intervalThreshold &&
                -b >= intervalThreshold &&
                Math.abs(c) <= intervalThreshold &&
                -d >= intervalThreshold)
            return "Meet";
        //overlaps
        if (-a >= intervalThreshold &&
                -b >= intervalThreshold &&
                c >= intervalThreshold &&
                -d >= intervalThreshold)
            return "Overlap";
        //starts
        if (Math.abs(a) <= intervalThreshold &&
                -b >= intervalThreshold &&
                c >= intervalThreshold &&
                -d >= intervalThreshold)
            return "Start";
        //during
        if (a >= intervalThreshold &&
                -b >= intervalThreshold &&
                c >= intervalThreshold &&
                -d >= intervalThreshold)
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
        if (-c >= intervalThreshold && -c <= 50 * intervalThreshold)
            return "Before";
        return "";
    }
}
