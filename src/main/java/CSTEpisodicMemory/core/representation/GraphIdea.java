package CSTEpisodicMemory.core.representation;

import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.util.IdeaPrinter.fullPrint;

public class GraphIdea {

    public Idea graph;

    public Map<Idea,Idea> coordinateMap = new HashMap<>();
    public Map<Idea, List<Link>> links = new HashMap<>();
    public Map<Idea, List<Link>> reverseLinks = new HashMap<>();

    private int coordinateCount = 0;
    private int nodeCount = 0;
    private int linkCount = 0;

    public GraphIdea(Idea graph){
        this.graph = graph;
    }

    public Idea insertNode(Idea node) {
        Idea coord = new Idea("Coordinate", coordinateCount++, "Property", 1);
        return insertNode(node, coord);
    }

    public Idea insertNode(Idea node, Idea coord){
        Idea nodeIdea = new Idea("Node", nodeCount++, "AbstractObject", 1);
        nodeIdea.add(node);
        nodeIdea.add(coord);
        graph.add(nodeIdea);
        coordinateMap.put(coord, nodeIdea);
        return nodeIdea;
    }

    public void insetLink(Idea nodeSource, Idea nodeDest, String type){

        Idea nodeIdeaSource;
        Optional<Idea> nodeOpt = graph.getL().stream().filter(e->e.getL().contains(nodeSource)).findFirst();
        nodeIdeaSource = nodeOpt.orElseGet(() -> insertNode(nodeSource));

        Idea nodeIdeaDest;
        nodeOpt = graph.getL().stream().filter(e->e.getL().contains(nodeDest)).findFirst();
        nodeIdeaDest = nodeOpt.orElseGet(() -> insertNode(nodeDest));

        Idea ideaLink = new Idea("Link", linkCount++, "Property", 1);
        ideaLink.add(new Idea("Source", nodeIdeaSource.get("Coordinate")));
        ideaLink.add(new Idea("Sink", nodeIdeaDest.get("Coordinate")));
        ideaLink.add(new Idea("Type", type, "Property", 1));
        graph.add(ideaLink);

        Link l = new Link();
        l.type = type;
        l.nodeSource = nodeIdeaSource;
        l.nodeDest = nodeIdeaDest;

        List<Link> nodeLinks = links.getOrDefault(nodeIdeaSource.get("Coordinate"), new ArrayList<Link>());
        nodeLinks.add(l);
        links.put(nodeIdeaSource.get("Coordinate"), nodeLinks);

        List<Link> nodeReverseLinks = links.getOrDefault(nodeIdeaDest.get("Coordinate"), new ArrayList<Link>());
        nodeReverseLinks.add(l);
        reverseLinks.put(nodeIdeaDest.get("Coordinate"), nodeReverseLinks);
    }

    public List<Link> getSuccesors(Idea node){
        Idea nodeIdeaSource;
        System.out.println(node);
        Optional<Idea> nodeOpt = graph.getL().stream().filter(e->e.getL().contains(node)).findFirst();
        if (nodeOpt.isPresent()){
            nodeIdeaSource = nodeOpt.get();
            System.out.println("found");
            if (links.containsKey(nodeIdeaSource.get("Coordinate"))){
                return links.get(nodeIdeaSource.get("Coordinate"));
            }
        }
        return new ArrayList<>();
    }

    public List<Link> getPredecessors(Idea node){
        Idea nodeIdeaDest;
        Optional<Idea> nodeOpt = graph.getL().stream().filter(e->e.getL().contains(node)).findFirst();
        if (nodeOpt.isPresent()){
            nodeIdeaDest = nodeOpt.get();
            if (links.containsKey(nodeIdeaDest)){
                return reverseLinks.get(nodeIdeaDest.get("Coordinate"));
            }
        }
        return new ArrayList<>();

    }

    public class Link{
        public String type;
        public Idea nodeSource;
        public Idea nodeDest;
    }

    public boolean hasNodeContent(Idea idea){
        return graph.getL().stream().anyMatch(e->e.getL().contains(idea));
    }

    public List<Idea> getNodes(){
        return graph.getL().stream().filter(e->e.getName().equals("Node")).collect(Collectors.toList());
    }

    public String toString(){
        String nodes = "";
        String links = "";
        for (Idea i : graph.getL()){
            if (i.getName().equals("Node")){
                nodes += " [Node_" + i.getValue() + " - " + i.get("Coordinate").getValue() + "]";
            }
            if(i.getName().equals("Link")){
                links += "  [Link_" + i.getValue() + " " + ((Idea)i.get("Source").getValue()).getValue() + " [" + i.get("Type").getValue() +"]> " + ((Idea)i.get("Sink").getValue()).getValue() + "]";
            }
        }
        return nodes + "\n" + links;
    }
}
