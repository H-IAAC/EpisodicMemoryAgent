package CSTEpisodicMemory;

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.util.Constants;
import WS3DCoppelia.util.Constants.BrickTypes;
import co.nstant.in.cbor.CborException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static WS3DCoppelia.util.Constants.BrickTypes.*;

public class ExperimentEnvironment {
    public WS3DCoppelia world;
    public Agent creature;
    public boolean initialized = false;

    public ExperimentEnvironment() {
        world = new WS3DCoppelia(16, 24);
        try {
            world.stopSimulation();
        } catch (
                CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        creature = world.createAgent(3f, 3f);
        //world.createAgent(2f, 2f);
        initializeRooms();
        try {
            world.startSimulation();
            initialized = true;
        } catch (IOException | CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeRooms() {
        insertWall(BLUE_BRICK, 0, 2, 0, 6);
        insertWall(BLUE_BRICK, 0, 2, 4, 2);
        insertWall(BLUE_BRICK, 4, 2, 4, 6);
        insertWall(BLUE_BRICK, 3, 6, 4, 6);
        insertWall(BLUE_BRICK, 0, 6, 2, 6);

        insertWall(RED_BRICK, 5, 0, 5, 6);
        insertWall(RED_BRICK, 5,0,9,0);
        insertWall(RED_BRICK, 9,0,9,6);
        insertWall(RED_BRICK, 7,6,9,6);
        insertWall(RED_BRICK, 5,6,6,6);

        insertWall(GREEN_BRICK, 3,8,3,12);
        insertWall(GREEN_BRICK, 3,12,7,12);
        insertWall(GREEN_BRICK, 3,8,4,8);
        insertWall(GREEN_BRICK, 5,8,7,8);
        insertWall(GREEN_BRICK, 7,8,7,12);

        insertWall(WHITE_BRICK, 2,6,2,7.98F);
        insertWall(WHITE_BRICK, 3,6.02F,6,6.02F);
        insertWall(WHITE_BRICK, 7,6.02F,10.02F,6.02F);
        insertWall(WHITE_BRICK, 2,7.98F,4,7.98F);
        insertWall(WHITE_BRICK, 5,7.98F,10,7.98F);
        insertWall(WHITE_BRICK, 10.02F,0,10.02F,6.02F);
        insertWall(WHITE_BRICK, 11.98F,0,11.98F,7);
        insertWall(WHITE_BRICK, 11.98F,8,11.98F,14);
        insertWall(WHITE_BRICK, 11.98F,15,11.98F,19.98F);
        insertWall(WHITE_BRICK, 10.02F,8,10.02F,10);
        insertWall(WHITE_BRICK, 10.02F,11,10.02F,18.02F);
        insertWall(WHITE_BRICK, 0,18.02F,6,18.02F);
        insertWall(WHITE_BRICK, 7,18.02F,10.02F,18.02F);
        insertWall(WHITE_BRICK, 0,19.98F,10,19.98F);
        insertWall(WHITE_BRICK, 11,19.98F,12,19.98F);

        insertWall(MAGENTA_BRICK, 12,5,12,7);
        insertWall(MAGENTA_BRICK, 12,8,12,11);
        insertWall(MAGENTA_BRICK, 12,5,16,5);
        insertWall(MAGENTA_BRICK, 16,5,16,11);
        insertWall(MAGENTA_BRICK, 12,11,16,11);

        insertWall(ORANGE_BRICK, 7.02F,8,7.02F,14);
        insertWall(ORANGE_BRICK, 7,8,10,8);
        insertWall(ORANGE_BRICK, 10,8,10,10);
        insertWall(ORANGE_BRICK, 10,11,10,14);
        insertWall(ORANGE_BRICK, 7,14,10,14);

        insertWall(GREY_BRICK, 12,12,12,14);
        insertWall(GREY_BRICK, 12,15,12,16);
        insertWall(GREY_BRICK, 12,12,16,12);
        insertWall(GREY_BRICK, 16,12,16,16);
        insertWall(GREY_BRICK, 12,16,16,16);

        insertWall(BROWN_BRICK, 9,20,10,20);
        insertWall(BROWN_BRICK, 9,20,9,24);
        insertWall(BROWN_BRICK, 9,24,15,24);
        insertWall(BROWN_BRICK, 15,20,15,24);
        insertWall(BROWN_BRICK, 11,20,15,20);

        insertWall(YELLOW_BRICK, 4,18,6,18);
        insertWall(YELLOW_BRICK, 7,18,10,18);
        insertWall(YELLOW_BRICK, 10,14,10,18);
        insertWall(YELLOW_BRICK, 4,14.02F,10,14.02F);
        insertWall(YELLOW_BRICK, 4,14,4,18);
    }

    private void insertWall(BrickTypes color, float x1, float y1, float x2, float y2){
        float wallWidth = 0.05f;
        world.createBrick(color, x1 - wallWidth, y1 - wallWidth, x2, y2);
    }

    public void stopSimulation() {
        try {
            world.stopSimulation();
            initialized = false;
        } catch (CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Agent newAgent(float x, float y, Constants.Color color) {
        Agent npc = world.createNPCAgent(x, y, color);
        return npc;
    }

}
