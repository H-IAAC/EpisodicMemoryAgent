package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.entity.CategoryIdea;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class EventCategory extends CategoryIdea {

    private final List<String> properiesList;

    public EventCategory(String name, List<String> properiesList) {
        super(name, null, "Episode", 2);
        this.properiesList = properiesList;
    }

    @Override
    public double membership(Idea idea) {
        List<ArrayRealVector> propertiesVector = new ArrayList<>();
        for (Idea step : idea.getL())
            propertiesVector.add(extractProperties(step));

        ArrayRealVector prevDirVector = propertiesVector.get(1).subtract(propertiesVector.get(0));
        ArrayRealVector currDirVector = propertiesVector.get(2).subtract(propertiesVector.get(1));
        boolean check = prevDirVector.getNorm() > 0.01 && getAbsAngle(prevDirVector, currDirVector) < 0.02;
//        System.out.println("---" + check + "---");
//        System.out.println(Arrays.toString(propertiesVector.get(0).toArray()));
//        System.out.println(Arrays.toString(propertiesVector.get(1).toArray()));
//        System.out.println(Arrays.toString(propertiesVector.get(2).toArray()));
        if (check)
            return 1.0;
        return 0;
    }

    private double getAbsAngle(ArrayRealVector vecA, ArrayRealVector vecB) {
        double normA = vecA.getNorm();
        double normB = vecB.getNorm();
        double cos = (vecA.dotProduct(vecB)) / (normA * normB);
        return Math.abs(Math.acos(cos));
    }

    private ArrayRealVector extractProperties(Idea idea) {
        ArrayRealVector propertyVector = new ArrayRealVector();
        for (String property : properiesList){
            propertyVector = (ArrayRealVector) propertyVector.append((float) idea.get(property).getValue());
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
        for (String property : properiesList){
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
