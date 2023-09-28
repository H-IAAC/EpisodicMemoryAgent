package CSTEpisodicMemory.sensor;

import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Bag;
import WS3DCoppelia.model.Leaflet;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

public class LeafletSense extends Codelet {

    private final Agent agent;
    private Memory leafletSenseMO;
    private Idea leaflets;

    public LeafletSense(Agent nc){
        agent = nc;
        this.name = "LeafletSense";
    }

    @Override
    public void accessMemoryObjects() {
        leafletSenseMO = (MemoryObject) this.getOutput("LEAFLETS");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (leafletSenseMO) {
        leaflets = (Idea) leafletSenseMO.getI();
        Bag bag = agent.getBag();
        int id = 1;
        Leaflet[] leafletList = agent.getLeaflets();
            for (Leaflet leaflet : leafletList) {
                //Get leaflet or created if does not exist
                Idea leafletIdea = leaflets.get(String.format("LEAFLET_%d", id));
                if (leafletIdea == null) {
                    leafletIdea = leaflets.add(new Idea(String.format("LEAFLET_%d", id), null, 0));
                    for (Constants.JewelTypes jewel : Constants.JewelTypes.values()) {
                        Idea colorIdea = new Idea(jewel.typeName(), null, 0);
                        colorIdea.add(new Idea("Need", 0, 1));
                        colorIdea.add(new Idea("Has", 0, 1));
                        colorIdea.add(new Idea("Remained", 0, 1));
                        leafletIdea.add(colorIdea);
                    }
                    leafletIdea.add(new Idea("Completed", false, 1));
                    leafletIdea.add(new Idea("ID", false, 1));
                }

                for (Constants.JewelTypes jewel : Constants.JewelTypes.values()) {
                    Idea colorIdea = leafletIdea.get(jewel.typeName());
                    colorIdea.get("Need").setValue(leaflet.getRequiredAmountOf(jewel));
                    colorIdea.get("Has").setValue(bag.getTotalCountOf(jewel));
                    colorIdea.get("Remained").setValue(leaflet.getRequiredAmountOf(jewel) - bag.getTotalCountOf(jewel));
                }

                leafletIdea.get("ID").setValue(leaflet.getId());

                id += 1;
            }
            leafletSenseMO.setI(leaflets);
        }
    }
}
