package CSTEpisodicMemory.habits;

import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Arrays;
import java.util.List;

public class TrackedPropertiesAssimilateAccommodateHabit implements Habit {

    private Idea owner;

    public TrackedPropertiesAssimilateAccommodateHabit(Idea owner) {
        this.owner = owner;
    }

    @Override
    public Idea exec(Idea idea) {
        Idea assimilateHabit = owner.get("property_habits.assimilate");
        Idea accommodateHabit = owner.get("property_habits.accommodate");
        String observedObject = (String) owner.get("Input_Category.ObservedObject").getValue();
        List<Idea> propertyCategories = owner.get("categories").getL();
        List<String> properties = (List<String>) owner.get("Input_Category.properties").getValue();

        assimilateHabit.get("properties").setValue(properties);
        accommodateHabit.get("properties").setValue(properties);

        Idea step1 = idea.getL().get(0);
        Idea step1PropertyCategory = null;
        Idea step2 = idea.getL().get(1);
        Idea step2PropertyCategory = null;

        Idea objectInstance1 = step1.get(observedObject);
        Idea objectInstance2 = step2.get(observedObject);

        synchronized (propertyCategories) {
            if (propertyCategories.size() == 0) {
                Idea newPropertyCategory1 = assimilateHabit.exec(objectInstance1);
                step1PropertyCategory = newPropertyCategory1;
                Idea newPropertyCategory2 = assimilateHabit.exec(objectInstance2);
                step2PropertyCategory = newPropertyCategory2;
                propertyCategories.add(step1PropertyCategory);
                propertyCategories.add(step2PropertyCategory);
            } else {

                Idea bestFitCategory1 = propertyCategories.get(0);
                Idea bestFitCategory2 = propertyCategories.get(0);
                double bestFitMembership1 = 0;
                double bestFitMembership2 = 0;
                for (Idea cat : propertyCategories) {

                    double catMembership1 = cat.membership(objectInstance1);
                    double catMembership2 = cat.membership(objectInstance2);
                    if (catMembership1 > bestFitMembership1) {
                        bestFitCategory1 = cat;
                        bestFitMembership1 = catMembership1;
                    }
                    if (catMembership2 > bestFitMembership2) {
                        bestFitCategory2 = cat;
                        bestFitMembership2 = catMembership2;
                    }
                }

                if (bestFitMembership1 >= 0.95) {
                    accommodateHabit.get("samples").getL().clear();
                    accommodateHabit.get("samples").add(objectInstance1);
                    accommodateHabit.exec(bestFitCategory1);
                    step1PropertyCategory = bestFitCategory1;
                } else {
                    Idea newPropertyCategory = assimilateHabit.exec(objectInstance1);
                    step1PropertyCategory = newPropertyCategory;
                    propertyCategories.add(step1PropertyCategory);
                }
                if (bestFitMembership2 >= 0.95) {
                    accommodateHabit.get("samples").getL().clear();
                    accommodateHabit.get("samples").add(objectInstance2);
                    accommodateHabit.exec(bestFitCategory2);
                    step2PropertyCategory = bestFitCategory2;
                } else {
                    Idea newPropertyCategory = assimilateHabit.exec(objectInstance2);
                    step2PropertyCategory = newPropertyCategory;
                    propertyCategories.add(step2PropertyCategory);
                }
            }
        }
        Idea returnIdea = new Idea("Properties_cat");
        Idea idea0 = new Idea("0", step1PropertyCategory);
        Idea idea1 = new Idea("1", step2PropertyCategory);
        returnIdea.add(idea0);
        returnIdea.add(idea1);
        return returnIdea;
    }

}