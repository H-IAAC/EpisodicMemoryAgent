package CSTEpisodicMemory.util.visualization;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class GoalGraphic {

    private final Mind m;
    private final double envW;
    private final double envH;
    private final int windowW;
    private final int windowH;

    public GoalGraphic(Mind m, double envW, double envH, int windowW, int windowH) {
        this.m = m;
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windowH;
    }

    public void draw(Graphics2D g){

        List<Memory> mems = m.getRawMemory().getAllMemoryObjects();
        Idea impulse = null;
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                impulse = (Idea) (((MemoryContainer) mo).getI());
            }
        }

        if (impulse !=null){
            if (impulse.get("State.Self.Position") != null) {
                float px = (float) impulse.get("State.Self.Position.X").getValue();
                float py = (float) impulse.get("State.Self.Position.Y").getValue();

                Rectangle2D.Double goal = new Rectangle2D.Double(-5, -5, 10, 10);

                AffineTransform reset = g.getTransform();

                g.setColor(new Color(0x0A84D2));
                g.translate(py * windowW/envW, px * windowH/envH);
                g.rotate(Math.toRadians(45));

                g.fill(goal);

                g.setTransform(reset);
            }
        }
    }
}
