/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory.sensor;

import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Vision extends Codelet {
    private Memory visionMO;
    private final Agent agent;


    public Vision(Agent nc) {
        agent = nc;		
        this.name = "Vision";
    }

    @Override
    public void accessMemoryObjects() {
            visionMO=(MemoryObject)this.getOutput("VISION");
    }

    @Override
    public void proc() {

         synchronized (visionMO) {
            List<Thing> lt = agent.getThingsInVision();
            //System.out.println("Vision:" + lt.toString());
            visionMO.setI(lt);
            //Class cl = List.class;
            //visionMO.setT(cl);
         }
    }//end proc()

    @Override
    public void calculateActivation() {

    }
}
