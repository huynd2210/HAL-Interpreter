package interpreter;

public class main {
    public static void main(String[] args) {
        String program = "00 START\n" +
                "01 LOADNUM 6.666\n" +
                "02 STORE 8\n" +
                "03 DIVNUM 2\n" +
                "04 OUT 1\n" +
                "05 STOP";
        Interpreter interpreter = new Interpreter();
        interpreter.run(program, true);
    }

}
