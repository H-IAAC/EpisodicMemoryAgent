package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.experiments.Environment;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.util.Constants;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;

public class SimpleAgentExecutor extends Thread{

    private Agent agent;
    private Environment env;
    private List<Float[]> route = new ArrayList<>();
    private Random rnd = new Random();
    private boolean recursive = false;

    public SimpleAgentExecutor(Environment env){
        this.env = env;
        initializeRoute();
    }

    private void initializeRoute() {


        //Chose spawn
        int start = 0;
        switch (rnd.nextInt(2)){
            case 0:
                start = 0;
                agent = env.newAgent(0.5F, 18.5F, Constants.Color.AGENT_RED);
                break;
            case 1:
                start = 16;
                agent = env.newAgent(11.5F, 0.5F, Constants.Color.AGENT_RED);
                break;
        }

        //Chose room to visit
        int[] rooms = new int[]{2,5,8,10,13,18,20,23};
        int roomToVisit = rooms[rnd.nextInt(rooms.length)];

        //Chose exit
        int[] exits = new int[]{15,24};
        int exit = exits[rnd.nextInt(exits.length)];

        //Construct route
        route = calculateRoute(start, roomToVisit);
        route.addAll(calculateRoute(roomToVisit, exit));
    }

    public void run(){
        for (Float[] nextPos : route){
            float offX = rnd.nextFloat() * 0.5F - 0.25F;
            float offY = rnd.nextFloat() * 0.5F - 0.25F;
            agent.moveTo(nextPos[0] + offX, nextPos[1] + offY);
            while (!agent.isInOccupancyArea(nextPos[0] + offX, nextPos[1] + offY)){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        agent.setRemove(true);

        if (recursive){
            SimpleAgentExecutor recursiveAgent = new SimpleAgentExecutor(env);
            recursiveAgent.setRecursive(true);
            recursiveAgent.start();
        }
    }

    public List<Float[]> calculateRoute(int start, int end){
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

        List<Float[]> route = new LinkedList<>();
        for (Node n :solver.getPathNodes(g.getNode(String.valueOf(end)))){
            route.add(new Float[]{(Float) n.getAttribute("x"), (Float) n.getAttribute("y")});
        }
        Collections.reverse(route);
        return route;
    }

    private void addNode(Graph g, int count, double x, double y){
        Node n = g.addNode(String.valueOf(count));
        n.setAttribute("value", 1);
        n.setAttribute("x", (float)x);
        n.setAttribute("y", (float)y);
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
