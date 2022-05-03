package interpreter;

import java.util.*;
import java.util.function.Consumer;

public class Interpreter {
    private final List<Double> register;
    private Double accumulator;
    private Integer programCounter;
    private Double io0;
    private Double io1;
    private final Map<String, Consumer<String>> instructionSet;

    public Interpreter() {
        this.register = new ArrayList<>();
        this.initRegister(150);
        this.io0 = 0d;
        this.io1 = 1d;
        this.accumulator = 0d;
        this.instructionSet = new HashMap<>();
        getInstructionSet();
    }

    public void run(String program, boolean isDebug) {
        this.executeProgram(this.parseProgram(program), isDebug);
    }

    private void printState(String currentInstruction){
        System.out.println("Next instruction: " + currentInstruction);
        System.out.println("Register: ");
        for (int i = 0; i < this.register.size(); i++) {
            if (this.register.get(i) != 0){
                System.out.println(i + " " + this.register.get(i));
            }
        }
        System.out.println("Accumulator: " + this.accumulator);
        System.out.println("----------------------------");
    }

    //Execute the entire program
    private void executeProgram(List<String> program, boolean isDebug) {
        this.programCounter = 0;
        while (!program.get(this.programCounter).equalsIgnoreCase("STOP")) {
            if (!program.get(this.programCounter).equalsIgnoreCase("START")) {
                executeInstruction(program.get(this.programCounter));
            }
            this.programCounter++;
            if (isDebug){
                printState(program.get(this.programCounter));
                Scanner sc = new Scanner(System.in);
                sc.nextLine();
            }
        }
        System.out.println("I/O 0: " + this.io0);
        System.out.println("I/O 1: " + this.io1);
        System.out.println("Program terminated successfully");
    }

    //Execute the instruction
    private void executeInstruction(String instruction) {
        String[] token = instruction.split(" ");
        Consumer<String> command = this.instructionSet.get(token[0]);
        command.accept(token[1]);
    }

    //Get lines from program and remove program number
    private List<String> parseProgram(String program) {
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

    private void getInstructionSet() {
        Consumer<String> storeInd = (operand) -> {
            Double indexPointer = this.register.get(Integer.parseInt(operand));
            this.register.set(indexPointer.intValue(), this.accumulator);
        };
        Consumer<String> loadInd = (operand) -> {
            Double indexPointer = this.register.get(Integer.parseInt(operand));
            this.accumulator = this.register.get(indexPointer.intValue());
        };
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
            }
        };
        Consumer<String> jumpNeg = (operand) -> {
            if (this.accumulator < 0f) {
                this.programCounter = Integer.parseInt(operand);
            }
        };
        Consumer<String> store = (operand) -> this.register.set(Integer.parseInt(operand), this.accumulator);
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
            Scanner sc = new Scanner(System.in);
            if (Integer.parseInt(operand) == 0) {
                this.io0 = Double.parseDouble(sc.nextLine());
                this.accumulator = this.io0;
            } else if (Integer.parseInt(operand) == 1) {
                this.io1 = Double.parseDouble(sc.nextLine());
                this.accumulator = this.io1;
            } else {
                throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
            }
        };
        Consumer<String> start = (empty) -> {
        };
//        Consumer<String> stop = (empty) -> {
//            System.out.println("I/O 0: " + this.io0);
//            System.out.println("I/O 1: " + this.io1);
//            System.out.println("Program terminated successfully");
//            System.exit(0);
//        };
        instructionSet.put("STOREIND", storeInd);
        instructionSet.put("LOADIND", loadInd);
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
    }

    //init and set register to the capacity
    private void initRegister(int capacity){
        for (int i = 0; i < capacity; i++) {
            this.register.add((double) 0);
        }
    }
}
