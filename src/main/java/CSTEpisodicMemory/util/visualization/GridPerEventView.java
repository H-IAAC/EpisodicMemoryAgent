package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class GridPerEventView {

    public GridPerEventView(Mind m){
        GraphIdea gg = null;
        Optional<Memory> selectedMem = m.getRawMemory().getAllMemoryObjects()
                .stream().filter(mem->mem.getName().equalsIgnoreCase("EPLTM"))
                .findFirst();
        if (selectedMem.isPresent()) {
            gg = new GraphIdea((GraphIdea) selectedMem.get().getI());
        }

        if (gg != null){
            List<Double> eventObjectX = new ArrayList<>();
            List<Double> eventObjectY = new ArrayList<>();
            List<Double> contextObjectX = new ArrayList<>();
            List<Double> contextObjectY = new ArrayList<>();
            HashMap<Integer, Integer> nodesIdx = new HashMap<>();
            int count = 1;
            for (Idea event : gg.getEventNodes()){
                Idea content = GraphIdea.getNodeContent(event);
                double eventPos = Double.parseDouble(content.getName().replace("Event", ""));

                List<Idea> initialNodes = event.get("Links.Initial.Node.Links.GridPlace").getL();
                List<Idea> finalNodes = event.get("Links.Final.Node.Links.GridPlace").getL();
                for (Idea initialNode : initialNodes) {
                    int initialCoord = (int) initialNode.getValue();
                    int initialIdx = nodesIdx.containsKey(initialCoord) ? nodesIdx.get(initialCoord) : count++;
                    nodesIdx.put(initialCoord, initialIdx);
                    eventObjectX.add(eventPos);
                    eventObjectY.add((double) initialIdx);
                }
                for (Idea finalNode : finalNodes) {
                    int finalCoord = (int) finalNode.getValue();
                    int finalIdx = nodesIdx.containsKey(finalCoord) ? nodesIdx.get(finalCoord) : count++;
                    nodesIdx.put(finalCoord, finalIdx);
                    eventObjectX.add(eventPos);
                    eventObjectY.add((double) finalIdx);
                }

                for (Idea context : gg.getChildrenWithLink(event, "ObjectContext")){
                    Idea contextObject = context.get("Links.Object").getL().get(0);
                    int objectCoord = (int) contextObject.getValue();
                    int objectIdx = nodesIdx.containsKey(objectCoord) ? nodesIdx.get(objectCoord) : count++;
                    nodesIdx.put(objectCoord, objectIdx);
                    contextObjectX.add(eventPos);
                    contextObjectY.add((double) objectIdx);
                }
            }

            Plot plt = Plot.create();
            plt.plot().add(eventObjectX,eventObjectY, "ob");
            plt.plot().add(contextObjectX, contextObjectY, "og");
            plt.title("Grid Cells Used");
            try {
                plt.show();
            } catch (IOException | PythonExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
