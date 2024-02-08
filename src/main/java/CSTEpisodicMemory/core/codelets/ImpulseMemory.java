package CSTEpisodicMemory.core.codelets;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;

public class ImpulseMemory extends MemoryContainer {

    private Memory lastReturn = null;

    public ImpulseMemory(MemoryContainer mem) {
        super(mem.getName());
    }

    @Override
    public synchronized Object getI() {
        Object get = super.getI();

        for (Memory mem : getAllMemories()) {
            if (mem.getI() == get) {
                if (lastReturn != null) {
                    if (lastReturn.getEvaluation().equals(mem.getEvaluation())) {
                        return lastReturn.getI();
                    }
                    System.out.println(mem.getEvaluation() + " - " + lastReturn.getEvaluation());
                }
                lastReturn = mem;
            }
        }
        //System.out.println("CHANGE @@@@@@@@@");
        return get;
    }
}
