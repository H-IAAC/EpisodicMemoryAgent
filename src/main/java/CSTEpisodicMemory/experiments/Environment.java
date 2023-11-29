package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.Main;
import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.util.Constants;
import co.nstant.in.cbor.CborException;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Environment {

    public WS3DCoppelia world;
    public Agent creature;
    public boolean initialized = false;

    public Environment(){
        this(10,10);
    }
    public Environment(float width, float heigth){
        world = new WS3DCoppelia(width, heigth);

        try {
            world.stopSimulation();
        } catch (CborException e) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        createCreature();
        initializeRooms();

        try {
            world.startSimulation();
            initialized = true;
        } catch (IOException | CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected abstract void createCreature();

    protected void insertWall(Constants.BrickTypes color, float x1, float y1, float x2, float y2){
        float wallWidth = 0.05f;
        world.createBrick(color, x1 - wallWidth, y1 - wallWidth, x2, y2);
    }

    protected void insertFood(float x, float y){
        world.createThing(Constants.FoodTypes.NPFOOD,x,y);
    }

    protected void insertJewel(float x, float y){
        Constants.JewelTypes[] types = Constants.JewelTypes.values();
        world.createThing(types[new Random().nextInt(types.length)],x,y);
    }

    public abstract void initializeRooms();

    public Agent newAgent(float x, float y, Constants.Color color) {
        Agent npc = world.createNPCAgent(x, y, color);
        return npc;
    }

    public void stopSimulation() {
        try {
            world.stopSimulation();
            initialized = false;
        } catch (CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
