package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.core.representation.GridLocation;
import WS3DCoppelia.util.Constants;

public class EnvironmentA extends Environment{

    public EnvironmentA(){ super(); }

    public EnvironmentA(float width, float heigth){
        super(width, heigth);
    }

    @Override
    protected void createCreature() {
        creature = world.createAgent(4,2.6F);
        creature.moveTo(4,2.5F);
    }

    public void initializeRooms(){
        insertWall(Constants.BrickTypes.BLUE_BRICK, 0,0,8,0);
        insertWall(Constants.BrickTypes.BLUE_BRICK, 0,0,0,3);
        insertWall(Constants.BrickTypes.BLUE_BRICK, 8,0,8,3);
        insertWall(Constants.BrickTypes.BLUE_BRICK, 8,3,1,3);
        insertWall(Constants.BrickTypes.WHITE_BRICK, 0,3,0,7);
        insertWall(Constants.BrickTypes.WHITE_BRICK, 1,3,1,7);
        insertWall(Constants.BrickTypes.RED_BRICK, 0,7,0,10);
        insertWall(Constants.BrickTypes.RED_BRICK, 1,7,8,7);
        insertWall(Constants.BrickTypes.RED_BRICK, 8,7,8,10);
        insertWall(Constants.BrickTypes.RED_BRICK, 0,10,8,10);

        insertWall(Constants.BrickTypes.GREEN_BRICK, 3.75F, 0.1F, 4.25F, 0.2F);
        insertWall(Constants.BrickTypes.MAGENTA_BRICK, 3.25F, 0.1F, 3.75F, 0.2F);
        insertWall(Constants.BrickTypes.ORANGE_BRICK, 4.25F, 0.1F, 4.75F, 0.2F);

        insertWall(Constants.BrickTypes.GREEN_BRICK, 3.75F, 9.8F, 4.25F, 9.9F);
        insertWall(Constants.BrickTypes.MAGENTA_BRICK, 3.25F, 9.8F, 3.75F, 9.9F);
        insertWall(Constants.BrickTypes.ORANGE_BRICK, 4.25F, 9.8F, 4.75F, 9.9F);
    }
}
