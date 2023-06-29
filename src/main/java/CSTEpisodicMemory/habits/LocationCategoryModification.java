package CSTEpisodicMemory.habits;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationCategoryModification implements Habit {

    private Idea owner;
    private float lr = 0.2f;

    public LocationCategoryModification(Idea owner){
        this.owner = owner;
    }

    @Override
    public Idea exec(Idea idea) {
        if (idea.getName().contains("Location") && idea.getValue() instanceof Category){
            for (Idea sample : owner.getL()){
                //System.out.println(IdeaHelper.fullPrint(sample));
                float sx = (float) sample.get("X").getValue();
                float sy = (float) sample.get("Y").getValue();

                float centerX = (float) idea.get("centerX").getValue();
                float centerY = (float) idea.get("centerY").getValue();

                idea.get("centerX").setValue(centerX*(1-lr) + sx*lr);
                idea.get("centerY").setValue(centerY*(1-lr) + sy*lr);
            }
            return idea;
        }
        return null;
    }
}
