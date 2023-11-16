package CSTEpisodicMemory.core.representation;

import CSTEpisodicMemory.util.BKDTree;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.KDTree;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

public class GridLocation {

    private final static double SCALE = 0.15;
    private final static double SQRT_3 = Math.sqrt(3);
    private final static int GRID_SIZE = 120;
    private static Idea[][] adjencyMap = new Idea[GRID_SIZE][GRID_SIZE];
    private static Idea[][] referenceMap = new Idea[GRID_SIZE][GRID_SIZE];
    private static BKDTree grid;

    private static GridLocation singleton = new GridLocation();

    public GridLocation() {
        List<KDTree.Node> nodes = new ArrayList<>();
        for (int u = -GRID_SIZE / 2; u < GRID_SIZE / 2; u++) {
            for (int v = -GRID_SIZE / 2; v < GRID_SIZE / 2; v++) {
                nodes.add(new KDTree.Node(toXY(u, v)));
                int i = u + GRID_SIZE / 2;
                int j = v + GRID_SIZE / 2;
                Idea gridIdea = new Idea("Grid_Place", i * GRID_SIZE + j, "Property", 1);
                gridIdea.add(new Idea("u", (double) u, "QualityDimension", 1));
                gridIdea.add(new Idea("v", (double) v, "QualityDimension", 1));
                Idea links = new Idea("Adjacent", null, "Link", 1);
                if (i - 1 >= 0) {
                    links.add(adjencyMap[i - 1][j]);
                    adjencyMap[i - 1][j].get("Adjacent").add(gridIdea);
                    if (j + 1 < GRID_SIZE) {
                        links.add(adjencyMap[i - 1][j + 1]);
                        adjencyMap[i - 1][j + 1].get("Adjacent").add(gridIdea);
                    }
                }
                if (j - 1 >= 0) {
                    links.add(adjencyMap[i][j - 1]);
                    adjencyMap[i][j - 1].get("Adjacent").add(gridIdea);
                }
                gridIdea.add(links);
                adjencyMap[i][j] = gridIdea;
                referenceMap[i][j] = IdeaHelper.shallowClone(gridIdea);
            }
        }
        //grid = new KDTree(2, nodes);
        Collections.shuffle(nodes);
        grid = new BKDTree(2, 30, nodes);
        //grid = new BKDTree(2, 13);
        //for (KDTree.Node n : nodes){
        //    grid.insert(n);
        //}
    }

    public int[] locateHCC(double x, double y) {
        KDTree.Node near = grid.findNearest(new KDTree.Node(x, y));
        //double u = ((SQRT_3 / 3 * near.getX() - near.getY() / 3)) / SCALE;
        double u = (SQRT_3 * near.getX() / (3 * SCALE)) - ((Math.abs((2 * near.getY()) / (3 * SCALE)) % 2) / 2);
        double v = (2 * near.getY() / 3) / SCALE;

        return new int[]{(int) Math.round(u), (int) Math.round(v)};
    }

    public int[] locateHCC(double[] coord) {
        return locateHCC(coord[0], coord[1]);
    }

    public Idea locateHCCIdea(double x, double y) {
        int[] hcc = locateHCC(x, y);
        return referenceMap[hcc[0] + GRID_SIZE / 2][hcc[1] + GRID_SIZE / 2];
    }

    public double[] toXY(double u, double v) {
        double x = SQRT_3 * ((Math.abs(v) % 2) / 2 + u) * SCALE;
        double y = (3 * v * SCALE) / 2;
        return new double[]{x, y};
    }

    public static String toColor(int[] hcc) {
        double r = hcc[0] * 255d / GRID_SIZE + 255d / 2;
        double g = hcc[1] * 255d / GRID_SIZE + 255d / 2;
        double b = hcc[1] * 255d / GRID_SIZE + 255d / 2;
        String sr = Integer.toHexString((int) r);
        sr = sr.length() == 2 ? sr : "0" + sr;
        String sg = Integer.toHexString((int) g);
        sg = sg.length() == 2 ? sg : "0" + sg;
        String sb = Integer.toHexString((int) b);
        sb = sb.length() == 2 ? sb : "0" + sb;
        return "#" + sr + sg + sb;
    }

    public List<double[]> getCenters() {
        List<double[]> centers = new ArrayList<>();
        for (KDTree.Node node : grid.getNodes()) {
            centers.add(node.getCoords());
        }
        return centers;
    }

    public List<Idea> trajectoryInHCC(double[] start, double[] end) {
        System.out.println(start[0] + ", " + start[1]);
        System.out.println(end[0] + ", " + end[1]);
        List<Idea> plan = new LinkedList<>();
        double[] currPlanPos = start;
        double bestDist = dist(currPlanPos, end);
        int count=0;
        while (dist(currPlanPos, end) > 0) {
            System.out.println(currPlanPos[0] + ", " + currPlanPos[1]);
            if(count++%100000 == 0)
                System.out.println(count);
            for (double[] adj : adjacentCellsHCC(currPlanPos)) {
                if (dist(adj, end) < bestDist) {
                    currPlanPos = adj;
                    bestDist = dist(adj, end);
                }
            }
            plan.add(referenceMap[(int) currPlanPos[0] + GRID_SIZE / 2][(int) currPlanPos[1] + GRID_SIZE / 2]);
        }
        return plan;
    }

    public double dist(double[] a, double[] b) {
        a = toXY(a[0], a[1]);
        b = toXY(b[0], b[1]);
        return Math.hypot(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1]));
    }

    public double manhattanDist(double[] a, double[] b) {
        a = toXY(a[0], a[1]);
        b = toXY(b[0], b[1]);
        return Math.abs(b[0] - a[0]) + Math.abs(b[1] - a[1]);
    }

    public List<double[]> adjacentCellsHCC(double[] pos) {
        List<double[]> adj = Arrays.asList(
                new double[]{pos[0] + 1, pos[1] + 1},
                new double[]{pos[0] + 1, pos[1] - 1},
                new double[]{pos[0] + 1, pos[1]},
                new double[]{pos[0] - 1, pos[1]},
                new double[]{pos[0], pos[1] - 1},
                new double[]{pos[0], pos[1] + 1}
        );
        return adj.stream().sorted(Comparator.comparingDouble(e->manhattanDist(e,new double[]{0,0}))).collect(Collectors.toList());
        //return Arrays.asList(
        //        new double[]{pos[0] + 1, pos[1] + 1},
        //        new double[]{pos[0] + 1, pos[1] - 1},
        //        new double[]{pos[0] + 1, pos[1]},
        //        new double[]{pos[0] - 1, pos[1]},
        //        new double[]{pos[0], pos[1] - 1},
        //        new double[]{pos[0], pos[1] + 1}
        //);
    }

    public static GridLocation getInstance() {
        return singleton;
    }

    public Idea getReferenceGridIdea(int u, int v) {
        return referenceMap[u + GRID_SIZE / 2][v + GRID_SIZE / 2];
    }
}
