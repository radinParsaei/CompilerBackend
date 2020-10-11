public class ValueBase implements java.io.Serializable {
  private Object data;
  private Data configData = SyntaxTree.getData();

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public Data getConfigData() {
    return configData;
  }

  public ValueBase setConfigData(Data configData) {
    this.configData = configData;
    return this;
  }

  @Override
  public String toString() {
    return this.getData() + "";
  }
}
