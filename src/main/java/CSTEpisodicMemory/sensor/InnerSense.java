/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CSTEpisodicMemory.sensor;

import WS3DCoppelia.model.Agent;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

/**
 *
 * @author bruno
 */
public class InnerSense extends Codelet{
    private Memory innerSenseMO;
    private Agent agent;
    private Idea cis;

    public InnerSense(Agent nc) {
            agent = nc;
            this.name = "InnerSense";
    }
    
    @Override
    public void accessMemoryObjects() {
            innerSenseMO=(MemoryObject)this.getOutput("INNER");
            cis = (Idea) innerSenseMO.getI();
    }

    public void proc() {
         cis.get("position").setValue(agent.getPosition());
         cis.get("pitch").setValue(agent.getPitch());
         cis.get("fuel").setValue(agent.getFuel());
    }

    @Override
    public void calculateActivation() {

    }
}
