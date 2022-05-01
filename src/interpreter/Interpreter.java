package interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Interpreter {
    private List<Double> register;
    private Double accumulator;
    private Integer programCounter;
    private Double io0;
    private Double io1;
    private Map<String, Consumer<String>> instructionSet;

    public Interpreter() {
        this.register = new ArrayList<>();
    }

    private void getInstructionSet() {
        Consumer<String> loadNum = (operand) -> this.accumulator = Double.parseDouble(operand);
        Consumer<String> out = (operand) -> {
            if (Integer.parseInt(operand) == 0) {
                this.io0 = this.accumulator;
            } else if (Integer.parseInt(operand) == 1) {
                this.io1 = this.accumulator;
            } else {
                throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
            }
        };
        Consumer<String> in = (operand) -> {
            if (Integer.parseInt(operand) == 0) {
                this.accumulator = this.io0;
            } else if (Integer.parseInt(operand) == 1) {
                this.accumulator = this.io1;
            } else {
                throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
            }
        };
        Consumer<String> start = (empty) -> {
        };
        Consumer<String> stop = (empty) -> {
            System.out.println("I/O 0: " + this.io0);
            System.out.println("I/O 1: " + this.io1);
            System.out.println("Program terminated successfully");
            System.exit(0);
        };

        instructionSet.put("LOADNUM", loadNum);
        instructionSet.put("OUT", out);
        instructionSet.put("IN", in);
        instructionSet.put("START", start);
        instructionSet.put("STOP", stop);
    }

    //Execute the entire program
    public void executeProgram(List<String> program) {
        program.forEach(this::executeInstruction);
    }

    //Execute the instruction
    private void executeInstruction(String instruction) {
        String[] token = instruction.split(" ");
        Consumer<String> command = this.instructionSet.get(token[0]);
        command.accept(token[1]);
    }

    //Get lines from program and remove program number
    public List<String> parseProgram(String program) {
        String[] lines = program.split("\n");
        List<String> instructions = new ArrayList<>();
        for (String line : lines) {
            String[] tokens = line.split(" ");
            //StringBuilder as temporary container for copying instruction over
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < tokens.length; i++) {
                sb.append(tokens[i]);
                sb.append(" ");
            }
            //Add instruction to instructions list
            instructions.add(sb.toString().trim());
        }
        return instructions;
    }


}
