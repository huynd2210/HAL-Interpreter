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
//        Consumer<String> storeInd = (operand) -> this.accumulator;
//        Consumer<String> loadInd = (operand) -> this.accumulator;
        Consumer<String> divNum = (operand) -> this.accumulator /= Double.parseDouble(operand);
        Consumer<String> div = (operand) -> this.accumulator /= this.register.get(Integer.parseInt(operand));
        Consumer<String> mulNum = (operand) -> this.accumulator *= Double.parseDouble(operand);
        Consumer<String> mul = (operand) -> this.accumulator *= this.register.get(Integer.parseInt(operand));
        Consumer<String> subNum = (operand) -> this.accumulator -= Double.parseDouble(operand);
        Consumer<String> sub = (operand) -> this.accumulator -= this.register.get(Integer.parseInt(operand));
        Consumer<String> addNum = (operand) -> this.accumulator += Double.parseDouble(operand);
        Consumer<String> add = (operand) -> this.accumulator += this.register.get(Integer.parseInt(operand));

        Consumer<String> jump = (operand) -> this.programCounter = Integer.parseInt(operand);
        Consumer<String> jumpNull = (operand) -> {
            if (this.accumulator == 0f) {
                this.programCounter = Integer.parseInt(operand);
            }
        };
        Consumer<String> jumpPos = (operand) -> {
            if (this.accumulator > 0f) {
                this.programCounter = Integer.parseInt(operand);
                this.executeProgram(parseProgram(operand));
            }
        };
        Consumer<String> jumpNeg = (operand) -> {
            if (this.accumulator < 0f) {
                this.programCounter = Integer.parseInt(operand);
            }
        };
        Consumer<String> store = (operand) -> this.register.add(Integer.parseInt(operand), this.accumulator);
        Consumer<String> load = (operand) -> this.accumulator = this.register.get(Integer.parseInt(operand));
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
//        instructionSet.put("STOREIND", storeInd);
//        instructionSet.put("LOADIND", loadInd);
        instructionSet.put("DIVNUM", divNum);
        instructionSet.put("DIV", div);
        instructionSet.put("MULNUM", mulNum);
        instructionSet.put("MUL", mul);
        instructionSet.put("SUBNUM", subNum);
        instructionSet.put("SUB", sub);
        instructionSet.put("ADDNUM", addNum);
        instructionSet.put("ADD", add);
        instructionSet.put("JUMP", jump);
        instructionSet.put("JUMPNULL", jumpNull);
        instructionSet.put("JUMPPOS", jumpPos);
        instructionSet.put("JUMPNEG", jumpNeg);
        instructionSet.put("STORE", store);
        instructionSet.put("LOAD", load);
        instructionSet.put("LOADNUM", loadNum);
        instructionSet.put("OUT", out);
        instructionSet.put("IN", in);
        instructionSet.put("START", start);
        instructionSet.put("STOP", stop);
    }

    //Execute the entire program
    public void executeProgram(List<String> program) {
//        program.forEach(this::executeInstruction);
        this.programCounter =0;
        while (true){
            program.get(this.programCounter);
        }
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
