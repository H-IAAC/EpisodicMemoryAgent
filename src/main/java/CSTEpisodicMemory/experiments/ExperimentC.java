package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.AgentMind;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.Vector2D;
import CSTEpisodicMemory.util.visualization.*;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.io.rest.RESTServer;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.AgentMind.constructRoomCategory;
import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class ExperimentC implements Experiment{

    public static long starTime = 0;
    Environment env;
    private AgentMind mind;
    GraphicMind gm;
    EventVisualizer ev;

    public void run(){
        starTime = System.currentTimeMillis();
        env = new EnvironmentA(8,10);

        mind = new AgentMind(env, createRoomsCategories(), false, true);

        IdeaVisualizer visu = new IdeaVisualizer(mind);
        for (String mem : mind.getRawMemory().getAllMemoryObjects().stream().map(Memory::getName).collect(Collectors.toList()))
            visu.addMemoryWatch(mem);
        visu.setMemoryWatchPrintLevel("EPLTM", 6);
        visu.setMemoryWatchPrintLevel("EVENTS", 5);
        visu.setVisible(true);

        //gm = new GraphicMind(mind, env, 8, 10, 700, 700, 0);
        //ev = new EventVisualizer(1000, 200, mind);

        //Show a moving agent
        Agent npcA = env.newAgent(1,2F, Constants.Color.AGENT_RED);
        Agent npcB = env.newAgent(1,1F, Constants.Color.AGENT_RED);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        StringBuilder finalReport = new StringBuilder();
        finalReport.append("Nodes Events Objs | Nodes Events Objs SPLink\n");

        for (int i=0;i<4;i++) {
            npcA.moveTo(7F, 2F);

            sleep();
            if (i>0){

                GraphIdea epltm = new GraphIdea((GraphIdea) mind.EPLTMO.getI());
                finalReport.append(epltm.getNodes().size() + " ");
                finalReport.append(epltm.getEventNodes().size() + " ");
                finalReport.append(epltm.getObjectNodes().size() + " ");
                long links = epltm.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count();
                finalReport.append(links + "\n");
            }
            npcB.moveTo(7F, 1F);

            sleep();
            npcA.moveTo(1F, 2F);
            npcB.moveTo(1F, 1F);

            sleep();
            npcA.moveTo(7F, 1F);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            npcB.moveTo(7F, 2F);

            sleep();
            npcA.moveTo(1F, 2F);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            npcB.moveTo(1F, 1F);

            sleep();

            //Force Episode segmentation
            Memory selectedMem = mind.getRawMemory().getAllMemoryObjects()
                    .stream().filter(m -> m.getName().equalsIgnoreCase("BOUNDARIES"))
                    .findFirst().get();

            Idea boundaries = (Idea) selectedMem.getI();
            Idea forcedBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
            long boundary = System.currentTimeMillis();
            forcedBoundary.add(new Idea("TimeStamp", boundary, "Property", 1));
            boundaries.add(forcedBoundary);
            selectedMem.setI(boundaries);
            System.out.println("Force episode Segmentation - " + (boundary - starTime));


            selectedMem = mind.getRawMemory().getAllMemoryObjects()
                    .stream().filter(m -> m.getName().equalsIgnoreCase("STORY"))
                    .findFirst().get();
            GraphIdea story = new GraphIdea(((Idea) selectedMem.getI()).getL().get(0).get("Story"));

            System.out.println("####Report####");
            System.out.println("---Encoding---");
            System.out.println("Número de nós: " + story.getNodes().size());
            System.out.println("Detected Events: " + story.getEventNodes().size());
            System.out.println("Contextos: " + story.getContextNodes().size());
            System.out.println("Objetos: " + story.getObjectNodes().size());
            System.out.println("Links espaciais: " + story.getContextNodes().stream().filter(e -> getNodeContent(e).getName().contains("SpatialLink")).count());
            System.out.println("Número de células de grade: " + story.getLocationNodes().size());

            finalReport.append(story.getNodes().size() + " ");
            finalReport.append(story.getEventNodes().size() + " ");
            finalReport.append(story.getObjectNodes().size() + " | ");

            Thing food  = env.world.createThing(Constants.FoodTypes.NPFOOD, 10, 10);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            env.creature.eatIt(food);
            food  = env.world.createThing(Constants.FoodTypes.NPFOOD, 10, 10);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            env.creature.eatIt(food);

        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        env.creature.moveTo(4F, 2.1F);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        GraphIdea epltm = new GraphIdea((GraphIdea) mind.EPLTMO.getI());
        finalReport.append(epltm.getNodes().size() + " ");
        finalReport.append(epltm.getEventNodes().size() + " ");
        finalReport.append(epltm.getObjectNodes().size() + " ");
        long links = epltm.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count();
        finalReport.append(links + "\n");

        mind.shutDown();

        System.out.println(finalReport);
        SpatialLinkPerEventView spatialLinkPerEventView = new SpatialLinkPerEventView(mind, true);
        ObjectCategoryPerEventView objectCategoryPerEventView = new ObjectCategoryPerEventView(mind, true);
        GridPerEventView gridPerEventView = new GridPerEventView(mind, true);

        String scores = "";
        for (Idea ep : epltm.getEpisodeNodes()){
            scores += "\n" + ep.getId();
            for (Idea ep2 : epltm.getEpisodeNodes()){
                double ss = IdeaHelper.scoreSimilarity(ep, ep2);
                scores += String.format("\t %.4f", ss);
            }
        }
        System.out.println(scores);
        System.out.println(epltm.getObjectNodes().stream().map(e->getNodeContent(e).getValue().toString()).toList());
        System.out.println(IdeaHelper.generateEPLTMDescription(epltm));
        shutdown();
        //visu.setVisible(false);
        //gm.stop();
        //env.stopSimulation();

    }

    private void sleep(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Idea> createRoomsCategories(){
        List<Idea> roomsCategoriesIdea = new ArrayList<>();
        Idea roomA = constructRoomCategory("RoomA",
                new Vector2D(0, 0),
                new Vector2D(8, 3));
        Idea roomB = constructRoomCategory("RoomB",
                new Vector2D(0, 3),
                new Vector2D(1, 7));
        Idea roomC = constructRoomCategory("RoomC",
                new Vector2D(0, 7),
                new Vector2D(8, 10));
        roomA.get("Adjacent").add(roomB);
        roomB.get("Adjacent").add(roomA);
        roomB.get("Adjacent").add(roomC);
        roomC.get("Adjacent").add(roomB);

        Idea exit = new Idea("Exit1", null, "Link", 1);
        exit.add(new Idea("Room", roomB));
        exit.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-3.5,2)));
        roomA.get("Exits").add(exit);

        Idea exit2 = new Idea("Exit2", null, "Link", 1);
        exit2.add(new Idea("Room", roomA));
        exit2.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(0,-2.5)));
        Idea exit3 = new Idea("Exit3", null, "Link", 1);
        exit3.add(new Idea("Room", roomC));
        exit3.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(0,2.5)));
        roomB.get("Exits").add(exit2);
        roomB.get("Exits").add(exit3);

        Idea exit4 = new Idea("Exit4", null, "Link", 1);
        exit4.add(new Idea("Room", roomB));
        exit4.add(new Idea("Grid_Place", GridLocation.getInstance().locateHCCIdea(-3.5,-2)));
        roomC.get("Exits").add(exit4);

        roomsCategoriesIdea.add(roomA);
        roomsCategoriesIdea.add(roomB);
        roomsCategoriesIdea.add(roomC);

        return roomsCategoriesIdea;
    }

    public void shutdown(){
        mind.shutDown();
        //gm.stop();
        //ev.setVisible(false);
        env.stopSimulation();
        System.exit(0);
    }
}
