package os;

import interpreter.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConnectionGraph {
    //    public Map<Interpreter, List<Interpreter>> graph;
    public Map<String, Interpreter> interpreterMap;

    public ConnectionGraph(String configFilePath) {
        this.interpreterMap = new HashMap<>();
        buildGraph("0:3 > 1:2\n" +
                "1:3 > 2:2\n" +
//                "1:1 > 2:1\n" +
                "2:3 > 3:2\n");

        setPrograms("0 HALPrograms/pipelineProgramHAL0\n" +
                "1 HALPrograms/pipelineProgram\n" +
                "2 HALPrograms/pipelineProgram\n" +
                "3 HALPrograms/pipelineProgramHALN-1\n");
    }

    public void startOS() {
        List<Thread> processors = new ArrayList<>();
        for (Interpreter interpreter : interpreterMap.values()) {
            Thread processor = new Thread(){
                public void run(){
                    interpreter.run(false);
                }
            };
            processors.add(processor);
        }
        for (Thread processor : processors) {
            processor.start();
        }
    }

    private void setPrograms(String programPathConfig){
        for (String line : programPathConfig.split("\n")) {
            String[] tokens = line.split(" ");
            this.interpreterMap.get(tokens[0]).addProgram(tokens[1]);
        }
    }

    private void buildGraph(String connectionsConfig) {
        for (String line : connectionsConfig.split("\n")) {
            String[] tokens = line.replace(">", "").replace(":", " ").split(" "); //0:3 > 1:2 --> 0 3  1 2
//            Connection connection = new Connection(new Interpreter(Integer.parseInt(tokens[3])), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[4]));
            String originId = tokens[0];
            String originIO = tokens[1];
            String targetId = tokens[3];
            String targetIO = tokens[4];
            addEntry(originId, targetId, Integer.parseInt(originIO), Integer.parseInt(targetIO));
        }
    }

    private void connect(Interpreter a, Interpreter b, int originIO, int targetIO) {
        a.ioList.get(originIO).originIO = originIO;
        a.ioList.get(originIO).targetIO = targetIO;
        b.ioList.get(targetIO).originIO = originIO;
        b.ioList.get(targetIO).targetIO = targetIO;
        a.ioList.set(originIO, b.ioList.get(targetIO));
    }

    private void addEntry(String originId, String targetId, int originIO, int targetIO) {
        if (!interpreterMap.containsKey(originId)) {
            interpreterMap.put(originId, new Interpreter(Integer.parseInt(originId)));
        }
        if (!interpreterMap.containsKey(targetId)) {
            interpreterMap.put(targetId, new Interpreter(Integer.parseInt(targetId)));
        }
        connect(interpreterMap.get(originId), interpreterMap.get(targetId), originIO, targetIO);
    }


    private String readConfig(String path) {
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

    private String splitProcessors(String config) {
        return config.split("(?=HAL-Verbindungen)")[0];
    }

    private String splitConnections(String config) {
        return config.split("(?=HAL-Verbindungen)")[1];
    }
}

