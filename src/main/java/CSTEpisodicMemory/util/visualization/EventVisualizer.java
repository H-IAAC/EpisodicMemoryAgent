package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class EventVisualizer extends JFrame {

    private int width;
    private int heigth;
    private List<Idea> events;
    private Mind a;

    public EventVisualizer(int width, int heigth, Mind m) {
        this.width = width;
        this.heigth = heigth;
        this.a = m;

        initComponents();

        java.util.Timer t = new Timer();
        EventVisualizer.mainTimerTask tt = new EventVisualizer.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 250, 50);

        setVisible(true);
    }

    private void initComponents(){
        setSize(width,heigth);

        JPanel panel = new JPanel(){

            protected int w = 0;
            @Override
            public Dimension getPreferredSize(){
                if (width > w)
                    return new Dimension(width, heigth);
                else
                    return new Dimension(w, heigth);
            }

            @Override
            protected void paintComponent(Graphics g){

                Optional<Memory> selectedMem = a.getRawMemory().getAllMemoryObjects()
                        .stream().filter(m->m.getName().equalsIgnoreCase("EPLTM"))
                        .findFirst();
                if (selectedMem.isPresent()) {
                    GraphIdea gg = new GraphIdea((GraphIdea) selectedMem.get().getI());
                    events = new ArrayList<>(gg.getEventNodes()).stream().map(e->GraphIdea.getNodeContent(e)).collect(Collectors.toList());
                }

                Graphics2D g2d = (Graphics2D) g;
                events.sort(Comparator.comparingLong(e->(long) e.get("Start").getValue()));
                long firstTimeStamp = 0;
                List<Long> lastEnd = new ArrayList<>();
                int newW = 0;
                int lastLevel = 0;
                for (Idea event : events){
                    long start = (long) event.get("Start").getValue();
                    long end = (long) event.get("End").getValue();

                    if (firstTimeStamp == 0)
                        firstTimeStamp = start;

                    start -= firstTimeStamp;
                    end -= firstTimeStamp;
                    long finalStart = start;
                    int level = (int) lastEnd.stream().filter(l-> finalStart <l).count();
                    if (level > 0 && level <= lastLevel)
                        level = lastLevel + 1;
                    int startY =  level * 25 + 5;
                    lastLevel = level;
                    double scale = 0.2;
                    Rectangle2D.Double eventSpan = new Rectangle2D.Double(start * scale,
                            startY,
                            (end-start) * scale - 10,
                            20);
                    g2d.setColor(Color.YELLOW);
                    g2d.fill(eventSpan);
                    g2d.setColor(Color.BLACK);
                    g2d.draw(eventSpan);
                    g2d.drawString(event.getName(), (float) (start*scale) + 5, startY+13);
                    newW += (end - start) / 5;
                    lastEnd.add(end);
                }
                w = newW;
            }
        };

        JScrollPane scroll = new JScrollPane(panel);
        add(scroll);
        pack();
        setVisible(true);
    }

    class mainTimerTask extends TimerTask {

        EventVisualizer l;

        public mainTimerTask(EventVisualizer ll) {
            l = ll;
        }

        public void run() {
            l.repaint();
        }
    }
}
