package CSTEpisodicMemory.categories;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StepEventCategory extends EventCategory {

    private int type = 0;
    private double stepSizeThreashold = 0.5;

    public StepEventCategory(String name, List<String> properiesList) {
        super(name, properiesList);
    }

    public StepEventCategory(String name, List<String> properiesList, String type) {
        super(name, properiesList);
        if (type.equalsIgnoreCase("stepUp"))
            this.type = 1;
        if (type.equalsIgnoreCase("stepDown")) {
            this.type = 2;
        }
    }

    public StepEventCategory(String name, List<String> properiesList, double stepSizeThreashold) {
        super(name, properiesList);
        this.stepSizeThreashold = stepSizeThreashold;
    }

    @Override
    protected boolean checkVectorChange(List<ArrayRealVector> propertiesVector) {
        int vectorSize = propertiesVector.size();
        if (vectorSize < 3){
            Logger.getLogger(LinearEventCategory.class.getName()).log(Level.SEVERE,
                    "Step Event Category " + this.name + " receveid buffer to small");
            return false;
        }
        //For now step will be a sudden change in properties vector magnitude
        double magA = propertiesVector.get(0).getNorm();
        double magB = propertiesVector.get(1).getNorm();
        double magC = propertiesVector.get(2).getNorm();
        boolean check = Math.abs(magC - magB) < 0.01; //Tolerance value
        switch (type){
            case 0:
                check = check & Math.abs(magB - magA) > this.stepSizeThreashold;
                break;
            case 1:
                check = check & (magB - magA) > this.stepSizeThreashold;
                break;
            case 2:
                check = check & (magA - magB) > this.stepSizeThreashold;
                break;
        }

        return check;
    }

    public double getStepSizeThreashold() {
        return stepSizeThreashold;
    }

    public void setStepSizeThreashold(double stepSizeThreashold) {
        if (stepSizeThreashold > 0)
            this.stepSizeThreashold = stepSizeThreashold;
    }
}
