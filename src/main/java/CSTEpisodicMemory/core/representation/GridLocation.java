package CSTEpisodicMemory.core.representation;

import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.KDTree;
import br.unicamp.cst.representation.idea.Idea;
import com.github.sh0nk.matplotlib4j.Plot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GridLocation {

    private final static double SCALE = 1.0;
    private final static double SQRT_3 = Math.sqrt(3);
    private final static int GRID_SIZE = 40;
    private static Idea[][] gridIdeas = new Idea[GRID_SIZE][GRID_SIZE];
    private static KDTree grid;

    private static GridLocation singleton = new GridLocation();

    public GridLocation() {
        List<KDTree.Node> nodes = new ArrayList<>();
        for (int u = -GRID_SIZE / 2; u < GRID_SIZE / 2; u++) {
            for (int v = -GRID_SIZE / 2; v < GRID_SIZE / 2; v++) {
                nodes.add(new KDTree.Node(toXY(u, v)));
                int i = u + GRID_SIZE/2;
                int j = v + GRID_SIZE/2;
                Idea gridIdea = new Idea("Grid_Place", i*GRID_SIZE+j, "Property", 1);
                gridIdea.add(new Idea("u", u, "QualityDimension", 1));
                gridIdea.add(new Idea("v", v, "QualityDimension", 1));
                Idea links = new Idea("Adjacent", null, "Link", 1);
                if (i-1>=0){
                    links.add(gridIdeas[i-1][j]);
                    gridIdeas[i-1][j].get("Adjacent").add(gridIdea);
                    if (j+1<GRID_SIZE){
                        links.add(gridIdeas[i-1][j+1]);
                        gridIdeas[i-1][j+1].get("Adjacent").add(gridIdea);
                    }
                }
                if (j-1>=0){
                    links.add(gridIdeas[i][j-1]);
                    gridIdeas[i][j-1].get("Adjacent").add(gridIdea);
                }
                gridIdea.add(links);
                gridIdeas[i][j] = gridIdea;
            }
        }
        grid = new KDTree(2, nodes);
    }

    public double[] locateHCC(double x, double y) {
        KDTree.Node near = grid.findNearest(new KDTree.Node(x, y));
        x = near.getX();
        y = near.getY();
        double u = (2 * y / 3) / SCALE;
        double v = (-(SQRT_3 / 3 * x + y / 3)) / SCALE;

        return new double[]{u, v};
    }

    public Idea locateHCCIdea(double x, double y){
        double[] hcc = locateHCC(x, y);
        return gridIdeas[(int) (hcc[0] + GRID_SIZE / 2)][(int) (hcc[1] + GRID_SIZE / 2)];
    }

    public double[] toXY(double r, double g) {
        double x = SQRT_3 * (g / 2 + r) * SCALE;
        double y = 3 * g / 2 * SCALE;
        return new double[]{x, y};
    }

    public static String toColor(double[] hcc) {
        double r = hcc[0] * 255 / 20 + 255 / 2;
        double g = hcc[1] * 255 / 20 + 255 / 2;
        double b = hcc[1] * 255 / 20 + 255 / 2;
        String sr = Integer.toHexString((int) r);
        sr = sr.length() == 2 ? sr : "0" + sr;
        String sg = Integer.toHexString((int) g);
        sg = sg.length() == 2 ? sg : "0" + sg;
        String sb = Integer.toHexString((int) b);
        sb = sb.length() == 2 ? sb : "0" + sb;
        return "#" + sr + sg + sb;
    }

    public static GridLocation getInstance() {
        return singleton;
    }
}
