package CSTEpisodicMemory.util;

import br.unicamp.cst.representation.idea.Idea;

public class IdeaPrinter {

    public static String fullPrint(Idea idea){
        return fullPrint(idea, "");
    }

    public static String fullPrint(Idea idea, String pre){
        String out = pre + idea.toString() + "[" + printValue(idea) + "]" + "\n";
        for (Idea l : idea.getL()){
            out += fullPrint(l, pre + "  ");
        }
        return out;
    }

    public static String printValue(Idea idea){
        String result;
        if (idea.isFloat() || idea.isDouble()) {
            result = String.format("%4.5f",idea.getValue());
        } else {
            try {
                int trial = Integer.parseInt(idea.getValue().toString());
                result = String.format("%d",trial);
            } catch(Exception ee) {
                try {
                    double trial = Double.parseDouble(idea.getValue().toString());
                    result = String.format("%4.1f",trial);
                }
                catch(Exception e) {
                    result = idea.getValue().toString();
                }
            }
        }
        return(result);
    }
}
