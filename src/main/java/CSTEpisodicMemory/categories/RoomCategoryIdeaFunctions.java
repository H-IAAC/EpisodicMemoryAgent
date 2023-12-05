package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Random;

import static java.lang.Math.abs;

public class RoomCategoryIdeaFunctions implements Category {

    private final String name;
    private Vector2D cornerA;
    private Vector2D cornerB;
    private Idea owner;

    public RoomCategoryIdeaFunctions(Idea owner, String name, Vector2D cornerA, Vector2D cornerB) {
        this.owner = owner;
        this.name = name;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    @Override
    public double membership(Idea idea) {
        Idea position = IdeaHelper.searchIdea(idea, "Position");
        if (position != null) {
            Vector2D pos = new Vector2D((double) position.get("X").getValue(), (double) position.get("Y").getValue());
            pos = pos.sub(cornerB);
            Vector2D diag = cornerA.sub(cornerB);
            if (abs(pos.getX()) <= abs(diag.getX()) && (abs(pos.getY()) <= abs(diag.getY())) && pos.isSameQuadrant(diag))
                return 1.0;
            return 0;
        }
        Idea grid = IdeaHelper.searchIdea(idea, "Grid_Place");
        if (grid != null){
            int[] gridCornerA = GridLocation.getInstance().locateHCC(cornerA.sub(cornerB).getX()/ 2, cornerA.sub(cornerB).getY()/2);
            int[] gridCornerB = GridLocation.getInstance().locateHCC(cornerB.sub(cornerA).getX()/2, cornerB.sub(cornerA).getY()/2);
            double u = (double) grid.get("u").getValue();
            double v = (double) grid.get("v").getValue();
            if (gridCornerA[0] > gridCornerB[0]) {
                if (gridCornerA[1] > gridCornerB[1]) {
                    if (gridCornerB[0] <= u && u <= gridCornerA[0] &&
                    gridCornerB[1] <= v && v <= gridCornerA[1])
                        return 1;
                } else {
                    if (gridCornerB[0] <= u && u <= gridCornerA[0] &&
                        gridCornerA[1] <= v && v <= gridCornerB[1])
                        return 1;
                }
            } else {
                if (gridCornerA[1] > gridCornerB[1]) {
                    if (gridCornerA[0] <= u && u <= gridCornerB[0] &&
                            gridCornerB[1] <= v && v <= gridCornerA[1])
                        return 1;
                } else {
                    if (gridCornerA[0] <= u && u <= gridCornerB[0] &&
                            gridCornerA[1] <= v && v <= gridCornerB[1])
                        return 1;
                }
            }
        }
        return 0;
    }

    @Override
    public Idea getInstance(Idea constraints) {
        Vector2D diag = cornerA.sub(cornerB);

        Random rnd = new Random();
        Vector2D rand = new Vector2D(rnd.nextDouble()*diag.getX(),
                rnd.nextDouble()*diag.getY());
        rand = rand.add(cornerB);

        Idea loc = new Idea("Position", owner, "Property", 0);
        loc.add(new Idea("X", (double) rand.getX(), "QualityDimension", 0));
        loc.add(new Idea("Y", (double) rand.getY(), "QualityDimension", 0));

        return loc;
    }
}
