package CSTEpisodicMemory.util;

import CSTEpisodicMemory.Environment;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.oracle.truffle.api.TruffleLanguage;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class GraphicMind extends JFrame {

    private double envW, envH;
    private int windowW, windowH;
    private JComponent window;
    private Mind m;
    private Environment env;

    private LocationGraphic loc;

    public GraphicMind(Mind m, Environment env, double envW, double envH, int windowW, int windoH) {
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windoH;
        this.m = m;
        this.env = env;

        initComponents();

        java.util.Timer t = new Timer();
        GraphicMind.mainTimerTask tt = new GraphicMind.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 200, 100);

        setVisible(true);
    }

    class mainTimerTask extends TimerTask {

        GraphicMind l;

        public mainTimerTask(GraphicMind ll) {
            l = ll;
        }

        public void run() {
            l.repaint();
        }
    }

    private void initComponents(){
        setSize(windowW,windowH);

        JComponent display = new JComponent() {
            LocationGraphic loc = new LocationGraphic(m, envW,envH,windowW,windowH);
            AgentGraphic agentGraphic = new AgentGraphic(env, envW,envH,windowW,windowH);
            GoalGraphic goalGraphic = new GoalGraphic(m, envW,envH,windowW,windowH);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWalls(g2d);
                loc.draw(g2d);
                goalGraphic.draw(g2d);
                agentGraphic.draw(g2d);
            }

            protected void drawWalls(Graphics2D g){
                double sx = windowW/envW;
                double sy = windowH/envH;
                List<Rectangle2D.Double> walls = Arrays.asList(
                        new Rectangle2D.Double(0*sx, 0*sy, 0.05*sx, 8.05*sy),
                        new Rectangle2D.Double(0*sx, 0*sy, 3.05*sx, 0.05*sy),
                        new Rectangle2D.Double(0*sx, 7.95*sy, 3.05*sx, 0.05*sy),
                        new Rectangle2D.Double(3*sx, 0*sy, 4.05*sx, 0.05*sy),
                        new Rectangle2D.Double(3*sx, 1*sy, 4.05*sx, 0.05*sy),
                        new Rectangle2D.Double(7*sx, 0*sy, 3.05*sx, 0.05*sy),
                        new Rectangle2D.Double(7*sx, 1*sy, 0.05*sx, 7.05*sy),
                        new Rectangle2D.Double(7*sx, 8*sy, 3.05*sx, 0.05*sy),
                        new Rectangle2D.Double(10*sx, 0*sy, 0.05*sx, 8.05*sy),
                        new Rectangle2D.Double(3*sx, 1*sy, 0.05*sx, 7.05*sy)
                );

                g.setColor(Color.DARK_GRAY);
                for (Rectangle2D.Double wall : walls){
                    g.fill(wall);
                }
            }
        };

        add(display);


    }
}
