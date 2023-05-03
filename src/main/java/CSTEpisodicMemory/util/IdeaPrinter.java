package CSTEpisodicMemory.util;

import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;

public class IdeaPrinter {

    transient static List<Object> listtoavoidloops = new ArrayList<Object>();

    public static String fullPrint(Idea idea){
        listtoavoidloops = new ArrayList<Object>();
        return fullPrint(idea, "");
    }

    private static String fullPrint(Idea idea, String pre){
        String ideaString = idea.toString() + "[" + printValue(idea) + "]";
        String out = pre + typeScopeString(idea) + ideaString + "\n";
        if (!listtoavoidloops.contains(idea.getId())) {
            listtoavoidloops.add(idea.getId());
            for (Idea l : idea.getL()) {
                out += fullPrint(l, pre + "  ");
            }
        }
        return out;
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
        switch (idea.getType()) {
            case 0:
                //Object 1
                return "Ex ■: ";
            case 1:
                //Property 1
                return "Ex ◪: ";
            case 2:
                //Link
                return "Link: ";
            case 3:
                //Dimension
                return "◖: ";
            case 4:
                //Episode 1
                return "Ex ->: ";
            case 5:
                //Composite
                return "Comp: ";
            case 6:
                //Aggregate
                return "Aggr: ";
            case 7:
                //Config
                return "Config: ";
            case 8:
                //Time
                return "◷: ";
            case 9:
                //Property 2
                return "Law ◪: ";
            case 10:
                //Object 2
                return "Law ■: ";
            case 11:
                //Episode 2
                return "Law ->: ";
            case 12:
                //Property 0
                return "Hipp ◪: ";
            case 13:
                //Object 0
                return "Hipp ■: ";
            case 14:
                //Episode 0
                return "Hipp ->: ";
        }
        return "No cat: ";
    }

    public static String csvPrint(Idea idea){
        return csvPrint(idea, "");
    }

    public static String csvPrint(Idea idea, String prefix){
        String csv = prefix + "{\n";
        csv += prefix + "  \"id\": " + idea.getId() + ",\n";
        csv += prefix + "  \"name\": \"" + idea.getName() + "\",\n";
        csv += prefix + "  \"value\": \"" + (idea.getValue() != null ? idea.getResumedValue():"") + "\",\n";
        StringBuilder lCsv = new StringBuilder();
        for (Idea l : idea.getL()){
            lCsv.append("\n").append(csvPrint(l, prefix + "    ")).append(",");
        }
        if (idea.getL().size() > 0) {
            lCsv.deleteCharAt(lCsv.length() - 1);
            csv += prefix + "  \"l\": [" + lCsv + "\n"+ prefix +"  ],\n";
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
        Idea hit = idea.get(name);
        if (hit == null){
            //!!!!This can generate infinite loops
            for (Idea i : idea.getL()){
                hit = searchIdea(i, name);
            }
        }
        return hit;
    }
}
