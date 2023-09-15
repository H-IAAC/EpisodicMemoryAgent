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
public class Propriosensor extends Codelet{
    private Memory innerSenseMO;
    private final Agent agent;
    private Idea cis;
    private boolean debug = false;

    public Propriosensor(Agent nc) {
            agent = nc;
            this.name = "Propriosensor";
    }

    public Propriosensor(Agent nc, boolean debug) {
        agent = nc;
        this.name = "Propriosensor";
        this.debug = debug;
    }
    
    @Override
    public void accessMemoryObjects() {
            innerSenseMO=(MemoryObject)this.getOutput("PROPIOSENSOR");
            cis = (Idea) innerSenseMO.getI();
    }

    public void proc() {
        synchronized (cis) {
            cis.get("Position").get("X").setValue(agent.getPosition().get(0));
            cis.get("Position").get("Y").setValue(agent.getPosition().get(1));
            cis.get("Pitch").setValue(agent.getPitch());
            cis.get("Fuel").setValue(agent.getFuel());
            int step = (int) cis.get("Step").getValue();
            cis.get("Step").setValue(step + 1);
            cis.get("TimeStamp").setValue(System.currentTimeMillis());
            if (debug) {
                System.out.println(cis.toStringFull());
            }
        }
        synchronized (innerSenseMO) {
            innerSenseMO.setI(cis);
        }
    }

    @Override
    public void calculateActivation() {

    }
}
