package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.entity.CategoryIdea;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

import static java.lang.Math.abs;

public class RoomCategoryIdea extends CategoryIdea {

    private Vector2D cornerA;
    private Vector2D cornerB;

    public RoomCategoryIdea(String name, Vector2D cornerA, Vector2D cornerB) {
        super(name, null, "AbstractObject", 2);
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    @Override
    public double membership(Idea idea) {
        Idea position = searcIdea(idea, "Position");
        Vector2D pos = new Vector2D((float) position.get("X").getValue(), (float) position.get("Y").getValue());
        pos = pos.sub(cornerB);
        Vector2D diag = cornerA.sub(cornerB);
        if (abs(pos.getX()) <= abs(diag.getX()) && (abs(pos.getY()) <= abs(diag.getY())) && pos.isSameQuadrant(diag))
            return 1.0;
        return 0;
    }

    @Override
    public Idea instantiation(List<Idea> constraints) {
        return null;
    }
}
