package CSTEpisodicMemory.util;

import CSTEpisodicMemory.Environment;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.oracle.truffle.api.TruffleLanguage;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
                loc.draw(g2d);
                goalGraphic.draw(g2d);
                agentGraphic.draw(g2d);
            }
        };

        add(display);


    }
}
