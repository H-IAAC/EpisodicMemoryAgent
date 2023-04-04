package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.entity.CategoryIdea;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

public abstract class EventCategory extends CategoryIdea {

    private final List<String> vectorPropertiesList;
    private final List<String> contextPropertiesList;

    public EventCategory(String name, List<String> properiesList) {
        super(name, null, "Episode", 2);
        this.vectorPropertiesList = properiesList;
        contextPropertiesList = new ArrayList<>();
    }

    public EventCategory(String name, List<String> vectorPropertiesList, List<String> contextPropertiesList) {
        super(name, null, "Episode", 2);
        this.vectorPropertiesList = vectorPropertiesList;
        this.contextPropertiesList = contextPropertiesList;
    }

    @Override
    public double membership(Idea idea) {
        List<ArrayRealVector> propertiesVector = new ArrayList<>();
        for (Idea step : idea.getL())
            propertiesVector.add(extractProperties(step));

        boolean check = isThisCategory(propertiesVector);
        if (check)
            return 1.0;
        return 0;
    }

    protected abstract boolean isThisCategory(List<ArrayRealVector> propertiesVector);


    private ArrayRealVector extractProperties(Idea idea) {
        ArrayRealVector propertyVector = new ArrayRealVector();
        for (String property : vectorPropertiesList){
            if (idea.get(property).getValue() instanceof Float)
                propertyVector = (ArrayRealVector) propertyVector.append((float) idea.get(property).getValue());
            if (idea.get(property).getValue() instanceof Integer)
                propertyVector = (ArrayRealVector) propertyVector.append((int) idea.get(property).getValue());
            if (idea.get(property).getValue() instanceof Double)
                propertyVector = (ArrayRealVector) propertyVector.append((double) idea.get(property).getValue());
        }
        return propertyVector;
    }

    @Override
    public Idea instantiation(List<Idea> constraints) {
        if (constraints != null){
            Idea eventIdea = new Idea("Event", this.getName(), "Episode", 1);
            Idea time1 = new Idea("", 1, "TimeStep", 1);
            Idea time2 = new Idea("", 2, "TimeStep", 1);
            time1.add(extractRelevant(constraints.get(0)));
            time2.add(extractRelevant(constraints.get(1)));
            eventIdea.add(time1);
            eventIdea.add(time2);
            return eventIdea;
        }
        return null;
    }

    private Idea extractRelevant(Idea idea) {
        Idea extracted = new Idea(idea.getName(), idea.getValue(), idea.getType());
        for (String property : vectorPropertiesList){
            extractMerge(extracted, idea, property);
        }
        for (String property : contextPropertiesList){
            extractMerge(extracted, idea, property);
        }
        return extracted;
    }

    private Idea extractMerge(Idea copy, Idea original, String path){
        Idea i = copy;
        Idea j = original;
        String[] spath = path.split("\\.");
        for (int k=1; k<spath.length; k++){
            Idea i_ = i.get(spath[k]);
            j = j.get(spath[k]);
            if (i_ == null){
                i_ = new Idea(j.getName(), j.getValue(), j.getType());
                i.add(i_);
            }
            i = i_;
        }
        return copy;
    }
}
