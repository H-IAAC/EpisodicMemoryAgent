package CSTEpisodicMemory.experiments;

import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;

public class SimpleAgentExecutor extends Thread{

    private Agent agent;
    private Environment env;
    private List<Double[]> route = new ArrayList<>();
    private Random rnd = new Random();
    private boolean recursive = false;
    private int searchJewelStep;
    private int start = -1;
    private int exit;

    public SimpleAgentExecutor(Environment env){
        this.env = env;
    }

    public SimpleAgentExecutor(SimpleAgentExecutor copy){
        this.env = copy.env;
        this.agent = copy.agent;
        this.recursive = copy.recursive;
        this.start = copy.start;
        this.exit = copy.exit;
    }

    private void initializeRoute() {
        route = new ArrayList<>();
        //Chose spawn
        if (start == -1) {
            switch (rnd.nextInt(2)) {
                case 0:
                    start = 0;
                    agent = env.newAgent(0.5F, 18.5F, Constants.Color.AGENT_RED);
                    break;
                case 1:
                    start = 16;
                    agent = env.newAgent(11.5F, 0.5F, Constants.Color.AGENT_RED);
                    break;
            }
        } else {
            if (exit == 15) {
                start = 16;
                route.add(new Double[]{11.5, 0.5});
            } else {
                start = 0;
                route.add(new Double[]{0.5, 18.5});
            }
        }

        //Chose room to visit
        int[] rooms = new int[]{2,5,8,10,13,18,20,23};
        int roomToVisit = rooms[rnd.nextInt(rooms.length)];

        //Chose exit
        int[] exits = new int[]{15,24};
        exit = exits[rnd.nextInt(exits.length)];

        //Construct route
        route.addAll(calculateRoute(start, roomToVisit));
        searchJewelStep = route.size();
        route.addAll(calculateRoute(roomToVisit, exit));
    }

    public void run(){
        initializeRoute();
        for (Double[] nextPos : route){
            if (route.indexOf(nextPos) == searchJewelStep) {
                Thing objective = null;
                List<Identifiable> vision = agent.getThingsInVision();
                for (Identifiable object : vision) {
                    if (object instanceof Thing) {
                        Thing thing = (Thing) object;
                        if (thing.isJewel()) {
                            objective = thing;
                            break;
                        }
                    }
                }

                if (objective != null) {
                    List<Double> pos = objective.getPos();
                    agent.moveTo(pos.get(0), pos.get(1));
                    while (!agent.isInOccupancyArea(pos.get(0), pos.get(1))) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    agent.sackIt(objective.getId());
                }
            }

            double offX = rnd.nextDouble() * 0.5 - 0.25;
            double offY = rnd.nextDouble() * 0.5 - 0.25;
            agent.moveTo(nextPos[0] + offX, nextPos[1] + offY);
            boolean moving = true;
            while (!agent.isInOccupancyArea(nextPos[0] + offX, nextPos[1] + offY)){
                double difx = env.creature.getPosition().get(0) - agent.getPosition().get(0);
                double dify = env.creature.getPosition().get(1) - agent.getPosition().get(1);
                if (Math.hypot(difx,dify) <= 0.5) {
                    agent.stop();
                    moving = false;
                } else if (!moving){
                    agent.moveTo(nextPos[0] + offX, nextPos[1] + offY);
                    moving = true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (recursive){
            SimpleAgentExecutor recursiveAgent = new SimpleAgentExecutor(this);
            recursiveAgent.start();
        } else {
            agent.setRemove(true);
        }
    }

    public List<Double[]> calculateRoute(int start, int end){
        Graph g = new SingleGraph("Test");

        int count = 0;
        addNode(g, count++, 0.5,18.5);  // 0
        addNode(g, count++, 6.5,18.5);  // 1
        addNode(g, count++, 6.5,17.5);  // 2
        addNode(g, count++, 10.5,18.5); // 3
        addNode(g, count++, 10.5,10.5); // 4
        addNode(g, count++, 9.5,10.5);  // 5
        addNode(g, count++, 10.5,7.5);  // 6
        addNode(g, count++, 4.5,7.5);   // 7
        addNode(g, count++, 4.5,8.5);   // 8
        addNode(g, count++, 2.5,7.5);   // 9
        addNode(g, count++, 2.5,5.5);   // 10
        addNode(g, count++, 2.5,6.5);   // 11
        addNode(g, count++, 6.5,6.5);   // 12
        addNode(g, count++, 6.5,5.5);   // 13
        addNode(g, count++, 10.5,6.5);  // 14
        addNode(g, count++, 10.5,0.5);  // 15

        addNode(g, count++, 11.5,0.5);  // 16
        addNode(g, count++, 11.5,7.5);  // 17
        addNode(g, count++, 12.5,7.5);  // 18
        addNode(g, count++, 11.5,14.5); // 19
        addNode(g, count++, 12.5,14.5); // 20
        addNode(g, count++, 11.5,19.5); // 21
        addNode(g, count++, 10.5,19.5); // 22
        addNode(g, count++, 10.5,20.5); // 23
        addNode(g, count++, 0.5,19.5);  // 24

        addNode(g, count++, 6.5,19.5);  // 25
        addNode(g, count++, 11.5,10.5); // 26
        addNode(g, count++, 10.5,14.5); // 27
        addNode(g, count++, 6.5,19.5);  // 28

        addEdge(g, 0,1);
        addEdge(g, 1,2);
        addEdge(g, 2,1);
        addEdge(g, 1,3);
        addEdge(g, 3,4);
        addEdge(g, 4,5);
        addEdge(g, 5,4);
        addEdge(g, 4,6);
        addEdge(g, 6,7);
        addEdge(g, 7,8);
        addEdge(g, 8,7);
        addEdge(g, 7,9);
        addEdge(g, 9,10);
        addEdge(g, 10,9);
        addEdge(g, 10,11);
        addEdge(g, 11,12);
        addEdge(g, 12,13);
        addEdge(g, 13,12);
        addEdge(g, 12,14);
        addEdge(g, 14,15);
        addEdge(g, 16,17);
        addEdge(g, 17,18);
        addEdge(g, 18,17);
        addEdge(g, 17,19);
        addEdge(g, 19,20);
        addEdge(g, 20,19);
        addEdge(g, 19,21);
        addEdge(g, 21,22);
        addEdge(g, 22,23);
        addEdge(g, 23,22);
        addEdge(g, 22,24);

        addEdge(g, 22,25);
        addEdge(g, 25,2);
        addEdge(g, 2,25);
        addEdge(g, 26,5);
        addEdge(g,5,26);
        addEdge(g,17,26);
        addEdge(g,3,27);
        addEdge(g,27,20);
        addEdge(g,20,27);
        addEdge(g,17,6);
        addEdge(g,14,17);
        addEdge(g, 9,11);
        addEdge(g, 6,17);

        Dijkstra solver = new Dijkstra(Dijkstra.Element.NODE, null, "value");
        solver.init(g);
        solver.setSource(String.valueOf(start));
        solver.compute();

        List<Double[]> route = new LinkedList<>();
        for (Node n :solver.getPathNodes(g.getNode(String.valueOf(end)))){
            route.add(new Double[]{(Double) n.getAttribute("x"), (Double) n.getAttribute("y")});
        }
        Collections.reverse(route);
        return route;
    }

    private void addNode(Graph g, int count, double x, double y){
        Node n = g.addNode(String.valueOf(count));
        n.setAttribute("value", 1);
        n.setAttribute("x", x);
        n.setAttribute("y", y);
    }

    private void addEdge(Graph g, int a, int b){
        String sa = String.valueOf(a);
        String sb = String.valueOf(b);
        g.addEdge(sa+sb, sa, sb, true);
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
}
