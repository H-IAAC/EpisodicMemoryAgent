package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.util.BKDTree;
import CSTEpisodicMemory.util.KDTree;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectCategory implements Category {

    public List<String> properties = new ArrayList<>();
    private String category = "";

    private BKDTree examplars;

    public ObjectCategory(Idea examplar){
        category = examplar.getCategory();
        properties = extractProperties(examplar);
        examplars = new BKDTree(properties.size());
        examplars.insert(new KDTree.Node(extractVector(examplar)));
    }

    private List<String> extractProperties(Idea examplar) {
        List<String> foundProperties = new ArrayList<>();
        for (Idea s : examplar.getL()){
            if ( (s.isType(3) || s.isType(1) || s.isType(9) || s.isType(12)) && s.isNumber()){
                foundProperties.add(s.getName());
            }
            List<String> subProperties = extractProperties(s);
            subProperties = subProperties.stream().map(e->s.getName()+"."+e).collect(Collectors.toList());
            foundProperties.addAll(subProperties);
        }
        return foundProperties;
    }

    @Override
    public double membership(Idea idea) {
        if (category.equals(idea.getCategory())){
            if (checkProperties(idea)) {
                double[] propertiesVector = extractVector(idea);
                KDTree.Node propertiesVectorNode = new KDTree.Node(propertiesVector);
                KDTree.Node bestExamplar = examplars.findNearest(propertiesVectorNode);
                double dist = bestExamplar.distance(propertiesVectorNode);
                return Math.exp(-dist/Math.pow(properties.size(), 2));
            }
        }
        return 0;
    }

    @Override
    public Idea getInstance(Idea constraints) {
        return null;
    }

    private boolean checkProperties(Idea idea){
        for (String property : properties){
            if (idea.get(property) == null)
                return false;
        }
        return true;
    }

    private double[] extractVector(Idea idea){
        double[] vec = new  double[properties.size()];
        for (int i = 0; i<properties.size(); i++){
            String property = properties.get(i);
            double val = Double.parseDouble(idea.get(property).getValue().toString());
            if (property.contains("ID"))
                val *= 10;
            vec[i] = val;
        }
        return vec;
    }

    public void insertExamplar(Idea idea){
        if (category.equals(idea.getCategory())) {
            if (checkProperties(idea)) {
                double[] propertiesVector = extractVector(idea);
                KDTree.Node propertiesVectorNode = new KDTree.Node(propertiesVector);
                examplars.insert(propertiesVectorNode);
            }
        }
    }

    public int exemplarsSize(){
        return examplars.getNodes().size();
    }
}
