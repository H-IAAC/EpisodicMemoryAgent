/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.context.GoalSelector;
import CSTEpisodicMemory.event.MoveEventTracker;
import CSTEpisodicMemory.perception.JewelDetector;
import CSTEpisodicMemory.perception.WallDetector;
import CSTEpisodicMemory.sensor.InnerSense;
import CSTEpisodicMemory.sensor.Vision;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.EventListener;
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
        //createCodeletGroup("Motor");
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


        //Inner Sense Codelet
        Codelet innerSenseCodelet = new InnerSense(env.creature);
        innerSenseCodelet.addOutput(innerSenseMO);
        insertCodelet(innerSenseCodelet, "Sensory");

        //Vision Sensor Codelet
        Codelet visionCodelet = new Vision(env.creature);
        visionCodelet.addOutput(visionMO);
        insertCodelet(visionCodelet, "Sensory");

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

        //Move Event Codelet
        Codelet moveEventTracker = new MoveEventTracker();
        moveEventTracker.addInput(innerSenseMO);
        moveEventTracker.addOutput(eventsMO);
        insertCodelet(moveEventTracker, "Perception");

        //Goal selector Codelet
        Codelet goalSelectorCodelet = new GoalSelector();
        goalSelectorCodelet.addInput(innerSenseMO);
        goalSelectorCodelet.addInput(knownJewelsMO);
        goalSelectorCodelet.addInput(wallsMO);
        goalSelectorCodelet.addOutput(goalsMO);
        insertCodelet(goalSelectorCodelet, "Context");

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
