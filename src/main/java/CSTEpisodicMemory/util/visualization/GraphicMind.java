package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.Environment;
import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        JToolBar toolBar = new JToolBar();
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("View Graph") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Optional<Memory> selectedMem = m.getRawMemory().getAllMemoryObjects()
                        .stream().filter(m->m.getName().equalsIgnoreCase("EPLTM"))
                        .findFirst();
                if (selectedMem.isPresent()) {
                    GraphIdea gg = new GraphIdea((GraphIdea) selectedMem.get().getI());
                    GraphIdeaVisualizer tt = new GraphIdeaVisualizer(800, 700, gg);
                }
            }
        }));
        JButton button = new JButton("View");
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        toolBar.add(button);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        JComponent display = new JComponent() {
            LocationGraphic loc = new LocationGraphic(m, envW,envH,windowW,windowH);
            AgentGraphic agentGraphic = new AgentGraphic(env, envW,envH,windowW,windowH);
            GoalGraphic goalGraphic = new GoalGraphic(m, envW,envH,windowW,windowH);
            MoveGraphic moveGraphic = new MoveGraphic(m, envW,envH,windowW,windowH);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWalls(g2d);
                loc.draw2(g2d);
                moveGraphic.draw(g2d);
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
