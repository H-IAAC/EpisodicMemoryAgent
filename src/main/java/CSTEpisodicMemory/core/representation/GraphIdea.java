package CSTEpisodicMemory.core.representation;

import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

public class GraphIdea {

    public Idea graph;

    public Map<Idea, Idea> coordinateMap = new HashMap<>();

    private int coordinateCount = 0;
    private int nodeCount = 0;

    public GraphIdea(Idea graph) {
        this.graph = graph;
        if (!graph.getL().isEmpty()) {
            for (Idea i : graph.getL()) {
                if (i.getName().equals("Node")) {
                    nodeCount++;
                    coordinateMap.put(i.get("Coordinate"), i);
                }
            }
            coordinateCount = nodeCount;
        }
    }

    public GraphIdea(GraphIdea clone) {
        this.graph = IdeaHelper.cloneIdea(clone.graph);
        if (!graph.getL().isEmpty()) {
            for (Idea i : graph.getL()) {
                if (i.getName().equals("Node")) {
                    nodeCount++;
                    coordinateMap.put(i.get("Coordinate"), i);
                }
            }
            coordinateCount = nodeCount;
        }
    }

    public Idea insertEventNode(Idea node) {
        return insertNode(node, "Event");
    }

    public Idea insertEpisodeNode(Idea node) {
        return insertNode(node, "Episode");
    }

    public Idea insertLocationNode(Idea node) {
        return insertNode(node, "Location");
    }

    public Idea insertContextNode(Idea node) {
        return insertNode(node, "Context");
    }

    public Idea insertPropertyNode(Idea node) {
        return insertNode(node, "Property");
    }

    public Idea insertObjectNode(Idea node) {
        return insertNode(node, "Object");
    }

    public Idea insertNode(Idea node, String type) {
        Idea coord = new Idea("Coordinate", coordinateCount++, "Property", 1);
        return insertNode(node, coord, type);
    }

    public Idea insertNode(Idea node, Idea coord, String type) {
        Idea existentNode = getNodeFromContent(node);
        if (existentNode == null) {
            Idea nodeIdea = new Idea("Node", nodeCount++, "AbstractObject", 1);
            Idea content = new Idea("Content", null, "Configuration", 1);
            content.add(node);
            nodeIdea.add(content);
            nodeIdea.add(coord);
            nodeIdea.add(new Idea("Type", type, "Property", 1));
            nodeIdea.add(new Idea("Links", null, "Configuration", 1));
            nodeIdea.add(new Idea("BackLinks", null, "Configuration", 1));
            nodeIdea.add(new Idea("Activation", 0d, "Property", 0));
            graph.add(nodeIdea);
            coordinateMap.put(coord, nodeIdea);
            return nodeIdea;
        } else {
            return existentNode;
        }
    }

    public void removeNode(Idea node) {
        Idea foundNode;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = this.getNodes().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node))
                    .findFirst();
            foundNode = nodeOpt.orElseGet(() -> insertEventNode(node));
        } else {
            foundNode = node;
        }

        Set<Idea> children = new HashSet<>();
        for (Idea link : foundNode.get("Links").getL()){
            children.addAll(link.getL());
        }
        for (Idea child : children){
            //Function removeLink may change 'BackLinks' list. So is called after collecting children
            removeLink(foundNode, child);
        }

       Set<Idea> parents = new HashSet<>();
        for (Idea backlink : foundNode.get("BackLinks").getL()){
            parents.addAll(backlink.getL());
        }
        for (Idea parent : parents){
            //Function removeLink may change 'BackLinks' list. So is called after collecting parents
            removeLink(parent, foundNode);
        }

        graph.getL().remove(foundNode);
    }

    public void insertLink(Idea nodeSource, Idea nodeDest, String type) {
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
        if (linksOfType == null) {
            linksOfType = new Idea(type, null, "Configuration", 1);
            links.add(linksOfType);
        }
        if (!linksOfType.getL().contains(nodeIdeaDest))
            linksOfType.add(nodeIdeaDest);

        Idea backLinks = nodeIdeaDest.get("BackLinks");
        Idea backLinksOfType = backLinks.get(type);
        if (backLinksOfType == null) {
            backLinksOfType = new Idea(type, null, "Configuration", 1);
            backLinks.add(backLinksOfType);
        }
        if (!backLinksOfType.getL().contains(nodeIdeaSource))
            backLinksOfType.add(nodeIdeaSource);
    }

    public void removeLink(Idea source, Idea dest) {
        Idea nodeIdeaSource;
        if (!source.getName().equals("Node")) {
            Optional<Idea> nodeOpt = this.getNodes().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), source))
                    .findFirst();
            nodeIdeaSource = nodeOpt.orElseGet(() -> insertEventNode(source));
        } else {
            nodeIdeaSource = source;
        }

        Idea nodeIdeaDest;
        if (!dest.getName().equals("Node")) {
            Optional<Idea> nodeOpt = this.getNodes().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), dest))
                    .findFirst();
            nodeIdeaDest = nodeOpt.orElseGet(() -> insertEventNode(dest));
        } else {
            nodeIdeaDest = dest;
        }

        List<Idea> linksIdeaToRemove = new ArrayList<>();
        for (Idea links : nodeIdeaSource.get("Links").getL()) {
            links.getL().remove(nodeIdeaDest);
            if (links.getL().isEmpty())
                linksIdeaToRemove.add(links);
        }
        nodeIdeaSource.get("Links").getL().removeAll(linksIdeaToRemove);

        List<Idea> backlinksIdeaToRemove = new ArrayList<>();
        for (Idea backLinks : nodeIdeaDest.get("BackLinks").getL()) {
            backLinks.getL().remove(nodeIdeaSource);
            if (backLinks.getL().isEmpty())
                backlinksIdeaToRemove.add(backLinks);
        }
        nodeIdeaDest.get("BackLinks").getL().removeAll(backlinksIdeaToRemove);
    }

    public List<Idea> getChildrenWithLink(Idea node, String linkType) {
        Idea nodeIdeaSource = null;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            if (nodeOpt.isPresent()) {
                nodeIdeaSource = nodeOpt.get();
            }
        } else {
            nodeIdeaSource = node;
        }

        if (nodeIdeaSource != null) {
            List<Idea> linksOut = nodeIdeaSource.get("Links").getL().stream()
                    .filter(l -> l.getName().equals(linkType))
                    .map(Idea::getL)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            return linksOut;
        }
        return new ArrayList<>();
    }

    public Map<String, List<Idea>> getSuccesors(Idea node) {
        Idea nodeIdeaSource = null;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            if (nodeOpt.isPresent()) {
                nodeIdeaSource = nodeOpt.get();
            }
        } else {
            nodeIdeaSource = node;
        }

        if (nodeIdeaSource != null) {
            Map<String, List<Idea>> linksOut = nodeIdeaSource.get("Links").getL().stream().collect(Collectors.toMap(Idea::getName, Idea::getL));
            return linksOut;
        }
        return new HashMap<>();
    }

    public Map<String, List<Idea>> getPredecessors(Idea node) {
        Idea nodeIdeaDest;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            nodeIdeaDest = nodeOpt.orElse(null);
        } else {
            nodeIdeaDest = node;
        }

        if (nodeIdeaDest != null) {
            Map<String, List<Idea>> linksIn = nodeIdeaDest.get("BackLinks").getL().stream().collect(Collectors.toMap(Idea::getName, Idea::getL));
            return linksIn;
        }
        return new HashMap<>();

    }

    public boolean hasNodeContent(Idea idea) {
        return this.getNodes().stream()
                .map(GraphIdea::getNodeContent)
                .anyMatch(e -> IdeaHelper.match(e, idea));

        //return graph.getL().stream().anyMatch(e->e.getL().contains(idea));
    }

    public List<Idea> getNodes() {
        return graph.getL().stream().filter(e -> e.getName().equals("Node")).collect(Collectors.toList());
    }

    public List<Idea> getEventNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Event"))
                .collect(Collectors.toList());
    }

    public List<Idea> getEpisodeNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Episode"))
                .collect(Collectors.toList());
    }

    public List<Idea> getLocationNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Location"))
                .collect(Collectors.toList());
    }

    public List<Idea> getContextNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Context"))
                .collect(Collectors.toList());
    }

    public List<Idea> getObjectNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Object"))
                .collect(Collectors.toList());
    }

    public List<Idea> getPropertiesNodes() {
        return graph.getL().stream()
                .filter(e -> e.getName().equals("Node"))
                .filter(e -> e.get("Type").getValue().equals("Property"))
                .collect(Collectors.toList());
    }

    public Idea setNodeActivation(Idea node, double val) {
        Idea foundNode = node;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }

        if (foundNode != null) {
            foundNode.get("Activation").setValue(val);
        }
        return foundNode;
    }

    public double getNodeActivation(Idea node) {
        Idea foundNode = node;
        if (!node.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), node)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }

        if (foundNode != null) {
            return (double) foundNode.get("Activation").getValue();
        }
        return 0.0;
    }

    public void resetNodeActivation(Idea node) {
        setNodeActivation(node, 0d);
    }

    public void propagateActivations(List<String> successorsLinks, List<String> predecessorsList) {
        for (Idea node : getNodes()) {
            if ((double) node.get("Activation").getValue() > 0d)
                propagateActivations(node, successorsLinks, predecessorsList);
        }
    }

    public void propagateActivations(Idea node, List<String> successorsLinks, List<String> predecessorsLinks) {
        double nodeActivation = (double) node.get("Activation").getValue();
        Map<String, List<Idea>> successors = getSuccesors(node);
        Map<String, List<Idea>> predecessors = getPredecessors(node);

        for (String linkType : successors.keySet()) {
            if (successorsLinks.contains(linkType)) {
                for (Idea linkedNode : successors.get(linkType)) {
                    double linkedActivation = (double) linkedNode.get("Activation").getValue();
                    if (linkedActivation < nodeActivation * 0.9) {
                        linkedNode.get("Activation").setValue(nodeActivation * 0.9);
                        propagateActivations(linkedNode, successorsLinks, predecessorsLinks);
                    }
                }
            }
        }
        for (String linkType : predecessors.keySet()) {
            if (predecessorsLinks.contains(linkType)) {
                for (Idea linkedNode : predecessors.get(linkType)) {
                    double linkedActivation = (double) linkedNode.get("Activation").getValue();
                    if (linkedActivation < nodeActivation * 0.9) {
                        linkedNode.get("Activation").setValue(nodeActivation * 0.9);
                        propagateActivations(linkedNode, successorsLinks, predecessorsLinks);
                    }
                }
            }
        }
    }

    public void resetActivations() {
        graph.getL().stream().filter(e -> e.getName().equals("Node"))
                .forEach(e -> e.get("Activation").setValue(0d));
    }

    public GraphIdea getEpisodeSubGraphCopy(Idea ep) {
        Idea foundNode = ep;
        if (!ep.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), ep)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }

        if (foundNode != null) {
            boolean endWithLinks = false;
            GraphIdea subGraph = new GraphIdea(new Idea(foundNode.getName()));
            Idea subEp = subGraph.insertEpisodeNode(IdeaHelper.cloneIdea(getNodeContent(foundNode)));

            Idea start = getChildrenWithLink(foundNode, "Begin").get(0);
            Idea end = getChildrenWithLink(foundNode, "End").get(0);
            Idea subStart = subGraph.insertEventNode(IdeaHelper.cloneIdea(getNodeContent(start)));
            Idea subEnd = subGraph.insertEventNode(IdeaHelper.cloneIdea(getNodeContent(end)));
            subGraph.insertLink(subEp, subStart, "Begin");
            subGraph.insertLink(subEp, subEnd, "End");
            Set<Idea> queue = new LinkedHashSet<>();
            queue.add(start);
            for (int i = 0; i < queue.size(); i++) {
                Idea root = new ArrayList<>(queue).get(i);
                Idea rootContentCopy = IdeaHelper.cloneIdea(getNodeContent(root));
                Map<String, List<Idea>> links = getSuccesors(root);
                for (String linkType : links.keySet()) {
                    if (!linkType.equals("Next")){
                        for (Idea node : links.get(linkType)) {
                            Idea nodeContentCopy = IdeaHelper.cloneIdea(getNodeContent(node));
                            if (root == end) {
                                if (!node.get("Type").getValue().equals("Event")) {
                                    endWithLinks = true;
                                    subGraph.insertNode(nodeContentCopy, (String) node.get("Type").getValue());
                                    subGraph.insertLink(rootContentCopy, nodeContentCopy, linkType);
                                    Map<String, List<Idea>> subContext = getSuccesors(node);
                                    if (!subContext.isEmpty()) {
                                        for (String subLinkType : subContext.keySet()) {
                                            for (Idea subContextNode : subContext.get(subLinkType)) {
                                                Idea subContextContentClone = IdeaHelper.cloneIdea(getNodeContent(subContextNode));
                                                subGraph.insertNode(subContextContentClone, (String) subContextNode.get("Type").getValue());
                                                subGraph.insertLink(nodeContentCopy, subContextContentClone, subLinkType);
                                            }
                                        }
                                    }
                                }
                            } else {
                                subGraph.insertNode(nodeContentCopy, (String) node.get("Type").getValue());
                                subGraph.insertLink(rootContentCopy, nodeContentCopy, linkType);
                                if (node.get("Type").getValue().equals("Event")) {
                                    queue.add(node);
                                } else {
                                    Map<String, List<Idea>> subContext = getSuccesors(node);
                                    if (!subContext.isEmpty()) {
                                        for (String subLinkType : subContext.keySet()) {
                                            for (Idea subContextNode : subContext.get(subLinkType)) {
                                                Idea subContextContentClone = IdeaHelper.cloneIdea(getNodeContent(subContextNode));
                                                subGraph.insertNode(subContextContentClone, (String) subContextNode.get("Type").getValue());
                                                subGraph.insertLink(nodeContentCopy, subContextContentClone, subLinkType);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!endWithLinks)
                System.out.println("No path to end event: " + getNodeContent(foundNode).getName());
            return subGraph;
        }
        return null;
    }

    public Idea getNodeFromContent(Idea content) {
        Idea foundNode = content;
        if (!content.getName().equals("Node")) {
            Optional<Idea> nodeOpt = graph.getL().stream()
                    .filter(e -> IdeaHelper.match(getNodeContent(e), content)).findFirst();
            foundNode = nodeOpt.orElse(null);
        }
        return foundNode;
    }

    public List<Idea> getAllNodesWithSimilarContent(Idea content) {
        return getAllNodesWithSimilarContent(content, 0.5);
    }

    public List<Idea> getAllNodesWithSimilarContent(Idea content, double threshold) {
        List<Idea> foundNodes = new ArrayList<>();
        for (Idea graphNode : graph.getL()){
            Idea graphNodeContent = getNodeContent(graphNode);
            if (graphNodeContent.isCategory()){
                if (graphNodeContent.membership(content) >= threshold){
                    foundNodes.add(graphNode);
                }
            } else {
                if (IdeaHelper.scoreSimilarity(content, graphNodeContent) > threshold){
                    foundNodes.add(graphNode);
                }
            }
        }
        return foundNodes;
    }

    public static Idea getNodeContent(Idea node) {
        if (node.getName().equals("Node")) {
            return node.get("Content").getL().get(0);
        }
        return null;
    }

    public void addAll(GraphIdea clone) {
        for (Idea n : clone.getNodes()) {
            Node node = new Node(n);
            this.insertNode(node.getContent(), node.getType());
        }
        for (Idea n : clone.getNodes()) {
            Node node = new Node(n);
            Map<String, List<Node>> links = node.getLinks();
            for (Map.Entry<String, List<Node>> link : links.entrySet()) {
                for (Node dest : link.getValue()) {
                    this.insertLink(node.getContent(), dest.getContent(), link.getKey());
                }
            }
        }
    }

    public String toString() {
        StringBuilder nodes = new StringBuilder();
        StringBuilder links = new StringBuilder();
        for (Idea i : graph.getL()) {
            if (i.getName().equals("Node")) {
                nodes.append(" [Node_").append(i.getValue()).append(" - ").append(i.get("Coordinate").getValue()).append("]");
            }
            if (i.getName().equals("Link")) {
                links.append("  [Link_").append(i.getValue()).append(" ").append(((Idea) i.get("Source").getValue()).getValue()).append(" [").append(i.get("Type").getValue()).append("]> ").append(((Idea) i.get("Sink").getValue()).getValue()).append("]");
            }
        }
        return nodes + "\n" + links;
    }

    public Idea commomParent(Idea nodeA, Idea nodeB) {
        List<Idea> aBacks = nodeA.get("BackLinks").getL().stream().flatMap(l -> l.getL().stream()).collect(Collectors.toList());
        List<Idea> bBacks = nodeB.get("BackLinks").getL().stream().flatMap(l -> l.getL().stream()).collect(Collectors.toList());

        Optional<Idea> parent = aBacks.stream().filter(bBacks::contains).findFirst();

        return parent.orElse(null);
    }

    public Idea commomParent(Idea nodeA, List<Idea> nodesB) {
        List<Idea> parents = new ArrayList<>();
        List<Idea> aBacks = nodeA.get("BackLinks").getL().stream().flatMap(l -> l.getL().stream()).collect(Collectors.toList());
        for (Idea nodeB : nodesB) {
            List<Idea> bBacks = nodeB.get("BackLinks").getL().stream().flatMap(l -> l.getL().stream()).collect(Collectors.toList());
            Optional<Idea> parent = aBacks.stream().filter(bBacks::contains).findFirst();
            parents.add(parent.orElse(null));
        }

        if (parents.contains(null) || parents.stream().distinct().count() != 1)
            return null;
        return parents.get(0);
    }

    public String toCSV(){
        List<Idea> listtoavoidloops = new ArrayList<>();
        return csvPrint(this.graph, "", listtoavoidloops, 0);
    }

    private String csvPrint(Idea idea, String prefix, List<Idea> listtoavoidloops, int currLevel){
        if (idea == null)
            return "{\"id\": 0, \"name\": \"NULL\", \"value\": \"NULL\", \"l\": [], \"type\": 1,\"category\": \"Property\", \"scope\": 0}";
        String csv = prefix + "{\n";
        csv += prefix + "  \"id\": " + idea.getId() + ",\n";
        csv += prefix + "  \"name\": \"" + idea.getName() + "\",\n";
        csv += prefix + "  \"value\": \"" + (idea.getValue() != null ? IdeaHelper.getIdeaResumedValue(idea):"") + "\",\n";
        StringBuilder lCsv = new StringBuilder();
        if (!listtoavoidloops.contains(idea) && !(idea.getName().equals("Node") && currLevel > 1)) {
            listtoavoidloops.add(idea);
            for (Idea l : idea.getL()) {
                lCsv.append("\n").append(csvPrint(l, prefix + "    ", listtoavoidloops, currLevel+1)).append(",");
            }
            if (!idea.getL().isEmpty()) {
                lCsv.deleteCharAt(lCsv.length() - 1);
                csv += prefix + "  \"l\": [" + lCsv + "\n" + prefix + "  ],\n";
            } else {
                csv += prefix + "  \"l\": [],\n";
            }
        } else {
            csv += prefix + "  \"l\": [],\n";
        }
        csv += prefix + "  \"type\": " + idea.getType() + ",\n";
        csv += prefix + "  \"category\": \"" + idea.getCategory() + "\",\n";
        csv += prefix + "  \"scope\": " + idea.getScope() + "\n";
        csv += prefix + "}";
        return csv;
    }

    public static class Node {
        Idea node;

        public Node(Idea node) {
            this.node = node;
        }

        public Idea getContent() {
            return node.get("Content").getL().get(0);
        }

        public void setContent(Idea content) {
            node.get("Content").setL(Arrays.asList(content));
        }

        public String getType() {
            return (String) node.get("Type").getValue();
        }

        public void setType(String type) {
            node.get("Type").setValue(type);
        }

        public double getActivation() {
            return (double) node.get("Activation").getValue();
        }

        public Map<String, List<Node>> getLinks() {
            return node.get("Links").getL().stream()
                    .collect(Collectors.toMap(
                            Idea::getName,
                            v -> v.getL().stream().map(i -> new Node(i)).toList()
                    ));
        }
    }
}
