/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.behavior.Collect;
import CSTEpisodicMemory.behavior.Move;
import CSTEpisodicMemory.categories.LinearEventCategory;
import CSTEpisodicMemory.categories.RoomCategoryIdeaFunctions;
import CSTEpisodicMemory.categories.StepEventCategory;
import CSTEpisodicMemory.core.codelets.EventTracker;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.episodic.EpisodeBinding;
import CSTEpisodicMemory.episodic.TimelineBufferCodelet;
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
import CSTEpisodicMemory.util.NodeState;
import CSTEpisodicMemory.util.Vector2D;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.*;
import br.unicamp.cst.representation.idea.Idea;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import scala.Int;

import java.util.*;

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
        Memory jewelsCounterMO;
        Memory wallsMO;
        Memory eventsMO;
        Memory goalsMO;
        Memory storyMO;
        Memory bufferMO;
        Memory categoriesRoomMO;
        Memory roomsMO;
        Memory leafletsMO;
        MemoryContainer impulsesMO;
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
        //Jewels Counter
        List<Idea> jewelsCounters = new ArrayList<>();
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            jewelsCounters.add(new Idea(type.typeName(), 0, "Property", 1));
        }
        Idea jewelCountersIdea = new Idea("JewelsCounters", null, 5);
        jewelCountersIdea.setL(jewelsCounters);
        jewelCountersIdea.add(new Idea("Step", 0, "TimeStep", 1));
        jewelCountersIdea.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));
        jewelsCounterMO = createMemoryObject("JEWELS_COUNTERS", jewelCountersIdea);
        //Detected Walls
        Idea wallsIdea = new Idea("Walls", null, 5);
        wallsMO = createMemoryObject("WALLS", wallsIdea);
        //Move Event Tracker
        Idea eventsIdea = new Idea("Events", null, 5);
        eventsMO = createMemoryObject("EVENTS", eventsIdea);
        //Goals
        Idea goalsIdea = new Idea("Goals", null, 5);
        goalsMO = createMemoryObject("GOALS", goalsIdea);

        //Story
        Idea storiesIdea = new Idea("Stories", null, "Composition", 1);
        Idea episode = new Idea("Episode", 0, "Episode", 1);
        Idea storyGraph = new Idea("Story", null, "Composition", 1);
        episode.add(storyGraph);
        storiesIdea.add(episode);
        storyMO = createMemoryObject("STORY", storiesIdea);
        //Buffer
        Idea bufferIdea = new Idea("Buffer", null, "Aggregation", 1);
        bufferMO = createMemoryObject("BUFFER", bufferIdea);
        //----Categories----
        //Rooms
        List<Idea> roomsCategoriesIdea = new ArrayList<>();
        roomsCategoriesIdea.add(constructRoomCategory("RoomA",
                new Vector2D(0, 0),
                new Vector2D(8, 3)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomB",
                new Vector2D(0, 3),
                new Vector2D(1, 7)));
        roomsCategoriesIdea.add(constructRoomCategory("RoomC",
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
        //Idea impulsesIdea = new Idea("Impulses", null, 0);
        impulsesMO = createMemoryContainer("IMPULSES");

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
        jewelDetectorCodelet.addOutput(jewelsCounterMO);
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
        Idea moveEventCategory = constructEventCategory("Move", Arrays.asList("Self.Position.X", "Self.Position.Y"), "Linear");
        EventTracker moveEventTracker = new EventTracker("INNER", "EVENTS", moveEventCategory, debug);
        moveEventTracker.setBufferSize(2);
        moveEventTracker.setBufferStepSize(2);
        moveEventTracker.addInput(innerSenseMO);
        moveEventTracker.addOutput(eventsMO);
        insertCodelet(moveEventTracker, "Perception");

        //Rotate Event Codelet
        Idea rotateEventCategory = constructEventCategory("Rotate", Arrays.asList("Self.Pitch"), "Linear");
        EventTracker rotateEventTracker = new EventTracker("INNER", "EVENTS", rotateEventCategory, debug);
        rotateEventTracker.setBufferSize(2);
        rotateEventTracker.setBufferStepSize(2);
        rotateEventTracker.addInput(innerSenseMO);
        rotateEventTracker.addOutput(eventsMO);
        insertCodelet(rotateEventTracker, "Perception");

        //Found Jewel Event
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            Idea foundJewelEventCategory = constructEventCategory("Found_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepUp");
            EventTracker jewelFoundEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", foundJewelEventCategory, debug);
            jewelFoundEventTracker.setBufferSize(2);
            jewelFoundEventTracker.setBufferStepSize(2);
            jewelFoundEventTracker.addInput(jewelsCounterMO);
            jewelFoundEventTracker.addOutput(eventsMO);
            insertCodelet(jewelFoundEventTracker, "Perception");
        }

        //Collect Jewel Event
        for (Constants.JewelTypes type : Constants.JewelTypes.values()){
            Idea collectJewelEventCategory = constructEventCategory("Collected_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepDown");
            EventTracker jewelCollectedEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", collectJewelEventCategory, debug);
            jewelCollectedEventTracker.setBufferSize(2);
            jewelCollectedEventTracker.setBufferStepSize(2);
            jewelCollectedEventTracker.addInput(jewelsCounterMO);
            jewelCollectedEventTracker.addOutput(eventsMO);
            insertCodelet(jewelCollectedEventTracker, "Perception");
        }

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
        collectActionCodelet.addOutput(jewelsCounterMO);
        insertCodelet(collectActionCodelet, "Behavioral");

        //Hands Motor Codelet
        Codelet handsMotorCodelet = new HandsActuatorCodelet(env.creature);
        handsMotorCodelet.addInput(handsMO);
        insertCodelet(handsMotorCodelet, "Motor");

        //Legs Motor Codelet
        Codelet legsMotorCodelet = new LegsActuatorCodelet(env.creature);
        legsMotorCodelet.addInput(legsMO);
        insertCodelet(legsMotorCodelet, "Motor");

        Codelet episodeBindingCodelet = new EpisodeBinding();
        episodeBindingCodelet.addInput(eventsMO);
        episodeBindingCodelet.addInput(impulsesMO);
        episodeBindingCodelet.addInput(roomsMO);
        episodeBindingCodelet.addInput(bufferMO);
        episodeBindingCodelet.addOutput(storyMO);
        insertCodelet(episodeBindingCodelet, "Behavioural");

        Codelet bufferCodelet = new TimelineBufferCodelet(Arrays.asList("INNER", "KNOWN_JEWELS", "IMPULSES"));
        bufferCodelet.addInput(innerSenseMO);
        bufferCodelet.addInput(knownJewelsMO);
        bufferCodelet.addInput(impulsesMO);
        bufferCodelet.addOutput(bufferMO);
        insertCodelet(bufferCodelet);

        bList.add(wallsDetectorCodelet);
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(100);

        bufferCodelet.setTimeStep(500);

        start();
    }

    private Idea initializeInnerSenseIdea(){
        Idea innerSense = new Idea("Self", "AGENT", "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X",0, 3));
        posIdea.add(new Idea("Y",0, 3));
        innerSense.add(posIdea);
        innerSense.add(new Idea("Pitch", null, "Property", 1));
        innerSense.add(new Idea("Fuel", null, "Property", 1));
        innerSense.add(new Idea("Step", 0, "TimeStep", 1));
        innerSense.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));

        return innerSense;
    }

    private Idea constructRoomCategory(String name, Vector2D cornerA, Vector2D cornerB){
        Idea idea = new Idea(name, null, "AbstractObject", 0);
        idea.setValue(new RoomCategoryIdeaFunctions(name, cornerA, cornerB));
        return idea;
    }

    private Idea constructEventCategory(String name, List<String> properties, String type){
        Idea idea = new Idea(name, null, "Episode", 2);
        switch (type){
            case "Linear":
                idea.setValue(new LinearEventCategory(name, properties));
                break;
            case "StepUp":
                idea.setValue(new StepEventCategory(name, properties, "StepUp"));
                break;
            case "StepDown":
                idea.setValue(new StepEventCategory(name, properties, "StepDown"));
                break;
        }
        return idea;
    }

    private Map<Integer, NodeState> updateValues(MutableValueGraph<Integer, String> graph, Integer goal, Map<Integer, NodeState> stateMap){
        for (Integer n : graph.predecessors(goal)){
            if (graph.edgeValueOrDefault(n, goal, "None").equals("EndPos")){
                if (stateMap.get(n).eval < stateMap.get(goal).eval*0.9) {
                    stateMap.get(n).eval = stateMap.get(goal).eval * 0.9;
                    stateMap = updateValues(graph, n, stateMap);
                }
            }
        }
        for (Integer n : graph.successors(goal)){
            if (graph.edgeValueOrDefault(goal, n , "None").equals("StartPos")){
                if (stateMap.get(n).eval < stateMap.get(goal).eval*0.9) {
                    stateMap.get(n).eval = stateMap.get(goal).eval * 0.9;
                    stateMap = updateValues(graph, n, stateMap);
                }
            }
        }
        return stateMap;
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
