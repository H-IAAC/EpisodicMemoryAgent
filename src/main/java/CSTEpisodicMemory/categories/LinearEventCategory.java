package CSTEpisodicMemory.categories;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinearEventCategory extends EventCategory {

    public static double threashold = 0.01;

    public LinearEventCategory(String name, List<String> properiesList) {
        super(name, properiesList);
    }

    @Override
    protected boolean checkVectorChange(List<ArrayRealVector> propertiesVector) {
        int vectorSize = propertiesVector.size();
        if (vectorSize < 3){
            Logger.getLogger(LinearEventCategory.class.getName()).log(Level.SEVERE,
                    "Linear Event Category " + this.name + " receveid buffer to small");
            return false;
        }
        ArrayRealVector prevDirVector = propertiesVector.get(1).subtract(propertiesVector.get(0));
        ArrayRealVector currDirVector = propertiesVector.get(2).subtract(propertiesVector.get(1));
        //System.out.println(propertiesVector.get(0) + " | " + propertiesVector.get(1) + " | " + propertiesVector.get(2));
        boolean check = prevDirVector.getNorm() > threashold && getAbsAngle(prevDirVector, currDirVector) < 0.02;
        return check;
    }

    @Override
    protected boolean checkVectorSize(List<ArrayRealVector> propertiesVector) {
        return propertiesVector.size() >= 3;
    }

    private double getAbsAngle(ArrayRealVector vecA, ArrayRealVector vecB) {
        double normA = vecA.getNorm();
        double normB = vecB.getNorm();
        double cos = (vecA.dotProduct(vecB)) / (normA * normB);
        return Math.abs(Math.acos(cos));
    }
}
