package CSTEpisodicMemory.core.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;

public abstract class MemoryCodelet extends Codelet {

    private Memory internalMO;
    private static int count = 0;

    public MemoryCodelet(Mind m, String name){
        internalMO = m.createMemoryObject(name + "_internal_memory");
        this.addInput(internalMO);
    }

    public Object getInternalMemoryI(){
        return internalMO.getI();
    }

    public void setInternalI(Object info){
        internalMO.setI(info);
    }
}
