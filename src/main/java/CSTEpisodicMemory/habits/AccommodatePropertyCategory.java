package CSTEpisodicMemory.habits;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

public class AccommodatePropertyCategory implements Habit {

    private final Idea owner;
    private final double lr = 0.2;

    public AccommodatePropertyCategory(Idea owner) {
        this.owner = owner;
    }

    @Override
    public Idea exec(Idea idea) {
        if (idea.getCategory().equalsIgnoreCase("Property") && idea.getValue() instanceof Category){
            List<String> properties = (List<String>) owner.get("properties").getValue();
            for (Idea sample : owner.get("samples").getL()){
                for (String property : properties){
                    Object sampleValue = sample.get(property).getValue();
                    Object centerValue = idea.get("center."+property).getValue();

                    if (sampleValue instanceof Float){
                        float newCenter = (float) ((float) centerValue*(1-lr) + (float) sampleValue*lr);
                        idea.get("center."+property).setValue(newCenter);
                    }
                    if (sampleValue instanceof Double){
                        double newCenter = (double) centerValue*(1-lr) + (double) sampleValue*lr;
                        idea.get("center."+property).setValue(newCenter);
                    }
                    if (sampleValue instanceof Integer){
                        int newCenter = (int) ((int) centerValue*(1-lr) + (int) sampleValue*lr);
                        idea.get("center."+property).setValue(newCenter);
                    }
                }
            }
            return idea;
        }
        return null;
    }
}
