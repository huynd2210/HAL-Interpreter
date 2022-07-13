import interpreter.Interpreter;
import interpreter.PageTable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

//    public static void runPipeline(){
//        ConnectionGraph connectionGraph = new ConnectionGraph("config/ConfigFilePipeline");
//        connectionGraph.startOS();
//    }
//
//    public static void runGraph(){
//        ConnectionGraph connectionGraph = new ConnectionGraph("config/ConfigFileGraph");
//        connectionGraph.startOS();
//    }

    public static void testPageTable(boolean isRandomReplacement) throws Exception {
        PageTable pt = new PageTable();
//        System.out.println(pt.resolveQuery((short) 1));
//        System.out.println(pt.resolveQuery((short) 1025));
//        System.out.println(pt.resolveQuery((short) 2049));
//        System.out.println(pt.resolveQuery((short) 3073));
//        System.out.println(pt.resolveQuery((short) 4098));
//        System.out.println(pt.resolveQuery((short) 4099));
//        System.out.println(pt.resolveQuery((short) 5121));
//
//        System.out.println(pt.fifoQueueForReplacement);
//        System.out.println(pt.logs);

        for (int i = 0; i < 1024; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 1024; i < 2048; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 2048; i < 3072; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 3072; i < 4096; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 4096; i < 5120; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 5120; i < 6144; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        for (int i = 6144; i < 7168; i++) {
            System.out.println(pt.resolveQuery((short) i, isRandomReplacement));
        }
        System.out.println(pt.logs.toString());
        System.out.println("There are in total: " + getAmountOfPageFaults(pt.logs.toString()) + " page faults, with 4 page faults resulting from initial insertion");
//        praktikum5(new String[]{"sampleHAL1961", "false"});
    }

    public static int getAmountOfPageFaults(String logs){
        return logs.split("\n").length;
    }

    public static void main(String[] args) throws Exception {
        testPageTable(false);
    }

    private static void praktikum2(String[] args) {
        String file = "NewtonF1";
        if (args.length == 0 || args[0] == null || args[0].equalsIgnoreCase("")) {
            System.out.println("File argument not found, defaulting to Add2Inputs");
        } else {
            file = args[0];
        }

        boolean isDebug = false;
        if (args.length < 2) {
            System.out.println("Debug argument not found, defaulting to false");
        } else {
            isDebug = Boolean.parseBoolean(args[1]);
        }

        System.out.println(file);
        String program = readProgramFile(file);
        System.out.println(program);

        Interpreter interpreter = new Interpreter(1);
        interpreter.addProgram(program);
        interpreter.run(isDebug);
        Scanner sc = new Scanner(System.in);
        System.out.println("Press any key to exit");
        sc.nextLine();
    }

    public static String readProgramFile(String path) {
        List<String> tmp = new ArrayList<>();
        try {
            tmp = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (String s : tmp) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
