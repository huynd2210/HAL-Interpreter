import interpreter.Interpreter;
import interpreter.MMU;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void prak5(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Random replacement mode? (y/n)");
        String rplMode = scanner.nextLine();
        boolean rplModeBool = rplMode.equalsIgnoreCase("y");
        Interpreter interpreter = new Interpreter(0, rplModeBool);
        if (rplModeBool){
            System.out.println("Random replacement mode enabled");
        } else {
            System.out.println("FIFO replacement with reference bit enabled");
        }
        interpreter.readProgram("HALPrograms/Prak5");
        interpreter.run(false);

    }



    public static void main(String[] args) throws Exception {
//        testMMU();
//        testMMUWithHAL();
        prak5();
    }

    public static void testMMUWithHAL(){
        boolean isRandom = false;
        Interpreter interpreter = new Interpreter(0, false);

        interpreter.setProgram(writeHALProgramForMMU());
        interpreter.run(false);

//        System.out.println(interpreter.registerMMU.storage);
    }

    public static void testMMU() throws Exception {
        boolean isRandom = true;
        MMU testMMU = new MMU(65536, new Interpreter(1, isRandom), isRandom);

        for (int i = 0 ; i < 65536; i++){
            testMMU.resolveStore(i, i + 1);
        }

        for (int i = 0 ; i < 65536; i++){
            Double aDouble = testMMU.resolveLoad(i);
//            System.out.println(aDouble);
        }


//        System.out.println(testMMU.storage);
        System.out.println("------------------");
        testMMU.printLog();

    }

    public static String writeHALProgramForMMU(){
        int i = 0;
        StringBuilder sb = new StringBuilder();
        i = addLine(sb, i, "START");
        int range = 5120;
        for (int j = 0; j <= 6025; j++){
            i = addLine(sb, i, "LOADNUM " + (j + 1));
            i = addLine(sb, i, "STORE " + j);
        }
        i = addLine(sb, i, "DUMPREG");
//        i = addLine(sb, i, "LOAD 0");
//        i = addLine(sb, i, "OUT 0");
//        i = addLine(sb, i, "LOAD 4095");
//        i = addLine(sb, i, "OUT 0");
//        i = addLine(sb, i, "LOAD 4096");
//        i = addLine(sb, i, "OUT 0");
        i = addLine(sb, i, "STOP");
//        System.out.println(sb.toString());
        return sb.toString();
    }

    private static int addLine(StringBuilder sb, int i, String line) {
        sb.append(i + " " + line + "\n");
        return ++i;
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

        Interpreter interpreter = new Interpreter(1, false);
        interpreter.readProgram(program);
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
