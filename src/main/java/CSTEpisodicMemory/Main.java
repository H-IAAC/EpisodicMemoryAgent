/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.categories.LinearEventCategory;
import CSTEpisodicMemory.core.representation.GridLocation;
import CSTEpisodicMemory.episodic.EpisodicGistExtraction;
import CSTEpisodicMemory.experiments.*;
import CSTEpisodicMemory.util.visualization.*;
import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import br.unicamp.cst.core.entities.Memory;
import com.coppeliarobotics.remoteapi.zmq.objects.special._sim;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    static AgentMind a;
    public static void main(String... args) {
        //new ExperimentA().run();
        Experiment exp = new ExperimentD();
        Agent.HUE_VARIATION = 0.01;
        LinearEventCategory.threshold = 0.01;
        EpisodicGistExtraction.objectCategoryThreshold = 0.7;
        for (int i=0; i<args.length;i++){
            if (args[i].equals("-evt")){
                LinearEventCategory.threshold = Double.parseDouble(args[i+1]);
            } else if (args[i].equals("-obj")) {
                EpisodicGistExtraction.objectCategoryThreshold = Double.parseDouble(args[i+1]);
            } else if (args[i].equals("-exp1")) {
                exp = new ExperimentC();
            } else if (args[i].equals("-exp2")) {
                exp = new ExperimentD();
            } else if (args[i].equals("-exp3")) {
                exp = new ExperimentF();
            } else if (args[i].equals("-hue")) {
                Agent.HUE_VARIATION = Double.parseDouble(args[i+1]);
            }
        }
        String[] expName = exp.toString().split("@")[0].split("\\.");
        System.out.println("Params Summary:\n" +
                "\tExperiment: " + expName[expName.length-1] + "\n" +
                "\tEvent Threshold: " + LinearEventCategory.threshold + "\n" +
                "\tObject Threshold: " + EpisodicGistExtraction.objectCategoryThreshold + "\n" +
                "\tHue: " + Agent.HUE_VARIATION);
        GridLocation.SCALE = 0.15;
        exp.run();
    }

    private static void test(){
        Environment env = new EnvironmentA();

        for (int i = 0; i < 4; i++){
            SimpleAgentExecutor execNPC = new SimpleAgentExecutor(env);
            execNPC.start();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
     private static void normal(){
         Logger.getLogger("codelets").setLevel(Level.SEVERE);
         // Create Environment
         Environment env=new EnvironmentA(); //Creates only a creature and some apples
         a = new AgentMind(env, new ArrayList<>());  // Creates the Agent Mind and start it
         // The following lines create the MindViewer and configure it
         //MindViewer mv = new MindViewer(a,"MindViewer", a.bList);
         //mv.setVisible(true);

         System.out.println("Init");
         IdeaVisualizer visu = new IdeaVisualizer(a);
         for (String mem : a.getRawMemory().getAllMemoryObjects().stream().map(Memory::getName).collect(Collectors.toList()))
             visu.addMemoryWatch(mem);
         visu.setMemoryWatchPrintLevel("EPLTM", 4);
         visu.setMemoryWatchPrintLevel("EVENTS", 5);
         visu.setVisible(true);

         GraphicMind lv = new GraphicMind(a, env, 10,8,10*80,8*80,0);


         EventVisualizer ev = new EventVisualizer(1000, 200, a);

         Runtime.getRuntime().addShutdownHook(new Thread(env::stopSimulation));
         runTestCommands(env, a);

     }

    public static void runTestCommands(Environment env, AgentMind a){
        /*
        env.world.createThing(Constants.FoodTypes.NPFOOD, 7.5f, 1.5f);
        env.world.createThing(Constants.FoodTypes.NPFOOD, 7.5f, 9.5f);
        env.world.createThing(Constants.JewelTypes.RED_JEWEL, 0.2f, 9.5f);
        env.world.createThing(Constants.JewelTypes.BLUE_JEWEL, 0.4f, 9.5f);
        env.world.createThing(Constants.JewelTypes.GREEN_JEWEL, 0.6f, 9.5f);
        env.world.createThing(Constants.JewelTypes.WHITE_JEWEL, 0.8f, 9.5f);
        env.world.createThing(Constants.JewelTypes.MAGENTA_JEWEL, 1.0f, 9.5f);
        env.world.createThing(Constants.JewelTypes.YELLOW_JEWEL, 1.2f, 9.5f);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
//        env.creature.moveTo(2f,2f);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        env.creature.moveTo(3f,1f);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //-------Explore Impulse (TEST ONLY) -----
        Idea impulse = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea dest = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea = new Idea("Position", a.roomA, "Property", 1);
        posIdea.add(new Idea("X",0.5f, 3));
        posIdea.add(new Idea("Y",2f, 3));
        dest.add(posIdea);
        state.add(dest);
        state.add(new Idea("Desire", 0.1, "Property", 1));
        impulse.add(state);
        //---------
        List<Memory> mems = a.getRawMemory().getAllMemoryObjects();
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse, 0.1, "Explore");
                //System.out.println(fullPrint((Idea) mo.getI()));
            }
        }

        //env.creature.moveTo(0.5f,2f);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        CategoriesPerEventView cc = new CategoriesPerEventView(a);

        Idea impulse_ = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state_ = new Idea("State", null, "Timestep", 0);
        Idea dest_ = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea_ = new Idea("Position", a.roomC, "Property", 1);
        posIdea_.add(new Idea("X",0.5f, 3));
        posIdea_.add(new Idea("Y",9f, 3));
        dest_.add(posIdea_);
        state_.add(dest_);
        state_.add(new Idea("Desire", 0.1, "Property", 1));
        impulse_.add(state_);
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse_, 0.1, "Explore");
                //System.out.println(fullPrint((Idea) mo.getI()));
            }
        }
        //env.creature.moveTo(0.5f,9f);
        */
    }
}
