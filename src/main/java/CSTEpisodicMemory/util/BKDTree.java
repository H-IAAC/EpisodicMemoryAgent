package CSTEpisodicMemory.util;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BKDTree {

    private ArrayList<KDTree> forest = new ArrayList<>();
    private KDTree workingTree;
    private int blockSize = 10;
    private int dimensions;

    public BKDTree(int dimensions_) {
        dimensions = dimensions_;
        workingTree = new KDTree(dimensions_);
        forest.add(new KDTree(dimensions_));
    }

    public BKDTree(int dimensions_, int blockSize_) {
        blockSize = blockSize_;
        dimensions = dimensions_;
        workingTree = new KDTree(dimensions_);
        forest.add(new KDTree(dimensions_));
    }

    public BKDTree(int dimensions_, int blockSize_, List<KDTree.Node> nodes) {
        blockSize = blockSize_;
        dimensions = dimensions_;
        int size = nodes.size();
        int start = size % blockSize;
        workingTree = new KDTree(dimensions, nodes.subList(0, start));
        if (size > blockSize){
            int div = size/blockSize;
            int c = 0;
            while (div > 0){
                int mod = div % 2;
                div = div / 2;
                if (mod == 1){
                    forest.add(new KDTree(dimensions, nodes.subList(start, (int) (Math.pow(2,c)*blockSize+start))));
                    start = (int) (Math.pow(2,c)*blockSize + start);
                } else {
                    forest.add(new KDTree(dimensions));
                }
                c++;
            }
        } else {
            forest.add(new KDTree(dimensions));
        }
    }

    public void insert(KDTree.Node node) {
        workingTree.insert(node);
        if (workingTree.size() > blockSize) {
            int i = 0;
            for (KDTree tree : forest) {
                if (tree.size() == 0)
                    break;
                i++;
            }

            List<KDTree.Node> collectedNodes = workingTree.getNodes();
            workingTree = new KDTree(dimensions);
            for (int j = 0; j < i; j++) {
                collectedNodes.addAll(forest.get(j).getNodes());
                forest.set(j, new KDTree(dimensions));
            }
            if (i < forest.size()) {
                forest.set(i, new KDTree(dimensions, collectedNodes));
            } else {
                forest.add(new KDTree(dimensions, collectedNodes));
            }
        }
    }

    public KDTree.Node findNearest(KDTree.Node target){
        KDTree.Node best = workingTree.findNearest(target);
        double bestDist = workingTree.distance();
        for (KDTree tree : forest){
            if (tree.size() > 0){
                KDTree.Node treeBest = tree.findNearest(target);
                if (tree.distance() < bestDist){
                    best = treeBest;
                    bestDist = tree.distance();
                }
            }
        }
        return best;
    }

    public List<KDTree.Node> getNodes(){
        List<KDTree.Node> nodes = workingTree.getNodes();
        for (KDTree tree : forest){
            nodes.addAll(tree.getNodes());
        }
        return nodes;
    }

}
