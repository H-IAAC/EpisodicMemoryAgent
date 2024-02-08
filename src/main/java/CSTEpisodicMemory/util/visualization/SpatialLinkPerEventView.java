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
import java.util.List;
import java.util.Optional;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class SpatialLinkPerEventView {

    public SpatialLinkPerEventView(Mind m){
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
                Idea content = getNodeContent(event);
                double eventPos = Double.parseDouble(content.getName().replace("Event", ""));

                Idea initialNode = event.get("Links.Initial").getL().get(0);
                Idea finalNode = event.get("Links.Final").getL().get(0);
                String nameA = getNodeContent(initialNode).getName();
                String nameB = getNodeContent(finalNode).getName();
                double initialPos = Double.parseDouble(nameA.replaceAll("[^0-9]", ""));
                double finalPos = Double.parseDouble(nameB.replaceAll("[^0-9]", ""));
                eventObjectX.add(eventPos);
                eventObjectY.add(initialPos);
                eventObjectX.add(eventPos);
                eventObjectY.add(finalPos);

                for (Idea context : gg.getChildrenWithLink(event, "ObjectContext")){
                    String nameC = getNodeContent(context).getName();
                    nameC = nameC.replaceAll("[^0-9]", "");
                    if (nameC != "") {
                        double posC = Double.parseDouble(nameC);
                        contextObjectX.add(eventPos);
                        contextObjectY.add(posC);
                    }
                }
            }

            try {
                PrintWriter out = new PrintWriter("links_data");
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
            try {
                PrintWriter out = new PrintWriter("region_data");
                out.println("x y type");
                int c = 1;
                for (Idea event : gg.getEventNodes()){
                    Idea region = getNodeContent(event.get("Links.Environment.Node"));
                    String regionID = region.getName().replace("Room", "");
                    if (regionID != ""){
                        int id = Integer.parseInt(regionID,30) - 9;
                        out.println(c + " " + id);
                    }
                    c++;
                }
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Plot plt = Plot.create();
            plt.plot().add(eventObjectX,eventObjectY, "ob");
            plt.plot().add(contextObjectX, contextObjectY, "og");
            plt.title("Spatial Links");
            try {
                plt.show();
            } catch (IOException | PythonExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
