package CSTEpisodicMemory.representation;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals(3, links.size());

        Set<String> types = links.keySet();
        assertTrue(types.contains("Next"));
        assertTrue(types.contains("Start"));
        assertTrue(types.contains("End"));

        List<Idea> dest = links.values().stream().flatMap(Collection::stream).map(GraphIdea::getNodeContent).collect(Collectors.toList());
        System.out.println(dest);
        assertTrue(dest.contains(event2));
        assertTrue(dest.contains(pos1));
        assertTrue(dest.contains(pos2));
    }

    @Test
    public void testActivation(){
        List<Idea> nodes = graphIdea.getNodes();
        assertTrue(nodes.stream().allMatch(n->(double) n.get("Activation").getValue() == 0d));

        graphIdea.setNodeActivation(pos1, 1d);
        assertTrue((double) graphIdea.getNodeFromContent(pos1).get("Activation").getValue() == 1d);

        graphIdea.propagateActivations(Arrays.asList("Next", "Start", "End"), Arrays.asList("Next", "Start", "End"));
        double event3Activatio = (double) graphIdea.getNodeFromContent(event3).get("Activation").getValue();
        assertTrue(event3Activatio == 0.9*0.9*0.9d);

        graphIdea.resetActivations();
        nodes = graphIdea.getNodes();
        assertTrue(nodes.stream().allMatch(n->(double) n.get("Activation").getValue() == 0d));
    }
}
