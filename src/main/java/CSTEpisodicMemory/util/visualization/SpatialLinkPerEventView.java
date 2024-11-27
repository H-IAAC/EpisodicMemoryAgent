package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.categories.ObjectCategory;
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

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;

public class SpatialLinkPerEventView {

    public SpatialLinkPerEventView(Mind m, boolean logOnly) {
        GraphIdea gg = null;
        Optional<Memory> selectedMem = m.getRawMemory().getAllMemoryObjects()
                .stream().filter(mem -> mem.getName().equalsIgnoreCase("EPLTM"))
                .findFirst();
        if (selectedMem.isPresent()) {
            gg = new GraphIdea((GraphIdea) selectedMem.get().getI());
        }

        if (gg != null) {
            List<Double> eventObjectX = new ArrayList<>();
            List<Double> eventObjectY = new ArrayList<>();
            List<String> eventObjectID = new ArrayList<>();
            HashMap<Double, String> IDMap = new HashMap<>();
            HashMap<Double, String> IDNameMap = new HashMap<>();
            List<Double> contextObjectX = new ArrayList<>();
            List<Double> contextObjectY = new ArrayList<>();
            for (Idea event : gg.getEventNodes()) {
                Idea content = getNodeContent(event);
                double eventPos = Double.parseDouble(content.getName().replace("Event", ""));

                Idea initialNode = event.get("Links.Initial").getL().get(0);
                Idea finalNode = event.get("Links.Final").getL().get(0);
                Idea initialObj = event.get("Links.Initial.Node.Links.Object").getL().get(0);
                Idea finalObj = event.get("Links.Final.Node.Links.Object").getL().get(0);
                String nameA = getNodeContent(initialNode).getName();
                String nameB = getNodeContent(finalNode).getName();
                double initialPos = Double.parseDouble(nameA.replaceAll("[^0-9]", ""));
                double finalPos = Double.parseDouble(nameB.replaceAll("[^0-9]", ""));
                eventObjectX.add(eventPos);
                eventObjectY.add(initialPos);
                double ia = (double) ((ObjectCategory) getNodeContent(initialObj).getValue()).id;
                String iak = "";
                if (IDMap.containsKey(ia)){
                    iak = IDMap.get(ia);
                } else {
                    iak = String.format("%x",IDMap.size() + 10);
                    IDMap.put(ia, iak);
                    IDNameMap.put(ia, getNodeContent(initialObj).getName() + "|" + ((ObjectCategory) getNodeContent(initialObj).getValue()) + "|" + event.getValue());
                }
                eventObjectID.add(iak);
                eventObjectX.add(eventPos);
                eventObjectY.add(finalPos);
                ia = (double) ((ObjectCategory) getNodeContent(finalObj).getValue()).id;
                iak = "";
                if (IDMap.containsKey(ia)){
                    iak = IDMap.get(ia);
                } else {
                    iak = String.format("%x",IDMap.size() + 10);
                    IDMap.put(ia, iak);
                    IDNameMap.put(ia, getNodeContent(finalObj).getName() + "|" + ((ObjectCategory) getNodeContent(finalObj).getValue()) + "|" + event.getValue());
                }
                eventObjectID.add(iak);

                for (Idea context : gg.getChildrenWithLink(event, "ObjectContext")) {
                    String nameC = getNodeContent(context).getName();
                    nameC = nameC.replaceAll("[^0-9]", "");
                    if (nameC != "") {
                        double posC = Double.parseDouble(nameC);
                        contextObjectX.add(eventPos);
                        contextObjectY.add(posC);
                    }
                }
            }

            //System.out.println(IDMap);
            //System.out.println(IDNameMap);
            try {
                PrintWriter out = new PrintWriter("links_data");
                out.println("x y type agent");
                for (int i = 0; i < eventObjectX.size(); i++) {
                    out.println(eventObjectX.get(i) + " " + eventObjectY.get(i) + " b " + eventObjectID.get(i));
                }
                for (int i = 0; i < contextObjectX.size(); i++) {
                    out.println(contextObjectX.get(i) + " " + contextObjectY.get(i) + " g 0");
                }
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                PrintWriter out = new PrintWriter("region_data");
                out.println("x y type");
                int c = 1;
                for (Idea event : gg.getEventNodes()) {
                    Idea regionNode = event.get("Links.Environment.Node");
                    if (regionNode != null) {
                        Idea region = getNodeContent(regionNode);
                        String regionID = region.getName().replace("Room", "");
                        if (regionID != "") {
                            int id = Integer.parseInt(regionID, 30) - 9;
                            out.println(c + " " + id);
                        }
                    } else {
                        out.println(c + " " + 0);
                    }
                    c++;
                }
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (!logOnly) {
                Plot plt = Plot.create();
                plt.plot().add(eventObjectX, eventObjectY, "ob");
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
}
