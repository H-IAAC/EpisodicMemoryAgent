package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.categories.ObjectCategory;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.habits.AssimilatePropertyCategory;
import CSTEpisodicMemory.habits.LocationCategoryGenerator;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.github.sh0nk.matplotlib4j.NumpyUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class EpisodeRetrievalTest {

    Mind m;
    MemoryObject cueMO;
    MemoryObject eplMO;
    MemoryObject propertiesMO;
    MemoryObject locationsMO;
    MemoryObject recallMO;
    EpisodeRetrieval episodeRetrievalCodelet;

    List<Idea> locCat = new ArrayList<>();
    List<Idea> propCat = new ArrayList<>();
    List<Idea> objects = new ArrayList<>();

    List<Idea> eventCategories = Stream.iterate(1, n -> n+1)
            .limit(50)
            .map(n->new Idea("EventCategory"+n))
            .collect(Collectors.toList());

    public EpisodeRetrievalTest(){
        for (Idea eventCat : eventCategories){
            eventCat.add(new Idea("ObservedObject", "object"));
        }
    }

    private void createMind(){
        m = new Mind();
        cueMO = m.createMemoryObject("CUE");
        eplMO = m.createMemoryObject("EPLTM");
        propertiesMO = m.createMemoryObject("PROPERTIES");
        locationsMO = m.createMemoryObject("LOCATION");
        recallMO = m.createMemoryObject("RECALL");
        episodeRetrievalCodelet = new EpisodeRetrieval();
        episodeRetrievalCodelet.addInput(cueMO);
        episodeRetrievalCodelet.addInput(eplMO);
        episodeRetrievalCodelet.addOutput(recallMO);
        episodeRetrievalCodelet.setIsMemoryObserver(true);
        cueMO.addMemoryObserver(episodeRetrievalCodelet);
        m.start();
    }

    private Map<Integer, List<Idea>> setRandomMemories(){
        Random rnd = new Random();

        Idea epltmIdea = new Idea("EPLTM", null, "Episode", 1);
        GraphIdea epltm = new GraphIdea(epltmIdea);

        LocationCategoryGenerator locGen = new LocationCategoryGenerator();
        locCat = new ArrayList<>();
        for (int i = 0; i<35; i++){
            Idea newCat = locGen.exec(null);
            locCat.add(epltm.insertLocationNode(newCat));
        }

        Idea assimilateSubHabit = new Idea("assimilate", null);
        assimilateSubHabit.setValue(new AssimilatePropertyCategory(assimilateSubHabit));
        assimilateSubHabit.add(new Idea("properties", null, "Property", 1));
        assimilateSubHabit.add(new Idea("samples", null, "Property", 1));

        assimilateSubHabit.get("properties").setValue(Arrays.asList("p1","p2"));
        Idea toLearn = new Idea("object", "test", "AbstractObject", 1);
        toLearn.add(new Idea("p1", 0, "QualityDimension", 1));
        toLearn.add(new Idea("p2", 1, "QualityDimension", 1));

        propCat = new ArrayList<>();
        for (int i = 0; i<50;i++) {
            toLearn.get("p1").setValue(i);
            toLearn.get("p2").setValue(i);
            Idea newCat = assimilateSubHabit.exec(toLearn);
            propCat.add(epltm.insertPropertyNode(newCat));
        }

        List<Idea> rndEvents = new ArrayList<>();

        Map<Integer, List<Idea>> toTest = new HashMap<>();
        for (int i = 1; i<300; i++){
            Idea ep = epltm.insertEpisodeNode(new Idea("Episode" + i, null, "Episode", 1));
            List<Idea> thisEpisodeEvents = new ArrayList<>();
            int nNodes = rnd.nextInt(5);
            for (int j = 0; j <= nNodes; j++){
                Idea rndEvent = eventCategories.get(rnd.nextInt(35));
                if (nNodes == 4 && j == 0) toTest.put(i, Arrays.asList(rndEvent, null));
                if (nNodes == 4 && j == 4) toTest.get(i).set(1, rndEvent);
                Idea event = new Idea("Event" + (i+j), rndEvent, "Episode", 1);
                event.add(new Idea("Start", rnd.nextLong(1000)+5000*i+ 1000L *j, "TimeStep", 1));
                event.add(new Idea("End", rnd.nextLong(1000)+5000*i+ 1000L *(j+1)+1000, "TimeStep", 1));
                Idea eventNode = epltm.insertEventNode(event);
                rndEvents.add(eventNode);
                createTemporalRelations(eventNode, thisEpisodeEvents, epltm);
                thisEpisodeEvents.add(eventNode);
            }
            epltm.insertLink(ep, thisEpisodeEvents.get(0), "Begin");
            epltm.insertLink(ep, thisEpisodeEvents.get(thisEpisodeEvents.size()-1), "End");
        }

        for (Idea event : rndEvents){
            epltm.insertLink(event, propCat.get(rnd.nextInt(50)), "Initial");
            epltm.insertLink(event, propCat.get(rnd.nextInt(50)), "Final");
            epltm.insertLink(event, locCat.get(rnd.nextInt(35)), "Location");
        }

        locationsMO.setI(locCat.stream().map(GraphIdea::getNodeContent).collect(Collectors.toList()));

        propertiesMO.setI(propCat.stream().map(GraphIdea::getNodeContent).collect(Collectors.toList()));

        eplMO.setI(epltm);

        return toTest;
    }

    private void createTemporalRelations(Idea event, List<Idea> otherNodes, GraphIdea story){
        Long start1, end1, start2, end2;
        start1 = (Long) GraphIdea.getNodeContent(event).get("Start").getValue();
        end1 = (Long) GraphIdea.getNodeContent(event).get("End").getValue();

        long closestBeforeSink = Long.MAX_VALUE;
        Idea closestBeforeSinkIdea = null;
        long closestBeforeSource = Long.MAX_VALUE;
        Idea closestBeforeSourceIdea = null;
        for (Idea nodeContent : otherNodes){

            start2 = (Long) GraphIdea.getNodeContent(nodeContent).get("Start").getValue();
            end2 = (Long) GraphIdea.getNodeContent(nodeContent).get("End").getValue();

            String relation = EpisodeBinding.temporalRelation(start1, end1, start2, end2);
            if (!relation.isEmpty()){
                if (relation.equals("Before")){
                    if (start2 - end1 < closestBeforeSink) {
                        closestBeforeSink = start2 - end1;
                        closestBeforeSinkIdea = nodeContent;
                    }
                } else {
                    story.insertLink(event, nodeContent, relation);
                }
            }
            relation = EpisodeBinding.temporalRelation(start2, end2, start1, end1);
            if (!relation.isEmpty()){
                if (relation.equals("Before")){
                    if (start1 - end2 < closestBeforeSource) {
                        closestBeforeSource = start1 - end2;
                        closestBeforeSourceIdea = nodeContent;
                    }
                } else {
                    story.insertLink(nodeContent, event, relation);
                }
            }
        }

        if (closestBeforeSinkIdea != null) {
            story.insertLink(event, closestBeforeSinkIdea, "Before");
        }
        if (closestBeforeSourceIdea != null) {
            story.insertLink(closestBeforeSourceIdea, event, "Before");
        }


    }

    private void setMemories(){
        Random rnd = new Random();
        Idea epltmIdea = new Idea("EPLTM", null, "Episode", 1);
        GraphIdea epltm = new GraphIdea(epltmIdea);

        //LocationCategoryGenerator locGen = new LocationCategoryGenerator();
        locCat = new ArrayList<>();
        for (int i = 0; i<40; i++){
            Idea newCat = GridLocation.getInstance().locateHCCIdea(new Random().nextFloat() * 10, new Random().nextFloat() * 10);
            locCat.add(newCat);
            epltm.insertLocationNode(newCat);
        }

        //Idea assimilateSubHabit = new Idea("assimilate", null);
        //assimilateSubHabit.setValue(new AssimilatePropertyCategory(assimilateSubHabit));
        //assimilateSubHabit.add(new Idea("properties", null, "Property", 1));
        //assimilateSubHabit.add(new Idea("samples", null, "Property", 1));

        //assimilateSubHabit.get("properties").setValue(Arrays.asList("p1","p2"));
        //Idea toLearn = new Idea("object", "test", "AbstractObject", 1);
        //toLearn.add(new Idea("p1", 0, "QualityDimension", 1));
        //toLearn.add(new Idea("p2", 1, "QualityDimension", 1));

        //propCat = new ArrayList<>();
        //for (int i = 0; i<16;i++) {
        //    toLearn.get("p1").setValue(i);
        //    toLearn.get("p2").setValue(i);
        //    Idea newCat = assimilateSubHabit.exec(toLearn);
        //    propCat.add(newCat);
        //    epltm.insertPropertyNode(newCat);
        //}

        objects = new ArrayList<>();
        for (int i=0; i<30; i++){
            Idea spatialLink = new Idea("SpatialLink" + i, null, "Link", 1);
            Idea spatialNode = epltm.insertContextNode(spatialLink);
            Idea obj = new Idea("Object", "ObjCat" + i % 3, "AbstractObject", 1);
            obj.add(new Idea("p1",i % 10 , "Property", 1));
            obj.add(new Idea("p2", i % 10 , "Property", 1));
            obj.add(new Idea("ID", i % 6, "Property", 1));
            Idea objNode = assimilateObject(obj, epltm);
            Idea gridPlace = GridLocation.getInstance().locateHCCIdea(new Random().nextFloat() * 10, new Random().nextFloat() * 10);
            //Idea gridNode = epltm.insertLocationNode(gridPlace);
            Idea gridNode = locCat.get(i+5);
            epltm.insertLink(spatialNode, objNode, "Object");
            epltm.insertLink(spatialNode, gridNode, "GridPlace");
            objects.add(spatialNode);
        }

        System.out.println("Object Categories: " + epltm.getObjectNodes().size());

        //Episodic Memory example
        Idea ep1 = epltm.insertEpisodeNode(new Idea("Episode1", null, "Episode", 1));
        Idea ep2 = epltm.insertEpisodeNode(new Idea("Episode2", null, "Episode", 1));
        Idea ep3 = epltm.insertEpisodeNode(new Idea("Episode3", null, "Episode", 1));
        Idea ep4 = epltm.insertEpisodeNode(new Idea("Episode4", null, "Episode", 1));

        Idea event1 = new Idea("Event1", eventCategories.get(0), "Episode", 1);
        event1.add(new Idea("Start", 0L, "TimeStep", 1));
        event1.add(new Idea("End", 1000L, "TimeStep", 1));
        epltm.insertEventNode(event1);
        Idea event2 = new Idea("Event2", eventCategories.get(1), "Episode", 1);
        event2.add(new Idea("Start", 1000L, "TimeStep", 1));
        event2.add(new Idea("End", 2000L, "TimeStep", 1));
        epltm.insertEventNode(event2);
        Idea event3 = new Idea("Event3", eventCategories.get(2), "Episode", 1);
        event3.add(new Idea("Start", 2000L, "TimeStep", 1));
        event3.add(new Idea("End", 3000L, "TimeStep", 1));
        epltm.insertEventNode(event3);
        Idea event4 = new Idea("Event4", eventCategories.get(0), "Episode", 1);
        event4.add(new Idea("Start", 4000L, "TimeStep", 1));
        event4.add(new Idea("End", 5000L, "TimeStep", 1));
        epltm.insertEventNode(event4);
        Idea event5 = new Idea("Event5", eventCategories.get(1), "Episode", 1);
        event5.add(new Idea("Start", 6000L, "TimeStep", 1));
        event5.add(new Idea("End", 7000L, "TimeStep", 1));
        epltm.insertEventNode(event5);
        Idea event6 = new Idea("Event6", eventCategories.get(0), "Episode", 1);
        event6.add(new Idea("Start", 8000L, "TimeStep", 1));
        event6.add(new Idea("End", 9000L, "TimeStep", 1));
        epltm.insertEventNode(event6);
        Idea event7 = new Idea("Event7", eventCategories.get(1), "Episode", 1);
        event7.add(new Idea("Start", 10000L, "TimeStep", 1));
        event7.add(new Idea("End", 11000L, "TimeStep", 1));
        epltm.insertEventNode(event7);
        Idea event8 = new Idea("Event8", eventCategories.get(2), "Episode", 1);
        event8.add(new Idea("Start", 11000L, "TimeStep", 1));
        event8.add(new Idea("End", 12000L, "TimeStep", 1));
        epltm.insertEventNode(event8);
        Idea event9 = new Idea("Event9", eventCategories.get(0), "Episode", 1);
        event9.add(new Idea("Start", 12000L, "TimeStep", 1));
        event9.add(new Idea("End", 13000L, "TimeStep", 1));
        epltm.insertEventNode(event9);

        epltm.insertLink(ep1, event1, "Begin");
        epltm.insertLink(ep1, event3, "End");
        epltm.insertLink(ep2, event4, "Begin");
        epltm.insertLink(ep2, event4, "End");
        epltm.insertLink(ep3, event5, "Begin");
        epltm.insertLink(ep3, event6, "End");
        epltm.insertLink(ep4, event7, "Begin");
        epltm.insertLink(ep4, event9, "End");

        epltm.insertLink(event1, event2, "Meet");
        epltm.insertLink(event1, event3, "Before");
        epltm.insertLink(event2, event3, "Meet");
        epltm.insertLink(event3, event4, "Next");
        epltm.insertLink(event4, event5, "Next");
        epltm.insertLink(event5, event6, "Before");
        epltm.insertLink(event6, event7, "Next");
        epltm.insertLink(event7, event8, "Meet");
        epltm.insertLink(event7, event9, "Before");
        epltm.insertLink(event8, event9, "Meet");

        epltm.insertLink(event1, objects.get(0),"Initial");
        epltm.insertLink(event1, objects.get(1),"Final");
        epltm.insertLink(event2, objects.get(2),"Initial");
        epltm.insertLink(event2, objects.get(3),"Final");
        epltm.insertLink(event3, objects.get(4),"Initial");
        epltm.insertLink(event3, objects.get(5),"Final");
        epltm.insertLink(event4, objects.get(6),"Initial");
        epltm.insertLink(event4, objects.get(7),"Final");
        epltm.insertLink(event5, objects.get(2),"Initial");
        epltm.insertLink(event5, objects.get(8),"Final");
        epltm.insertLink(event6, objects.get(9),"Initial");
        epltm.insertLink(event6, objects.get(10),"Final");
        epltm.insertLink(event7, objects.get(11),"Initial");
        epltm.insertLink(event7, objects.get(12),"Final");
        epltm.insertLink(event8, objects.get(12),"Initial");
        epltm.insertLink(event8, objects.get(13),"Final");
        epltm.insertLink(event9, objects.get(14),"Initial");
        epltm.insertLink(event9, objects.get(15),"Final");

        epltm.insertLink(event1, objects.get(16), "ObjectContext");
        epltm.insertLink(event2, objects.get(17), "ObjectContext");
        epltm.insertLink(event3, objects.get(18), "ObjectContext");
        epltm.insertLink(event4, objects.get(19), "ObjectContext");
        epltm.insertLink(event5, objects.get(20), "ObjectContext");
        epltm.insertLink(event6, objects.get(21), "ObjectContext");
        epltm.insertLink(event7, objects.get(22), "ObjectContext");
        epltm.insertLink(event8, objects.get(23), "ObjectContext");
        epltm.insertLink(event9, objects.get(24), "ObjectContext");

        epltm.insertLink(event1, locCat.get(0), "Location");
        epltm.insertLink(event2, locCat.get(0), "Location");
        epltm.insertLink(event3, locCat.get(1), "Location");
        epltm.insertLink(event4, locCat.get(2), "Location");
        epltm.insertLink(event5, locCat.get(2), "Location");
        epltm.insertLink(event6, locCat.get(3), "Location");
        epltm.insertLink(event7, locCat.get(3), "Location");
        epltm.insertLink(event8, locCat.get(0), "Location");
        epltm.insertLink(event9, locCat.get(1), "Location");

        locationsMO.setI(locCat);

        propertiesMO.setI(propCat);

        eplMO.setI(epltm);
    }

    private Idea assimilateObject(Idea objectContent, GraphIdea epLTMGraph) {
        double bestMem = 0;
        Idea bestObjCat = null;
        for (Idea objNode : epLTMGraph.getObjectNodes()){
            Idea objCat = getNodeContent(objNode);
            double mem = objCat.membership(objectContent);
            if (mem > bestMem) {
                bestMem = mem;
                bestObjCat = objCat;
            }
        }
        if (bestObjCat != null){
            if (bestMem >= 0.9){
                if (bestMem >=0.95) {
                    ObjectCategory cat = (ObjectCategory) bestObjCat.getValue();
                    cat.insertExamplar(objectContent);
                    bestObjCat.setValue(cat);
                }
                return epLTMGraph.getNodeFromContent(bestObjCat);
            }
        }
        ObjectCategory newObjCatFunc = new ObjectCategory(objectContent);
        Idea newObjCat = new Idea(objectContent.getName(), newObjCatFunc, "AbstractObject", 2);
        return epLTMGraph.insertObjectNode(newObjCat);
    }

    @Test
    public void eventRetrieveTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cue = new Idea("EventTest", eventCategories.get(1), "Episode", 1);
        Idea step1 = new Idea("", 1, "TimeStep", 1);
        Idea step2 = new Idea("", 2, "TimeStep", 1);
        Idea obj = new Idea("Object", "ObjCat2", "AbstractObject", 1);
        obj.add(new Idea("p1", 2, "QualityDimension", 1));
        obj.add(new Idea("p2", 2, "QualityDimension", 1));
        Idea obj2 = obj.clone();
        obj2.get("p1").setValue(8);
        obj2.get("p2").setValue(8);
        step1.add(obj);
        step2.add(obj2);
        cue.add(step1);
        cue.add(step2);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEventNode(cue);
        cueMO.setI(cueGraph);
        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        //Check number of items
        Assertions.assertEquals(13, storyRecall.getNodes().size());

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(1, ep.size());
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode3");

        //Check events
        List<Idea> begin = storyRecall.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(1, begin.size());
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals("Event5", beginEvent.getName());
        List<Idea> end = storyRecall.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(end.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event6");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialObject = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalObject = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialObject = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalObject = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(getNodeContent(objects.get(2)), getNodeContent(beginEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(8)), getNodeContent(beginEventFinalObject));
        Assertions.assertEquals(getNodeContent(objects.get(9)), getNodeContent(endEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(10)), getNodeContent(endEventFinalObject));

    }

    @Test
    public void unsuccessfulRetrievalTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cue = new Idea("EventTest", eventCategories.get(3), "Episode", 1);
        Idea step1 = new Idea("", 1, "TimeStep", 1);
        Idea step2 = new Idea("", 2, "TimeStep", 1);
        Idea obj = new Idea("object", "ObjCat2", "AbstractObject", 1);
        obj.add(new Idea("p1", -3, "QualityDimension", 1));
        obj.add(new Idea("p2", -3, "QualityDimension", 1));
        Idea obj2 = obj.clone();
        obj2.get("p1").setValue(-4);
        obj2.get("p2").setValue(-4);
        step1.add(obj);
        step2.add(obj2);
        cue.add(step1);
        cue.add(step2);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEventNode(cue);
        cueMO.setI(cueGraph);
        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        Assertions.assertEquals(storyRecall.getNodes().size(), 0);

    }

    @Test
    public void episodeRetrievalTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cueEp = new Idea("EpisodeTest", null, "Episode", 1);
        Idea firstEvent = new Idea("EventTest1", eventCategories.get(0), 1);
        Idea finalEvent = new Idea("EventTest2", eventCategories.get(2), 1);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEpisodeNode(cueEp);
        cueGraph.insertEventNode(firstEvent);
        cueGraph.insertEventNode(finalEvent);
        cueGraph.insertLink(cueEp, firstEvent, "Begin");
        cueGraph.insertLink(cueEp, finalEvent, "End");
        cueMO.setI(cueGraph);

        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(ep.size(), 1);
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode1");

        //Check events
        List<Idea> begin = storyRecall.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(begin.size(), 1);
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(beginEvent.getName(), "Event1");
        List<Idea> end = storyRecall.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(end.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event3");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialObject = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalObject = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialObject = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalObject = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(getNodeContent(objects.get(0)), getNodeContent(beginEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(1)), getNodeContent(beginEventFinalObject));
        Assertions.assertEquals(getNodeContent(objects.get(4)), getNodeContent(endEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(5)), getNodeContent(endEventFinalObject));
    }

    @Test
    public void eventSequenceRetrievalTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cueEvent1 = new Idea("TestEvent1", eventCategories.get(1), 1);
        Idea cueEvent2 = new Idea("TestEvent2", eventCategories.get(2), 1);
        Idea cueEvent3 = new Idea("TestEvent3", eventCategories.get(0), 1);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEventNode(cueEvent1);
        cueGraph.insertEventNode(cueEvent2);
        cueGraph.insertEventNode(cueEvent3);
        cueGraph.insertLink(cueEvent1, cueEvent2, "Meet");
        cueGraph.insertLink(cueEvent2, cueEvent3, "Meet");
        cueMO.setI(cueGraph);

        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(ep.size(), 1);
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode4");

        //Check events
        List<Idea> begin = storyRecall.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(begin.size(), 1);
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(beginEvent.getName(), "Event7");
        List<Idea> end = storyRecall.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(end.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event9");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialObject = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalObject = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialObject = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalObject = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(getNodeContent(objects.get(11)), getNodeContent(beginEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(12)), getNodeContent(beginEventFinalObject));
        Assertions.assertEquals(getNodeContent(objects.get(14)), getNodeContent(endEventInitialObject));
        Assertions.assertEquals(getNodeContent(objects.get(15)), getNodeContent(endEventFinalObject));
    }

    @Test
    public void multipleEpisodesTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cueEvent1 = new Idea("TestEvent1", eventCategories.get(2), 1);
        Idea cueEvent2 = new Idea("TestEvent2", eventCategories.get(0), 1);
        Idea cueEvent3 = new Idea("TestEvent3", eventCategories.get(1), 1);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEventNode(cueEvent1);
        cueGraph.insertEventNode(cueEvent2);
        cueGraph.insertEventNode(cueEvent3);
        cueGraph.insertLink(cueEvent1, cueEvent2, "Before");
        cueGraph.insertLink(cueEvent2, cueEvent3, "Before");
        cueMO.setI(cueGraph);

        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(3, ep.size());
        List<String> epNames = ep.stream().map(GraphIdea::getNodeContent).map(Idea::getName).collect(Collectors.toList());
        Assertions.assertTrue(epNames.contains("Episode1"));
        Assertions.assertTrue(epNames.contains("Episode2"));
        Assertions.assertTrue(epNames.contains("Episode3"));

    }

    @Test
    public void largeEPLTMTest(){
        createMind();
        Map<Integer, List<Idea>> testCategories = setRandomMemories();
        double totalTime = 0;

        for (Map.Entry<Integer, List<Idea>> test : testCategories.entrySet()) {
            Idea cueEp = new Idea("EpisodeTest", null, "Episode", 1);
            Idea firstEvent = new Idea("EventTest1", test.getValue().get(0), 1);
            Idea finalEvent = new Idea("EventTest2", test.getValue().get(1), 1);

            GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
            cueGraph.insertEpisodeNode(cueEp);
            cueGraph.insertEventNode(firstEvent);
            cueGraph.insertEventNode(finalEvent);
            cueGraph.insertLink(cueEp, firstEvent, "Begin");
            cueGraph.insertLink(cueEp, finalEvent, "End");

            double start = System.currentTimeMillis();
            cueMO.setI(cueGraph);
            while (recallMO.getI() == null) {
                assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
            }
            totalTime += System.currentTimeMillis() - start;

            GraphIdea storyRecall = (GraphIdea) recallMO.getI();

            List<Idea> recallEpisode = storyRecall.getEpisodeNodes().stream().map(GraphIdea::getNodeContent).collect(Collectors.toList());
            Assertions.assertEquals(1, recallEpisode.size());
            //Assertions.assertEquals("Episode" + test.getKey(), recallEpisode.get(0).getName());

            List<Idea> begin = storyRecall.getChildrenWithLink(recallEpisode.get(0), "Begin");
            Idea beginEvent = getNodeContent(begin.get(0));
            Assertions.assertEquals(test.getValue().get(0) ,beginEvent.getValue() );
            List<Idea> end = storyRecall.getChildrenWithLink(recallEpisode.get(0), "End");
            Idea endEvent = getNodeContent(end.get(0));
            Assertions.assertEquals(test.getValue().get(1) ,endEvent.getValue() );
        }

        System.out.println("Total: " + totalTime + " - Size: "+ testCategories.size());
        System.out.println("Avg proc time: " + totalTime /testCategories.size());
    }

    @Test
    public void testObjectRetrieval(){
        createMind();
        setMemories();

        //create a Cue
        Idea object = new Idea("Object", "ObjCat0", "AbstractObject", 1);
        object.add(new Idea("ID", 0, "Property", 1));

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertObjectNode(object);
        cueMO.setI(cueGraph);

        long start = System.currentTimeMillis();
        while (recallMO.getI() == null){
            assert System.currentTimeMillis() - start <= 5000; //Probably throw an error would be better
        }

        GraphIdea storyRecall = (GraphIdea) recallMO.getI();

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(2, ep.size());
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode1");
        epContent = getNodeContent(ep.get(1));
        Assertions.assertEquals(epContent.getName(), "Episode4");
    }

}
