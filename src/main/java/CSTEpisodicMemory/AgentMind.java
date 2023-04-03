/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.behavior.Collect;
import CSTEpisodicMemory.behavior.Move;
import CSTEpisodicMemory.categories.EventCategory;
import CSTEpisodicMemory.categories.RoomCategoryIdea;
import CSTEpisodicMemory.context.GoalSelector;
import CSTEpisodicMemory.entity.EventTracker;
import CSTEpisodicMemory.impulses.CollectJewelImpulse;
import CSTEpisodicMemory.impulses.GoToJewelImpulse;
import CSTEpisodicMemory.motor.HandsActuatorCodelet;
import CSTEpisodicMemory.motor.LegsActuatorCodelet;
import CSTEpisodicMemory.perception.JewelDetector;
import CSTEpisodicMemory.perception.RoomDetector;
import CSTEpisodicMemory.perception.WallDetector;
import CSTEpisodicMemory.sensor.InnerSense;
import CSTEpisodicMemory.sensor.LeafletSense;
import CSTEpisodicMemory.sensor.Vision;
import CSTEpisodicMemory.util.Vector2D;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class AgentMind extends Mind {

    private boolean debug = false;
    public List<Codelet> bList = new ArrayList<Codelet>();

    public AgentMind(Environment env, boolean debug){
        this.debug = debug;
        initializeMindAndStar(env);
    }
    public AgentMind(Environment env){
        super();
        initializeMindAndStar(env);
    }

    private void initializeMindAndStar(Environment env){
        // Create CodeletGroups and MemoryGroups for organizing Codelets and Memories
        createCodeletGroup("Sensory");
        createCodeletGroup("Motor");
        createCodeletGroup("Perception");
        createCodeletGroup("Context");
        createCodeletGroup("Behavioral");
        //createMemoryGroup("Sensory");
        //createMemoryGroup("Motor");
        //createMemoryGroup("Working");

        Memory innerSenseMO;
        Memory visionMO;
        Memory knownJewelsMO;
        Memory wallsMO;
        Memory eventsMO;
        Memory goalsMO;
        Memory categoriesRoomMO;
        Memory roomsMO;
        Memory leafletsMO;
        Memory impulsesMO;
        MemoryContainer handsMO;
        MemoryContainer legsMO;

        //Inner Sense
        Idea innerSenseIdea = initializeInnerSenseIdea();
        innerSenseMO = createMemoryObject("INNER", innerSenseIdea);
        //Vision sensor
        visionMO = createMemoryObject("VISION");
        //Detected Jewels
        Idea jewelsIdea = new Idea("Jewels", null, 5);
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", jewelsIdea);
        //Detected Walls
        Idea wallsIdea = new Idea("Walls", null, 5);
        wallsMO = createMemoryObject("WALLS", wallsIdea);
        //Move Event Tracker
        Idea eventsIdea = new Idea("Events", null, 5);
        eventsMO = createMemoryObject("EVENTS", eventsIdea);
        //Goals
        Idea goalsIdea = new Idea("Goals", null, 5);
        goalsMO = createMemoryObject("GOALS", goalsIdea);
        //----Categories----
        //Rooms
        List<RoomCategoryIdea> roomsCategoriesIdea = new ArrayList<>();
        roomsCategoriesIdea.add(new RoomCategoryIdea("RoomA",
                new Vector2D(0, 0),
                new Vector2D(8, 3)));
        roomsCategoriesIdea.add(new RoomCategoryIdea("RoomB",
                new Vector2D(0, 3),
                new Vector2D(1, 7)));
        roomsCategoriesIdea.add(new RoomCategoryIdea("RoomC",
                new Vector2D(0, 7),
                new Vector2D(8, 10)));
        categoriesRoomMO = createMemoryObject("ROOM_CATEGORIES", roomsCategoriesIdea);
        Idea roomIdea = new Idea("Room", null, "AbstractObject", 1);
        roomsMO = createMemoryObject("ROOM", roomIdea);
        //--------
        //Leaflets
        Idea leafletsIdea = new Idea("Leaflets", null, 0);
        leafletsMO = createMemoryObject("LEAFLETS", leafletsIdea);
        //Impulses
        Idea impulsesIdea = new Idea("Impulses", null, 0);
        impulsesMO = createMemoryObject("IMPULSES", impulsesIdea);
        //Hands
        handsMO = createMemoryContainer("HANDS");
        //Leags
        legsMO = createMemoryContainer("LEGS");

        //Inner Sense Codelet
        Codelet innerSenseCodelet = new InnerSense(env.creature);
        innerSenseCodelet.addOutput(innerSenseMO);
        insertCodelet(innerSenseCodelet, "Sensory");

        //Vision Sensor Codelet
        Codelet visionCodelet = new Vision(env.creature);
        visionCodelet.addOutput(visionMO);
        insertCodelet(visionCodelet, "Sensory");

        //Leaflet Sense Codelet
        Codelet leafletSenseCodelet = new LeafletSense(env.creature);
        leafletSenseCodelet.addOutput(leafletsMO);
        insertCodelet(leafletSenseCodelet, "Sensory");

        //Jewel Detector Codelet
        Codelet jewelDetectorCodelet = new JewelDetector(debug);
        jewelDetectorCodelet.addInput(visionMO);
        jewelDetectorCodelet.addOutput(knownJewelsMO);
        insertCodelet(jewelDetectorCodelet, "Perception");

        //Walls Detector Codelet
        Codelet wallsDetectorCodelet = new WallDetector(debug);
        wallsDetectorCodelet.addInput(visionMO);
        wallsDetectorCodelet.addOutput(wallsMO);
        insertCodelet(wallsDetectorCodelet, "Perception");

        //RoomDetector Codelet
        Codelet roomDetectorCodelet = new RoomDetector();
        roomDetectorCodelet.addInput(innerSenseMO);
        roomDetectorCodelet.addInput(categoriesRoomMO);
        roomDetectorCodelet.addOutput(roomsMO);
        insertCodelet(roomDetectorCodelet, "Perception");

        //Move Event Codelet
        EventCategory moveEventCategory = new EventCategory("Move", Arrays.asList("Self.Position.X", "Self.Position.Y"));
        EventTracker moveEventTracker = new EventTracker("INNER", "EVENTS", moveEventCategory, debug);
        moveEventTracker.setBufferSize(2);
        moveEventTracker.setBufferStepSize(2);
        moveEventTracker.addInput(innerSenseMO);
        moveEventTracker.addOutput(eventsMO);
        insertCodelet(moveEventTracker, "Perception");

        //Rotate Event Codelet
        EventCategory rotateEventCategory= new EventCategory("Rotate", Arrays.asList("Self.Pitch", "Self.Pitch"));
        EventTracker rotateEventTracker = new EventTracker("INNER", "EVENTS", rotateEventCategory, debug);
        rotateEventTracker.setBufferSize(2);
        rotateEventTracker.setBufferStepSize(2);
        rotateEventTracker.addInput(innerSenseMO);
        rotateEventTracker.addOutput(eventsMO);
        insertCodelet(rotateEventTracker, "Perception");

        //Impulses
        //Go to jewel
        Codelet goToJewelImpulse = new GoToJewelImpulse();
        goToJewelImpulse.addInput(innerSenseMO);
        goToJewelImpulse.addInput(knownJewelsMO);
        goToJewelImpulse.addInput(leafletsMO);
        goToJewelImpulse.addOutput(impulsesMO);
        insertCodelet(goToJewelImpulse, "Behavioral");

        //Collect Jewel
        Codelet collectJewelImpulse = new CollectJewelImpulse();
        collectJewelImpulse.addInput(innerSenseMO);
        collectJewelImpulse.addInput(knownJewelsMO);
        collectJewelImpulse.addOutput(impulsesMO);
        insertCodelet(collectJewelImpulse, "Behavioral");
        ////Goal selector Codelet
        //Codelet goalSelectorCodelet = new GoalSelector();
        //goalSelectorCodelet.addInput(innerSenseMO);
        //goalSelectorCodelet.addInput(knownJewelsMO);
        //goalSelectorCodelet.addInput(wallsMO);
        //goalSelectorCodelet.addOutput(goalsMO);
        //insertCodelet(goalSelectorCodelet, "Context");

        //Move Action/Behaviour
        Codelet moveActionCodelet = new Move();
        moveActionCodelet.addInput(impulsesMO);
        moveActionCodelet.addOutput(legsMO);
        insertCodelet(moveActionCodelet, "Behavioral");

        //Collect Action/Behaviour
        Codelet collectActionCodelet = new Collect();
        collectActionCodelet.addInput(impulsesMO);
        collectActionCodelet.addInput(knownJewelsMO);
        collectActionCodelet.addOutput(handsMO);
        insertCodelet(collectActionCodelet, "Behavioral");

        //Hands Motor Codelet
        Codelet handsMotorCodelet = new HandsActuatorCodelet(env.creature);
        handsMotorCodelet.addInput(handsMO);
        insertCodelet(handsMotorCodelet, "Motor");

        //Legs Motor Codelet
        Codelet legsMotorCodelet = new LegsActuatorCodelet(env.creature);
        legsMotorCodelet.addInput(legsMO);
        insertCodelet(legsMotorCodelet, "Motor");

        bList.add(wallsDetectorCodelet);
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(100);

        start();
    }

    private Idea initializeInnerSenseIdea(){
        Idea innerSense = new Idea("Self", "AGENT", "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", null, "QualityDimension", 1));
        posIdea.add(new Idea("Y", null, "QualityDimension", 1));
        innerSense.add(posIdea);
        innerSense.add(new Idea("Pitch", null, "Property", 1));
        innerSense.add(new Idea("Fuel", null, "Property", 1));
        innerSense.add(new Idea("Step", 0, "TimeStep", 1));

        return innerSense;
    }

//    private Idea initializePerceptionIdea(){
//        Idea perceptionIdea = new Idea("Perception", null, 7);
//        Idea innerSense = new Idea("Self", "AGENT", "AbstractObject", 1);
//        Idea posIdea = new Idea("Position", null, "Property", 1);
//        posIdea.add(new Idea("X", null, "QualityDimension", 1));
//        posIdea.add(new Idea("Y", null, "QualityDimension", 1));
//        innerSense.add(posIdea);
//        innerSense.add(new Idea("Pitch", null, "Property", 1));
//        innerSense.add(new Idea("Fuel", null, "Property", 1));
//        perceptionIdea.add(innerSense);
//        perceptionIdea.add(new Idea("Jewels", null, 7));
//        perceptionIdea.add(new Idea("Walls", null, 7));
//    }
}
