package CSTEpisodicMemory.util;

import br.unicamp.cst.representation.idea.Idea;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdeaHelperTest {

    @Test
    public void matchIdeasTest(){
        System.out.println("Testing");
        Idea a = new Idea("TestA", 1, 0);
        Idea b = new Idea("TestB", 2, 1);
        a.add(b);
        Idea c = new Idea("TestA", 1, 0);
        Idea d = new Idea("TestB", 2, 1);
        c.add(d);

        assertTrue(IdeaHelper.match(b,d));
        assertTrue(IdeaHelper.match(b,d));
        assertFalse(IdeaHelper.match(a,b));
        assertFalse(IdeaHelper.match(a,d));
    }
}
