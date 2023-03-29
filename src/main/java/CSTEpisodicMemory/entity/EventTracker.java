package CSTEpisodicMemory.entity;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.LinkedList;
import java.util.List;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public abstract class EventTracker extends Codelet {

    private String inputMemoryName = "PERCEPTION_MEMORY";
    private String outputMemoryName = "EVENTS_MEMORY";
    private Memory perceptionInputMO;
    private Memory eventsOutputMO;

    private int bufferSize = 1;
    private int bufferStepSize = 1;
    private List<Idea> inputIdeaBuffer = new LinkedList<Idea>();
    private Idea initialEventIdea;
    private Idea currentInputIdea;
    private String eventCategoryName = null;
    private int count = 1;

    public EventTracker(String inputMemoryName, String outputMemoryName) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
    }

    @Override
    public void accessMemoryObjects() {
        this.perceptionInputMO=(MemoryObject)this.getInput(this.getInputMemoryName());
        this.currentInputIdea = (Idea) perceptionInputMO.getI();
        this.eventsOutputMO=(MemoryObject)this.getOutput(this.getOutputMemoryName());
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        //Initialize event track buffer
        if (inputIdeaBuffer.size() == 0) {
            inputIdeaBuffer.add(currentInputIdea.clone());
        } else {
            if (inputIdeaBuffer.size() < this.bufferSize) {
                if (((int) currentInputIdea.get("Step").getValue())
                        - ((int) inputIdeaBuffer.get(inputIdeaBuffer.size() - 1).get("Step").getValue())
                        >= bufferStepSize) {
                    inputIdeaBuffer.add(currentInputIdea.clone());
                }
            } else {
                if (((int) currentInputIdea.get("Step").getValue())
                        - ((int) inputIdeaBuffer.get(inputIdeaBuffer.size() - 1).get("Step").getValue())
                        >= bufferStepSize) {
                    //Check if current state is coherent with previous states and event category
                    boolean check = belongsToEvent(inputIdeaBuffer, currentInputIdea);
                    if (check) {
                        Idea drop = inputIdeaBuffer.remove(0);
                        //Copies start of the event
                        if (initialEventIdea == null) this.initialEventIdea = drop.clone();
                        inputIdeaBuffer.add(currentInputIdea.clone());
                    } else {
                        if (initialEventIdea != null) {
                            Idea event = constructEventIdea(inputIdeaBuffer.get(inputIdeaBuffer.size()-1));
                            inputIdeaBuffer.clear();
                            inputIdeaBuffer.add(currentInputIdea.clone());
                            initialEventIdea = null;
                            Idea eventsIdea = (Idea) eventsOutputMO.getI();
                            eventsIdea.add(event);
                            System.out.println(fullPrint(eventsIdea));
                        } else {
                            inputIdeaBuffer.remove(0);
                            inputIdeaBuffer.add(currentInputIdea.clone());
                        }
                    }
                }
            }
        }
    }

    private Idea constructEventIdea(Idea lastEventIdea) {
        Idea eventIdea = new Idea("Event" + count++, this.eventCategoryName, "Episode", 1);
        Idea time1 = new Idea("", 1, "TimeStep", 1);
        Idea time2 = new Idea("", 2, "TimeStep", 1);
            time1.add(extractRelevant(this.initialEventIdea));
            time2.add(extractRelevant(lastEventIdea));
            eventIdea.add(time1);
            eventIdea.add(time2);
            return eventIdea;
    }

    public abstract boolean belongsToEvent(List<Idea> inputIdeaBuffer, Idea currentInputIdea);

    public abstract Idea extractRelevant(Idea idea);

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize > 0)
            this.bufferSize = bufferSize;
    }

    public int getBufferStepSize() {
        return bufferStepSize;
    }

    public void setBufferStepSize(int bufferStepSize) {
        if (bufferStepSize > 0)
            this.bufferStepSize = bufferStepSize;
    }

    public List<Idea> getInputIdeaBuffer() {
        return inputIdeaBuffer;
    }

    public void setInputIdeaBuffer(List<Idea> inputIdeaBuffer) {
        this.inputIdeaBuffer = inputIdeaBuffer;
    }

    public Idea getInitialEventIdea() {
        return initialEventIdea;
    }

    public void setInitialEventIdea(Idea initialEventIdea) {
        this.initialEventIdea = initialEventIdea;
    }

    public Idea getCurrentInputIdea() {
        return currentInputIdea;
    }

    public void setCurrentInputIdea(Idea currentInputIdea) {
        this.currentInputIdea = currentInputIdea;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getInputMemoryName() {
        return inputMemoryName;
    }

    public void setInputMemoryName(String inputMemoryName) {
        this.inputMemoryName = inputMemoryName;
    }

    public String getOutputMemoryName() {
        return outputMemoryName;
    }

    public void setOutputMemoryName(String outputMemoryName) {
        this.outputMemoryName = outputMemoryName;
    }

    public void setEventCategoryName(String eventCategoryName) {
        this.eventCategoryName = eventCategoryName;
    }
}
