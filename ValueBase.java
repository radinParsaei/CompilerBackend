public class ValueBase implements java.io.Serializable {
  private Object data;

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return this.getData() + "";
  }
}
