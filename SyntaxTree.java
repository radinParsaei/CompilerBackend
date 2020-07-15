import java.util.HashMap;

public class SyntaxTree {
  Object objectToValue(Object object) {
   if (object instanceof java.lang.Number) {
     return new SyntaxTree.Number((java.lang.Number)object);
   } else if (object instanceof String) {
     return new SyntaxTree.Text((String)object);
   }
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() * ((java.lang.Number)v2.getData()).doubleValue());
      } else if (v1 instanceof Number) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((java.lang.Number)v1.getData()).intValue(); i++) {
          result.append(v2.getData());
        }
        return new Text(result.toString());
      } else if (v2 instanceof Number) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((java.lang.Number)v2.getData()).intValue(); i++) {
          result.append(v1.getData());
        }
        return new Text(result.toString());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR * STR");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() / ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in /");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((java.lang.Number)v1.getData()).doubleValue() % ((java.lang.Number)v2.getData()).doubleValue());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in %");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      return v1.toString().equals(v2.toString())? new SyntaxTree.Number(1) : new SyntaxTree.Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      return (v1.toString().equals(v2.toString()) && v1 instanceof Number == v2 instanceof Number)? new SyntaxTree.Number(1) : new SyntaxTree.Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number((((java.lang.Number)v1.getData()).doubleValue() > ((java.lang.Number)v2.getData()).doubleValue())? 1:0);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in >");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number((((java.lang.Number)v1.getData()).doubleValue() >= ((java.lang.Number)v2.getData()).doubleValue())? 1:0);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in >=");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number((((java.lang.Number)v1.getData()).doubleValue() < ((java.lang.Number)v2.getData()).doubleValue())? 1:0);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in <");
        return new Number(0);
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
      if (!(v1 instanceof Number || v1 instanceof Text)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number((((java.lang.Number)v1.getData()).doubleValue() <= ((java.lang.Number)v2.getData()).doubleValue())? 1:0);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in <=");
        return new Number(0);
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
      if (!(value instanceof Number || value instanceof Text)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return new Number(-(((java.lang.Number)value.getData()).doubleValue()));
      } else {
        return new Text(new StringBuilder((String)value.getData()).reverse().toString());
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
