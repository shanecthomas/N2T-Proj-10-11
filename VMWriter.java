import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class VMWriter {
  private static HashMap<Segment, String> segment = new HashMap<Segment, String>();
  private static HashMap<Command, String> command = new HashMap<Command, String>();
  private PrintWriter out;
  static {
    segment.put(Segment.CONSTANT, "constant");
    segment.put(Segment.ARGUMENT, "argument");
    segment.put(Segment.LOCAL, "local");
    segment.put(Segment.STATIC, "static");
    segment.put(Segment.THIS, "this");
    segment.put(Segment.THAT, "that");
    segment.put(Segment.POINTER, "pointer");
    segment.put(Segment.TEMP, "temp");
    command.put(Command.ADD, "add");
    command.put(Command.SUB, "sub");
    command.put(Command.NEG, "neg");
    command.put(Command.EQ, "eq");
    command.put(Command.GT, "gt");
    command.put(Command.LT, "lt");
    command.put(Command.AND, "and");
    command.put(Command.OR, "or");
    command.put(Command.NOT, "not");
  }
  
  //Create .vm output file, prepare for writing
  public VMWriter(File f) throws FileNotFoundException {
    out = new PrintWriter(f);
  }

  //Write VM Push command
  public void writePush(Segment s, int i) {
    out.print("push " + segment.get(s) + " " + String.valueOf(i) + "\n");
  }

  //Write VM Pop command
  public void writePop(Segment s, int i) {
    out.print("pop " + segment.get(s) + " " + String.valueOf(i) + "\n");
  }

  //Write VM arthimetic-logical command
  public void writeArithmetic(Command c) {
    out.print(command.get(c) + "\n");
  }

  //Write VM Label command
  public void writeLabel(String label) {
    out.print("label " + label + "\n");
  }

  //Write VM goto command
  public void writeGoTo(String label) {
    out.print("goto " + label + "\n");
  }

  //write VM if-goto command
  public void writeIf(String label) {
    out.print("if-goto " + label + "\n");
  }

  //write VM Call command
  public void writeCall(String name, int nArgs) {
    out.print("call " + name + " " + String.valueOf(nArgs) + "\n");
  }

  //Write VM function command
  public void writeFunction(String name, int nVars) {
    out.print("function " + name + " " + String.valueOf(nVars) + "\n");
  }

  //Write VM Return command
  public void writeReturn() {
    out.print("return\n");
  }

  public void writeCommand(String s1, String s2, String s3) {
    out.print(s1 + " " + s2 + " " + s3 + "\n");
  }
  
  //Close output file
  public void close() {
    out.close();
  }
}