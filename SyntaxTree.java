import java.util.HashMap;

public class SyntaxTree {
  public static ValueBase objectToValue(Object object) {
    try {
      if (object instanceof java.lang.Number) {
       return new SyntaxTree.Number((java.lang.Number)object);
      } else if (object instanceof String) {
       return new SyntaxTree.Text((String)object);
      } else if (object instanceof Boolean || (boolean)object == true || (boolean)object == false) {
       return new SyntaxTree.Boolean((boolean)object);
      } else if (object == null) {
       return new SyntaxTree.Null();
      }
    } catch (ClassCastException ignore) {}
   return (ValueBase)object;
 }

  private static HashMap<String, ValueBase> variables = new HashMap<>();
  public static class Number extends ValueBase {
    public Number(java.lang.Number number){
      if (number.doubleValue() == number.intValue()) this.setData(number.intValue());
      else this.setData(number);
    }
  }

  public static class Text extends ValueBase {
   public Text(String text) {
     this.setData(text);
   }
  }

  public static class Boolean extends ValueBase {
    public Boolean(boolean bool) {
      this.setData(bool);
    }

    @Override
    public String toString() {
      return ((boolean)getData()? "True" : "False");
    }
  }

  public static class Null extends ValueBase {
    public Null() {
      this.setData(null);
    }
  }

  public static class Variable extends ValueBase implements java.io.Serializable {
    private String variableName;
    public Variable(String variableName) {
      this.variableName = variableName;
    }

    @Override
    public Object getData() {
      ValueBase tmp = variables.get(variableName);
      if (tmp == null) Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, variableName);
      return tmp;
    }

    public String getVariableName() {
      return variableName;
    }
  }

  public static class SetVariable extends ProgramBase implements java.io.Serializable {
    private String variableName;
    private ValueBase value;
    public SetVariable(String variableName, ValueBase value) {
      this.variableName = variableName;
      this.value = value;
    }

    @Override
    void eval() {
      variables.put(variableName, value);
    }

    public String getVariableName() {
      return variableName;
    }

    public ValueBase getVariableValue() {
      return value;
    }
  }

  public static class Add extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Add(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() + ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        return new Text(v1.toString() + v2.toString());
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Sub extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Sub(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() - ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        return new Text(v1.toString().replace(v2.toString(), ""));
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Mul extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Mul(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() * ((java.lang.Number)v2.getData()).doubleValue());
      } else if (v1 instanceof Number && v2 instanceof Text) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((java.lang.Number)v1.getData()).intValue(); i++) {
          result.append(v2.getData());
        }
        return new Text(result.toString());
      } else if (v2 instanceof Number && v1 instanceof Text) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((java.lang.Number)v2.getData()).intValue(); i++) {
          result.append(v1.getData());
        }
        return new Text(result.toString());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL * STR | BOOL | NULL");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Div extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Div(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() / ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in /");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Mod extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Mod(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() % ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in %");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Equals extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Equals(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      boolean equalAsBoolAndNumber = false;
      if (v1 instanceof Boolean) {
        v1 = new Number(((boolean)v1.getData())? 1 : 0);
      }
      if (v2 instanceof Boolean) {
        v2 = new Number(((boolean)v2.getData())? 1 : 0);
      }
      return new SyntaxTree.Boolean(v1.toString().equals(v2.toString()) || equalAsBoolAndNumber);
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class StrictEquals extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public StrictEquals(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      return new Boolean((v1.toString().equals(v2.toString()) && v1 instanceof Number == v2 instanceof Number));
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class GreaterThan extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public GreaterThan(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Boolean((((java.lang.Number)v1.getData()).doubleValue() > ((java.lang.Number)v2.getData()).doubleValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in >");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class GreaterThanOrEqual extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public GreaterThanOrEqual(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Boolean((((java.lang.Number)v1.getData()).doubleValue() >= ((java.lang.Number)v2.getData()).doubleValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in >=");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class LesserThan extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public LesserThan(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Boolean((((java.lang.Number)v1.getData()).doubleValue() < ((java.lang.Number)v2.getData()).doubleValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in <");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class LesserThanOrEqual extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public LesserThanOrEqual(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Boolean((((java.lang.Number)v1.getData()).doubleValue() <= ((java.lang.Number)v2.getData()).doubleValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in <=");
        return new Null();
      }
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class Negative extends ValueBase implements java.io.Serializable {
    private ValueBase value;
    public Negative(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return new Number(-(((java.lang.Number)value.getData()).doubleValue()));
      } else if (value instanceof Text) {
        return new Text(new StringBuilder((String)value.getData()).reverse().toString());
      }
      return value;
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class Not extends ValueBase implements java.io.Serializable {
    private ValueBase value;
    public Not(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return new Boolean((((java.lang.Number)value.getData()).doubleValue()) == 0? true : false);
      } else if (value instanceof Boolean) {
        return new Boolean(!(boolean)value.getData());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NULL in !");
        return new Null();
      }
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class BitwiseNot extends ValueBase implements java.io.Serializable {
    private ValueBase value;
    public BitwiseNot(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return ~((java.lang.Number)value.getData()).intValue();
      } else if (value instanceof Boolean) {
        return (java.lang.Number)((boolean)value.getData()? -2:-1);
      } else if (value instanceof Null) {
        return -1;
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in ~");
        return new Null();
      }
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class Programs extends ProgramBase implements java.io.Serializable {
    private ProgramBase[] programs;
    public Programs(ProgramBase... programs) {
      this.programs = programs;
    }
    public ProgramBase[] getPrograms() {
      return programs;
    }

    @Override
    void eval() {
      for (ProgramBase program : programs) {
        program.eval();
      }
    }
  }

  public static class Print extends ProgramBase implements java.io.Serializable {
    private ValueBase[] args;
    private ValueBase separator = new Text(" ");
    public ValueBase[] getArgs() {
      return this.args;
    }
    public ValueBase getSeparator() {
      return this.separator;
    }
    public Print setSeparator(ValueBase separator) {
      this.separator = separator;
      return this;
    }
    public Print(ValueBase... args) {
      this.args = args;
    }

    @Override
    void eval() {
      for (int i = 0; i < args.length; i++) {
        System.out.print(args[i].getData());
        if (i < args.length - 1) System.out.print(separator);
      }
    }
  }
}
