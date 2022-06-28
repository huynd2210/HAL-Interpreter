package os;

import interpreter.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionGraph {
    public Map<Interpreter, List<Connection>> graph;
    public ConnectionGraph(String configFilePath){
        this.graph = new HashMap<>();
        buildGraph("0:3 > 1:2\n" +
                "1:3 > 2:2\n" +
                "1:1 > 2:1\n" +
                "2:3 > 3:2\n");
    }

    private void buildGraph(String connectionsConfig){
        for (String line : connectionsConfig.split("\n")) {
            String[] tokens = line.replace(">", "").replace(":", " ").split(" "); //0:3 > 1:2 --> 0 3  1 2
            Connection connection = new Connection(new Interpreter(Integer.parseInt(tokens[3])), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[4]));
            addEntry(tokens[0], connection);
        }
    }

    private void addEntry(String originId, Connection connection){
        Interpreter sample = new Interpreter(Integer.parseInt(originId), true);
        if (!graph.containsKey(sample)) {
            graph.put(new Interpreter((Integer.parseInt(originId))), new ArrayList<>());
        }
        graph.get(sample).add(connection);
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
}

