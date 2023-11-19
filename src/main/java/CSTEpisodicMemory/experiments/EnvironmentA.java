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
        creature = world.createAgent(1,1);
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

        int[] hcc = GridLocation.getInstance().locateHCC(-3.5,1.8);
        double[] xy = GridLocation.getInstance().toXY(hcc[0], hcc[1]);
        xy[0] += 4;
        xy[1] += 1.5;
        insertWall(Constants.BrickTypes.MAGENTA_BRICK, (float) (xy[0]-0.02), (float) (xy[1]-0.02), (float) (xy[0]+0.02), (float) (xy[1]+0.02));
    }
}
