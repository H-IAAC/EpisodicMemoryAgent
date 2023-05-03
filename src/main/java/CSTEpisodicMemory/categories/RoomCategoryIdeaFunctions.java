package CSTEpisodicMemory.categories;

import CSTEpisodicMemory.core.representation.CategoryFunctions;
import CSTEpisodicMemory.core.representation.IdeaPlus;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.representation.idea.Idea;

import java.util.List;

import static java.lang.Math.abs;

public class RoomCategoryIdeaFunctions implements CategoryFunctions {

    private final String name;
    private Vector2D cornerA;
    private Vector2D cornerB;

    public RoomCategoryIdeaFunctions(String name, Vector2D cornerA, Vector2D cornerB) {
        this.name = name;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    @Override
    public double membership(Idea idea) {
        Idea position = IdeaPlus.searchIdea(idea, "Position");
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
