package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ObjectCategoryPerEventView {

    public ObjectCategoryPerEventView(Mind m, boolean logOnly){
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

                Idea initialNode = event.get("Links.Initial.Node.Links.Object").getL().get(0);
                Idea finalNode = event.get("Links.Final.Node.Links.Object").getL().get(0);
                int initialCoord = (int) initialNode.getValue();
                int finalCoord = (int) finalNode.getValue();
                int initialIdx = nodesIdx.containsKey(initialCoord) ? nodesIdx.get(initialCoord) : count++;
                int finalIdx = nodesIdx.containsKey(finalCoord) ? nodesIdx.get(finalCoord) : count++;
                nodesIdx.put(initialCoord, initialIdx);
                nodesIdx.put(finalCoord, finalIdx);

                eventObjectX.add(eventPos);
                eventObjectY.add((double) initialIdx);
                eventObjectX.add(eventPos);
                eventObjectY.add((double) finalIdx);

                for (Idea context : gg.getChildrenWithLink(event, "ObjectContext")){
                    Idea contextObject = context.get("Links.Object").getL().get(0);
                    int objectCoord = (int) contextObject.getValue();
                    int objectIdx = nodesIdx.containsKey(objectCoord) ? nodesIdx.get(objectCoord) : count++;
                    nodesIdx.put(objectCoord, objectIdx);
                    contextObjectX.add(eventPos);
                    contextObjectY.add((double) objectIdx);
                }
            }

            try {
                PrintWriter out = new PrintWriter("object_data");
                out.println("x y type");
                for (int i = 0; i < eventObjectX.size(); i++){
                    out.println(eventObjectX.get(i) + " " + eventObjectY.get(i) + " b");
                }
                for (int i = 0; i < contextObjectX.size(); i++){
                    out.println(contextObjectX.get(i) + " " + contextObjectY.get(i) + " g");
                }
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (!logOnly) {
                Plot plt = Plot.create();
                plt.plot().add(eventObjectX, eventObjectY, "ob");
                plt.plot().add(contextObjectX, contextObjectY, "og");
                plt.title("Objects Category");
                try {
                    plt.show();
                } catch (IOException | PythonExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
