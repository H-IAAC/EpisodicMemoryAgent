package CSTEpisodicMemory.habits;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LocationCategoryGenerator implements Habit {

    private static int locCount = 0;

    @Override
    public List<Idea> exec(Idea idea) {
        if (idea.getName().equals("Position")) {
            float startX = (float) idea.get("X").getValue();
            float startY = (float) idea.get("Y").getValue();
            Idea newLocation = new Idea("Location_"+locCount++, null, "AbstractObject", 2);
            newLocation.add(new Idea("centerX", startX, "Property", 1));
            newLocation.add(new Idea("centerY", startY, "Property", 1));
            newLocation.add(new Idea("radius", 1d, "Property", 1));

            newLocation.setValue(new Category() {
                private Idea owner = newLocation;

                @Override
                public Idea getInstance(List<Idea> constraints) {
                    float centerX = (float) owner.get("centerX").getValue();
                    float centerY = (float) owner.get("centerY").getValue();
                    double radius = (double) owner.get("radius").getValue();

                    Idea instance = new Idea("Position", null, "Property", 0);
                    double x = Math.sqrt(radius) * new Random().nextDouble();
                    double y = Math.sqrt(radius) * new Random().nextDouble();

                    instance.add(new Idea("X", x + centerX, "QualityDimension", 0));
                    instance.add(new Idea("Y", y + centerY, "QualityDimension", 0));
                    return instance;
                }

                @Override
                public double membership(Idea idea) {
                    float centerX = (float) owner.get("centerX").getValue();
                    float centerY = (float) owner.get("centerY").getValue();
                    double radius = (double) owner.get("radius").getValue();

                    //System.out.println(idea);
                    //System.out.println(idea.getName());
                    if (!idea.getName().equals("Position"))
                        return 0d;
                    //System.out.println("TESTING");
                    float x = (float) idea.get("X").getValue();
                    float y = (float) idea.get("Y").getValue();
                    if (Math.hypot(x-centerX,y-centerY) <= radius)
                        return 1.0;
                    double membership = Math.exp(-((x-centerX)*(x-centerX)) - ((y-centerY)*(y-centerY)));
                    double offset = Math.exp(-radius*radius);
                    return membership + offset;
                }
            });
            return Arrays.asList(newLocation);
        }
        return null;
    }
}
