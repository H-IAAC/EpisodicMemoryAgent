package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.AgentMind;
import CSTEpisodicMemory.Main;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.representation.idea.Idea;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static CSTEpisodicMemory.AgentMind.constructRoomCategory;

public class ExperimentA {

    public static void run(){
        Environment env = new EnvironmentA(8,10);

        AgentMind mind = new AgentMind(env, createRoomsCategories());
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
