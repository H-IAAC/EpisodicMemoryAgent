/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory;

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.util.Constants.BrickTypes;
import co.nstant.in.cbor.CborException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class Environment {
    public WS3DCoppelia world;
    public Agent creature;
    public boolean initialized = false;

    public Environment() {
        world = new WS3DCoppelia(8, 10);
        creature = world.createAgent(1f, 1f);
        initializaRooms();
        try {
            world.startSimulation();
            initialized = true;
        } catch (IOException | CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Robot " + creature.getName() + " is ready to go.");
    }

    private void initializaRooms() {
        float wallWidth = 0.05f;
        world.createBrick(BrickTypes.BLUE_BRICK, 0-wallWidth, 0-wallWidth, 8, 0);
        world.createBrick(BrickTypes.BLUE_BRICK, 0-wallWidth, 0-wallWidth, 0, 3);
        world.createBrick(BrickTypes.BLUE_BRICK, 8-wallWidth, 0-wallWidth, 8, 3);
        world.createBrick(BrickTypes.BLUE_BRICK, 8-wallWidth, 3-wallWidth, 1, 3);
        
        world.createBrick(BrickTypes.WHITE_BRICK, 0-wallWidth, 3-wallWidth, 0, 7);
        world.createBrick(BrickTypes.WHITE_BRICK, 1-wallWidth, 3-wallWidth, 1, 7);
        
        world.createBrick(BrickTypes.RED_BRICK, 0-wallWidth, 7-wallWidth, 0, 10);
        world.createBrick(BrickTypes.RED_BRICK, 1-wallWidth, 7-wallWidth, 8, 7);
        world.createBrick(BrickTypes.RED_BRICK, 8-wallWidth, 7-wallWidth, 8, 10);
        world.createBrick(BrickTypes.RED_BRICK, 0-wallWidth, 10-wallWidth, 8, 10);
    }

    public void stopSimulation(){
        try {
            world.stopSimulation();
            initialized = false;
        } catch (CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
