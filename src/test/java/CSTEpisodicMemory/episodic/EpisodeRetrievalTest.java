package CSTEpisodicMemory.episodic;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.habits.AssimilatePropertyCategory;
import CSTEpisodicMemory.habits.LocationCategoryGenerator;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    List<Idea> eventCategories = Arrays.asList(new Idea("EventCategory1"),
            new Idea("EventCategory2"),
            new Idea("EventCategory3"),
            new Idea("EventCategory4"));

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
        episodeRetrievalCodelet.addInput(propertiesMO);
        episodeRetrievalCodelet.addInput(locationsMO);
        episodeRetrievalCodelet.addOutput(recallMO);
        episodeRetrievalCodelet.setIsMemoryObserver(true);
        cueMO.addMemoryObserver(episodeRetrievalCodelet);
        m.start();
    }

    private void setMemories(){
        Idea epltmIdea = new Idea("EPLTM", null, "Epsisode", 1);
        GraphIdea epltm = new GraphIdea(epltmIdea);

        LocationCategoryGenerator locGen = new LocationCategoryGenerator();
        locCat = new ArrayList<>();
        for (int i = 0; i<4; i++){
            Idea newCat = locGen.exec(null);
            locCat.add(newCat);
            epltm.insertLocationNode(newCat);
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
        for (int i = 0; i<16;i++) {
            toLearn.get("p1").setValue(i);
            toLearn.get("p2").setValue(i);
            Idea newCat = assimilateSubHabit.exec(toLearn);
            propCat.add(newCat);
            epltm.insertPropertyNode(newCat);
        }

        //Episodic Memory example
        Idea ep1 = epltm.insertEpisodeNode(new Idea("Episode1", null, "Episode", 1));
        Idea ep2 = epltm.insertEpisodeNode(new Idea("Episode2", null, "Episode", 1));
        Idea ep3 = epltm.insertEpisodeNode(new Idea("Episode3", null, "Episode", 1));
        Idea ep4 = epltm.insertEpisodeNode(new Idea("Episode4", null, "Episode", 1));

        Idea event1 = epltm.insertEventNode(new Idea("Event1", eventCategories.get(0), "Episode", 1));
        Idea event2 = epltm.insertEventNode(new Idea("Event2", eventCategories.get(1), "Episode", 1));
        Idea event3 = epltm.insertEventNode(new Idea("Event3", eventCategories.get(2), "Episode", 1));
        Idea event4 = epltm.insertEventNode(new Idea("Event4", eventCategories.get(0), "Episode", 1));
        Idea event5 = epltm.insertEventNode(new Idea("Event5", eventCategories.get(1), "Episode", 1));
        Idea event6 = epltm.insertEventNode(new Idea("Event6", eventCategories.get(0), "Episode", 1));
        Idea event7 = epltm.insertEventNode(new Idea("Event7", eventCategories.get(1), "Episode", 1));
        Idea event8 = epltm.insertEventNode(new Idea("Event8", eventCategories.get(2), "Episode", 1));
        Idea event9 = epltm.insertEventNode(new Idea("Event9", eventCategories.get(0), "Episode", 1));

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
        epltm.insertLink(event3, event4, "Before");
        epltm.insertLink(event4, event5, "Before");
        epltm.insertLink(event5, event6, "Before");
        epltm.insertLink(event6, event7, "Before");
        epltm.insertLink(event7, event8, "Meet");
        epltm.insertLink(event7, event9, "Before");
        epltm.insertLink(event8, event9, "Meet");

        epltm.insertLink(event1, propCat.get(0),"Initial");
        epltm.insertLink(event1, propCat.get(1),"Final");
        epltm.insertLink(event2, propCat.get(2),"Initial");
        epltm.insertLink(event2, propCat.get(3),"Final");
        epltm.insertLink(event3, propCat.get(4),"Initial");
        epltm.insertLink(event3, propCat.get(5),"Final");
        epltm.insertLink(event4, propCat.get(6),"Initial");
        epltm.insertLink(event4, propCat.get(7),"Final");
        epltm.insertLink(event5, propCat.get(2),"Initial");
        epltm.insertLink(event5, propCat.get(8),"Final");
        epltm.insertLink(event6, propCat.get(9),"Initial");
        epltm.insertLink(event6, propCat.get(10),"Final");
        epltm.insertLink(event7, propCat.get(11),"Initial");
        epltm.insertLink(event7, propCat.get(12),"Final");
        epltm.insertLink(event8, propCat.get(12),"Initial");
        epltm.insertLink(event8, propCat.get(13),"Final");
        epltm.insertLink(event9, propCat.get(14),"Initial");
        epltm.insertLink(event9, propCat.get(15),"Final");

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

    @Test
    public void eventRetrieveTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cue = new Idea("EventTest", eventCategories.get(1), "Episode", 1);
        Idea step1 = new Idea("", 1, "TimeStep", 1);
        Idea step2 = new Idea("", 2, "TimeStep", 1);
        Idea obj = new Idea("object", null, "AbstractObject", 1);
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

        //Check if is correct episode
        List<Idea> ep = storyRecall.getEpisodeNodes();
        Assertions.assertEquals(ep.size(), 1);
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode3");

        //Check events
        List<Idea> begin = storyRecall.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(begin.size(), 1);
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(beginEvent.getName(), "Event5");
        List<Idea> end = storyRecall.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event6");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialProperty = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalProperty = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialProperty = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalProperty = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(propCat.get(2).membership(beginEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(8).membership(beginEventFinalProperty), 1.0);
        Assertions.assertEquals(propCat.get(9).membership(endEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(10).membership(endEventFinalProperty), 1.0);

    }

    @Test
    public void unsuccessfulRetrievalTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cue = new Idea("EventTest", eventCategories.get(3), "Episode", 1);
        Idea step1 = new Idea("", 1, "TimeStep", 1);
        Idea step2 = new Idea("", 2, "TimeStep", 1);
        Idea obj = new Idea("object", null, "AbstractObject", 1);
        obj.add(new Idea("p1", -3, "QualityDimension", 1));
        obj.add(new Idea("p2", -3, "QualityDimension", 1));
        Idea obj2 = obj.clone();
        obj2.get("p1").setValue(-3);
        obj2.get("p2").setValue(-3);
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
        Idea firstEvent = new Idea("EventTest1", eventCategories.get(1), 1);
        Idea finalEvent = new Idea("EventTest2", eventCategories.get(0), 1);

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
        Assertions.assertEquals(epContent.getName(), "Episode4");

        //Check events
        List<Idea> begin = storyRecall.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(begin.size(), 1);
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(beginEvent.getName(), "Event7");
        List<Idea> end = storyRecall.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event9");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialProperty = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalProperty = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialProperty = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalProperty = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(propCat.get(11).membership(beginEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(12).membership(beginEventFinalProperty), 1.0);
        Assertions.assertEquals(propCat.get(14).membership(endEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(15).membership(endEventFinalProperty), 1.0);
    }

    @Test
    public void eventSequenceRetrivalTest(){
        createMind();
        setMemories();

        //create a Cue
        Idea cueEvent1 = new Idea("TestEvent1", eventCategories.get(1), 1);
        Idea cueEvent2 = new Idea("TestEvent2", eventCategories.get(2), 1);
        Idea cueEvent3 = new Idea("TestEvent3", eventCategories.get(0), 1);

        GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
        cueGraph.insertEpisodeNode(cueEvent1);
        cueGraph.insertEpisodeNode(cueEvent2);
        cueGraph.insertEpisodeNode(cueEvent3);
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
        Idea endEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event9");

        Assertions.assertEquals(storyRecall.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialProperty = storyRecall.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalProperty = storyRecall.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialProperty = storyRecall.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalProperty = storyRecall.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(propCat.get(11).membership(beginEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(12).membership(beginEventFinalProperty), 1.0);
        Assertions.assertEquals(propCat.get(14).membership(endEventInitialProperty), 1.0);
        Assertions.assertEquals(propCat.get(15).membership(endEventFinalProperty), 1.0);
    }
}
