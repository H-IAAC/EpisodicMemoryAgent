package CSTEpisodicMemory.representation;

import CSTEpisodicMemory.core.representation.GraphIdea;
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
}
