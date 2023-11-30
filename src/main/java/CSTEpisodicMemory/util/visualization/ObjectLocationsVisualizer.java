package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import br.unicamp.cst.representation.idea.Idea;
import com.oracle.truffle.regex.tregex.nodes.input.InputEndsWithNode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Timer;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class ObjectLocationsVisualizer extends JFrame {

    private GraphIdea graphIdea;
    double sx = 40;
    double sy = 40;
    private List<List<Ellipse2D.Double>> positions = new ArrayList<>();
    private List<List<Ellipse2D.Double>> objects = new ArrayList<>();
    int selectValue = 1;

    public ObjectLocationsVisualizer(GraphIdea graph) {
        this.graphIdea = graph;

        setSize((int) (sx*24)+50, (int) (sy*16)+80);
        initComponents();

        repaint();
        setVisible(true);
    }

    private void initComponents(){
        extractPositions();
        JSlider scroll = new JSlider(JSlider.HORIZONTAL, 1, positions.isEmpty()?1:positions.size(), 1);
        scroll.setMajorTickSpacing(5);
        scroll.setPaintTicks(true);
        scroll.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                selectValue = scroll.getValue();
                repaint();
            }
        });
        JCheckBox enableScroll = new JCheckBox("Enable Scroll", false);
        enableScroll.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                repaint();
            }
        });
        JPanel top = new JPanel(new BorderLayout());
        top.add(scroll, BorderLayout.CENTER);
        top.add(enableScroll, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JComponent display = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawWalls(g2d);
                if (enableScroll.isSelected()) {
                    for (int i=0;i<selectValue;i++){
                        g2d.setColor(new Color(0x002DC011 + ((Math.max(0,i+20-selectValue)*0xFF)/Math.min(20,selectValue))*0x01000000, true));
                        List<Ellipse2D.Double> ellips = positions.get(i);
                        for (Ellipse2D.Double ellip : ellips.subList(0, ellips.size()-1)) {
                            g2d.fill(ellip);
                        }
                        ellips = objects.get(i);
                        if(ellips.size()>0) {
                            g2d.setColor(new Color(0x00064AC7 + ((Math.max(0, i + 20 - selectValue) * 0xFF) / Math.min(20, selectValue)) * 0x01000000, true));
                            g2d.fill(ellips.get(0));
                            if (ellips.size() > 1) {
                                g2d.setColor(new Color(0x00F63A22 + ((Math.max(0, i + 20 - selectValue) * 0xFF) / Math.min(20, selectValue)) * 0x01000000, true));
                                g2d.fill(ellips.get(1));
                            }
                        }
                    }
                    List<Ellipse2D.Double> ellips = positions.get(selectValue-1);
                    g2d.setColor(new Color(0xFFA700));
                    g2d.fill(ellips.get(ellips.size()-1));
                } else {
                    g2d.setColor(new Color(0x3F2DC011, true));
                    for (List<Ellipse2D.Double> ellips : positions){
                        for (Ellipse2D.Double ellip : ellips.subList(0,ellips.size()-1)){
                            g2d.fill(ellip);
                        }
                    }
                }
            }


            private void drawWalls(Graphics2D g){

                List<Rectangle2D.Double> walls = Arrays.asList(
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

                g.setColor(Color.DARK_GRAY);
                for (Rectangle2D.Double wall : walls){
                    g.fill(wall);
                }
            }
        };

        add(display);
    }

    private Rectangle2D.Double makeRectangle(double x1, double y1, double x2, double y2){
        return new Rectangle2D.Double(y1*sx,x1*sy, (y2-y1+0.05)*sx,(x2-x1+0.05)*sy);
    }

    private void extractPositions(){
        for (Idea eventNode : graphIdea.getEventNodes()){
            List<Ellipse2D.Double> eventPositions = new ArrayList<>();
            List<Ellipse2D.Double> eventObjects = new ArrayList<>();
            List<Idea> rooms = graphIdea.getChildrenWithLink(eventNode, "Environment");
            if (!rooms.isEmpty()){
                Idea room = getNodeContent(rooms.get(0));
                for (Idea spatialLink : graphIdea.getChildrenWithLink(eventNode, "ObjectContext")){
                    for (Idea gridNode : graphIdea.getChildrenWithLink(spatialLink, "GridPlace")){
                        Idea gridPlace = getNodeContent(gridNode);
                        double u = (double) gridPlace.get("u").getValue();
                        double v = (double) gridPlace.get("v").getValue();
                        double[] xy = GridLocation.getInstance().toXY(u,v);
                        xy[0] += (double) room.get("center.x").getValue();
                        xy[1] += (double) room.get("center.y").getValue();
                        Ellipse2D.Double ellip = new Ellipse2D.Double(sx*xy[1],sy*xy[0], GridLocation.SCALE*sx*2, GridLocation.SCALE*sy*2);
                        eventPositions.add(ellip);
                    }
                }

                List<Idea> selfPosNode = graphIdea.getChildrenWithLink(eventNode,"GridPlace");
                if (!selfPosNode.isEmpty()){
                    Idea gridPlace = getNodeContent(selfPosNode.get(0));
                    double u = (double) gridPlace.get("u").getValue();
                    double v = (double) gridPlace.get("v").getValue();
                    double[] xy = GridLocation.getInstance().toXY(u,v);
                    xy[0] += (double) room.get("center.x").getValue();
                    xy[1] += (double) room.get("center.y").getValue();
                    Ellipse2D.Double ellip = new Ellipse2D.Double(sx*xy[1],sy*xy[0], GridLocation.SCALE*sx*2, GridLocation.SCALE*sy*2);
                    eventPositions.add(ellip);
                }

                List<Idea> initialObjNode = graphIdea.getChildrenWithLink(eventNode,"Initial");
                if (!initialObjNode.isEmpty()){
                    List<Idea> objs = graphIdea.getChildrenWithLink(initialObjNode.get(0), "Object");
                    if (!getNodeContent(objs.get(0)).getName().equals("Self")) {
                        for (Idea gridNode : graphIdea.getChildrenWithLink(initialObjNode.get(0), "GridPlace")) {
                            Idea gridPlace = getNodeContent(gridNode);
                            double u = (double) gridPlace.get("u").getValue();
                            double v = (double) gridPlace.get("v").getValue();
                            double[] xy = GridLocation.getInstance().toXY(u, v);
                            xy[0] += (double) room.get("center.x").getValue();
                            xy[1] += (double) room.get("center.y").getValue();
                            Ellipse2D.Double ellip = new Ellipse2D.Double(sx * xy[1], sy * xy[0], GridLocation.SCALE * sx * 2, GridLocation.SCALE * sy * 2);
                            eventObjects.add(ellip);
                        }
                    }
                }
                List<Idea> finalObjNode = graphIdea.getChildrenWithLink(eventNode,"Initial");
                if (!finalObjNode.isEmpty()){
                    List<Idea> objs = graphIdea.getChildrenWithLink(finalObjNode.get(0), "Object");
                    if (!getNodeContent(objs.get(0)).getName().equals("Self")) {
                        for (Idea gridNode : graphIdea.getChildrenWithLink(finalObjNode.get(0), "GridPlace")) {
                            Idea gridPlace = getNodeContent(gridNode);
                            double u = (double) gridPlace.get("u").getValue();
                            double v = (double) gridPlace.get("v").getValue();
                            double[] xy = GridLocation.getInstance().toXY(u, v);
                            xy[0] += (double) room.get("center.x").getValue();
                            xy[1] += (double) room.get("center.y").getValue();
                            Ellipse2D.Double ellip = new Ellipse2D.Double(sx * xy[1], sy * xy[0], GridLocation.SCALE * sx * 2, GridLocation.SCALE * sy * 2);
                            eventObjects.add(ellip);
                        }
                    }
                }
            }
            objects.add(eventObjects);
            positions.add(eventPositions);
        }
    }
}
