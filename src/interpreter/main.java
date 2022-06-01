package interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {
//        String program = "00 START\n" +
//                "01 LOADNUM 6.666\n" +
//                "02 STORE 8\n" +
//                "03 DIVNUM 2\n" +
//                "04 OUT 1\n" +
//                "05 STOP";
        String program = readProgramFile("sampleProgram");
        System.out.println(program);
//        Path tmp = Paths.get("sample");
//        Files.createFile(tmp);
        Interpreter interpreter = new Interpreter();
        interpreter.run(program, true);

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
