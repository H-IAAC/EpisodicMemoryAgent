package CSTEpisodicMemory.core.representation;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

public class GraphIdea {

    public Idea graph;

    public Map<Idea,Idea> coordinateMap = new HashMap<>();

    private int coordinateCount = 0;
    private int nodeCount = 0;

    public GraphIdea(Idea graph){
        this.graph = graph;
        if (!graph.getL().isEmpty()){
            for (Idea i : graph.getL()){
                if (i.getName().equals("Node")) {
                    nodeCount++;
                    coordinateMap.put(i.get("Coordinate"), i);
                }
            }
            coordinateCount = nodeCount;
        }
    }

    public GraphIdea(GraphIdea clone){
        this.graph = IdeaHelper.cloneIdea(clone.graph);
        if (!graph.getL().isEmpty()){
            for (Idea i : graph.getL()){
                if (i.getName().equals("Node")) {
                    nodeCount++;
                    coordinateMap.put(i.get("Coordinate"), i);
                }
            }
            coordinateCount = nodeCount;
        }
    }

    public Idea insertEventNode(Idea node){
        return insertNode(node, "Event");
    }

    public Idea insertEpisodeNode(Idea node){
        return insertNode(node, "Episode");
    }

    public Idea insertLocationNode(Idea node){
        return insertNode(node, "Location");
    }

    public Idea insertContextNode(Idea node){
        return insertNode(node, "Context");
    }

    public Idea insertNode(Idea node, String type) {
        Idea coord = new Idea("Coordinate", coordinateCount++, "Property", 1);
        return insertNode(node, coord, type);
    }

    public Idea insertNode(Idea node, Idea coord, String type){
        Idea existentNode = getNodeFromContent(node);
        if (existentNode == null) {
            Idea nodeIdea = new Idea("Node", nodeCount++, "AbstractObject", 1);
            Idea content = new Idea("Content", null, "Configuration", 1);
            content.add(node);
            nodeIdea.add(content);
            nodeIdea.add(coord);
            nodeIdea.add(new Idea("Type", type, "Property", 1));
            nodeIdea.add(new Idea("Links", null, "Configuration", 1));
            nodeIdea.add(new Idea("Activation", 0d, "Property", 0));
            graph.add(nodeIdea);
            coordinateMap.put(coord, nodeIdea);
            return nodeIdea;
        } else {
            return existentNode;
        }
    }

    public void insertLink(Idea nodeSource, Idea nodeDest, String type){
        Idea nodeIdeaSource;
        if (!nodeSource.getName().equals("Node")) {
            Optional<Idea> nodeOpt = this.getNodes().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), nodeSource))
                    .findFirst();
            nodeIdeaSource = nodeOpt.orElseGet(() -> insertEventNode(nodeSource));
        } else {
            nodeIdeaSource = nodeSource;
        }

        Idea nodeIdeaDest;
        if (!nodeDest.getName().equals("Node")) {
            Optional<Idea> nodeOpt = this.getNodes().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), nodeDest))
                    .findFirst();
            nodeIdeaDest = nodeOpt.orElseGet(() -> insertEventNode(nodeDest));
        } else {
            nodeIdeaDest = nodeDest;
        }

        Idea links = nodeIdeaSource.get("Links");
        Idea linksOfType = links.get(type);
        if (linksOfType == null){
            linksOfType = new Idea(type, null, "Configuration", 1);
            links.add(linksOfType);
        }
        linksOfType.add(nodeIdeaDest);
    }

    public List<Idea> getChildrenWithLink(Idea node, String linkType){
        Idea nodeIdeaSource = null;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            if (nodeOpt.isPresent()){
                nodeIdeaSource = nodeOpt.get();
            }
        } else {
            nodeIdeaSource = node;
        }

        if (nodeIdeaSource != null){
            List<Idea> linksOut = nodeIdeaSource.get("Links").getL().stream()
                    .filter(l->l.getName().equals(linkType))
                    .map(Idea::getL)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            return linksOut;
        }
        return new ArrayList<>();
    }

    public Map<String, List<Idea>> getSuccesors(Idea node){
        Idea nodeIdeaSource = null;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            if (nodeOpt.isPresent()){
                nodeIdeaSource = nodeOpt.get();
            }
        } else {
            nodeIdeaSource = node;
        }

        if (nodeIdeaSource != null){
            Map<String, List<Idea>> linksOut = nodeIdeaSource.get("Links").getL().stream().collect(Collectors.toMap(Idea::getName, Idea::getL));
            return linksOut;
        }
        return new HashMap<>();
    }

    public Map<String, List<Idea>> getPredecessors(Idea node){
        Idea nodeIdeaDest;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            nodeIdeaDest = nodeOpt.orElse(null);
        } else {
            nodeIdeaDest = node;
        }

        HashMap<String, List<Idea>> linksIn = new HashMap<>();
        if (nodeIdeaDest != null){
            for (Idea n : this.getNodes()){
                for (Idea link : n.get("Links").getL()){
                    if (link.getL().stream().anyMatch(l->IdeaHelper.match(l, nodeIdeaDest))){
                        List<Idea> linksInType = linksIn.getOrDefault(link.getName(), new ArrayList<>());
                        linksInType.add(n);
                        linksIn.put(link.getName(), linksInType);
                    }
                }
            }
        }
        return linksIn;

    }

    public boolean hasNodeContent(Idea idea){
        return this.getNodes().stream()
                .map(GraphIdea::getNodeContent)
                .anyMatch(e->IdeaHelper.match(e,idea));

        //return graph.getL().stream().anyMatch(e->e.getL().contains(idea));
    }

    public List<Idea> getNodes(){
        return graph.getL().stream().filter(e->e.getName().equals("Node")).collect(Collectors.toList());
    }

    public List<Idea> getEventNodes(){
        return graph.getL().stream()
                .filter(e->e.getName().equals("Node"))
                .filter(e->e.get("Type").getValue().equals("Event"))
                .collect(Collectors.toList());
    }

    public List<Idea> getEpisodeNodes(){
        return graph.getL().stream()
                .filter(e->e.getName().equals("Node"))
                .filter(e->e.get("Type").getValue().equals("Episode"))
                .collect(Collectors.toList());
    }

    public List<Idea> getLocationNodes(){
        return graph.getL().stream()
                .filter(e->e.getName().equals("Node"))
                .filter(e->e.get("Type").getValue().equals("Location"))
                .collect(Collectors.toList());
    }

    public List<Idea> getContextNodes(){
        return graph.getL().stream()
                .filter(e->e.getName().equals("Node"))
                .filter(e->e.get("Type").getValue().equals("Context"))
                .collect(Collectors.toList());
    }

    public void setNodeActivation(Idea node, double val){
        Idea foundNode = node;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }

        if (foundNode != null){
            foundNode.get("Activation").setValue(val);
        }
    }

    public void resetNodeActivation(Idea node){
        setNodeActivation(node, 0d);
    }

    public void propagateActivations(List<String> successorsLinks, List<String> predecessorsList){
        for (Idea node : getNodes()){
            if ((double) node.get("Activation").getValue() > 0d)
                propagateActivations(node, successorsLinks, predecessorsList);
        }
    }

    private void propagateActivations(Idea node, List<String> successorsLinks, List<String> predecessorsLinks){
        double nodeActivation = (double) node.get("Activation").getValue();
        Map<String, List<Idea>> successors = getSuccesors(node);
        Map<String, List<Idea>> predecessors = getPredecessors(node);

        for (String linkType : successors.keySet()){
            if (successorsLinks.contains(linkType)){
                for (Idea linkedNode : successors.get(linkType)){
                    double linkedActivation = (double) linkedNode.get("Activation").getValue();
                    if (linkedActivation < nodeActivation*0.9){
                        linkedNode.get("Activation").setValue(nodeActivation*0.9);
                        propagateActivations(linkedNode, successorsLinks,predecessorsLinks);
                    }
                }
            }
        }
        for (String linkType : predecessors.keySet()){
            if (predecessorsLinks.contains(linkType)){
                for (Idea linkedNode : predecessors.get(linkType)){
                    double linkedActivation = (double) linkedNode.get("Activation").getValue();
                    if (linkedActivation < nodeActivation*0.9){
                        linkedNode.get("Activation").setValue(nodeActivation*0.9);
                        propagateActivations(linkedNode, successorsLinks,predecessorsLinks);
                    }
                }
            }
        }
    }

    public void resetActivations(){
        graph.getL().stream().filter(e->e.getName().equals("Node"))
                .forEach(e->e.get("Activation").setValue(0d));
    }

    public Idea getNodeFromContent(Idea content){
        Idea foundNode = content;
        if (!content.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), content)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }
        return foundNode;
    }

    public static Idea getNodeContent(Idea node){
        if (node.getName().equals("Node")){
            return node.get("Content").getL().get(0);
        }
        return null;
    }

    public String toString(){
        StringBuilder nodes = new StringBuilder();
        StringBuilder links = new StringBuilder();
        for (Idea i : graph.getL()){
            if (i.getName().equals("Node")){
                nodes.append(" [Node_").append(i.getValue()).append(" - ").append(i.get("Coordinate").getValue()).append("]");
            }
            if(i.getName().equals("Link")){
                links.append("  [Link_").append(i.getValue()).append(" ").append(((Idea) i.get("Source").getValue()).getValue()).append(" [").append(i.get("Type").getValue()).append("]> ").append(((Idea) i.get("Sink").getValue()).getValue()).append("]");
            }
        }
        return nodes + "\n" + links;
    }
}
