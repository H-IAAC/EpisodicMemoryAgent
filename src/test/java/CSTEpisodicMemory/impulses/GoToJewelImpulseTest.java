package CSTEpisodicMemory.impulses;

import CSTEpisodicMemory.perception.JewelDetector;
import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Test;

public class GoToJewelImpulseTest {

    public MemoryObject createInnerMemory(){
        MemoryObject inner = new MemoryObject();
        Idea innerIdea = new Idea("Self", null, 0);
        inner.setI(innerIdea);
        inner.setName("INNER");
        return inner;
    }

    public MemoryObject createJewelsMemory(WS3DCoppelia world){
        MemoryObject jewels = new MemoryObject();
        Thing wantJewel = world.createThing(Constants.JewelTypes.BLUE_JEWEL, 1f, 1f);
        jewels.setName("KNOWN_JEWELS");
        Idea jewel = new Idea("Jewels", null, 0);
        jewel.add(JewelDetector.constructJewelIdea(wantJewel));
        jewels.setI(jewel);
        return jewels;
    }
    @Test
    public void codeletTest(){
        WS3DCoppelia world = new WS3DCoppelia();
        GoToJewelImpulse codelet = new GoToJewelImpulse();
        codelet.addInput(createInnerMemory());
        codelet.addInput(createJewelsMemory(world));
    }
}
