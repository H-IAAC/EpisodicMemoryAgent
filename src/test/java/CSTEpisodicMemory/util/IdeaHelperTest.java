package CSTEpisodicMemory.util;

import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertTrue(IdeaHelper.match(b,d));
        Assertions.assertTrue(IdeaHelper.match(b,d));
        Assertions.assertFalse(IdeaHelper.match(a,b));
        Assertions.assertFalse(IdeaHelper.match(a,d));
    }
}
