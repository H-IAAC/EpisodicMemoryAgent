package CSTEpisodicMemory.categories;

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
        Vector2D pos = new Vector2D((float) position.get("X").getValue(), (float) position.get("Y").getValue());
        pos = pos.sub(cornerB);
        Vector2D diag = cornerA.sub(cornerB);
        if (abs(pos.getX()) <= abs(diag.getX()) && (abs(pos.getY()) <= abs(diag.getY())) && pos.isSameQuadrant(diag))
            return 1.0;
        return 0;
    }

    @Override
    public Idea getInstance(Idea constraints) {
        Vector2D diag = cornerA.sub(cornerB);

        Random rnd = new Random();
        Vector2D rand = new Vector2D(rnd.nextFloat()*diag.getX(),
                rnd.nextFloat()*diag.getY());
        rand = rand.add(cornerB);

        Idea loc = new Idea("Position", owner, "Property", 0);
        loc.add(new Idea("X", (float) rand.getX(), "QualityDimension", 0));
        loc.add(new Idea("Y", (float) rand.getY(), "QualityDimension", 0));

        return loc;
    }
}
