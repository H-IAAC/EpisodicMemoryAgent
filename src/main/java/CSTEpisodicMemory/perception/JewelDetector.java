/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory.perception;

import WS3DCoppelia.model.Identifiable;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author bruno
 */
public class JewelDetector extends Codelet {

    private Memory visionMO;
    private Memory knownJewelsMO;
    private Memory jewelsCountersMO;
    private boolean debug = false;

    public JewelDetector() {
        this.name = "JewelDetector";
    }

    public JewelDetector(boolean debug) {
        this.name = "JewelDetector";
        this.debug = debug;
    }

    @Override
    public void accessMemoryObjects() {
        this.visionMO = (MemoryObject) this.getInput("VISION");
        this.knownJewelsMO = (MemoryObject) this.getOutput("JEWELS");
        this.jewelsCountersMO = (MemoryObject) getOutput("JEWELS_COUNTERS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (visionMO) {
            synchronized (knownJewelsMO) {

                CopyOnWriteArrayList<Identifiable> vision = new CopyOnWriteArrayList((List<Identifiable>) visionMO.getI());
                Idea jewelsIdea = (Idea) knownJewelsMO.getI();
                if (debug) {
                    System.out.println(jewelsIdea.toStringFull());
                }
                jewelsIdea.setL(new ArrayList<>());
                for (Identifiable obj : vision) {
                    if (obj instanceof Thing) {
                        Thing t = (Thing) obj;
                        if (t.isJewel()) {
                            jewelsIdea.add(constructJewelIdea(t));
                            //synchronized (jewelsCountersMO) {
                            //    Idea jewelsCountersIdea = (Idea) jewelsCountersMO.getI();
                            //    List<Idea> counters = jewelsCountersIdea.getL();
                            //    jewelsCountersIdea.get("Step").setValue((int) jewelsCountersIdea.get("Step").getValue() + 1);
                            //    jewelsCountersIdea.get("TimeStamp").setValue(System.currentTimeMillis());
                            //    for (Idea counter : counters) {
                            //        if (counter.getName().equals(t.getTypeName())) {
                            //            int count = (int) counter.getValue() + 1;
                            //            counter.setValue(count);
                            //        }
                            //    }
                            //}
                        }
                        //synchronized (jewelsCountersMO) {
                        //    Idea jewelsCountersIdea = (Idea) jewelsCountersMO.getI();
                        //    List<Idea> counters = jewelsCountersIdea.getL();
                        //    jewelsCountersIdea.get("Step").setValue((int) jewelsCountersIdea.get("Step").getValue() + 1);
                        //    jewelsCountersIdea.get("TimeStamp").setValue(System.currentTimeMillis());
                        //}
                    }
                }
            }
        }
    }

    public static Idea constructJewelIdea(Thing t) {

        Idea jewelIdea = new Idea("Jewel", t.getTypeName(), "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X", t.getPos().get(0), "QualityDimension", 1));
        posIdea.add(new Idea("Y", t.getPos().get(1), "QualityDimension", 1));
        jewelIdea.add(posIdea);
        Idea color = new Idea("Color", t.getTypeName().split("_")[0], "Property", 1);
        color.add(new Idea("R", t.getColor().get(0), "QualityDimension", 1));
        color.add(new Idea("G", t.getColor().get(1), "QualityDimension", 1));
        color.add(new Idea("B", t.getColor().get(2), "QualityDimension", 1));
        jewelIdea.add(color);
        jewelIdea.add(new Idea("ID", t.getId(), "Property", 1));
        return jewelIdea;
    }
}
