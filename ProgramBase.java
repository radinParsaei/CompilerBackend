public class ProgramBase implements java.io.Serializable, Cloneable {
  protected Data data;

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public ProgramBase() {
    data = SyntaxTree.getData();
  }

  void eval() {
    //run tasks
  }

  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
