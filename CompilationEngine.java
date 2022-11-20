import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class CompilationEngine {
  private JackTokenizer tokenizer;
  private SymbolTable symbolTable = new SymbolTable();
  private VMWriter vmWriter;
  private int labelCount = 0;
  private String className;
  private String subName;
  
  //Create compilation engine with given input/output
  //Next routine called is compileClass
  public CompilationEngine (File in, File out) throws FileNotFoundException {
    tokenizer = new JackTokenizer(in);
    vmWriter = new VMWriter(out);
  }

  //Compile complete class
  public void compileClass() throws IOException {
    tokenizer.advance();
    tokenizer.advance();
    className = tokenizer.identifier();
    tokenizer.advance();
    compileClassVarDec();
    compileSubroutine();
    vmWriter.close();
  }

  //Compile static variable declaratoin or field declaraction
  public void compileClassVarDec() {
    tokenizer.advance();
    if ((tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') || (tokenizer.keyWord() == KeyWord.CONSTRUCTOR || tokenizer.keyWord() == KeyWord.FUNCTION || tokenizer.keyWord() == KeyWord.METHOD)) {
      tokenizer.getLastToken();
      return;
    }
    KindOf kind = null;
    String type = "";
    String name = "";
    if (tokenizer.keyWord() == KeyWord.STATIC)
      kind = KindOf.STATIC;
    else if (tokenizer.keyWord() == KeyWord.FIELD)
      kind = KindOf.FIELD;
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
      type = tokenizer.getToken();
    else if (tokenizer.tokenType() == TokenType.IDENTIFIER)
      type = tokenizer.identifier();
    else
      type = "";
    do {
      tokenizer.advance();
      name = tokenizer.identifier();
      symbolTable.define(name, type, kind);
      tokenizer.advance();
    } while (tokenizer.symbol() != ';');
    compileClassVarDec();
  }

  //Compile complete method, function, constructor
  public void compileSubroutine() throws IOException {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
      tokenizer.getLastToken();
      return;
    }
    KeyWord keyword = tokenizer.keyWord();
    symbolTable.reset();
    if (tokenizer.keyWord() == KeyWord.METHOD)
      symbolTable.define("this", className, KindOf.ARG);
    String type = "";
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.VOID)
      type = "void";
    else {
      tokenizer.getLastToken();
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
        type = tokenizer.getToken();
      else if (tokenizer.tokenType() == TokenType.IDENTIFIER)
        type = tokenizer.identifier();
      else
        type = "";
    }
    tokenizer.advance();
    subName = tokenizer.identifier();
    tokenizer.advance();
    compileParameterList();
    tokenizer.advance();
    compileSubroutineBody(keyword);
    compileSubroutine();
  }

  //Compile (empty?) paramater list. Ignores parenthesis tokens ()
  public void compileParameterList() {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
      tokenizer.getLastToken();
      return;
    }
    String type = "";
    tokenizer.getLastToken();
    do {
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
        type = tokenizer.getToken();
      else if (tokenizer.tokenType() == TokenType.IDENTIFIER)
        type = tokenizer.identifier();
      else
        type = "";
      tokenizer.advance();
      symbolTable.define(tokenizer.identifier(), type, KindOf.ARG);
      tokenizer.advance();
    } while (tokenizer.symbol() != ')');
      tokenizer.getLastToken();
  }

  //Compile subroutine body
  public void compileSubroutineBody(KeyWord keyword) throws IOException {
    tokenizer.advance();
    compileVarDec();
    String func = "";
    if (className.length() != 0 && subName.length() != 0)
      func = className + "." + subName;
    vmWriter.writeFunction(func, symbolTable.varCount(KindOf.VAR));
    if (keyword == KeyWord.METHOD) {
      vmWriter.writePush(Segment.ARGUMENT, 0);
      vmWriter.writePush(Segment.POINTER, 0);
    }
    else if (keyword == KeyWord.CONSTRUCTOR) {
      vmWriter.writePush(Segment.CONSTANT, symbolTable.varCount(KindOf.FIELD));
      vmWriter.writeCall("Memory.alloc", 1);
      vmWriter.writePop(Segment.POINTER, 0);
    }
    compileStatements();
    tokenizer.advance();
  }

  //Compile var declaration
  public void compileVarDec() {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.VAR) {
      tokenizer.getLastToken();
      return;
    }
    String type = "";
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
      type = tokenizer.getToken();
    else if (tokenizer.tokenType() == TokenType.IDENTIFIER)
      type = tokenizer.identifier();
    else
      type = "";
    do {
      tokenizer.advance();
      symbolTable.define(tokenizer.identifier(), type, KindOf.ARG);
      tokenizer.advance();
    } while (tokenizer.symbol() != ';');
    compileVarDec();
  }

  //Compile sequence of statements. Ignores curly brackets {}
  public void compileStatements() throws IOException {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
      tokenizer.getLastToken();
      return;
    }
    switch (tokenizer.keyWord()) {
      case LET:
        compileLet();
        break;
      case IF:
        compileIf();
        break;
      case WHILE:
        compileWhile();
        break;
      case DO:
        compileDo();
        break;
      case RETURN:
        compileReturn();
        break;
      default:
        break;
    }
    compileStatements();
  }

  //Compile let statement
  public void compileLet() throws IOException {
    tokenizer.advance();
    String var = tokenizer.identifier();
    tokenizer.advance();
    boolean exp = false;
    if (tokenizer.symbol() == '[') {
      exp = true;
      vmWriter.writePush(getSegment(symbolTable.kindOf(var)), symbolTable.indexOf(var));
      compileExpression();
      tokenizer.advance();
      vmWriter.writeArithmetic(Command.ADD);
    }
    if (exp)
      tokenizer.advance();
    compileExpression();
    tokenizer.advance();
    if (exp) {
      vmWriter.writePop(Segment.TEMP, 0);
      vmWriter.writePop(Segment.POINTER, 1);
      vmWriter.writePush(Segment.TEMP, 0);
      vmWriter.writePop(Segment.THAT, 0);
    }
    else
      vmWriter.writePop(getSegment(symbolTable.kindOf(var)), symbolTable.indexOf(var));
  }

  //Compile if statement, possibly with trailing else clause
  public void compileIf() throws IOException {
    String nextLabel = "LABEL_" + (labelCount++);
    String elseLabel = "LABEL_" + (labelCount++);
    tokenizer.advance();
    compileExpression();
    tokenizer.advance();
    vmWriter.writeArithmetic(Command.NOT);
    vmWriter.writeIf(elseLabel);
    tokenizer.advance();
    compileStatements();
    tokenizer.advance();
    vmWriter.writeGoTo(nextLabel);
    vmWriter.writeLabel(elseLabel);
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.ELSE) {
      tokenizer.advance();
      compileStatements();
      tokenizer.advance();
    }
    else
      tokenizer.getLastToken();
    vmWriter.writeLabel(nextLabel);
  }

  //Compile while statement
  public void compileWhile() throws IOException {
    String breakLabel = "LABEL_" + (labelCount++);
    String whileLabel = "LABEL_" + (labelCount++);
    vmWriter.writeLabel(whileLabel);
    tokenizer.advance();
    compileExpression();
    tokenizer.advance();
    vmWriter.writeArithmetic(Command.NOT);
    vmWriter.writeIf(breakLabel);
    tokenizer.advance();
    compileStatements();
    tokenizer.advance();
    vmWriter.writeGoTo(whileLabel);
    vmWriter.writeLabel(breakLabel);
  }

  //Compile do statement
  public void compileDo() throws IOException {
    compileSubroutineCall();
    tokenizer.advance();
    vmWriter.writePop(Segment.TEMP, 0);
  }

  //Compile return statement
  public void compileReturn() throws IOException {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';')
      vmWriter.writePush(Segment.CONSTANT, 0);
    else {
      tokenizer.getLastToken();
      compileExpression();
      tokenizer.advance();
    }
    vmWriter.writeReturn();
  }

  //Compile expression
  public void compileExpression() throws IOException {
    compileTerm();
    do {
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.isOp()) {
        String op = "";
        switch (tokenizer.symbol()) {
          case '+':
            op = "add";
            break;
          case '-':
            op = "sub";
            break;
          case '*':
            op = "call Math.multiply 2";
            break;
          case '/':
            op = "call Math.divide 2";
            break;
          case '<':
            op = "lt";
            break;
          case '>':
            op = "gt";
            break;
          case '=':
            op = "eq";
            break;
          case '&':
            op = "and";
            break;
          case '|':
            op = "or";
            break;
          default:
            op = "";
            break;
        }
        compileTerm();
        vmWriter.writeCommand(op, "", "");
      }
      else {
        tokenizer.getLastToken();
        break;
      }
    } while (true);
  }

  //Compile term
  //If current token == identifier, resolve to variable, array element, subroutine call
  //Look for [ ( .
  public void compileTerm() throws IOException {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
      String id = tokenizer.identifier();
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
        vmWriter.writePush(getSegment(symbolTable.kindOf(id)), symbolTable.indexOf(id));
        compileExpression();
        tokenizer.advance();
        vmWriter.writeArithmetic(Command.ADD);
        vmWriter.writePop(Segment.POINTER, 1);
        vmWriter.writePush(Segment.THAT, 0);
      }
      else if (tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
        tokenizer.getLastToken();
        tokenizer.getLastToken();
        compileSubroutineCall();
      }
      else {
        tokenizer.getLastToken();
        vmWriter.writePush(getSegment(symbolTable.kindOf(id)), symbolTable.indexOf(id));
      }
    }
    else {
      if (tokenizer.tokenType() == TokenType.INT_CONST)
        vmWriter.writePush(Segment.CONSTANT, tokenizer.intVal());
      else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
        String s = tokenizer.stringVal();
        vmWriter.writePush(Segment.CONSTANT, s.length());
        vmWriter.writeCall("String.appendChar", 2);
        for (int i = 0; i < s.length(); i++) {
          vmWriter.writePush(Segment.CONSTANT, (int)s.charAt(i));
          vmWriter.writeCall("String.appendChar", 2);
        }
      }
      else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.TRUE) {
        vmWriter.writePush(Segment.CONSTANT, 0);
        vmWriter.writeArithmetic(Command.NOT);
      }
      else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.THIS) {
        vmWriter.writePush(Segment.POINTER, 0);
      }
      else if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.FALSE || tokenizer.keyWord() == KeyWord.NULL)) {
        vmWriter.writePush(Segment.CONSTANT, 0);
      }
      else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
        compileExpression();
        tokenizer.advance();
      }
      else if (tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
        char c = tokenizer.symbol();
        compileTerm();
        if (c == '-')
          vmWriter.writeArithmetic(Command.NEG);
        else
          vmWriter.writeArithmetic(Command.NOT);
      }
    }
  }

  //Compile (empty?) comma-seperated list of expressions
  //Return number of expressions in list
  public int compileExpressionList() throws IOException {
    int nArgs = 0;
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')')
      tokenizer.getLastToken();
    else {
      nArgs = 1;
      tokenizer.getLastToken();
      compileExpression();
      do {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
          compileExpression();
          nArgs++;
        }
        else {
          tokenizer.getLastToken();
          break;
        }
      } while (true);
    }
    return nArgs;
  }

  public void compileSubroutineCall() throws IOException {
    tokenizer.advance();
    String name = tokenizer.identifier();
    int nArgs = 0;
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
      vmWriter.writePush(Segment.POINTER, 0);
      nArgs = compileExpressionList() + 1;
      tokenizer.advance();
      vmWriter.writeCall(className + "." + name, nArgs);
    }
    else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
      String obj = name;
      tokenizer.advance();
      name = tokenizer.identifier();
      String type = symbolTable.typeOf(obj);
      if (type.equals(""))
        name = obj + "." + name;
      else {
        nArgs = 1;
        vmWriter.writePush(getSegment(symbolTable.kindOf(obj)), symbolTable.indexOf(obj));
        name = symbolTable.typeOf(obj) + "." + name;
      }
      tokenizer.advance();
      nArgs += compileExpressionList();
      tokenizer.advance();
      vmWriter.writeCall(name, nArgs);
    }
  }

  private Segment getSegment(KindOf kind) {
    switch (kind) {
      case FIELD:
        return Segment.THIS;
      case STATIC:
        return Segment.STATIC;
      case VAR:
        return Segment.LOCAL;
      case ARG:
        return Segment.ARGUMENT;
      default:
        return Segment.NONE;
    }
  }
}