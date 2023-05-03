package CSTEpisodicMemory.core.representation;

import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

public abstract class CategoryIdea extends Idea {

    public CategoryIdea(String name, Object value, String category, int scope) {
        super(name, value, category, scope);
    }

    public abstract double membership(Idea idea);
    public abstract Idea instantiation(List<Idea> constraints);

    public static Idea searchIdea(Idea idea, String name){
        Idea hit = idea.get(name);
        if (hit == null){
            //!!!!This can generate infinite loops
            for (Idea i : idea.getL()){
                hit = searchIdea(i, name);
            }
        }
        return hit;
    }
}
