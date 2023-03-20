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
    private boolean debug = false;

    public InnerSense(Agent nc) {
            agent = nc;
            this.name = "InnerSense";
    }

    public InnerSense(Agent nc, boolean debug) {
        agent = nc;
        this.name = "InnerSense";
        this.debug = debug;
    }
    
    @Override
    public void accessMemoryObjects() {
            innerSenseMO=(MemoryObject)this.getOutput("INNER");
            cis = (Idea) innerSenseMO.getI();
    }

    public void proc() {
        cis.get("Position").get("X").setValue(agent.getPosition().get(0));
        cis.get("Position").get("Y").setValue(agent.getPosition().get(1));
        cis.get("Pitch").setValue(agent.getPitch());
        cis.get("Fuel").setValue(agent.getFuel());
        int step = (int) cis.get("Step").getValue();
        cis.get("Step").setValue(step + 1);
        if (debug) {
            System.out.println(cis.toStringFull());
        }
    }

    @Override
    public void calculateActivation() {

    }
}
