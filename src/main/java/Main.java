import interpreter.Interpreter;
import os.ConnectionGraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        ConnectionGraph connectionGraph = new ConnectionGraph("asd");
        connectionGraph.startOS();

//        System.out.println(connectionGraph.interpreterMap.get("0").program);
//        connectionGraph.interpreterMap.get("0").run(false);

        //        String config = "HAL - Prozessoren :\n" +
//                "0 p0 . hal\n" +
//                "1 p1 . hal\n" +
//                "2 p2 . hal\n" +
//                "3 p3 . hal\n" +
//                "HAL - Verbindungen :\n" +
//                "0:3 > 1:2\n" +
//                "1:3 > 2:2\n" +
//                "2:3 > 3:2";
//        System.out.println(splitProcessors(config));
//        System.out.println(splitConnections(config));
        System.out.println("Started");
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

    private static String splitProcessors(String config){
        return config.split("(?=HAL - Verbindungen :)")[0];
    }
    private static String splitConnections(String config){
        return config.split("(?=HAL - Verbindungen :)")[1];
    }
}
