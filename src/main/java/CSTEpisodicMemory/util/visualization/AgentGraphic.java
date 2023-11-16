package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.experiments.Environment;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

public class AgentGraphic{

    private final Environment env;
    private final double envW;
    private final double envH;
    private final int windowW;
    private final int windowH;

    private final int size = 30;

    public AgentGraphic(Environment env, double envW, double envH, int windowW, int windowH) {
        this.env = env;
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windowH;
    }

    protected void draw(Graphics2D g){
        List<Float> pos = env.creature.getPosition();
        float pitch = env.creature.getPitch();

        AffineTransform reset = g.getTransform();

        Path2D.Double ag = new Path2D.Double();
        ag.moveTo(size*0.712, 0);
        ag.lineTo(-0.288*size,size/2.0);
        ag.lineTo(-0.288*size, -size/2.0);
        ag.closePath();

        Line2D.Double head = new Line2D.Double(0,0,size*0.712,0);

        g.setColor(Color.BLACK);
        g.translate(pos.get(1) * windowW/envW, pos.get(0) * windowH/envH);
        String fPos = String.format("%.2f - %.2f", pos.get(0), pos.get(1));
        g.drawString(fPos, 20, 20);

        g.rotate(-pitch + Math.toRadians(90));
        g.setColor(new Color(0xFFA700));
        g.fill(ag);
        g.setColor(Color.BLACK);
        g.draw(head);

        g.setTransform(reset);
    }
}
