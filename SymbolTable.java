import java.util.HashMap;

public class SymbolTable {
  private HashMap<String, Symbol> classSymbols = new HashMap<String, Symbol>();
  private HashMap<String, Symbol> subSymbols = new HashMap<String, Symbol>();
  private HashMap<KindOf, Integer> index = new HashMap<KindOf, Integer>();
  
  //Create new symbol table
  public SymbolTable() {
    index.put(KindOf.ARG, 0);
    index.put(KindOf.FIELD, 0);
    index.put(KindOf.STATIC, 0);
    index.put(KindOf.VAR, 0);
  }

  //Empties symbol table, indexes == 0
  //Called when starting compile subroutine delcaration
  public void reset() {
    subSymbols.clear();
    index.put(KindOf.VAR, 0);
    index.put(KindOf.ARG, 0);
  }

  //Defines (adds) new varaible of given name, type, kind
  //Assign index value of kind, adds +1 to index
  public void define(String name, String type, KindOf kind) {
    if (kind == KindOf.ARG || kind == KindOf.VAR) {
      int i = index.get(kind);
      Symbol s = new Symbol(type, kind, i);
      index.put(kind, i+1);
      subSymbols.put(name, s);
    }
    else if (kind == KindOf.STATIC || kind == KindOf.FIELD) {
      int i = index.get(kind);
      Symbol s = new Symbol(type, kind, i);
      index.put(kind, i+1);
      classSymbols.put(name, s);
    }
  }

  //Return num of varaibles of given kind in table
  public int varCount(KindOf kind) {
    return index.get(kind);
  }

  //Return kind of identifier. If not found, return NONE
  public KindOf kindOf(String name) {
    if (classSymbols.get(name) != null) {
      Symbol s = classSymbols.get(name);
      return s.getKind();
    }
    else if (subSymbols.get(name) != null) {
      Symbol s = subSymbols.get(name);
      return s.getKind();
    }
    else {
      return KindOf.NONE;
    }
  }

  //Return type of variable
  public String typeOf(String name) {
    if (classSymbols.get(name) != null) {
      Symbol s = classSymbols.get(name);
      return s.getType();
    }
    else if (subSymbols.get(name) != null) {
      Symbol s = subSymbols.get(name);
      return s.getType();
    }
    else {
      return "";
    }
  }

  //Return index of varaible
  public int indexOf(String name) {
    if (classSymbols.get(name) != null) {
      Symbol s = classSymbols.get(name);
      return s.getIndex();
    }
    else if (subSymbols.get(name) != null) {
      Symbol s = subSymbols.get(name);
      return s.getIndex();
    }
    else {
      return -1;
    }
  }
}