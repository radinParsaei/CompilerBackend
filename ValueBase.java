import java.math.BigDecimal;

public class ValueBase implements java.io.Serializable {
  private Object data;
  private Data configData = SyntaxTree.getData();

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SyntaxTree.Number || obj instanceof SyntaxTree.Text || obj instanceof SyntaxTree.Boolean || obj instanceof SyntaxTree.Null || obj instanceof SyntaxTree.List || obj instanceof SyntaxTree.CreateInstance)) {
      obj = ((ValueBase) obj).getData();
    }
    while (obj instanceof ValueBase) {
      obj = ((ValueBase) obj).getData();
    }
    Object value = getData();
    while (value instanceof ValueBase) {
      value = ((ValueBase) value).getData();
    }
    if (value == null && obj == null) {
      return true;
    } else if (value == null || obj == null) {
      return false;
    }
    return value.equals(obj);
  }

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
