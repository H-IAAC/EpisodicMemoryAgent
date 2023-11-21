package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriesPerEventView {

    public CategoriesPerEventView(Mind m){
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
            for (Idea event : gg.getEventNodes()){
                Idea content = GraphIdea.getNodeContent(event);
                double eventPos = Double.parseDouble(content.getName().replace("Event", ""));

                Idea initialNode = event.get("Links.Initial").getL().get(0);
                Idea finalNode = event.get("Links.Final").getL().get(0);
                String nameA = GraphIdea.getNodeContent(initialNode).getName();
                String nameB = GraphIdea.getNodeContent(finalNode).getName();
                double initialPos = Double.parseDouble(nameA.replaceAll("[^0-9]", ""));
                double finalPos = Double.parseDouble(nameB.replaceAll("[^0-9]", ""));
                eventObjectX.add(eventPos);
                eventObjectY.add(initialPos);
                eventObjectX.add(eventPos);
                eventObjectY.add(finalPos);

                for (Idea context : gg.getChildrenWithLink(event, "ObjectContext")){
                    String nameC = GraphIdea.getNodeContent(context).getName();
                    System.out.println(nameC);
                    nameC = nameC.replaceAll("[^0-9]", "");
                    if (nameC != "") {
                        double posC = Double.parseDouble(nameC);
                        contextObjectX.add(eventPos);
                        contextObjectY.add(posC);
                    }
                }
            }

            Plot plt = Plot.create();
            plt.plot().add(eventObjectX,eventObjectY, "ob");
            plt.plot().add(contextObjectX, contextObjectY, "og");
            try {
                plt.show();
            } catch (IOException | PythonExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
