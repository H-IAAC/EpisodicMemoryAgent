package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.experiments.Environment;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class GraphicMind extends JFrame {

    private final double envW;
    private final double envH;
    private final int windowW;
    private final int windowH;
    private JComponent window;
    private final Mind m;
    private final Environment env;

    private LocationGraphic loc;
    private java.util.Timer t;

    public GraphicMind(Mind m, Environment env, double envW, double envH, int windowW, int windoH, int experiment) {
        this.envW = envW;
        this.envH = envH;
        this.windowW = windowW;
        this.windowH = windoH;
        this.m = m;
        this.env = env;

        initComponents(experiment);

        t = new Timer();
        GraphicMind.mainTimerTask tt = new mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 200, 100);

        setVisible(true);
    }

    static class mainTimerTask extends TimerTask {

        GraphicMind l;

        public mainTimerTask(GraphicMind ll) {
            l = ll;
        }

        public void run() {
            l.repaint();
        }
    }

    public void stop(){
        t.cancel();
        setVisible(false);
    }

    private void initComponents(int experiment){
        setSize(windowW + 180,windowH-50);

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
                    //GraphIdea gg = new GraphIdea(((Idea) selectedMem.get().getI()).getL().get(0).get("Story"));
                    GraphIdeaVisualizer tt = new GraphIdeaVisualizer(800, 700, gg);
                    //GraphstreamVisualizer tt = new GraphstreamVisualizer(800, 700, gg);
                }
            }
        }));
        popup.add(new JMenuItem(new AbstractAction("View Plot") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CategoriesPerEventView cc = new CategoriesPerEventView(m);
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
            //LocationGraphic loc = new LocationGraphic(m, envW,envH,windowW,windowH);
            AgentGraphic agentGraphic = new AgentGraphic(env, envW,envH,windowW,windowH);
            GoalGraphic goalGraphic = new GoalGraphic(m, envW,envH,windowW,windowH);
            MoveGraphic moveGraphic = new MoveGraphic(m, envW,envH,windowW,windowH);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWalls(g2d, experiment);
                //loc.draw2(g2d);
                moveGraphic.draw(g2d);
                if (experiment != 0)
                    goalGraphic.draw(g2d);
                agentGraphic.draw(g2d);
            }

            private void drawWalls(Graphics2D g, int experiment){
                double sx = windowW/envW;
                double sy = windowH/envH;

                List<Rectangle2D.Double> walls = new ArrayList<>();
                if (experiment == 0) {
                    walls = Arrays.asList(
                            new Rectangle2D.Double(0 * sx, 0 * sy, 0.05 * sx, 8.05 * sy),
                            new Rectangle2D.Double(0 * sx, 0 * sy, 3.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(0 * sx, 7.95 * sy, 3.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(3 * sx, 0 * sy, 4.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(3 * sx, 1 * sy, 4.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(7 * sx, 0 * sy, 3.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(7 * sx, 1 * sy, 0.05 * sx, 7.05 * sy),
                            new Rectangle2D.Double(7 * sx, 8 * sy, 3.05 * sx, 0.05 * sy),
                            new Rectangle2D.Double(10 * sx, 0 * sy, 0.05 * sx, 8.05 * sy),
                            new Rectangle2D.Double(3 * sx, 1 * sy, 0.05 * sx, 7.05 * sy)
                    );
                } else if (experiment == 1) {
                    walls = Arrays.asList(
                            makeRectangle(0,2,0,6),
                            makeRectangle(0,2,4,2),
                            makeRectangle(4, 2, 4, 6),
                            makeRectangle(3, 6, 4, 6),
                            makeRectangle(0, 6, 2, 6),
                            makeRectangle(5,0,5,6),
                            makeRectangle(5,0,9,0),
                            makeRectangle(9,0,9,6),
                            makeRectangle(7,6,9,6),
                            makeRectangle(5,6,6,6),
                            makeRectangle(3,8,3,12),
                            makeRectangle(3,12,7,12),
                            makeRectangle(3,8,4,8),
                            makeRectangle(5,8,7,8),
                            makeRectangle(7,8,7,12),
                            makeRectangle(2,6,2,8),
                            makeRectangle(3,6,6,6),
                            makeRectangle(7,6,10,6),
                            makeRectangle(2,8,4,8),
                            makeRectangle(5,8,10,8),
                            makeRectangle(10,0,10,6),
                            makeRectangle(12,0,12,7),
                            makeRectangle(12,8,12,14),
                            makeRectangle(12,15,12,20),
                            makeRectangle(10,8,10,10),
                            makeRectangle(10,11,10,18),
                            makeRectangle(0,18,6,18),
                            makeRectangle(7,18,10,18),
                            makeRectangle(0,20,10,20),
                            makeRectangle(11,20,12,20),
                            makeRectangle(12,5,12,7),
                            makeRectangle(12,8,12,11),
                            makeRectangle(12,5,16,5),
                            makeRectangle(16,5,16,11),
                            makeRectangle(12,11,16,11),
                            makeRectangle(7,8,7,14),
                            makeRectangle(7,8,10,8),
                            makeRectangle(10,8,10,10),
                            makeRectangle(10,11,10,14),
                            makeRectangle(7,14,10,14),
                            makeRectangle(12,12,12,14),
                            makeRectangle(12,15,12,16),
                            makeRectangle(12,12,16,12),
                            makeRectangle(16,12,16,16),
                            makeRectangle(12,16,16,16),
                            makeRectangle(9,20,10, 20),
                            makeRectangle(9,20,9,  24),
                            makeRectangle(9,24,15, 24),
                            makeRectangle(15,20,15,24),
                            makeRectangle(11,20,15,20),
                            makeRectangle(4,18,6,18),
                            makeRectangle(7,18,10,18),
                            makeRectangle(10,14,10,18),
                            makeRectangle(4,14,10,14),
                            makeRectangle(4,14,4,18)
                    );
                }

                g.setColor(Color.DARK_GRAY);
                for (Rectangle2D.Double wall : walls){
                    g.fill(wall);
                }
            }
        };

        add(display);

    }

    private Rectangle2D.Double makeRectangle(double x1, double y1, double x2, double y2){
        double sx = windowW/envH;
        double sy = windowH/envW;
        return new Rectangle2D.Double(y1*sx,x1*sy, (y2-y1+0.05)*sx,(x2-x1+0.05)*sy);
    }
}
