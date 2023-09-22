package CSTEpisodicMemory.util;

import org.opt4j.benchmarks.K;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Implementation copied from https://rosettacode.org/wiki/K-d_tree#Java
public class KDTree {
    private int dimensions_;
    private Node root_ = null;
    private Node best_ = null;
    private double bestDistance_ = 0;
    private int visited_ = 0;
    private int size = 0;

    public KDTree(int dimensions){
        dimensions_ = dimensions;
    }

    public KDTree(int dimensions, List<Node> nodes) {
        dimensions_ = dimensions;
        size = nodes.size();
        root_ = makeTree(nodes, 0, nodes.size(), 0);
    }

    public Node findNearest(Node target) {
        best_ = null;
        visited_ = 0;
        bestDistance_ = Integer.MAX_VALUE;
        if (root_ == null)
            return null;
        nearest(root_, target, 0);
        return best_;
    }

    public int visited() {
        return visited_;
    }

    public double distance() {
        return Math.sqrt(bestDistance_);
    }

    private void nearest(Node root, Node target, int index) {
        if (root == null)
            return;
        ++visited_;
        double d = root.distance(target);
        if (best_ == null || d < bestDistance_) {
            bestDistance_ = d;
            best_ = root;
        }
        if (bestDistance_ == 0)
            return;
        double dx = root.get(index) - target.get(index);
        index = (index + 1) % dimensions_;
        nearest(dx > 0 ? root.left_ : root.right_, target, index);
        if (dx * dx >= bestDistance_)
            return;
        nearest(dx > 0 ? root.right_ : root.left_, target, index);
    }

    public void insert(Node node){
        if (root_ == null)
            root_ = node;
        else
            insert(node, root_, 0);
        size++;
    }

    private void insert(Node node, Node curr, int level){
        int index = level % dimensions_;
        NodeComparator cmp = new NodeComparator(index);
        if (cmp.compare(curr, node) < 0 ){
            if (curr.left_ == null)
                curr.left_ = node;
            else
                insert(node, curr.left_, level + 1);
        } else {
            if (curr.right_ == null)
                curr.right_ = node;
            else
                insert(node, curr.right_, level + 1);
        }
    }

    private void remove(Node node, Node parent, int level){
        Node min;
        if (node.right_ != null){
            min = minNode(node.right_, level % dimensions_, level + 1);
        } else {
            min = minNode(node.left_, level % dimensions_, level + 1);
        }
    }

    private Node minNode(Node node, int index, int level){
        if (node.left_ == null && node.right_ == null)
            return node;
        if(index == level % dimensions_){
            return minNode(node.left_, index, level + 1);
        }
        double min = node.coords_[index];
        Node minL = null;
        Node minR = null;
        if (node.right_ != null)
            minR = minNode(node.right_, index, level + 1);
        if (node.left_ != null)
            minL = minNode(node.left_, index, level + 1);

        if (minR != null) {
            if (minL != null) {
                if (min < minL.coords_[index] && min < minR.coords_[index])
                    return node;
                if(minR.coords_[index] < min && minR.coords_[index] < minL.coords_[index])
                    return minR;
                return minL;
            }
            if (min < minR.coords_[index])
                return node;
            return minR;
        }

        if (minL != null) {
            if (min < minL.coords_[index])
                return node;
            return minL;
        }
        return node;
    }

    public List<Node> getNodes(){
        return traverse(root_, new ArrayList<>());
    }

    public Node querry(Node node){
        return querry(node, root_, 0);
    }

    public Node querry(Node node, Node curr, int level){
        int index = level % dimensions_;
        NodeComparator cmp = new NodeComparator(index);
        if (curr == null)
            return null;
        if(node.distance(curr) == 0){
            return curr;
        }
        if (cmp.compare(node, curr) < 0)
            return querry(node, curr.left_, level + 1);
        else
            return querry(node, curr.right_, level + 1);
    }

    private List<Node> traverse(Node root, List<Node> nodes) {
        if (root != null){
            nodes.add(root);
            nodes = traverse(root.left_, nodes);
            nodes = traverse(root.right_, nodes);
        }
        return nodes;
    }

    private Node makeTree(List<Node> nodes, int begin, int end, int index) {
        if (end <= begin)
            return null;
        int n = begin + (end - begin)/2;
        Node node = QuickSelect.select(nodes, begin, end - 1, n, new NodeComparator(index));
        index = (index + 1) % dimensions_;
        node.left_ = makeTree(nodes, begin, n, index);
        node.right_ = makeTree(nodes, n + 1, end, index);
        return node;
    }

    public int size(){
        return size;
    }

    private static class NodeComparator implements Comparator<Node> {
        private int index_;

        private NodeComparator(int index) {
            index_ = index;
        }
        public int compare(Node n1, Node n2) {
            return Double.compare(n1.get(index_), n2.get(index_));
        }
    }

    public static class Node {
        private double[] coords_;
        private Node left_ = null;
        private Node right_ = null;

        public Node(double[] coords) {
            coords_ = coords;
        }
        public Node(double x, double y) {
            this(new double[]{x, y});
        }
        public Node(double x, double y, double z) {
            this(new double[]{x, y, z});
        }
        double get(int index) {
            return coords_[index];
        }
        public double distance(Node node) {
            double dist = 0;
            for (int i = 0; i < coords_.length; ++i) {
                double d = coords_[i] - node.coords_[i];
                dist += d * d;
            }
            return dist;
        }
        public String toString() {
            StringBuilder s = new StringBuilder("(");
            for (int i = 0; i < coords_.length; ++i) {
                if (i > 0)
                    s.append(", ");
                s.append(coords_[i]);
            }
            s.append(')');
            return s.toString();
        }

        public double getX(){ return coords_[0];}
        public double getY(){ return coords_[1];}
        public double[] getCoords(){ return coords_;}
    }
}

