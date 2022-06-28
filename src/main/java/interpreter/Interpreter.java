package interpreter;

import os.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Interpreter {
    public int id;
    private final List<Double> register;
    private Double accumulator;
    private Integer programCounter;
    private Integer currentInstruction;
    public List<Connection> ioList;
    private final Map<String, Consumer<String>> instructionSet;
    public String program;

    public Interpreter(int id) {
        this.id = id;
        this.register = new ArrayList<>();
        int registerCapacity = 40;
        this.initRegister(registerCapacity);
        this.ioList = new ArrayList<>();
        int maxIO = 4;
        for (int i = 0; i < maxIO; i++) {
            if (i == 0) {
                this.ioList.add(new Connection(true));
            } else if (i == 1) {
                this.ioList.add(new Connection(true));
            } else {
                this.ioList.add(new Connection());
            }
        }
        this.accumulator = 0d;
        this.instructionSet = new HashMap<>();
        getInstructionSet();
    }

    public Interpreter(int id, boolean isEmptyInterpreter) {
        this.id = id;
        this.instructionSet = new HashMap<>();
        this.register = new ArrayList<>();
    }

    public void addProgram(String pathToProgram) {
        this.program = readProgramFile(pathToProgram);
    }

    public String readProgramFile(String path) {
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

    public void run(boolean isDebug) {
//        long startTime = System.nanoTime();
        System.out.println("Processor: " + this.id + " starting");
        this.executeProgram(this.parseProgram(this.program), isDebug);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime) / 1000000; //ms
//        printRunTime(duration);

    }

    private void printRunTime(long duration) {
        int durationSecond = (int) duration / 1000;
        System.out.print("Program duration: ");
        if (durationSecond > 60) {
            System.out.print(durationSecond / 60 + "m ");
        }
        System.out.print(durationSecond % 60 + "s\n");
    }

    private void printState(String currentInstruction) {
        System.out.println("Next instruction: " + currentInstruction);
        System.out.println("Register: ");
        for (int i = 0; i < this.register.size(); i++) {
            if (this.register.get(i) != 0) {
                System.out.println(i + " " + this.register.get(i));
            }
        }
        System.out.println("\nAccumulator: " + this.accumulator);
        System.out.println("Current Instruction: " + this.currentInstruction);
        System.out.println("PC: " + this.programCounter);
        System.out.println("----------------------------");
    }

    //Execute the entire program
    private void executeProgram(List<String> program, boolean isDebug) {
        this.programCounter = 0;
        this.currentInstruction = 0;
        while (!program.get(this.currentInstruction).equalsIgnoreCase("STOP")) {
            programGuard(program.get(this.currentInstruction));
            if (!program.get(this.currentInstruction).equalsIgnoreCase("START")) {
                executeInstruction(program.get(this.currentInstruction));
            }
            this.currentInstruction++;
            programCounter++;
            if (isDebug) {
                printState(program.get(this.currentInstruction));
                Scanner sc = new Scanner(System.in);
                sc.nextLine();
            }
        }
//        System.out.println("------------");
//        System.out.println("I/O 0: " + this.io0);
//        System.out.println("I/O 1: " + this.io1);
//        System.out.println("------------");
//        System.out.println("Program Counter: " + programCounter);
//        System.out.println("Program terminated successfully");
    }

    //Execute the instruction
    private void executeInstruction(String instruction) {
//        System.out.println("Processor: " + this.id + " executing instruction: " + instruction);
        String[] token = instruction.split(" ");
        Consumer<String> command = this.instructionSet.get(token[0]);
        command.accept(token[1]);
    }

    private void programGuard(String instruction) {
        String[] token = instruction.split(" ");
        if (this.currentInstruction == 0 && !instruction.equalsIgnoreCase("START")) {
            throw new IllegalArgumentException("START instruction not at the top");
        }
        if (!this.instructionSet.containsKey(token[0])) {
            throw new IllegalArgumentException("Instruction: " + token[0] + " not found");
        }
        if ((!instruction.equalsIgnoreCase("START") && !instruction.equalsIgnoreCase("STOP")) && !isNumeric(token[1])) {
            throw new IllegalArgumentException("Instruction: " + instruction + " does not take string argument");
        }
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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
        Consumer<String> jump = (operand) -> this.currentInstruction = Integer.parseInt(operand) - 1;
        Consumer<String> jumpNull = (operand) -> {
            if (this.accumulator == 0f) {
                this.currentInstruction = Integer.parseInt(operand) - 1;
            }
        };
        Consumer<String> jumpPos = (operand) -> {
            if (this.accumulator > 0f) {
                this.currentInstruction = Integer.parseInt(operand) - 1;
            }
        };
        Consumer<String> jumpNeg = (operand) -> {
            if (this.accumulator < 0f) {
                this.currentInstruction = Integer.parseInt(operand) - 1;
            }
        };
        Consumer<String> store = (operand) -> this.register.set(Integer.parseInt(operand), this.accumulator);
        Consumer<String> load = (operand) -> this.accumulator = this.register.get(Integer.parseInt(operand));
        Consumer<String> loadNum = (operand) -> this.accumulator = Double.parseDouble(operand);
        Consumer<String> out = (operand) -> {
            if (Integer.parseInt(operand) < 0 || Integer.parseInt(operand) >= this.ioList.size()) {
                throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
            }
            if (this.ioList.get(Integer.parseInt(operand)).isConnectedToUserIO) {
                System.out.println("--------------");
                System.out.println("Processor: " + this.id + " I/O " + operand + ": " + this.accumulator);
                System.out.println("--------------");
            } else {
                this.ioList.get(Integer.parseInt(operand)).buffer.put(this.accumulator);
            }
        };

        Consumer<String> in = (operand) -> {
            if (Integer.parseInt(operand) < 0 || Integer.parseInt(operand) >= this.ioList.size()) {
                throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
            }
            if (this.ioList.get(Integer.parseInt(operand)).isConnectedToUserIO) {
                Scanner sc = new Scanner(System.in);
                System.out.print("User input for Processor: " + this.id + ", I/O " + Integer.parseInt(operand) + ":");
                this.accumulator = Double.parseDouble(sc.nextLine());
            } else {
                this.accumulator = this.ioList.get(Integer.parseInt(operand)).buffer.get();
            }
        };
        Consumer<String> start = (empty) -> {
        };
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
    private void initRegister(int capacity) {
        for (int i = 0; i < capacity; i++) {
            this.register.add((double) 0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interpreter that = (Interpreter) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Interpreter{" +
                "id=" + id +
                ", ioList=" + ioList +
                '}';
    }
}
