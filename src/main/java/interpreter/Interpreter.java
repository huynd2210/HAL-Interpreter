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
  public String program;
  public List<String> ioList;
  public List<Page> pages;
  public MMU registerMMU;
  public MMU programMMU;
  public final int maxPages = 4;
  private Double accumulator;
  private Integer programCounter;
  private Integer currentInstruction;
  private final Map<String, Consumer<String>> instructionSet;
  //address space is 2^16
  private final int virtualAddressSize = 65536;
  private boolean isRandomReplacement;

  public Interpreter(int id, boolean isRandomReplacement) {
    this.id = id;
//        this.register = new ArrayList<>();
    this.pages = new ArrayList<>();
    this.ioList = new ArrayList<>();
    this.isRandomReplacement = isRandomReplacement;
    this.registerMMU = new MMU(virtualAddressSize, this, isRandomReplacement);
    this.programMMU = new MMU(virtualAddressSize, this, isRandomReplacement);
    int maxIO = 6;
    this.initIO(maxIO);
    this.accumulator = 0d;
    this.instructionSet = new HashMap<>();
    getInstructionSet();
  }

  public void readProgram(String pathToProgram) {
    this.program = readProgramFile(pathToProgram);
  }

  public void setProgram(String program) {
    this.program = program;
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

  public int getAmountOfPageFaults(String logs) {
    return logs.split("\n").length;
  }

  public void run(boolean isDebug) {
    System.out.println("Processor: " + this.id + " starting");
    long startTime = System.nanoTime();
    this.executeProgram(this.parseProgram(this.program), isDebug);
    long endTime = System.nanoTime();
    System.out.println("Logs:");
    this.registerMMU.printLog();
    System.out.println("Amount of page faults: ");
    System.out.println(getAmountOfPageFaults(this.registerMMU.logger.toString()));
//    long duration = (endTime - startTime) / 1000000; //ms
    long duration = (endTime - startTime) / 1000000; //ms
    printRunTime(duration);
  }

  private void printRunTime(long duration) {
//    int durationSecond = (int) duration / 1000;
    System.out.print("Program duration: ");
    System.out.println(duration);
//    if (durationSecond > 60) {
//      System.out.print(durationSecond / 60 + "m ");
//    }
//    System.out.print(durationSecond % 60 + "s\n");
  }

  private void printState(String currentInstruction) {
    System.out.println("Next instruction: " + currentInstruction);
    System.out.println("Register: ");
    for (int i = 0; i < this.pages.size(); i++) {
      for (int j = 0; j < this.pages.get(i).virtualAddressRegisters.size(); j++) {
        System.out.println(this.pages.get(i).get(j));
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
    if (token.length == 2) {
      command.accept(token[1]);
    } else {
      command.accept("");
    }
  }

  private void programGuard(String instruction) {
    String[] token = instruction.split(" ");
    if (this.currentInstruction == 0 && !instruction.equalsIgnoreCase("START")) {
      throw new IllegalArgumentException("START instruction not at the top");
    }
    if (!this.instructionSet.containsKey(token[0])) {
      throw new IllegalArgumentException("Instruction: " + token[0] + " not found");
    }
    if ((!instruction.equalsIgnoreCase("START")
            && !instruction.equalsIgnoreCase("STOP")
            && !instruction.equalsIgnoreCase("DUMPREG")
            && !instruction.equalsIgnoreCase("DUMPPROG")
            && !isNumeric(token[1]))) {
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
      Double indexPointer = 0.0;
      try {
        indexPointer = this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        this.registerMMU.resolveStore(indexPointer.intValue(), this.accumulator);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> loadInd = (operand) -> {
      Double indexPointer = 0.0;
      try {
        indexPointer = this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        this.registerMMU.resolveStore(indexPointer.intValue(), this.accumulator);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> divNum = (operand) -> this.accumulator /= Double.parseDouble(operand);
    Consumer<String> div = (operand) -> {
      try {
        this.accumulator /= this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> mulNum = (operand) -> this.accumulator *= Double.parseDouble(operand);
    Consumer<String> mul = (operand) -> {
      try {
        this.accumulator *= this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> subNum = (operand) -> this.accumulator -= Double.parseDouble(operand);
    Consumer<String> sub = (operand) -> {
      try {
        this.accumulator -= this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> addNum = (operand) -> this.accumulator += Double.parseDouble(operand);
    Consumer<String> add = (operand) -> {
      try {
        this.accumulator += this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
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
    Consumer<String> store = (operand) -> {
      try {
        this.registerMMU.resolveStore(Integer.parseInt(operand), this.accumulator);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    Consumer<String> load = (operand) -> {
      try {
//                this.accumulator = this.register.get(Integer.parseInt(operand));
        this.accumulator = this.registerMMU.resolveLoad(Integer.parseInt(operand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    Consumer<String> loadNum = (operand) -> this.accumulator = Double.parseDouble(operand);


    Consumer<String> out = operand -> {
      if (Integer.parseInt(operand) < 0 || Integer.parseInt(operand) >= this.ioList.size()) {
        throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
      }
      ioList.set(Integer.parseInt(operand), this.accumulator.toString());
      System.out.println("I/O " + operand + ": " + this.ioList.get(Integer.parseInt(operand)));
    };

    Consumer<String> in = operand -> {
      if (Integer.parseInt(operand) < 0 || Integer.parseInt(operand) >= this.ioList.size()) {
        throw new IllegalArgumentException("I/O " + operand + " doesnt exist");
      }
      Scanner sc = new Scanner(System.in);
      System.out.println("User Input for I/O: " + operand);
      this.accumulator = Double.parseDouble(sc.nextLine());
    };

    Consumer<String> start = (empty) -> {
    };

    Consumer<String> dumpreg = (empty) -> {
      int counter = 0;
      StringBuilder sb = new StringBuilder("\n");
      this.pages.sort(Comparator.comparingInt(Page::getPageId));
      for (int i = 0; i < this.pages.size(); i++) {
        for (int j = 0; j < this.pages.get(i).pageSize; j++) {
          sb.append("Register: ").append(counter).append(" value ").append(this.pages.get(i).get(j)).append("\n");
          counter++;
        }
      }
      System.out.println(sb);
    };

    Consumer<String> dumpall = (empty) -> {
      this.pages.sort(Comparator.comparingInt(Page::getPageId));
      StringBuilder sb = new StringBuilder("\n");
      for (int i = 0; i < this.pages.size(); i++) {
        for (int j = 0; j < this.pages.get(i).pageSize; j++) {
          sb.append("Page: " + this.pages.get(i).pageId + " Register: " + j + "  value: " + this.pages.get(i).get(j) + "\n");
        }
      }
      System.out.println(sb);
    };
    Consumer<String> dumpprog = (empty) -> {
      System.out.println(program);
    };
    instructionSet.put("DUMPREG", dumpreg);
    instructionSet.put("DUMPPROG", dumpprog);
    instructionSet.put("DUMPALL", dumpall);
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

  private void initIO(int maxIO) {
    for (int i = 0; i < maxIO; i++) {
      this.ioList.add("");
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
