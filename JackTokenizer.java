import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.HashMap;

public class JackTokenizer {
  private static String token = "";
  private static TokenType type = TokenType.NONE;
  private int index = 0;
  private ArrayList<String> allTokens = new ArrayList<String>();
  private static Pattern pattern;
  private static String allKeyword = "";
  private static String allSymbol = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
  private static String allInt = "[0-9]+";
  private static String allString = "\"[^\"\n]*\"";
  private static String allIdentifier = "[a-zA-Z_]\\w*";
  private static HashMap<String, KeyWord> keyword = new HashMap<String, KeyWord>();
  private static HashSet<Character> operation = new HashSet<Character>();

  static {
    keyword.put("class", KeyWord.CLASS);
    keyword.put("constructor", KeyWord.CONSTRUCTOR);
    keyword.put("function", KeyWord.FUNCTION);
    keyword.put("method", KeyWord.METHOD);
    keyword.put("field", KeyWord.FIELD);
    keyword.put("static", KeyWord.STATIC);
    keyword.put("var", KeyWord.VAR);
    keyword.put("int", KeyWord.INT);
    keyword.put("char", KeyWord.CHAR);
    keyword.put("boolean", KeyWord.BOOLEAN);
    keyword.put("void", KeyWord.VOID);
    keyword.put("true", KeyWord.TRUE);
    keyword.put("false", KeyWord.FALSE);
    keyword.put("null", KeyWord.NULL);
    keyword.put("this", KeyWord.THIS);
    keyword.put("let", KeyWord.LET);
    keyword.put("do", KeyWord.DO);
    keyword.put("if", KeyWord.IF);
    keyword.put("else", KeyWord.ELSE);
    keyword.put("while", KeyWord.WHILE);
    keyword.put("return", KeyWord.RETURN);
    operation.add('+');
    operation.add('-');
    operation.add('*');
    operation.add('/');
    operation.add('&');
    operation.add('|');
    operation.add('<');
    operation.add('>');
    operation.add('=');
  }
  
  //open input .jack file & gets ready for tokenization
  public JackTokenizer(File f) throws FileNotFoundException {
    Scanner file = new Scanner(f);
    String rawLine = "";
    String line = "";
    while (file.hasNext()) {
      rawLine = file.nextLine();
      int i = rawLine.indexOf("//");
      if (i != -1)
        rawLine = rawLine.substring(0, i);
      rawLine.trim();
      if (rawLine.length() > 0)
        line += rawLine + "\n";
    }
    int start = line.indexOf("/*");
    if (start != -1) {
      rawLine = line;
      int end = line.indexOf("*/");
      while (start != -1) {
        if (end == -1) {
          line = line.substring(0, start-1);
          break;
        }
        rawLine = rawLine.substring(0, start) + rawLine.substring(end + 2);
        start = rawLine.indexOf("/*");
        end = rawLine.indexOf("*/");
        if (start == -1)
          line = rawLine;
      }
    }
    line.trim();
    for (String s: keyword.keySet()) {
      allKeyword += s + "|";
    }
    pattern = Pattern.compile(allIdentifier + "|" + allKeyword + allSymbol + "|" + allInt + "|" + allString);
    Matcher match = pattern.matcher(line);
    while (match.find()) {
      allTokens.add(match.group());
    }
  }

  //Are there more tokens in the input?
  public boolean hasMoreTokens() {
    return index < allTokens.size();
  }

  //Get next token from input, makes current token
  //Only called if hasMoreTokens == true
  //Initially no current token
  public void advance() {
    if (hasMoreTokens()) {
      token = allTokens.get(index);
      index++;
    }
    if (token.matches(allKeyword))
      type = TokenType.KEYWORD;
    else if (token.matches(allSymbol))
      type = TokenType.SYMBOL;
    else if (token.matches(allInt))
      type = TokenType.INT_CONST;
    else if (token.matches(allString))
      type = TokenType.STRING_CONST;
    else if (token.matches(allIdentifier))
      type = TokenType.IDENTIFIER;
  }

  public String getToken() {
    return token;
  }

  public void getLastToken() {
    if (index > 0) {
      index--;
      token = allTokens.get(index);
    }
  }

  //Returns type of current token as constant
  public TokenType tokenType() {
    return type;
  }

  //Returns keyword which is current token as constant
  //Only called if tokenType == KEYWORD
  public KeyWord keyWord() {
    if (type == TokenType.KEYWORD)
      return keyword.get(token);
    return KeyWord.NULL;
  }

  //Return char which is current token.
  //Only called if tokenType == SYMBOL
  public char symbol() {
    if (type == TokenType.SYMBOL)
      return token.charAt(0);
    return '?';
  }

  //Return string which is current token
  //Only called if tokenType == IDENTIFIER
  public String identifier() {
    if (type == TokenType.IDENTIFIER)
      return token;
    return null;
  }

  //Return int value of current token
  //Only called if tokenType == INT_CONST
  public int intVal() {
    if (type == TokenType.INT_CONST)
      return Integer.parseInt(token);
    return -99999999;
  }

  //Return string value of current token without opening/closing double quotes
  //Only called if tokenType == STRING_CONST
  public String stringVal() {
    if (type == TokenType.STRING_CONST)
      return token.substring(1, token.length() - 1);
    return null;
  }

  public boolean isOp() {
    return operation.contains(symbol());
  }

  public static String noSpaces(String s) {
    String noS = "";
    if (s.length() != 0) {
      String[] chars = s.split(" ");
      for (String c: chars) {
        noS += c;
      }
    }
    return noS;
  }
}