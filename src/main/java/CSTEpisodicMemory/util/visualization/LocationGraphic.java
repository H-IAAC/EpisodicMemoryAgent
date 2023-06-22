package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class LocationGraphic {

    private Mind m;
    private double envW, envH;
    private int windowW, windowH;

    public LocationGraphic(Mind m, double envW, double envH, int windowW, int windowH) {
        this.m = m;
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windowH;
    }

    protected void draw(Graphics2D g){
        List<Memory> mems = m.getRawMemory().getAllMemoryObjects();
        List<Idea> locations = new ArrayList<>();
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("LOCATION")) {
                locations = (List<Idea>) mo.getI();
            }
        }

        g.setColor(new Color(0x99008A00, true));

        for (Idea loc : locations){
            float cx =(float) loc.get("centerX").getValue();
            float cy =(float) loc.get("centerY").getValue();
            double r = (double) loc.get("radius").getValue();

            Ellipse2D.Double draw = new Ellipse2D.Double((cy-r/2) * windowW/envW,
                    (cx-r/2) * windowH/envH,
                    r * windowW/envW,
                    r * windowH/envH);
            g.fill(draw);
        }
    }

    protected void draw2(Graphics2D g){
        List<Memory> mems = m.getRawMemory().getAllMemoryObjects();
        List<Idea> locations = new ArrayList<>();
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("extra")) {
                locations = (List<Idea>) mo.getI();
            }
        }


        if (locations != null) {
            for (Idea locNode : locations) {
                Idea loc = GraphIdea.getNodeContent(locNode);
                float cx = (float) loc.get("centerX").getValue();
                float cy = (float) loc.get("centerY").getValue();
                double r = (double) loc.get("radius").getValue();

                Ellipse2D.Double draw = new Ellipse2D.Double((cy - r / 2) * windowW / envW,
                        (cx - r / 2) * windowH / envH,
                        r * windowW / envW,
                        r * windowH / envH);

                double act = (double) locNode.get("Activation").getValue();
                Color p = new Color(0, 138, 0, (int) (240 * act) + 15);
                g.setColor(p);
                g.fill(draw);
                g.setColor(Color.BLACK);
                g.drawString(loc.getName(), (int) ((cy - r / 2) * windowW / envW), (int) ((cx - r / 2) * windowH / envH));
                g.drawString(String.format("%.4f", act), (int) ((cy - r / 2) * windowW / envW), (int) ((cx - r / 2) * windowH / envH + 30));
            }
        }

    }
}
