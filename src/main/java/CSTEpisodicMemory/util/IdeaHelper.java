package CSTEpisodicMemory.util;

import CSTEpisodicMemory.core.representation.GraphIdea;
import br.unicamp.cst.representation.idea.Idea;

import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.core.representation.GraphIdea.getNodeContent;
import static com.google.common.base.Strings.commonPrefix;

public class IdeaHelper {

    static List<Object> listtoavoidloops = new ArrayList<>();
    static int maxLevel;

    public static String fullPrint(Idea idea){
        listtoavoidloops = new ArrayList<>();
        return fullPrint(idea, "");
    }

    private static String fullPrint(Idea idea, String pre){
        if (idea == null)
            return pre + "NULL\n";
        String ideaString = idea.toString() + "[" + printValue(idea) + "]";
        StringBuilder out = new StringBuilder(pre + typeScopeString(idea) + ideaString + "\n");
        if (!listtoavoidloops.contains(idea.getId())) {
            listtoavoidloops.add(idea.getId());
            for (Idea l : idea.getL()) {
                out.append(fullPrint(l, pre + "  "));
            }
        }
        return out.toString();
    }

    public static String printValue(Idea idea){
        String result = " ";
        if (idea.getValue() != null){
            if (idea.isFloat() || idea.isDouble()) {
                result = String.format("%4.5f", idea.getValue());
            } else {
                try {
                    int trial = Integer.parseInt(idea.getValue().toString());
                    result = String.format("%d", trial);
                } catch (Exception ee) {
                    try {
                        double trial = Double.parseDouble(idea.getValue().toString());
                        result = String.format("%4.1f", trial);
                    } catch (Exception e) {
                        result = idea.getValue().toString();
                    }
                }
            }
        }
        return(result);
    }

    private static String typeScopeString(Idea idea){
        return switch (idea.getType()) {
            case 0 -> "Ex ■: "; //Object 1
            case 1 -> "Ex ◪: "; //Property
            case 2 -> "Link: "; //Link
            case 3 -> "◖: "; //Dimension
            case 4 -> "Ex ->: "; //Episode 1
            case 5 -> "Comp: "; //Composite
            case 6 -> "Aggr: "; //Aggregate
            case 7 -> "Config: "; //Config
            case 8 -> "◷: "; //Time
            case 9 -> "Law ◪: "; //Property 2
            case 10 -> "Law ■: "; //Object 2
            case 11 -> "Law ->: "; //Episode 2
            case 12 -> "Hipp ◪: "; //Property 0
            case 13 -> "Hipp ■: "; //Object 0
            case 14 -> "Hipp ->: "; //Episode 0
            default -> "No cat: ";
        };
    }

    public static String csvPrint(Idea idea){
        listtoavoidloops = new ArrayList<>();
        maxLevel = 10;
        return csvPrint(idea, "", listtoavoidloops, 0);
    }

    public static String csvPrint(Idea idea, int maxLevel_){
        listtoavoidloops = new ArrayList<>();
        maxLevel = maxLevel_;
        return csvPrint(idea, "", listtoavoidloops, 0);
    }

    public static String csvPrint(Idea idea, String prefix, List<Object> listtoavoidloops, int currLevel){
        if (idea == null)
            return "{\"id\": 0, \"name\": \"NULL\", \"value\": \"NULL\", \"l\": [], \"type\": 1,\"category\": \"Property\", \"scope\": 0}";
        String csv = prefix + "{\n";
        csv += prefix + "  \"id\": " + idea.getId() + ",\n";
        csv += prefix + "  \"name\": \"" + idea.getName() + "\",\n";
        csv += prefix + "  \"value\": \"" + (idea.getValue() != null ? getIdeaResumedValue(idea):"") + "\",\n";
        StringBuilder lCsv = new StringBuilder();
        if (!listtoavoidloops.contains(idea) && currLevel < maxLevel) {
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

    public static Idea searchIdea(Idea idea, String name){
        if (idea.getName().equals(name))
            return idea;

        Idea hit = idea.get(name);
        if (hit == null){
            //!!!!This can generate infinite loops
            for (Idea i : idea.getL()){
                hit = searchIdea(i, name);
            }
        }
        return hit;
    }

    public static boolean match(Idea a, Idea b){
        return match(a,b,new ArrayList<>(), new ArrayList<>());
    }

    public static boolean match(Idea a, Idea b, List<Idea> loopsA, List<Idea> loopsB){
        if (a == null || b == null)
            return false;

        if (a.getId() == b.getId())
            return true;

        boolean testValue = false;
        if (a.getValue() == null && b.getValue() == null)
            testValue = true;
        else if (a.getValue() != null && b.getValue() != null)
            if (a.getValue().equals(b.getValue()))
                testValue = true;
        if (a.getName().equals(b.getName())
                && testValue
                && a.getType() == b.getType()){

            if (loopsA.contains(a) || loopsB.contains(b))
                return true;
            loopsA.add(a);
            loopsB.add(b);
            for (Idea s : a.getL()){
                boolean hasSub = b.getL().stream().anyMatch(e->match(s,e, loopsA, loopsB));
                if (!hasSub)
                    return false;
            }
            return true;
        }
        return false;
    }

    public static Idea shallowClone(Idea idea){
        Idea clone = new Idea(idea.getName(), idea.getValue(), idea.getCategory(), idea.getScope());
        for (Idea s : idea.getL()){
            clone.add(new Idea(s.getName(), s.getValue(), s.getCategory(), s.getScope()));
        }
        return clone;
    }

    public static Idea referenceClone(Idea idea) {
        Idea clone = new Idea(idea.getName(), idea.getValue(), idea.getCategory(), idea.getScope());
        for (Idea s : idea.getL()) {
            clone.add(s);
        }
        return clone;
    }

    public static Idea cloneIdea(Idea idea){
        return cloneIdea(idea, new HashMap<>());
    }

    public static Idea cloneIdea(Idea idea, Map<Idea,Idea> avoidLoops){

        Idea newnode;
        newnode = new Idea(idea.getName(), idea.getValue(), idea.getType(), idea.getCategory(), idea.getScope());
        newnode.setL(new ArrayList());
        avoidLoops.put(idea, newnode);
        for (Idea i : idea.getL()) {
            if (!avoidLoops.containsKey(i)) {
                Idea ni = cloneIdea(i, avoidLoops);
                newnode.add(ni);
            }else {
                newnode.add(avoidLoops.get(i));
            }
        }
        return newnode;
    }


    public static String getIdeaResumedValue(Idea idea){
        String result;
        if (idea.isFloat() || idea.isDouble()) {
            result = String.format("%4.4f",idea.getValue());
        }
        else {
            try {
                int trial = Integer.parseInt(idea.getValue().toString());
                result = String.format("%d",trial);
            } catch(Exception ee) {
                try {
                    double trial = Double.parseDouble(idea.getValue().toString());
                    result = String.format("%4.4f",trial);
                }
                catch(Exception e) {
                    result = idea.getValue().toString();
                }
            }
        }
        return(result);
    }

    private static class Similarity{
        protected double countTotal = 0;
        protected double countSimilar = 0;
    }

    public static double scoreSimilarity(Idea a, Idea b){
        return scoreSimilarity(a, b, new ArrayList<>(), new ArrayList<>(), new Similarity());
    }

    private static double scoreSimilarity(Idea a, Idea b, List<Idea> loopsA, List<Idea> loopsB, Similarity similarity) {
        if (a == null || b == null) {
            similarity.countTotal++;
            similarity.countSimilar++;
            return 1.0;
        }

        if (a.getId() == b.getId()) {
            similarity.countTotal++;
            similarity.countSimilar++;
            return 1.0;
        }

        similarity.countTotal += 3; // Name, Value and Type
        similarity.countSimilar += countSimpleSimilaity(a, b);

        if (loopsA.contains(a) || loopsB.contains(b))
            return similarity.countSimilar / similarity.countTotal;
        loopsA.add(a);
        loopsB.add(b);

        for (Idea s : a.getL()){
            double maxSubSimilarity = 0;
            Idea bestSubIdea = null;
            for (Idea ss : b.getL()){
                double subSimilarity = countSimpleSimilaity(s, ss);
                if (subSimilarity > maxSubSimilarity) {
                    maxSubSimilarity = subSimilarity;
                    bestSubIdea = ss;
                }
            }
            scoreSimilarity(s, bestSubIdea, loopsA, loopsB, similarity);
        }
        return similarity.countSimilar / similarity.countTotal;
    }

    public static double countSimpleSimilaity(Idea a, Idea b){
        if (a == null || b == null)
            return 3;

        if (a.getId() == b.getId())
            return 3;

        double countSimilar = 0;
        if (a.getValue() == null && b.getValue() == null)
            countSimilar++;
        else if (a.getValue() != null && b.getValue() != null)
            if (a.getValue().equals(b.getValue()))
                countSimilar++;
        if (a.getName().equals(b.getName()))
            countSimilar++;
        if (a.getType() == b.getType())
            countSimilar++;

        return countSimilar;
    }

    public static String generateEPLTMDescription(GraphIdea epltm){
        StringBuilder desc = new StringBuilder();
        int epCount = 1;
        for (Idea episode : epltm.getEpisodeNodes()){
            desc.append("Episode " + epCount++ + ":\n");
            epltm.resetActivations();
            epltm.setNodeActivation(episode, 1.0);
            epltm.propagateActivations(Arrays.asList("Begin", "Meet", "Before", "Start", "During", "Overlap", "Finish", "Equal"), new ArrayList<>());
            List<Idea> episodeEvents = epltm.getEventNodes().stream().filter(e->epltm.getNodeActivation(e) > 0).toList();
            int eventCount = 1;
            for (Idea event : episodeEvents){
                desc.append(eventCount++ + ") [" + ((Idea) getNodeContent(event).getValue()).getName() + "] ");
                Idea spatialLinkInitial = epltm.getChildrenWithLink(event, "Initial").get(0);
                Idea spatialLinkFinal = epltm.getChildrenWithLink(event, "Final").get(0);
                String initialObj = getNodeContent(epltm.getChildrenWithLink(spatialLinkInitial, "Object").get(0)).getName();
                String finalObj = getNodeContent(epltm.getChildrenWithLink(spatialLinkFinal, "Object").get(0)).getName();
                desc.append(initialObj + " Occupation: [");
                for (Idea grid : epltm.getChildrenWithLink(spatialLinkInitial, "GridPlace")){
                    Idea gridInfo = getNodeContent(grid);
                    desc.append("(" + gridInfo.get("u").getValue() + "," + gridInfo.get("v").getValue() + "), ");
                }
                desc.append("] |");
                desc.append(finalObj + " Occupation: [");
                for (Idea grid : epltm.getChildrenWithLink(spatialLinkFinal, "GridPlace")){
                    Idea gridInfo = getNodeContent(grid);
                    desc.append("(" + gridInfo.get("u").getValue() + "," + gridInfo.get("v").getValue() + "), ");
                }
                desc.append("]\n");
            }
            desc.append("\n");
        }

        return desc.toString();
    }
    public static double scoreSimilarity2(Idea a, Idea b){
        return scoreSimilarity2(a, b, new ArrayList<>(), new ArrayList<>(), new Similarity());
    }
    private static double scoreSimilarity2(Idea a, Idea b, List<Idea> loopsA, List<Idea> loopsB, Similarity similarity) {
        if (a == null || b == null) {
            similarity.countTotal++;
            similarity.countSimilar++;
            return 1.0;
        }

        /*if (a.getId() == b.getId()) {
            similarity.countTotal++;
            similarity.countSimilar++;
            return 1.0;
        }*/

        similarity.countTotal += 3; // Name, Value and Type
        similarity.countSimilar += countSimpleSimilaity2(a, b);

        if (loopsA.contains(a) || loopsB.contains(b))
            return similarity.countSimilar / similarity.countTotal;
        loopsA.add(a);
        loopsB.add(b);

        for (Idea s : a.getL()){
            double maxSubSimilarity = 0;
            Idea bestSubIdea = null;
            for (Idea ss : b.getL()){
                double subSimilarity = countSimpleSimilaity2(s, ss);//, new ArrayList<>(loopsA), new ArrayList<>(loopsB), new Similarity()); //countSimpleSimilaity(s, ss);
                if (subSimilarity > maxSubSimilarity) {
                    maxSubSimilarity = subSimilarity;
                    bestSubIdea = ss;
                }
            }
            scoreSimilarity2(s, bestSubIdea, loopsA, loopsB, similarity);
        }
        return similarity.countSimilar / similarity.countTotal;
    }

    public static double countSimpleSimilaity2(Idea a, Idea b){
        if (a == null || b == null)
            return 3;

        if (a.getId() == b.getId())
            return 3;

        double countSimilar = 0;
        if (a.getValue() == null && b.getValue() == null)
            countSimilar++;
        else if (a.getValue() != null && b.getValue() != null)
            if (a.getValue().equals(b.getValue()))
                countSimilar++;
        countSimilar +=  commonPrefix(a.getName(), b.getName()).length() * 1.0 / Math.max(a.getName().length(), b.getName().length());
        if (a.getType() == b.getType())
            countSimilar++;

        return countSimilar;
    }
}

