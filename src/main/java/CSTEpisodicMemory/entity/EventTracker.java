package CSTEpisodicMemory.entity;

import CSTEpisodicMemory.categories.EventCategory;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class EventTracker extends Codelet {

    private String inputMemoryName = "PERCEPTION_MEMORY";
    private String outputMemoryName = "EVENTS_MEMORY";
    private Memory perceptionInputMO;
    private Memory eventsOutputMO;

    private int bufferSize = 1;
    private int bufferStepSize = 1;
    private List<Idea> inputIdeaBuffer = new LinkedList<Idea>();
    private Idea initialEventIdea;
    private Idea currentInputIdea;
    private int count = 1;
    private double detectionTreashold = 0.5;
    private EventCategory trackedEventCategory;

    public EventTracker(String inputMemoryName, String outputMemoryName, EventCategory trackedEventCategory) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.trackedEventCategory = trackedEventCategory;
    }

    public EventTracker(String inputMemoryName, String outputMemoryName, double detectionTreashold, EventCategory trackedEventCategory) {
        this.inputMemoryName = inputMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.detectionTreashold = detectionTreashold;
        this.trackedEventCategory = trackedEventCategory;
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
                if (checkElapsedTime()) {
                    inputIdeaBuffer.add(currentInputIdea.clone());
                }
            } else {
                if (checkElapsedTime()) {
                    //Check if current state is coherent with previous states and event category
                    Idea testEvent = constructTestEvent();
                    if (trackedEventCategory.membership(testEvent) >= detectionTreashold) {
                        Idea drop = inputIdeaBuffer.remove(0);
                        //Copies start of the event
                        if (initialEventIdea == null) this.initialEventIdea = drop.clone();
                        inputIdeaBuffer.add(currentInputIdea.clone());
                    } else {
                        if (initialEventIdea != null) {
                            List<Idea> constraints = new ArrayList<>();
                            constraints.add(initialEventIdea);
                            constraints.add(inputIdeaBuffer.get(inputIdeaBuffer.size()-1));
                            Idea event = trackedEventCategory.instantiation(constraints);
                            event.setName("Event" + count++);
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

    private Idea constructTestEvent() {
        Idea testEvent = new Idea("Event", null, "Episode", 0);
        List<Idea> steps = new ArrayList<>();
        for (int i = 0; i<this.inputIdeaBuffer.size(); i++){
            Idea step = new Idea("Step", i, "Timestep", 0);
            step.add(inputIdeaBuffer.get(i));
            steps.add(step);
        }
        Idea step = new Idea("Step", this.inputIdeaBuffer.size(), "Timestep", 0);
        step.add(currentInputIdea);
        steps.add(step);
        testEvent.setL(steps);
        return testEvent;
    }

    private boolean checkElapsedTime() {
        return ((int) currentInputIdea.get("Step").getValue())
                - ((int) inputIdeaBuffer.get(inputIdeaBuffer.size() - 1).get("Step").getValue())
                >= bufferStepSize;
    }

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
}
