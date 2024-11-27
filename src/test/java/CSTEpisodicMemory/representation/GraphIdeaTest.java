package CSTEpisodicMemory.representation;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.habits.AssimilatePropertyCategory;
import CSTEpisodicMemory.habits.LocationCategoryGenerator;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class GraphIdeaTest {

    public GraphIdea graphIdea;
    Idea event1;
    Idea event2;
    Idea event3;
    Idea pos1;
    Idea pos2;
    Idea pos3;

    public GraphIdeaTest(){
        event1 = new Idea("Event1", null, "Episode", 1);
        event2 = new Idea("Event2", null, "Episode", 1);
        event3 = new Idea("Event3", null, "Episode", 1);
        pos1 = new Idea("Pos1", 1, "Property", 1);
        pos2 = new Idea("Pos2", 2, "Property", 1);
        pos3 = new Idea("Pos3", 3, "Property", 1);

        graphIdea = new GraphIdea(new Idea("Graph", null));
        graphIdea.insertEventNode(event1);
        graphIdea.insertEventNode(event2);
        graphIdea.insertEventNode(event3);
        graphIdea.insertEventNode(pos1);
        graphIdea.insertEventNode(pos2);
        graphIdea.insertEventNode(pos3);

        graphIdea.insertLink(event1, event2, "Next");
        graphIdea.insertLink(event2, event3, "Next");
        graphIdea.insertLink(event1, pos1, "Start");
        graphIdea.insertLink(event1, pos2, "End");
        graphIdea.insertLink(event2, pos2, "Start");
        graphIdea.insertLink(event2, pos3, "End");
    }

    @Test
    public void testLinks(){
        Map<String, List<Idea>> links = graphIdea.getSuccesors(event1);
        Assertions.assertEquals(3, links.size());

        Set<String> types = links.keySet();
        Assertions.assertTrue(types.contains("Next"));
        Assertions.assertTrue(types.contains("Start"));
        Assertions.assertTrue(types.contains("End"));

        List<Idea> dest = links.values().stream().flatMap(Collection::stream).map(GraphIdea::getNodeContent).collect(Collectors.toList());
        System.out.println(dest);
        Assertions.assertTrue(dest.contains(event2));
        Assertions.assertTrue(dest.contains(pos1));
        Assertions.assertTrue(dest.contains(pos2));
    }

    @Test
    public void testActivation(){
        List<Idea> nodes = graphIdea.getNodes();
        Assertions.assertTrue(nodes.stream().allMatch(n->(double) n.get("Activation").getValue() == 0d));

        graphIdea.setNodeActivation(pos1, 1d);
        Assertions.assertEquals(1d, (double) graphIdea.getNodeFromContent(pos1).get("Activation").getValue(), 0.0);

        graphIdea.propagateActivations(Arrays.asList("Next", "Start", "End"), Arrays.asList("Next", "Start", "End"));
        double event3Activatio = (double) graphIdea.getNodeFromContent(event3).get("Activation").getValue();
        Assertions.assertEquals(0.9 * 0.9 * 0.9d, event3Activatio, 0.0);

        graphIdea.resetActivations();
        nodes = graphIdea.getNodes();
        Assertions.assertTrue(nodes.stream().allMatch(n->(double) n.get("Activation").getValue() == 0d));
    }

    @Test
    public void testGraphLinkTraversal(){
        List<Idea> children = graphIdea.getChildrenWithLink(event1, "Next");

        Assertions.assertEquals(children.size(), 1);
        Assertions.assertEquals(getNodeContent(children.get(0)), event2);

        Assertions.assertEquals(graphIdea.getChildrenWithLink(event1, "FakeLink").size(), 0);
        Assertions.assertEquals(graphIdea.getChildrenWithLink(new Idea("FakeNode"), "FakeLink").size(), 0);
    }

    @Test
    public void emptyGraphTest(){
        GraphIdea test = new GraphIdea(new Idea("Graph"));

        Assertions.assertEquals(test.getNodes().size(), 0);
    }

    @Test
    public void episodeSubGraphTest(){
        Idea epltmIdea = new Idea("EPLTM", null, "Epsisode", 1);
        GraphIdea epltm = new GraphIdea(epltmIdea);

        LocationCategoryGenerator locGen = new LocationCategoryGenerator();
        List<Idea> locCat = new ArrayList<>();
        List<Idea> subContexts = new ArrayList<>();
        for (int i = 0; i<4; i++){
            Idea newCat = locGen.exec(null);
            locCat.add(newCat);
            epltm.insertLocationNode(newCat);
            subContexts.add(new Idea("SubContext"+i, null, 2));
            Idea subNode = epltm.insertContextNode(subContexts.get(i));
            epltm.insertLink(newCat, subNode, "SubContext");
        }

        Idea assimilateSubHabit = new Idea("assimilate", null);
        assimilateSubHabit.setValue(new AssimilatePropertyCategory(assimilateSubHabit));
        assimilateSubHabit.add(new Idea("properties", null, "Property", 1));
        assimilateSubHabit.add(new Idea("samples", null, "Property", 1));

        assimilateSubHabit.get("properties").setValue(Arrays.asList("p1","p2"));
        Idea toLearn = new Idea("object", "test", "AbstractObject", 1);
        toLearn.add(new Idea("p1", 0, "QualityDimension", 1));
        toLearn.add(new Idea("p2", 1, "QualityDimension", 1));

        List<Idea> eventCategories = Arrays.asList(new Idea("EventCategory1"),
                new Idea("EventCategory2"),
                new Idea("EventCategory3"),
                new Idea("EventCategory4"));

        for (Idea eventCat : eventCategories){
            eventCat.add(new Idea("ObservedObject", "object"));
        }

        List<Idea> propCat = new ArrayList<>();
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


        GraphIdea subGraph = epltm.getEpisodeSubGraphCopy(ep3);

        List<Idea> ep = subGraph.getEpisodeNodes();
        Assertions.assertEquals(1, ep.size());
        Idea epContent = getNodeContent(ep.get(0));
        Assertions.assertEquals(epContent.getName(), "Episode3");

        //Check events
        List<Idea> begin = subGraph.getChildrenWithLink(epContent, "Begin");
        Assertions.assertEquals(1, begin.size());
        Idea beginEvent = getNodeContent(begin.get(0));
        Assertions.assertEquals("Event5", beginEvent.getName());
        List<Idea> end = subGraph.getChildrenWithLink(epContent, "End");
        Assertions.assertEquals(end.size(), 1);
        Idea endEvent = getNodeContent(end.get(0));
        Assertions.assertEquals(endEvent.getName(), "Event6");

        Assertions.assertEquals(subGraph.getChildrenWithLink(begin.get(0), "Before").get(0), end.get(0));

        //Check Properties
        Idea beginEventInitialProperty = subGraph.getChildrenWithLink(begin.get(0), "Initial").get(0);
        Idea beginEventFinalProperty = subGraph.getChildrenWithLink(begin.get(0), "Final").get(0);
        Idea endEventInitialProperty = subGraph.getChildrenWithLink(end.get(0), "Initial").get(0);
        Idea endEventFinalProperty = subGraph.getChildrenWithLink(end.get(0), "Final").get(0);

        Assertions.assertEquals(propCat.get(2), getNodeContent(beginEventInitialProperty));
        Assertions.assertEquals(propCat.get(8), getNodeContent(beginEventFinalProperty));
        Assertions.assertEquals(propCat.get(9), getNodeContent(endEventInitialProperty));
        Assertions.assertEquals(propCat.get(10), getNodeContent(endEventFinalProperty));

        List<Idea> loc = subGraph.getChildrenWithLink(beginEvent, "Location");
        Assertions.assertEquals(locCat.get(2), getNodeContent(loc.get(0)));
        List<Idea> subContext = subGraph.getChildrenWithLink(loc.get(0), "SubContext");
        Assertions.assertEquals(subContexts.get(2), getNodeContent(subContext.get(0)));
    }

    @Test
    public void ideaMatchTest(){
        Idea a = new Idea("A", 0, 1);
        a.add(new Idea("S1", 1, 2));
        a.add(new Idea("S2", 2, 2));
        Idea b = new Idea("A", 0, 1);
        b.add(new Idea("S1", 1, 2));
        b.add(new Idea("S2", 2, 2));
        b.add(new Idea("S3", 3, 2));

        Assertions.assertTrue(IdeaHelper.match(a, b));
        Assertions.assertFalse(IdeaHelper.match(b, a));
    }
}
