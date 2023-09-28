package CSTEpisodicMemory.core.representation;

import CSTEpisodicMemory.util.BKDTree;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.KDTree;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;

public class GridLocation {

    private final static double SCALE = 0.3;
    private final static double SQRT_3 = Math.sqrt(3);
    private final static int GRID_SIZE = 40;
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

    public double[] locateHCC(double x, double y) {
        KDTree.Node near = grid.findNearest(new KDTree.Node(x, y));
        //double u = ((SQRT_3 / 3 * near.getX() - near.getY() / 3)) / SCALE;
        double u = (SQRT_3 * near.getX() / (3 * SCALE)) - ((Math.abs((2 * near.getY()) / (3 * SCALE)) % 2) / 2);
        double v = (2 * near.getY() / 3) / SCALE;

        return new double[]{u, v};
    }

    public double[] locateHCC(double[] coord) {
        return locateHCC(coord[0], coord[1]);
    }

    public Idea locateHCCIdea(double x, double y) {
        double[] hcc = locateHCC(x, y);
        return referenceMap[(int) (Math.round(hcc[0]) + GRID_SIZE / 2)][(int) (Math.round(hcc[1]) + GRID_SIZE / 2)];
    }

    public double[] toXY(double u, double v) {
        double x = SQRT_3 * ((Math.abs(v) % 2) / 2 + u) * SCALE;
        double y = (3 * v * SCALE) / 2;
        return new double[]{x, y};
    }

    public static String toColor(double[] hcc) {
        double r = hcc[0] * 255 / GRID_SIZE + 255d / 2;
        double g = hcc[1] * 255 / GRID_SIZE + 255d / 2;
        double b = hcc[1] * 255 / GRID_SIZE + 255d / 2;
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
        List<Idea> plan = new LinkedList<>();
        double[] currPlanPos = start;
        double bestDist = dist(currPlanPos, end);
        while (dist(currPlanPos, end) > 0) {
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
        return Math.abs(b[0] - a[0]) + Math.abs(b[1] - a[1]);
    }

    public List<double[]> adjacentCellsHCC(double[] pos) {
        return Arrays.asList(
                new double[]{pos[0] + 1, pos[1] + 1},
                new double[]{pos[0] + 1, pos[1] - 1},
                new double[]{pos[0] + 1, pos[1]},
                new double[]{pos[0] - 1, pos[1]},
                new double[]{pos[0], pos[1] - 1},
                new double[]{pos[0], pos[1] + 1}
        );
    }

    public static GridLocation getInstance() {
        return singleton;
    }

    public Idea getReferenceGridIdea(int u, int v) {
        return referenceMap[u + GRID_SIZE / 2][v + GRID_SIZE / 2];
    }
}
