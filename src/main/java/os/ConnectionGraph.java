package os;

import interpreter.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionGraph {
    private Map<Interpreter, List<Connection>> graph;
    public ConnectionGraph(String configFilePath){

    }

    private void buildGraph(String connectionsConfig){
        List<String> connectionsDescriptions = new ArrayList<>();
        for (String line : connectionsConfig.split(" ")) {
            String[] tokens = line.replace(">", "").replace(":", " ").split(" ");
            Connection connection = new Connection(new Interpreter(Integer.parseInt(tokens[2])), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));

        }
    }

    private void addEntry(String originId, Connection connection){
//        if (graph.containsKey())
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
