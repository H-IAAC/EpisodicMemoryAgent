package CSTEpisodicMemory.representation;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.Test;

import java.util.List;
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

        graphIdea.insetLink(event1, event2, "Next");
        graphIdea.insetLink(event2, event3, "Next");
        graphIdea.insetLink(event1, pos1, "Start");
        graphIdea.insetLink(event1, pos2, "End");
        graphIdea.insetLink(event2, pos2, "Start");
        graphIdea.insetLink(event2, pos3, "End");
    }

    @Test
    public void testLinks(){
        System.out.println(graphIdea.links);
        List<GraphIdea.Link> links = graphIdea.getSuccesors(event1);
        assertEquals(3, links.size());

        List<String> types = links.stream().map(e->e.type).collect(Collectors.toList());
        assertTrue(types.contains("Next"));
        assertTrue(types.contains("Start"));
        assertTrue(types.contains("End"));

        List<Idea> dest = links.stream().map(e->e.nodeDest.getL().get(1)).collect(Collectors.toList());
        System.out.println(dest);
        assertTrue(dest.contains(event2));
        assertTrue(dest.contains(pos1));
        assertTrue(dest.contains(pos2));
    }
}
