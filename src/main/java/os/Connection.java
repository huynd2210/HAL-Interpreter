package os;

import interpreter.Interpreter;

public class Connection {
//    public Interpreter target;
    public int originIO;
    public int targetIO;
    public Buffer buffer;
    public boolean isConnectedToUserIO;

    public Connection(boolean isConnectedToUserIO){
        this.isConnectedToUserIO = isConnectedToUserIO;
    }

    public Connection(){
        this.buffer = new Buffer();
    }

    @Override
    public String toString() {
        return "Connection{" +
//                "target=" + target +
                ", originIO=" + originIO +
                ", targetIO=" + targetIO +
                '}';
    }
}
