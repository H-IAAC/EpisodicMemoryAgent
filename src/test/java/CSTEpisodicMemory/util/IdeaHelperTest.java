package CSTEpisodicMemory.util;

import CSTEpisodicMemory.categories.ObjectCategory;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class IdeaHelperTest {

    @Test
    public void matchIdeasTest(){
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

    @Test
    public void objectCategoryTest(){
        Idea ex = new Idea("EX", "test", "AbstractObject", 1);
        ex.add(new Idea("A", 0, "Property", 1));
        Idea ss = new Idea("B", null, "Property", 1);
        ss.add(new Idea("C", 1, "QualityDimension", 1));
        Idea sss = new Idea("D", null, "Property", 1);
        sss.add(new Idea("E", 3, "QualityDimension", 1));
        ss.add(sss);
        ex.add(ss);
        ObjectCategory obj = new ObjectCategory(ex);
        System.out.println(Arrays.toString(obj.properties.toArray()));
        Idea ex2 = IdeaHelper.cloneIdea(ex);
        ex2.get("B.C").setValue(1.3);
        System.out.println(obj.membership(ex2));
        ex2.get("B.C").setValue(2);
        System.out.println(obj.membership(ex2));
        ex2.get("B.D.E").setValue(2);
        System.out.println(obj.membership(ex2));
    }
}
