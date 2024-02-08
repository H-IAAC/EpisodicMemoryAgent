package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.AgentMind;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.util.Vector2D;
import CSTEpisodicMemory.util.visualization.GraphicMind;
import CSTEpisodicMemory.util.visualization.IdeaVisualizer;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.io.rest.RESTServer;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.AgentMind.constructRoomCategory;
import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class ExperimentB {

    public static long starTime = 0;

    public static void run(){
        starTime = System.currentTimeMillis();
        Environment env = new EnvironmentA(8,10);

        AgentMind mind = new AgentMind(env, createRoomsCategories(), false, true);
        RESTServer rs = new RESTServer(mind, 5000, true);

        IdeaVisualizer visu = new IdeaVisualizer(mind);
        for (String mem : mind.getRawMemory().getAllMemoryObjects().stream().map(Memory::getName).collect(Collectors.toList()))
            visu.addMemoryWatch(mem);
        visu.setMemoryWatchPrintLevel("EPLTM", 4);
        visu.setMemoryWatchPrintLevel("EVENTS", 5);
        visu.setVisible(true);

        GraphicMind gm = new GraphicMind(mind, env, 8, 10, 700, 700, 0);

        //Show a moving agent
        Agent npc = env.newAgent(1,1.5F, Constants.Color.AGENT_RED);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Move Agent");
        npc.moveTo(7F,1.5F);

        //Force Episode segmentation
        Memory selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("BOUNDARIES"))
                .findFirst().get();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Idea boundaries = (Idea) selectedMem.getI();
        Idea forcedBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
        long boundary = System.currentTimeMillis();
        forcedBoundary.add(new Idea("TimeStamp", boundary, "Property", 1));
        boundaries.add(forcedBoundary);
        selectedMem.setI(boundaries);
        System.out.println("Force episode Segmentation - " + (boundary-starTime));


        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("STORY"))
                .findFirst().get();
        GraphIdea story = new GraphIdea(((Idea) selectedMem.getI()).getL().get(0).get("Story"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //env.creature.moveTo(4, 2.3F);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("####Report####");
        System.out.println("---Encoding---");
        System.out.println("Número de nós: " + story.getNodes().size());
        System.out.println("Detected Events: " +  story.getEventNodes().size());
        System.out.println("Contextos: " + story.getContextNodes().size());
        System.out.println("Objetos: " + story.getObjectNodes().size());
        System.out.println("Links espaciais: " + story.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count());
        System.out.println("Número de células de grade: " + story.getLocationNodes().size());

        //Second pass
        System.out.println("Move Agent");
        npc.moveTo(1F,1.5F);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("BOUNDARIES"))
                .findFirst().get();
        forcedBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
        boundary = System.currentTimeMillis();
        forcedBoundary.add(new Idea("TimeStamp", boundary, "Property", 1));
        boundaries.add(forcedBoundary);
        selectedMem.setI(boundaries);
        System.out.println("Force episode Segmentation - " + (boundary-starTime));

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("STORY"))
                .findFirst().get();
        story = new GraphIdea(((Idea) selectedMem.getI()).getL().get(0).get("Story"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //env.creature.moveTo(4, 2.1F);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("####Report####");
        System.out.println("---Encoding---");
        System.out.println("Número de nós: " + story.getNodes().size());
        System.out.println("Detected Events: " +  story.getEventNodes().size());
        System.out.println("Contextos: " + story.getContextNodes().size());
        System.out.println("Objetos: " + story.getObjectNodes().size());
        System.out.println("Links espaciais: " + story.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count());
        System.out.println("Número de células de grade: " + story.getLocationNodes().size());


        //Third pass
        System.out.println("Move Agent");
        npc.moveTo(7F,1.5F);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("BOUNDARIES"))
                .findFirst().get();
        forcedBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
        boundary = System.currentTimeMillis();
        forcedBoundary.add(new Idea("TimeStamp", boundary, "Property", 1));
        boundaries.add(forcedBoundary);
        selectedMem.setI(boundaries);
        System.out.println("Force episode Segmentation - " + (boundary-starTime));

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("STORY"))
                .findFirst().get();
        story = new GraphIdea(((Idea) selectedMem.getI()).getL().get(0).get("Story"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //env.creature.moveTo(4, 1.9F);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("####Report####");
        System.out.println("---Encoding---");
        System.out.println("Número de nós: " + story.getNodes().size());
        System.out.println("Detected Events: " +  story.getEventNodes().size());
        System.out.println("Contextos: " + story.getContextNodes().size());
        System.out.println("Objetos: " + story.getObjectNodes().size());
        System.out.println("Links espaciais: " + story.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count());
        System.out.println("Número de células de grade: " + story.getLocationNodes().size());


        //Fourth pass
        System.out.println("Move Agent");
        npc.moveTo(1F,1.5F);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("BOUNDARIES"))
                .findFirst().get();
        forcedBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
        boundary = System.currentTimeMillis();
        forcedBoundary.add(new Idea("TimeStamp", boundary, "Property", 1));
        boundaries.add(forcedBoundary);
        selectedMem.setI(boundaries);
        System.out.println("Force episode Segmentation - " + (boundary-starTime));

        selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m -> m.getName().equalsIgnoreCase("STORY"))
                .findFirst().get();
        story = new GraphIdea(((Idea) selectedMem.getI()).getL().get(0).get("Story"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        env.creature.moveTo(4F, 2.3F);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("####Report####");
        System.out.println("---Encoding---");
        System.out.println("Número de nós: " + story.getNodes().size());
        System.out.println("Detected Events: " +  story.getEventNodes().size());
        System.out.println("Contextos: " + story.getContextNodes().size());
        System.out.println("Objetos: " + story.getObjectNodes().size());
        System.out.println("Links espaciais: " + story.getContextNodes().stream().filter(e->getNodeContent(e).getName().contains("SpatialLink")).count());
        System.out.println("Número de células de grade: " + story.getLocationNodes().size());

        mind.shutDown();
        //visu.setVisible(false);
        //gm.stop();
        //env.stopSimulation();

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
}
