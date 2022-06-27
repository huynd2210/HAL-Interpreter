package os;

import interpreter.Interpreter;

public class Connection {
    public Interpreter target;
    public int originIO;
    public int targetIO;

    public Connection(Interpreter target, int originIO, int targetIO){
        this.target = target;
        this.originIO = originIO;
        this.targetIO = targetIO;
    }

}
