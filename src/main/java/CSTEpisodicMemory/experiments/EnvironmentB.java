package CSTEpisodicMemory.experiments;

import CSTEpisodicMemory.core.representation.GridLocation;

import static WS3DCoppelia.util.Constants.BrickTypes.*;
import static WS3DCoppelia.util.Constants.BrickTypes.YELLOW_BRICK;

public class EnvironmentB extends Environment{

    public EnvironmentB(){ super(); }
    public EnvironmentB(float width, float heigth){
        super(width, heigth);
    }

    @Override
    protected void createCreature() {
        creature = world.createAgent(3,3);
    }

    @Override
    public void initializeRooms() {
        insertWall(BLUE_BRICK, 0, 2, 0, 6);
        insertWall(BLUE_BRICK, 0, 2, 4, 2);
        insertWall(BLUE_BRICK, 4, 2, 4, 6);
        insertWall(BLUE_BRICK, 3, 6, 4, 6);
        insertWall(BLUE_BRICK, 0, 6, 2, 6);

        insertFood(3.4F, 2.6F);
        //insertFood(3.8F, 2.6F);
        //insertFood(3.4F, 2.2F);
        insertFood(3.8F, 2.2F);
        insertFood(0.2F, 2.2F);
        //insertFood(0.6F, 2.2F);
        //insertFood(0.2F, 2.6F);
        insertFood(0.6F, 2.6F);

        insertWall(RED_BRICK, 5,0,5,6);
        insertWall(RED_BRICK, 5,0,9,0);
        insertWall(RED_BRICK, 9,0,9,6);
        insertWall(RED_BRICK, 7,6,9,6);
        insertWall(RED_BRICK, 5,6,6,6);

        insertJewel(7,4.5F);
        insertJewel(7.5F,4.5F);
        insertJewel(6.5F,4.5F);

        insertFood(5.4F, 0.6F);
        insertFood(8.4F, 0.6F);

        insertWall(GREEN_BRICK, 3,8,3,12);
        insertWall(GREEN_BRICK, 3,12,7,12);
        insertWall(GREEN_BRICK, 3,8,4,8);
        insertWall(GREEN_BRICK, 5,8,7,8);
        insertWall(GREEN_BRICK, 7,8,7,12);

        insertJewel(4.5F, 9.5F);
        insertJewel(5,9.5F);
        insertJewel(4,9.5F);

        insertFood(3.4F, 8.6F);
        insertFood(6.4F, 8.6F);

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

        insertJewel(13.5F, 7.5F);
        insertJewel(13.5F, 7);
        insertJewel(13.5F, 8);

        insertFood(12.4F, 5.6F);
        insertFood(15.4F, 10.6F);

        insertWall(ORANGE_BRICK, 7.02F,8,7.02F,14);
        insertWall(ORANGE_BRICK, 7,8,10,8);
        insertWall(ORANGE_BRICK, 10,8,10,10);
        insertWall(ORANGE_BRICK, 10,11,10,14);
        insertWall(ORANGE_BRICK, 7,14,10,14);

        insertJewel(8, 10.5F);
        insertJewel(8, 11);
        insertJewel(8, 10);

        insertFood(7.4F, 8.6F);
        insertFood(9.4F, 13.6F);

        insertWall(GREY_BRICK, 12,12,12,14);
        insertWall(GREY_BRICK, 12,15,12,16);
        insertWall(GREY_BRICK, 12,12,16,12);
        insertWall(GREY_BRICK, 16,12,16,16);
        insertWall(GREY_BRICK, 12,16,16,16);

        insertJewel(13.5F,14);
        insertJewel(13.5F,14.5F);
        insertJewel(13.5F,15);

        insertFood(12.4F, 12.6F);
        insertFood(15.4F, 14.6F);

        insertWall(BROWN_BRICK, 9,20,10, 20);
        insertWall(BROWN_BRICK, 9,20,9,  24);
        insertWall(BROWN_BRICK, 9,24,15, 24);
        insertWall(BROWN_BRICK, 15,20,15,24);
        insertWall(BROWN_BRICK, 11,20,15,20);

        insertFood(9.2F, 23.8F);
        //insertFood(9.6F, 23.8F);
        //insertFood(9.2F, 23.4F);
        insertFood(9.6F, 23.4F);
        insertFood(14.4F, 23.4F);
        //insertFood(14.8F, 23.4F);
        //insertFood(14.4F, 23.8F);
        insertFood(14.8F, 23.8F);

        insertWall(YELLOW_BRICK, 4,18,6,18);
        insertWall(YELLOW_BRICK, 7,18,10,18);
        insertWall(YELLOW_BRICK, 10,14,10,18);
        insertWall(YELLOW_BRICK, 4,14.02F,10,14.02F);
        insertWall(YELLOW_BRICK, 4,14,4,18);

        insertJewel(6,16.5F);
        insertJewel(6.5F,16.5F);
        insertJewel(7,16.5F);

        insertFood(4.4F, 14.6F);
        insertFood(9.4F, 17.6F);
    }
}
