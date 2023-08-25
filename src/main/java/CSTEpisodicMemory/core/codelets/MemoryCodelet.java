package CSTEpisodicMemory.core.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;

public abstract class MemoryCodelet extends Codelet {

    private Memory internalMO;

    public MemoryCodelet(Mind m){
        internalMO = m.createMemoryObject("internal_memory");
        this.addInput(internalMO);
    }

    public Object getInternalMemoryI(){
        return internalMO.getI();
    }

    public void setInternalI(Object info){
        internalMO.setI(info);
    }
}
