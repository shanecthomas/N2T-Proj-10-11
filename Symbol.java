public class Symbol {
  private String type;
  private KindOf kind;
  private int index;

  public Symbol(String t, KindOf k, int i) {
    type = t;
    kind = k;
    index = i;
  }

  public String getType() {
    return type;
  }

  public KindOf getKind() {
    return kind;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "Symbol{type=" + type + '\'' + ", kind=" + kind + ", index=" + index + '}';
  }
}