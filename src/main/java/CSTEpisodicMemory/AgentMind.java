/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.behavior.Collect;
import CSTEpisodicMemory.behavior.Eat;
import CSTEpisodicMemory.behavior.Move;
import CSTEpisodicMemory.categories.LinearEventCategory;
import CSTEpisodicMemory.categories.RoomCategoryIdeaFunctions;
import CSTEpisodicMemory.categories.StepEventCategory;
import CSTEpisodicMemory.core.codelets.EpisodeBoundaryDetection;
import CSTEpisodicMemory.core.codelets.EventTracker;
import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.episodic.EpisodeBinding;
import CSTEpisodicMemory.episodic.EpisodicGistExtraction;
import CSTEpisodicMemory.episodic.BufferCodelet;
import CSTEpisodicMemory.experiments.Environment;
import CSTEpisodicMemory.habits.*;
import CSTEpisodicMemory.impulses.*;
import CSTEpisodicMemory.motor.HandsActuatorCodelet;
import CSTEpisodicMemory.motor.LegsActuatorCodelet;
import CSTEpisodicMemory.perception.*;
import CSTEpisodicMemory.sensor.Propriosensor;
import CSTEpisodicMemory.sensor.LeafletSense;
import CSTEpisodicMemory.sensor.Vision;
import CSTEpisodicMemory.util.IdeaHelper;
import CSTEpisodicMemory.util.Vector2D;
import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.util.Constants;
import bibliothek.gui.dock.station.screen.window.InternalScreenDockWindowFactory;
import br.unicamp.cst.core.entities.*;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author bruno
 */
public class AgentMind extends Mind {

    private boolean debug = false;
    public List<Codelet> bList = new ArrayList<>();

    public AgentMind(Environment env, List<Idea> roomsCategories, boolean debug) {
        this.debug = debug;
        initializeMindAndStar(env, roomsCategories);
    }

    public AgentMind(Environment env, List<Idea> roomsCategories) {
        super();
        initializeMindAndStar(env, roomsCategories);
    }

    private void initializeMindAndStar(Environment env, List<Idea> roomsCategoriesIdea) {
        // Create CodeletGroups and MemoryGroups for organizing Codelets and Memories
        createCodeletGroup("Sensory");
        createCodeletGroup("Motor");
        createCodeletGroup("Perception");
        createCodeletGroup("Context");
        createCodeletGroup("Behavioral");
        createMemoryGroup("Perceptual");
        createMemoryGroup("Context");
        //createMemoryGroup("Working");

        Memory propriosensorMO;
        Memory innerSenseMO;
        Memory visionMO;
        Memory jewelsPerceptionMO;
        Memory knownJewelsMO;
        Memory agentPerceptionMO;
        Memory knownAgentsMO;
        Memory jewelsCounterMO;
        Memory foodPerceptionMO;
        Memory knownFoodsMO;
        Memory wallsPerceptionMO;
        Memory knownWallsMO;
        Memory eventsMO;
        Memory goalsMO;
        Memory storyMO;
        Memory perceptualBufferMO;
        Memory contextBufferMO;
        Memory EPLTMO;
        Memory locationsMO;
        Memory categoriesRoomMO;
        Memory roomsMO;
        Memory leafletsMO;
        Memory episodeBoundariesMO;
        MemoryContainer impulsesMO;
        MemoryContainer handsMO;
        MemoryContainer legsMO;


        //Inner Sense
        Idea innerSenseIdea = initializeInnerSenseIdea();
        propriosensorMO = createMemoryObject("PROPRIOSENSOR", innerSenseIdea);
        innerSenseMO = createMemoryObject("INNER");
        registerMemory(innerSenseMO, "Perceptual");
        //Vision sensor
        visionMO = createMemoryObject("VISION", new ArrayList<Identifiable>());

        //Detected Foods
        Idea foodsIdea = new Idea("Foods", null, 5);
        foodPerceptionMO = createMemoryObject("FOOD", foodsIdea);
        registerMemory(foodPerceptionMO, "Perceptual");

        //Known Foods
        Idea knownFoodsIdea = new Idea("Foods", null, 5);
        knownFoodsMO = createMemoryObject("KNOWN_FOODS", knownFoodsIdea);

        //Known Jewels
        Idea knownJewelsIdea = new Idea("Jewels", null, 5);
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewelsIdea);

        //Detected Jewels
        Idea jewelsIdea = new Idea("Jewels", null, 5);
        jewelsPerceptionMO = createMemoryObject("JEWELS", jewelsIdea);
        registerMemory(jewelsPerceptionMO, "Perceptual");

        //Detected Agents
        Idea agentsIdea = new Idea("Agents", null, 5);
        agentPerceptionMO = createMemoryObject("AGENTS", agentsIdea);
        registerMemory(agentPerceptionMO, "Perceptual");

        //Known Agents
        Idea knownAgentsIdea = new Idea("Agents", null, 5);
        knownAgentsMO = createMemoryObject("KNOWN_AGENTS", knownAgentsIdea);

        //Jewels Counter
        List<Idea> jewelsCounters = new ArrayList<>();
        for (Constants.JewelTypes type : Constants.JewelTypes.values()) {
            jewelsCounters.add(new Idea(type.typeName(), 0, "Property", 1));
        }
        Idea jewelCountersIdea = new Idea("JewelsCounters", null, 5);
        jewelCountersIdea.setL(jewelsCounters);
        jewelCountersIdea.add(new Idea("Step", 0, "TimeStep", 1));
        jewelCountersIdea.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));
        jewelsCounterMO = createMemoryObject("JEWELS_COUNTERS", jewelCountersIdea);

        //Detected Walls
        Idea wallsIdea = new Idea("Walls", null, 5);
        wallsPerceptionMO = createMemoryObject("WALLS", wallsIdea);
        registerMemory(wallsPerceptionMO, "Perceptual");

        //Known Walls
        Idea knownWallsIdea = new Idea("Walls", null, 5);
        knownWallsMO = createMemoryObject("KNOWN_WALLS", knownWallsIdea);

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
        //Buffers
        Idea perceptualBuffer = new Idea("Perceptual Buffer", null, "Configuration", 1);
        perceptualBufferMO = createMemoryObject("PERCEPTUAL_BUFFER", perceptualBuffer);
        Idea contextBuffer = new Idea("Context Buffer", null, "Configuration", 1);
        contextBufferMO = createMemoryObject("CONTEXT_BUFFER", contextBuffer);

        //Episodic Long-term memory
        Idea epLTM = new Idea("epLTM", null, "Configuration", 1);
        GraphIdea epLTMGraph = new GraphIdea(epLTM);
        EPLTMO = createMemoryObject("EPLTM", epLTMGraph);
        //----Categories----
        //Rooms
        categoriesRoomMO = createMemoryObject("ROOM_CATEGORIES", roomsCategoriesIdea);
        Idea roomIdea = new Idea("Room", null, "AbstractObject", 1);
        roomsMO = createMemoryObject("ROOM", roomIdea);
        registerMemory(roomsMO, "Context");

        //Events
        List<Idea> eventsCategoriesIdea = new ArrayList<>();
        // Elements are added when event tracker codelets are instantiated

        //Locations
        List<Idea> locationsList = new ArrayList<>();
        locationsMO = createMemoryObject("LOCATION", locationsList);
        //--------
        //Leaflets
        Idea leafletsIdea = new Idea("Leaflets", null, 0);
        leafletsMO = createMemoryObject("LEAFLETS", leafletsIdea);
        //registerMemory(leafletsMO, "Perceptual");
        //Impulses
        //Idea impulsesIdea = new Idea("Impulses", null, 0);
        impulsesMO = createMemoryContainer("IMPULSES");
        registerMemory(impulsesMO, "Context");

        episodeBoundariesMO = createMemoryObject("BOUNDARIES", new Idea("Boundaries"));


        //Hands
        handsMO = createMemoryContainer("HANDS");
        //Leags
        legsMO = createMemoryContainer("LEGS");

        //Inner Sense Codelet
        Codelet innerSenseCodelet = new Propriosensor(env.creature);
        innerSenseCodelet.addOutput(propriosensorMO);
        insertCodelet(innerSenseCodelet, "Sensory");

        Codelet selfGridLocator = new GridLocatorCodelet("PROPRIOSENSOR", "INNER");
        selfGridLocator.addInput(propriosensorMO);
        selfGridLocator.addOutput(innerSenseMO);
        selfGridLocator.addInput(roomsMO);
        selfGridLocator.addInput(categoriesRoomMO);
        insertCodelet(selfGridLocator);

        //Vision Sensor Codelet
        Codelet visionCodelet = new Vision(env.creature);
        visionCodelet.addOutput(visionMO);
        insertCodelet(visionCodelet, "Sensory");

        //Leaflet Sense Codelet
        Codelet leafletSenseCodelet = new LeafletSense(env.creature);
        leafletSenseCodelet.addOutput(leafletsMO);
        insertCodelet(leafletSenseCodelet, "Sensory");

        //Food Detector Codelet
        Codelet foodDetectorCodelet = new FoodDetector();
        foodDetectorCodelet.addInput(visionMO);
        foodDetectorCodelet.addInput(knownFoodsMO);
        foodDetectorCodelet.addInput(roomsMO);
        foodDetectorCodelet.addOutput(foodPerceptionMO);
        insertCodelet(foodDetectorCodelet, "Perception");

        Codelet foodsLearner = new PerceptualLearnerCodelet("FOOD", "KNOWN_FOODS");
        foodsLearner.setIsMemoryObserver(true);
        foodsLearner.addInput(foodPerceptionMO);
        foodsLearner.addOutput(knownFoodsMO);
        insertCodelet(foodsLearner, "Perception");

        //Codelet foodGridLocator = new GridLocatorCodelet("FOOD", "FOOD");
        //foodGridLocator.addInput(foodPerceptionMO);
        //foodGridLocator.addOutput(foodPerceptionMO);
        //foodGridLocator.addInput(categoriesRoomMO);
        //foodGridLocator.addInput(roomsMO);
        //insertCodelet(foodGridLocator);

        //Agent Detector
        Codelet agentDetectorCodelet = new AgentDetector();
        agentDetectorCodelet.addInput(visionMO);
        agentDetectorCodelet.addInput(roomsMO);
        agentDetectorCodelet.addInput(knownAgentsMO);
        agentDetectorCodelet.addOutput(agentPerceptionMO);
        insertCodelet(agentDetectorCodelet, "Perception");

        Codelet agentLearner = new PerceptualLearnerCodelet("AGENTS", "KNOWN_AGENTS");
        agentLearner.setIsMemoryObserver(true);
        agentLearner.addInput(agentPerceptionMO);
        agentLearner.addOutput(knownAgentsMO);
        insertCodelet(agentLearner, "Perception");

        //Codelet agentGridLocator = new GridLocatorCodelet("AGENTS", "AGENTS");
        //agentGridLocator.addInput(agentPerceptionMO);
        //agentGridLocator.addOutput(agentPerceptionMO);
        //agentGridLocator.addInput(roomsMO);
        //agentGridLocator.addInput(categoriesRoomMO);
        //insertCodelet(agentGridLocator, "Perception");

        //Jewel Detector Codelet
        Codelet jewelDetectorCodelet = new JewelDetector(debug);
        jewelDetectorCodelet.addInput(visionMO);
        jewelDetectorCodelet.addInput(knownJewelsMO);
        jewelDetectorCodelet.addInput(roomsMO);
        jewelDetectorCodelet.addOutput(jewelsPerceptionMO);
        jewelDetectorCodelet.addOutput(jewelsCounterMO);
        insertCodelet(jewelDetectorCodelet, "Perception");

        Codelet jewelLearner = new PerceptualLearnerCodelet("JEWELS", "KNOWN_JEWELS");
        jewelLearner.setIsMemoryObserver(true);
        jewelLearner.addInput(jewelsPerceptionMO);
        jewelLearner.addOutput(knownJewelsMO);
        insertCodelet(jewelLearner, "Perception");


        //Codelet jewelGridLocator = new GridLocatorCodelet("JEWELS", "JEWELS");
        //jewelGridLocator.addInput(jewelsPerceptionMO);
        //jewelGridLocator.addOutput(jewelsPerceptionMO);
        //jewelGridLocator.addInput(roomsMO);
        //jewelGridLocator.addInput(categoriesRoomMO);
        //insertCodelet(jewelGridLocator);

        //Walls Detector Codelet
        Codelet wallsDetectorCodelet = new WallDetector(debug);
        wallsDetectorCodelet.addInput(visionMO);
        wallsDetectorCodelet.addInput(roomsMO);
        wallsDetectorCodelet.addInput(knownWallsMO);
        wallsDetectorCodelet.addOutput(wallsPerceptionMO);
        insertCodelet(wallsDetectorCodelet, "Perception");

        Codelet wallsLearner = new PerceptualLearnerCodelet("WALLS", knownWallsMO.getName());
        wallsLearner.setIsMemoryObserver(true);
        wallsLearner.addInput(wallsPerceptionMO);
        wallsLearner.addOutput(knownWallsMO);
        insertCodelet(wallsLearner, "Perception");

        //Codelet wallGridLocator = new GridLocatorCodelet("WALLS", "WALLS");
        //wallGridLocator.addInput(roomsMO);
        //wallGridLocator.addInput(categoriesRoomMO);
        //wallGridLocator.addInput(wallsPerceptionMO);
        //wallGridLocator.addOutput(wallsPerceptionMO);
        //insertCodelet(wallGridLocator);

        //RoomDetector Codelet
        Codelet roomDetectorCodelet = new RoomDetector();
        roomDetectorCodelet.addInput(propriosensorMO);
        roomDetectorCodelet.addInput(categoriesRoomMO);
        roomDetectorCodelet.addOutput(roomsMO);
        insertCodelet(roomDetectorCodelet, "Perception");

        //Move Event Codelet
        Idea moveEventCategory = constructEventCategory("Move", Arrays.asList("Position.X", "Position.Y"), "Linear");
        ///Idea moveEventCategory = constructEventCategory("Move", Arrays.asList("Self.Position.X", "Self.Position.Y"), "Linear");
        eventsCategoriesIdea.add(moveEventCategory);
        EventTracker moveEventTracker = new EventTracker(this, "PERCEPTUAL_BUFFER", "EVENTS", moveEventCategory, debug);
        moveEventTracker.setBufferSize(2);
        moveEventTracker.setBufferStepSizeInMillis(150);
        moveEventTracker.addInput(perceptualBufferMO);
        moveEventTracker.addOutput(eventsMO);
        moveEventTracker.addInput(episodeBoundariesMO);
        insertCodelet(moveEventTracker, "Perception");

        //Rotate Event Codelet
        Idea rotateEventCategory = constructEventCategory("Rotate", Arrays.asList("Pitch"), "Linear");
        ///Idea rotateEventCategory = constructEventCategory("Rotate", Arrays.asList("Self.Pitch"), "Linear");
        EventTracker rotateEventTracker = new EventTracker(this, "PERCEPTUAL_BUFFER", "EVENTS", rotateEventCategory, debug);
        rotateEventTracker.setBufferSize(2);
        rotateEventTracker.setBufferStepSizeInMillis(150);
        rotateEventTracker.addInput(perceptualBufferMO);
        rotateEventTracker.addOutput(eventsMO);
        rotateEventTracker.addInput(episodeBoundariesMO);
        insertCodelet(rotateEventTracker, "Perception");

        Idea shrinkEventCategory = constructEventCategory("Collected", Arrays.asList("Size.X", "Size.Y", "Size.Z"), "Linear");
        EventTracker shrinkEventTracker = new EventTracker(this, "PERCEPTUAL_BUFFER", "EVENTS", shrinkEventCategory, debug);
        shrinkEventTracker.setBufferSize(2);
        shrinkEventTracker.setBufferStepSizeInMillis(50);
        shrinkEventTracker.addInput(perceptualBufferMO);
        shrinkEventTracker.addOutput(eventsMO);
        shrinkEventTracker.addInput(episodeBoundariesMO);
        insertCodelet(shrinkEventTracker, "Perception");

        //Found Jewel Event
        ////for (Constants.JewelTypes type : Constants.JewelTypes.values()){
        ////    Idea foundJewelEventCategory = constructEventCategory("Found_" + type.typeName(), Arrays.asList(type.typeName()), "StepUp");
        ////    ///Idea foundJewelEventCategory = constructEventCategory("Found_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepUp");
        ////    EventTracker jewelFoundEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", foundJewelEventCategory, debug);
        ////    jewelFoundEventTracker.setBufferSize(2);
        ////    jewelFoundEventTracker.setBufferStepSize(2);
        ////    jewelFoundEventTracker.addInput(jewelsCounterMO);
        ////    jewelFoundEventTracker.addOutput(eventsMO);
        ////    jewelFoundEventTracker.addBroadcast(contextDriftMO);
        ////    insertCodelet(jewelFoundEventTracker, "Perception");
        ////}

        //////Collect Jewel Event
        ////for (Constants.JewelTypes type : Constants.JewelTypes.values()){
        ////    Idea collectJewelEventCategory = constructEventCategory("Collected_" + type.typeName(), Arrays.asList(type.typeName()), "StepDown");
        ////    ///Idea collectJewelEventCategory = constructEventCategory("Collected_" + type.typeName(), Arrays.asList("JewelsCounters." + type.typeName()), "StepDown");
        ////    EventTracker jewelCollectedEventTracker = new EventTracker("JEWELS_COUNTERS", "EVENTS", collectJewelEventCategory, debug);
        ////    jewelCollectedEventTracker.setBufferSize(2);
        ////    jewelCollectedEventTracker.setBufferStepSize(2);
        ////    jewelCollectedEventTracker.addInput(jewelsCounterMO);
        ////    jewelCollectedEventTracker.addOutput(eventsMO);
        ////    jewelCollectedEventTracker.addBroadcast(contextDriftMO);
        ////    insertCodelet(jewelCollectedEventTracker, "Perception");
        ////}

        //Impulses
        //Go to jewel
        Codelet goToJewelImpulse = new GoToJewelImpulse();
        goToJewelImpulse.addInput(innerSenseMO);
        goToJewelImpulse.addInput(knownJewelsMO);
        goToJewelImpulse.addInput(leafletsMO);
        goToJewelImpulse.addOutput(impulsesMO);
        insertCodelet(goToJewelImpulse, "Behavioral");

        //Go to Food
        Codelet goToFoodImpulse = new GoToFoodImpulse();
        goToFoodImpulse.addInput(innerSenseMO);
        goToFoodImpulse.addInput(knownFoodsMO);
        goToFoodImpulse.addOutput(impulsesMO);
        insertCodelet(goToFoodImpulse, "Behavioral");

        //Collect Jewel
        Codelet collectJewelImpulse = new CollectJewelImpulse();
        collectJewelImpulse.addInput(innerSenseMO);
        collectJewelImpulse.addInput(knownJewelsMO);
        collectJewelImpulse.addOutput(impulsesMO);
        insertCodelet(collectJewelImpulse, "Behavioral");

        //Collect Food
        Codelet collectFoodImpulse = new EatFoodImpulse();
        collectFoodImpulse.addInput(innerSenseMO);
        collectFoodImpulse.addInput(knownFoodsMO);
        collectFoodImpulse.addOutput(impulsesMO);
        insertCodelet(collectFoodImpulse, "Behavioral");

        Memory extra;
        extra = createMemoryObject("extra");
        //Explore
        Codelet exploreImpulse = new ExploreImpulse();
        exploreImpulse.addInput(knownJewelsMO);
        exploreImpulse.addInput(innerSenseMO);
        exploreImpulse.addInput(roomsMO);
        exploreImpulse.addInput(locationsMO);
        exploreImpulse.addInput(EPLTMO);
        exploreImpulse.addInput(categoriesRoomMO);
        exploreImpulse.addOutput(impulsesMO);
        insertCodelet(exploreImpulse, "Behavioral");


        //Move Action/Behaviour
        Codelet moveActionCodelet = new Move();
        moveActionCodelet.addInput(impulsesMO);
        moveActionCodelet.addInput(knownWallsMO);
        moveActionCodelet.addOutput(legsMO);
        moveActionCodelet.addInput(innerSenseMO);
        moveActionCodelet.addInput(locationsMO);
        moveActionCodelet.addInput(roomsMO);
        moveActionCodelet.addInput(EPLTMO);
        moveActionCodelet.addOutput(extra);
        insertCodelet(moveActionCodelet, "Behavioral");

        //Collect Action/Behaviour
        Codelet collectActionCodelet = new Collect();
        collectActionCodelet.addInput(impulsesMO);
        collectActionCodelet.addInput(knownJewelsMO);
        collectActionCodelet.addOutput(handsMO);
        collectActionCodelet.addOutput(jewelsCounterMO);
        insertCodelet(collectActionCodelet, "Behavioral");

        //Eat Action
        Codelet eatAction = new Eat();
        eatAction.addInput(impulsesMO);
        eatAction.addInput(knownFoodsMO);
        eatAction.addOutput(handsMO);
        insertCodelet(eatAction, "Behavioral");

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
        episodeBindingCodelet.addInput(contextBufferMO);
        episodeBindingCodelet.addInput(perceptualBufferMO);
        episodeBindingCodelet.addInput(episodeBoundariesMO);
        episodeBindingCodelet.addOutput(storyMO);
        insertCodelet(episodeBindingCodelet, "Behavioural");

        BufferCodelet perceptualBufferCodelet = new BufferCodelet();
        perceptualBufferCodelet.setCheckPerception(true);
        perceptualBufferCodelet.addInputs(getMemoryGroupList("Perceptual"));
        perceptualBufferCodelet.addOutput(perceptualBufferMO);
        insertCodelet(perceptualBufferCodelet);

        Codelet contextBufferCodelet = new BufferCodelet();
        contextBufferCodelet.addInputs(getMemoryGroupList("Context"));
        contextBufferCodelet.addInput(innerSenseMO);
        contextBufferCodelet.addOutput(contextBufferMO);
        insertCodelet(contextBufferCodelet);

        Codelet episodeBoundaryCodelet = new EpisodeBoundaryDetection();
        episodeBoundaryCodelet.addInput(contextBufferMO);
        episodeBoundaryCodelet.addOutput(episodeBoundariesMO);
        insertCodelet(episodeBoundaryCodelet);

        Idea locAdpatHabit = new Idea("LocationAdaptHabit", null);
        locAdpatHabit.setValue(new LocationCategoryModification(locAdpatHabit));
        Idea locGenHabit = new Idea("LocationGeneratorHabit", null);
        locGenHabit.setValue(new LocationCategoryGenerator());
        Idea propertiesLearningHabit = new Idea("PropertiesLearningHabit", null);
        propertiesLearningHabit.setValue(new TrackedPropertiesAssimilateAccommodateHabit(propertiesLearningHabit));
        propertiesLearningHabit.add(new Idea("Input_Category", null, "Configuration", 1));
        propertiesLearningHabit.add(new Idea("categories", null, "Aggregation", 1));
        Idea subHabits = new Idea("property_habits", null, "Aggregation", 1);
        Idea assimilateSubHabit = new Idea("assimilate", null);
        assimilateSubHabit.setValue(new AssimilatePropertyCategory(assimilateSubHabit));
        assimilateSubHabit.add(new Idea("properties", null, "Property", 1));
        assimilateSubHabit.add(new Idea("samples", null, "Property", 1));
        Idea accommodateSubHabit = new Idea("accommodate", null);
        accommodateSubHabit.setValue(new AccommodatePropertyCategory(accommodateSubHabit));
        accommodateSubHabit.add(new Idea("properties", null, "Property", 1));
        accommodateSubHabit.add(new Idea("samples", null, "Property", 1));
        subHabits.add(assimilateSubHabit);
        subHabits.add(accommodateSubHabit);
        propertiesLearningHabit.add(subHabits);

        Memory propertiesMO;
        propertiesMO = createMemoryObject("PROPERTIES", new ArrayList<Idea>());
        Codelet episodicGistCodelet = new EpisodicGistExtraction(locAdpatHabit, locGenHabit, propertiesLearningHabit);
        episodicGistCodelet.addInput(storyMO);
        episodicGistCodelet.addInput(locationsMO);
        episodicGistCodelet.addOutput(EPLTMO);
        episodicGistCodelet.addInput(propertiesMO);
        insertCodelet(episodicGistCodelet, "Behavioural");

        bList.add(wallsDetectorCodelet);
        for (Codelet c : this.getCodeRack().getAllCodelets()) {
            c.setTimeStep(100);
            c.setProfiling(true);
        }

        //perceptualBufferCodelet.setTimeStep(250);

        start();
    }

    private Idea initializeInnerSenseIdea() {
        Idea innerSense = new Idea("Self", "AGENT", "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", 0, 3));
        posIdea.add(new Idea("Y", 0, 3));
        innerSense.add(posIdea);
        innerSense.add(new Idea("Pitch", null, "Property", 1));
        innerSense.add(new Idea("Fuel", null, "Property", 1));
        innerSense.add(new Idea("Step", 0, "TimeStep", 1));
        innerSense.add(new Idea("TimeStamp", System.currentTimeMillis(), "Property", 1));

        return innerSense;
    }

    public static Idea constructRoomCategory(String name, Vector2D cornerA, Vector2D cornerB) {
        Idea idea = new Idea(name, null, "AbstractObject", 0);
        idea.setValue(new RoomCategoryIdeaFunctions(idea, name, cornerA, cornerB));
        idea.add(new Idea("Adjacent", null, "Link", 1));
        Idea center = new Idea("center", null, "Property", 1);
        Vector2D middle = Vector2D.middlePoint(cornerA, cornerB);
        center.add(new Idea("x", middle.getX(), "QualityDimension", 1));
        center.add(new Idea("y", middle.getY(), "QualityDimension", 1));
        idea.add(center);
        idea.add(new Idea("Exits", null, "Link", 1));
        return idea;
    }

    private Idea constructEventCategory(String name, List<String> properties, String type) {
        Idea idea = new Idea(name, null, "Episode", 2);
        ///String object = properties.get(0).split("\\.")[0];
        ///idea.add(new Idea("ObservedObject", object, "Property", 1));
        ///List<String> cleanProperties = properties.stream().map(s->s.substring(s.indexOf(".") + 1)).collect(Collectors.toList());
        ///idea.add(new Idea("properties", cleanProperties, "Property", 1));
        idea.add(new Idea("properties", properties, "Property", 1));

        switch (type) {
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
