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

    @Override
    public String toString() {
        return "Connection{" +
                "target=" + target +
                ", originIO=" + originIO +
                ", targetIO=" + targetIO +
                '}';
    }
}
