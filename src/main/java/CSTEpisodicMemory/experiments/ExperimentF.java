package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.AgentMind;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.Vector2D;
import CSTEpisodicMemory.util.visualization.*;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.AgentMind.constructRoomCategory;
import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class ExperimentF implements Experiment {

    AgentMind mind;
    Environment env;
    IdeaVisualizer visu;
    //GraphicMind gm;
    List<SimpleAgentExecutor> actors = new ArrayList<>();
    public void run() {
        env = new EnvironmentB(16, 24);
        mind = new AgentMind(env, createRoomsCategories());
        visu = new IdeaVisualizer(mind);
        for (String mem : mind.getRawMemory().getAllMemoryObjects().stream().map(Memory::getName).collect(Collectors.toList()))
            visu.addMemoryWatch(mem);
        visu.setMemoryWatchPrintLevel("EPLTM", 4);
        visu.setMemoryWatchPrintLevel("EVENTS", 5);
        visu.setVisible(true);
        //gm = new GraphicMind(mind, env, 16, 24, 16 * 30, 24 * 30, 1);
        double start = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            SimpleAgentExecutor execNPC = new SimpleAgentExecutor(env);
            execNPC.setRecursive(true);
            execNPC.start();
            actors.add(execNPC);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExperimentF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Map<Integer,Idea> testEpisodes = new HashMap<>();
        while (env.creature.getFuel() > 100) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.printf("Skip sleep");
            }

            synchronized (mind.storyMO) {
                Idea stories = (Idea) mind.storyMO.getI();
                Idea lastStory = stories.getL().get(0);
                int lastEp = (int) lastStory.getValue();
                testEpisodes.put(lastEp, IdeaHelper.cloneIdea(lastStory.get("Story")));
            }
        }

        System.out.println("\n\n###### Final Time: " + (System.currentTimeMillis() - start) + " ######\n");

        SpatialLinkPerEventView cc = new SpatialLinkPerEventView(mind, true);
        ObjectCategoryPerEventView ss = new ObjectCategoryPerEventView(mind, true);
        GridPerEventView ww = new GridPerEventView(mind, true);
        /*
        Optional<Memory> selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("EPLTM"))
                .findFirst();
        if (selectedMem.isPresent()) {
            GraphIdea gg = new GraphIdea((GraphIdea) selectedMem.get().getI());
            ObjectLocationsVisualizer oo = new ObjectLocationsVisualizer(gg);
        }*/

        //testMemoryRetrieval(mind);

        //compareReconstruction(mind, new ArrayList<>(testEpisodes.values()));
        System.out.println("FINISH");
        shutdown();
    }

    @Override
    public void shutdown() {
        for (SimpleAgentExecutor actor : actors){
            actor.setRecursive(false);
        }
        mind.shutDown();
        env.stopSimulation();
        //gm.stop();
        visu.setVisible(false);
        System.exit(0);
    }

    private static void compareReconstruction(AgentMind mind, List<Idea> testEpisodes) {
        int successes = 0;

        for (Idea testEpisode : testEpisodes) {
            List<String> temporalRelations = Arrays.asList("Before", "Meet", "Between", "Overlap", "Start", "During", "Finish", "Equal");
            GraphIdea cue = new GraphIdea(new Idea("Cue"));
            GraphIdea story = new GraphIdea(testEpisode);
            for (Idea event : story.getEventNodes()) {
                Idea eventContent = getNodeContent(event);
                Idea sourceNode = cue.insertEventNode(eventContent);
                for (Map.Entry<String, List<Idea>> link : story.getSuccesors(event).entrySet()) {
                    if (temporalRelations.contains(link.getKey())) {
                        for (Idea dest : link.getValue()) {
                            Idea destContent = getNodeContent(dest);
                            Idea destNode = cue.insertEventNode(destContent);
                            cue.insertLink(sourceNode, destNode, link.getKey());
                        }
                    }
                }
            }

            System.out.println("Event Cues: " + cue.getEventNodes().size());
            mind.recallMO.setI(null);
            mind.cueMO.setI(cue);

            long start = System.currentTimeMillis();
            while (mind.recallMO.getI() == null) {
                try {
                    Thread.sleep(100);
                    System.out.print("Recalling " + (System.currentTimeMillis() - start) / 1000.0 + "ms\r");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(" ");

            GraphIdea storyRecall = (GraphIdea) mind.recallMO.getI();
            System.out.println("---Recovered Episodes for Agent 21----");
            System.out.println("Episodes: " + storyRecall.getEpisodeNodes().size());
            System.out.println("Events: " + storyRecall.getEventNodes().size());

            PrintWriter out;
            try {
                out = new PrintWriter("cue_data");
                String csv = cue.toCSV();
                out.println(csv);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                out = new PrintWriter("testEp_data");
                String csv = story.toCSV();
                out.println(csv);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                out = new PrintWriter("recalled_data");
                String csv = storyRecall.toCSV();
                out.println(csv);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            boolean sameNumEvents = story.getEventNodes().size() == storyRecall.getEventNodes().size();

            boolean haveAllEvents = true;
            for (Idea event : story.getEventNodes()) {
                boolean found = false;
                for (Idea eventRecall : storyRecall.getEventNodes())
                    if (getNodeContent(event).getName().equals(getNodeContent(eventRecall).getName())) {
                        found = true;
                        break;
                    }
                if (!found) {
                    haveAllEvents = false;
                    break;
                }
            }

            if (sameNumEvents && haveAllEvents)
                successes++;
        }

        System.out.println("Successful retrieval of [ " + successes + " / " + testEpisodes.size() + " ]");

    }

    private static void testMemoryRetrieval(AgentMind mind) {
        Idea knownAgents = (Idea) mind.knownAgentsMO.getI();
        System.out.println("Unique known agents: " + knownAgents.getL().size());
        for (Idea knownAgent : knownAgents.getL()) {
            Idea cue = new Idea("EventTest", null, "Episode", 1);
            Idea step1 = new Idea("", 1, "TimeStep", 1);
            Idea step2 = new Idea("", 2, "TimeStep", 1);
            String agentName = knownAgent.getName();
            int agentId = (int) knownAgent.get("ID").getValue();
            Idea object = new Idea(agentName, "AGENT", "AbstractObject", 1);
            object.add(new Idea("ID", agentId, "Property", 1));
            step1.add(object);
            step2.add(object);
            cue.add(step1);
            cue.add(step2);

            GraphIdea cueGraph = new GraphIdea(new Idea("Cue"));
            cueGraph.insertEventNode(cue);
            mind.cueMO.setI(cueGraph);

            long start = System.currentTimeMillis();
            while (mind.recallMO.getI() == null) {
                try {
                    Thread.sleep(100);
                    System.out.print("Recalling " + (System.currentTimeMillis() - start) / 1000.0 + "ms\r");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(" ");

            GraphIdea storyRecall = (GraphIdea) mind.recallMO.getI();

            System.out.println("---Recovered Episodes for Agent " + agentId +"----");
            System.out.println("Time: "+ (System.currentTimeMillis()-start));
            System.out.println("Episodes: " + storyRecall.getEpisodeNodes().size());
            System.out.println("Events: " + storyRecall.getEventNodes().size());
            String eee = "";
            for (Idea ep : storyRecall.getEpisodeNodes()){
                for (Idea ev : storyRecall.getEpisodeSubGraphCopy(ep).getEventNodes()){
                    eee += getNodeContent(ep).getName() + "|" + getNodeContent(ev).getName() + "|" + getNodeContent(ev).getValue().toString() + " ";
                }
            }
            System.out.println("Event types: " + eee);
        }

    }

    private static List<Idea> createRoomsCategories() {
        List<Idea> roomsCategoriesIdea = new ArrayList<>();


        roomsCategoriesIdea.add(constructRoomCategory("RoomA", // 0
                new Vector2D(0, 2),
                new Vector2D(4, 6)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomB", // 1
                new Vector2D(5, 0),
                new Vector2D(9, 6)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomD", // 2
                new Vector2D(3, 8),
                new Vector2D(7, 12)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomE", // 3
                new Vector2D(7, 8),
                new Vector2D(10, 14)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomG", // 4
                new Vector2D(12, 5),
                new Vector2D(16, 11)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomH", // 5
                new Vector2D(12, 12),
                new Vector2D(16, 16)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomK", // 6
                new Vector2D(9, 20),
                new Vector2D(15, 24)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomI", // 7
                new Vector2D(4, 14),
                new Vector2D(10, 18)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomJ", // 8
                new Vector2D(0, 18),
                new Vector2D(10, 20)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomF", // 9
                new Vector2D(10, 0),
                new Vector2D(12, 20)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomC", // 10
                new Vector2D(2, 6),
                new Vector2D(10, 8)));

        roomsCategoriesIdea.get(0).get("Adjacent").add(roomsCategoriesIdea.get(10));
        roomsCategoriesIdea.get(1).get("Adjacent").add(roomsCategoriesIdea.get(10));
        roomsCategoriesIdea.get(2).get("Adjacent").add(roomsCategoriesIdea.get(10));
        roomsCategoriesIdea.get(3).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(4).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(5).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(6).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(7).get("Adjacent").add(roomsCategoriesIdea.get(8));
        roomsCategoriesIdea.get(8).get("Adjacent").add(roomsCategoriesIdea.get(7));
        roomsCategoriesIdea.get(8).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(8));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(6));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(5));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(3));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(4));
        roomsCategoriesIdea.get(9).get("Adjacent").add(roomsCategoriesIdea.get(10));
        roomsCategoriesIdea.get(10).get("Adjacent").add(roomsCategoriesIdea.get(9));
        roomsCategoriesIdea.get(10).get("Adjacent").add(roomsCategoriesIdea.get(0));
        roomsCategoriesIdea.get(10).get("Adjacent").add(roomsCategoriesIdea.get(1));
        roomsCategoriesIdea.get(10).get("Adjacent").add(roomsCategoriesIdea.get(2));

        Idea exit1 = new Idea("Exit1", null, "Link", 1);
        exit1.add(new Idea("Room", roomsCategoriesIdea.get(10)));
        exit1.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(0.5, 2.5)));
        roomsCategoriesIdea.get(0).get("Exits").add(exit1);

        Idea exit2 = new Idea("Exit2", null, "Link", 1);
        exit2.add(new Idea("Room", roomsCategoriesIdea.get(10)));
        exit2.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-0.5, 3.5)));
        roomsCategoriesIdea.get(1).get("Exits").add(exit2);

        Idea exit3 = new Idea("Exit3", null, "Link", 1);
        exit3.add(new Idea("Room", roomsCategoriesIdea.get(10)));
        exit3.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-0.5, -2.5)));
        roomsCategoriesIdea.get(2).get("Exits").add(exit3);

        Idea exit4 = new Idea("Exit4", null, "Link", 1);
        exit4.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit4.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(2.5, -0.5)));
        roomsCategoriesIdea.get(3).get("Exits").add(exit4);

        Idea exit5 = new Idea("Exit5", null, "Link", 1);
        exit5.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit5.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-2.5, -0.5)));
        roomsCategoriesIdea.get(4).get("Exits").add(exit5);

        Idea exit6 = new Idea("Exit6", null, "Link", 1);
        exit6.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit6.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-2.5, 0.5)));
        roomsCategoriesIdea.get(5).get("Exits").add(exit6);

        Idea exit7 = new Idea("Exit7", null, "Link", 1);
        exit7.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit7.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-1.5, -2.5)));
        roomsCategoriesIdea.get(6).get("Exits").add(exit7);

        Idea exit8 = new Idea("Exit8", null, "Link", 1);
        exit8.add(new Idea("Room", roomsCategoriesIdea.get(8)));
        exit8.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-0.5, 2.5)));
        roomsCategoriesIdea.get(7).get("Exits").add(exit8);

        Idea exit9 = new Idea("Exit9", null, "Link", 1);
        exit9.add(new Idea("Room", roomsCategoriesIdea.get(7)));
        exit9.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(1.5, -1.5)));
        roomsCategoriesIdea.get(8).get("Exits").add(exit9);

        Idea exit10 = new Idea("Exit10", null, "Link", 1);
        exit10.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit10.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(5.5, 0)));
        roomsCategoriesIdea.get(8).get("Exits").add(exit10);

        Idea exit11 = new Idea("Exit11", null, "Link", 1);
        exit11.add(new Idea("Room", roomsCategoriesIdea.get(8)));
        exit11.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-1.5, 9)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit11);

        Idea exit12 = new Idea("Exit12", null, "Link", 1);
        exit12.add(new Idea("Room", roomsCategoriesIdea.get(6)));
        exit12.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-0.5, 10.5)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit12);

        Idea exit13 = new Idea("Exit13", null, "Link", 1);
        exit13.add(new Idea("Room", roomsCategoriesIdea.get(5)));
        exit13.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(1.5, 4.5)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit13);

        Idea exit14 = new Idea("Exit14", null, "Link", 1);
        exit14.add(new Idea("Room", roomsCategoriesIdea.get(3)));
        exit14.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-1.5, 0.5)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit14);

        Idea exit15 = new Idea("Exit15", null, "Link", 1);
        exit15.add(new Idea("Room", roomsCategoriesIdea.get(4)));
        exit15.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(1.5, -2.5)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit15);

        Idea exit16 = new Idea("Exit16", null, "Link", 1);
        exit16.add(new Idea("Room", roomsCategoriesIdea.get(10)));
        exit16.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-1.5, -3)));
        roomsCategoriesIdea.get(9).get("Exits").add(exit16);

        Idea exit17 = new Idea("Exit17", null, "Link", 1);
        exit17.add(new Idea("Room", roomsCategoriesIdea.get(0)));
        exit17.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-3.5, -1.5)));
        roomsCategoriesIdea.get(10).get("Exits").add(exit17);

        Idea exit18 = new Idea("Exit18", null, "Link", 1);
        exit18.add(new Idea("Room", roomsCategoriesIdea.get(9)));
        exit18.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(4.5, 0)));
        roomsCategoriesIdea.get(10).get("Exits").add(exit18);

        Idea exit19 = new Idea("Exit19", null, "Link", 1);
        exit19.add(new Idea("Room", roomsCategoriesIdea.get(1)));
        exit19.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(0.5, -1.5)));
        roomsCategoriesIdea.get(10).get("Exits").add(exit19);

        Idea exit20 = new Idea("Exit20", null, "Link", 1);
        exit20.add(new Idea("Room", roomsCategoriesIdea.get(2)));
        exit20.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-1.7, 1.5)));
        roomsCategoriesIdea.get(10).get("Exits").add(exit20);

        return roomsCategoriesIdea;
    }
}
