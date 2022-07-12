import interpreter.Interpreter;
import interpreter.PageTable;
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

    public static void runPipeline(){
        ConnectionGraph connectionGraph = new ConnectionGraph("config/ConfigFilePipeline");
        connectionGraph.startOS();
    }

    public static void runGraph(){
        ConnectionGraph connectionGraph = new ConnectionGraph("config/ConfigFileGraph");
        connectionGraph.startOS();
    }


//    public static int getPageNumber(short virtualAddress) {
//        short virtualPageNumberMask = (short) 64512;
//        short maskedAddress = (short) (virtualAddress & virtualPageNumberMask);
//        return (maskedAddress >> 10);
//    }
//
//    public static short virtualToPhysicalAddress(short virtualAddress) {
//        //first 6bit, note: 6bits for the entirety of virtual address, but physical address wont fully use 6bits during translation
//        int pageNumber = getPageNumber(virtualAddress);
//        int offsetMask = 1023;
//        int maskedOffset = virtualAddress & offsetMask; //10 offset bits
//
//        int physicalAddress = pageNumberAndPageInformationMap.get(pageNumber).physicalPageFrameMask | maskedOffset;
//        return (short) physicalAddress;
//    }

    public static void main(String[] args) throws Exception {
//        System.out.println(virtualToPhysicalAddress((short)8197));
        PageTable pt = new PageTable();
//        System.out.println(pt.resolveQuery((short) 10));
//        System.out.println(pt.resolveQuery((short) 15));
//        System.out.println(pt.resolveQuery((short) 3));
        System.out.println(pt.resolveQuery((short) 1027));
        System.out.println(pt.logs.toString());

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
