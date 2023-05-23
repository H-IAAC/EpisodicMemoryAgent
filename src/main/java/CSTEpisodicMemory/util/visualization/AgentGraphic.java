package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.Environment;
import br.unicamp.cst.core.entities.Mind;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

public class AgentGraphic{

    private Environment env;
    private double envW, envH;
    private int windowW, windowH;

    private int size = 30;

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

        g.setColor(new Color(0xFFA700));

        Path2D.Double ag = new Path2D.Double();
        ag.moveTo(size*0.712, 0);
        ag.lineTo(-0.288*size,size/2);
        ag.lineTo(-0.288*size, -size/2);
        ag.closePath();

        Line2D.Double head = new Line2D.Double(0,0,size*0.712,0);

        g.translate(pos.get(1) * windowW/envW, pos.get(0) * windowH/envH);
        g.rotate(-pitch + Math.toRadians(90));
        g.fill(ag);
        g.setColor(Color.BLACK);
        g.draw(head);

        g.setTransform(reset);
    }
}
