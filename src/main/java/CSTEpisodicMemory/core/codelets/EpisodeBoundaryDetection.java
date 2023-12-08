package CSTEpisodicMemory.core.codelets;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.Iterator;
import java.util.List;

public class EpisodeBoundaryDetection extends Codelet {

    private Memory contextBufferMO;
    private Idea buffer;
    private Memory episodeBoundariesMO;
    private Idea boundaries;

    private long lastCheckedTimeStamp = -1L;

    public EpisodeBoundaryDetection(){
        System.out.println("Created");
        this.name = "Episode_Boundary";
    }
    @Override
    public void accessMemoryObjects() {
        contextBufferMO = (MemoryObject) getInput("CONTEXT_BUFFER");
        buffer = (Idea) contextBufferMO.getI();
        episodeBoundariesMO = (MemoryObject) getOutput("BOUNDARIES");
        boundaries = (Idea) episodeBoundariesMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        synchronized (buffer){
            Iterator<Idea> bufferIt = buffer.getL().iterator();
            Idea prevStep = null;
            if(bufferIt.hasNext())
                prevStep = bufferIt.next();
            while (bufferIt.hasNext()){
                Idea thisStep = bufferIt.next();
                if (lastCheckedTimeStamp < (long) thisStep.getValue()) {
                    lastCheckedTimeStamp = (long) thisStep.getValue();
                    for (Idea context : thisStep.getL()) {

                        if (!context.getName().equals("Self") && prevStep.getL().stream().noneMatch(c -> IdeaHelper.match(c, context))) {
                            addEpisodeBoundary(context, prevStep.getValue());
                        }
                    }
                }
                prevStep = thisStep;
            }
        }
    }

    private void addEpisodeBoundary(Idea context, Object value) {
        //Hard coded boundary types
        if (context.getCategory().equals("Goal")){
            Idea epBoundary = new Idea("EP Boundary", "Hard", "Property", 1);
            epBoundary.add(new Idea("TimeStamp", value, "Property", 1));
            synchronized (episodeBoundariesMO) {
                boundaries.add(epBoundary);
                episodeBoundariesMO.setI(boundaries);
            }
        } else if(context.getCategory().equals("AbstractObject")){
            Idea epBoundary = new Idea("EP Boundary", "Soft", "Property", 1);
            epBoundary.add(new Idea("TimeStamp", value, "Property", 1));
            synchronized (episodeBoundariesMO) {
                boundaries.add(epBoundary);
                episodeBoundariesMO.setI(boundaries);
            }

        }
    }
}
