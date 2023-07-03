package CSTEpisodicMemory.habits;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AssimilatePropertyCategory implements Habit {

    private static final double START_RADIUS = 0.5;

    private Idea owner;
    private int count = 0;

    public AssimilatePropertyCategory(Idea owner) {
        this.owner = owner;
    }

    @Override
    public Idea exec(Idea idea) {
        List<String> propertiesList = (List<String>) owner.get("properties").getValue();
        String name = propertiesList.get(0).split("\\.")[0];

        Idea newCat = new Idea(name + count++, null, "Properties", 2);
        if (name.equals("Pitch"))
            newCat.add(new Idea("radius", 0.05, "Property", 1));
        else
            newCat.add(new Idea("radius", START_RADIUS, "Property", 1));

        Idea center = new Idea("center", null, "Aggregation", 1);
        for (String property : propertiesList){
            Object propertyValue = idea.get(property).getValue();
            center.add(new Idea(property, propertyValue, "Property", 1));
        }
        newCat.add(center);

        newCat.setValue(new Category() {
            private Idea owner = newCat;
            @Override
            public Idea getInstance(Idea constraints) {
                Idea instance = new Idea(name, null, "Property", 0);

                double radius = (double) owner.get("radius").getValue();
                List<Idea> centers = owner.get("center").getL();
                Random rnd = new Random();
                double scale = Math.sqrt(radius / centers.size());
                for (Idea propertyCenter : centers) {
                    Object centerValue = propertyCenter.getValue();
                    double propertyInstanceValue = scale * rnd.nextDouble();

                    if (centerValue instanceof Float) {
                        if (propertyCenter.getName().equals(name)){
                            instance.setValue((float) centerValue + (float) propertyInstanceValue);
                        }
                        instance.add(new Idea(propertyCenter.getName(),
                                (float) centerValue + (float) propertyInstanceValue,
                                "QualityDimension", 0));
                    }
                    if (centerValue instanceof Double){
                        if (propertyCenter.getName().equals(name)){
                            instance.setValue((double) centerValue + propertyInstanceValue);
                        }
                        instance.add(new Idea(propertyCenter.getName(),
                                (double) centerValue + propertyInstanceValue,
                                "QualityDimension", 0));
                    }
                    if (centerValue instanceof Integer){
                        if (propertyCenter.getName().equals(name)){
                            instance.setValue((int) centerValue + (int) propertyInstanceValue);
                        }
                        instance.add(new Idea(propertyCenter.getName(),
                                (int) centerValue + (int) propertyInstanceValue,
                                "QualityDimension", 0));
                    }
                }
                return instance;
            }

            @Override
            public double membership(Idea idea) {
                List<Idea> centers = owner.get("center").getL();
                double sum = 0;
                for (Idea propertyCenter : centers) {
                    Object centerValue = propertyCenter.getValue();
                    Idea propertyValue = idea.get(propertyCenter.getName());
                    if (propertyValue == null)
                        return 0;
                    double dif = Double.parseDouble(centerValue.toString()) - Double.parseDouble(propertyValue.getValue().toString());
                    sum += dif * dif;
                }

                double radius = (double) owner.get("radius").getValue();
                double membership = Math.exp(-sum) * Math.exp(radius*radius);

                return membership;
            }
        });
        return newCat;
    }
}
