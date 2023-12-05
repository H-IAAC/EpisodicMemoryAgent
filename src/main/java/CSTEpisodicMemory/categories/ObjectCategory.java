package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.util.BKDTree;
import CSTEpisodicMemory.util.KDTree;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectCategory implements Category {

    public List<String> properties = new ArrayList<>();
    private String category = "";
    private int id = 0;
    private String idProperty = "";

    private BKDTree examplars;

    private int generatorCount = 1;

    public ObjectCategory(Idea examplar) {
        category = examplar.getValue().toString();
        properties = extractProperties(examplar);
        Optional<String> idProperty = properties.stream().filter(s -> s.contains("ID")).findFirst();
        if (idProperty.isPresent()) {
            id = (int) examplar.get(idProperty.get()).getValue();
            properties.remove(idProperty.get());
            this.idProperty = idProperty.get();
        }
        examplars = new BKDTree(properties.size());
        examplars.insert(new KDTree.Node(extractVector(examplar)));
    }

    private List<String> extractProperties(Idea examplar) {
        List<String> foundProperties = new ArrayList<>();
        for (Idea s : examplar.getL()) {
            if ((s.isType(3) || s.isType(1) || s.isType(9) || s.isType(12)) && s.isNumber()) {
                foundProperties.add(s.getName());
            }
            List<String> subProperties = extractProperties(s);
            subProperties = subProperties.stream().map(e -> s.getName() + "." + e).collect(Collectors.toList());
            foundProperties.addAll(subProperties);
        }
        return foundProperties;
    }

    @Override
    public double membership(Idea idea) {
        double eval = 0;
        if (category.equals(idea.getValue().toString())) {
            double[] propertiesVector = extractVector(idea);
            KDTree.Node propertiesVectorNode = new KDTree.Node(propertiesVector);
            KDTree.Node bestExamplar = examplars.findNearest(propertiesVectorNode);
            double dist = bestExamplar.distance(propertiesVectorNode);
            eval = Math.exp(-dist / Math.pow(properties.size(), 2));
            if (idProperty != "") {
                eval *= 0.7;
                if (idea.get(idProperty) != null && (int) idea.get(idProperty).getValue() == id) {
                    if (checkProperties(idea) == 0){
                        eval = 0.7;
                    } else {
                        eval += 0.3;
                    }
                }
            }
        }
        return eval;
    }

    @Override
    public Idea getInstance(Idea constraints) {
        List<KDTree.Node> allExamplars = examplars.getNodes();
        KDTree.Node selected = allExamplars.get(new Random().nextInt(allExamplars.size()));
        Idea instance = new Idea(category + "_" + generatorCount++, category, "AbstractObject", 1);
        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            Idea parent = instance;
            for (String s : property.split(".")) {
                if (parent.get(s) != null) {
                    parent = parent.get(s);
                } else {
                    Idea newSubIdea = new Idea(s, null, "Property", 1);
                    parent.add(newSubIdea);
                    parent = newSubIdea;
                }
            }
            parent.setValue(selected.getCoords()[i]);
        }
        return instance;
    }

    private int checkProperties(Idea idea) {
        int count = 0;
        for (String property : properties) {
            if (idea.get(property) != null)
                count++;
        }
        return count;
    }

    private double[] extractVector(Idea idea) {
        double[] vec = new double[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            if (idea.get(property) != null) {
                double val = Double.parseDouble(idea.get(property).getValue().toString());
                vec[i] = val;
            } else {
                vec[i] = 0;
            }
        }
        return vec;
    }

    public void insertExamplar(Idea idea) {
        if (category.equals(idea.getCategory())) {
            if (checkProperties(idea) == properties.size()) {
                double[] propertiesVector = extractVector(idea);
                KDTree.Node propertiesVectorNode = new KDTree.Node(propertiesVector);
                examplars.insert(propertiesVectorNode);
            }
        }
    }

    public int exemplarsSize() {
        return examplars.getNodes().size();
    }
}
