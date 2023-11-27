package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GridLocation;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class MoveGraphic {

    private final Mind m;
    private final double envW;
    private final double envH;
    private final int windowW;
    private final int windowH;

    public MoveGraphic(Mind m, double envW, double envH, int windowW, int windowH) {
        this.m = m;
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windowH;
    }

    public void draw(Graphics2D g){
        List<Memory> mems = m.getRawMemory().getAllMemoryObjects();
        List<Idea> extra = null;
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("extra")) {
                extra = (List<Idea>) (((MemoryObject) mo).getI());
            }
        }

        if (extra != null && extra.size()>1){
            Idea room = extra.get(0);
            for (Idea grid : extra.subList(1, extra.size())){
                double[] destPos = GridLocation.getInstance().toXY((double) grid.get("u").getValue(), (double) grid.get("v").getValue());
                double px = destPos[0] + (double) room.get("center.x").getValue();
                double py = destPos[1] + (double) room.get("center.y").getValue();

                Rectangle2D.Double goal = new Rectangle2D.Double(-5, -5, 10, 10);

                AffineTransform reset = g.getTransform();

                g.setColor(new Color(0x45FF1B));
                g.translate(py * windowW/envW, px * windowH/envH);
                g.rotate(Math.toRadians(45));

                g.fill(goal);

                g.setTransform(reset);
            }
        }

        Idea move = null;
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("LEGS")) {
                move = (Idea) (((MemoryContainer) mo).getI());
            }
        }

        if (move !=null){
            float px = (float) move.get("X").getValue() +0.1F;
            float py = (float) move.get("Y").getValue();

            Rectangle2D.Double goal = new Rectangle2D.Double(-5, -5, 10, 10);

            AffineTransform reset = g.getTransform();

            g.setColor(new Color(0xFFA61B));
            g.translate(py * windowW/envW, px * windowH/envH);
            g.rotate(Math.toRadians(45));

            g.fill(goal);

            g.setTransform(reset);

        }
    }
}
