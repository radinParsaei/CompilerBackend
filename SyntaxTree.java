import java.util.HashMap;
import java.math.BigDecimal;

public class SyntaxTree {
  public static ValueBase objectToValue(Object object) {
    try {
      if (object instanceof BigDecimal) {
       return new SyntaxTree.Number((BigDecimal)object);
      } else if (object instanceof String) {
       return new SyntaxTree.Text((String)object);
      } else if (object instanceof Integer) {
       return new SyntaxTree.Number((int)object);
     } else if (object instanceof Byte) {
       return new SyntaxTree.Number((byte)object);
      } else if (object instanceof Boolean || (boolean)object == true || (boolean)object == false) {
       return new SyntaxTree.Boolean((boolean)object);
      } else if (object == null) {
       return new SyntaxTree.Null();
      }
    } catch (ClassCastException ignore) {}
   return (ValueBase)object;
 }

  private static HashMap<String, ValueBase> variables = new HashMap<>();
  public static HashMap<String, ValueBase> getVariables() {
    return variables;
  }
  private static HashMap<String, ProgramBase> functions = new HashMap<>();
  public static HashMap<String, ProgramBase> getFunctions() {
    return functions;
  }
  public static class Number extends ValueBase {
    public Number(BigDecimal number) {
      this.setData(number);
    }
    public Number(double number) {
      this.setData(new BigDecimal(number));
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
    private boolean isStatic = true;
    public SetVariable(String variableName, ValueBase value) {
      this.variableName = variableName;
      this.value = value;
    }

    public SetVariable setIsStatic(boolean isStatic) {
      this.isStatic = isStatic;
      return this;
    }

    public boolean getIsStatic() {
      return isStatic;
    }

    @Override
    void eval() {
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null)) {
        value = (ValueBase)value.getData();
      }
      variables.put(variableName, value);
    }

    public String getVariableName() {
      return variableName;
    }

    public ValueBase getVariableValue() {
      return value;
    }
  }

  public static class Function extends ProgramBase implements java.io.Serializable {
    private String functionName;
    private ProgramBase program;
    public Function(String functionName, ProgramBase program) {
      this.functionName = functionName;
      if (functions.containsKey(functionName)) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, functionName);
      }
      functions.put(functionName, null);
      this.program = program;
    }

    @Override
    void eval() {
      functions.put(functionName, program);
    }

    public String getFunctionName() {
      return functionName;
    }

    public ProgramBase getProgram() {
      return program;
    }
  }

  public static class CallFunction extends ProgramBase implements java.io.Serializable {
    private String functionName;
    public CallFunction(String functionName) {
      this.functionName = functionName;
    }

    @Override
    void eval() {
      ProgramBase program = functions.get(functionName);
      if (program == null) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS, functionName);
      }
      program.eval();
    }

    public String getFunctionName() {
      return functionName;
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
        return new Number(((BigDecimal)v1.getData()).add((BigDecimal)v2.getData()));
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
        return new Number(((BigDecimal)v1.getData()).subtract((BigDecimal)v2.getData()));
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
        return new Number(((BigDecimal)v1.getData()).multiply((BigDecimal)v2.getData()));
      } else if (v1 instanceof Number && v2 instanceof Text) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((BigDecimal)v1.getData()).intValue(); i++) {
          result.append(v2.getData());
        }
        return new Text(result.toString());
      } else if (v2 instanceof Number && v1 instanceof Text) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < ((BigDecimal)v2.getData()).intValue(); i++) {
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
        return new Number(((BigDecimal)v1.getData()).divide((BigDecimal)v2.getData()));
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
        return new Number(((BigDecimal)v1.getData()).remainder((BigDecimal)v2.getData()));
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
      if (v1 instanceof Boolean && v2 instanceof Number) {
        if (((boolean)v1.getData())) {
          if (!((BigDecimal)v2.getData()).equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        } else {
          if (((BigDecimal)v2.getData()).equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        }
      }
      if (v2 instanceof Boolean && v1 instanceof Number) {
        if (((boolean)v2.getData())) {
          if (!((BigDecimal)v1.getData()).equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        } else {
          if (((BigDecimal)v1.getData()).equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        }
      }
      return new SyntaxTree.Boolean(v2.toString().equals(v1.toString()));
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
        return new Boolean((((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData())) == 1);
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
        int tmp = (((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData()));
        return new Boolean(tmp == 1 || tmp == 0);
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
        return new Boolean((((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData())) == -1);
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
        int tmp = (((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData()));
        return new Boolean(tmp == -1 || tmp == 0);
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

  public static class And extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public And(ValueBase v1, ValueBase v2) {
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
      if (v1 instanceof Boolean && v2 instanceof Boolean) {
        return new Boolean((boolean)v1.getData() && (boolean)v2.getData());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NUM | NULL in &&");
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

  public static class Or extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Or(ValueBase v1, ValueBase v2) {
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
      if (v1 instanceof Boolean && v2 instanceof Boolean) {
        return new Boolean((boolean)v1.getData() || (boolean)v2.getData());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NUM | NULL in ||");
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

  public static class BitwiseAnd extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public BitwiseAnd(ValueBase v1, ValueBase v2) {
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
      if (v1 instanceof Boolean && v2 instanceof Boolean) {
        return new Boolean((boolean)v1.getData() & (boolean)v2.getData());
      } else if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() & ((BigDecimal)v2.getData()).intValue()));
      } else if (v1 instanceof Number && v2 instanceof Boolean) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() & ((boolean)v2.getData()? 1:0)));
      } else if (v1 instanceof Boolean && v2 instanceof Number) {
        return new Number(new BigDecimal(((boolean)v1.getData()? 1:0) & ((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NULL in &");
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

  public static class BitwiseOr extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public BitwiseOr(ValueBase v1, ValueBase v2) {
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
      if (v1 instanceof Boolean && v2 instanceof Boolean) {
        return new Boolean((boolean)v1.getData() | (boolean)v2.getData());
      } else if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() | ((BigDecimal)v2.getData()).intValue()));
      } else if (v1 instanceof Number && v2 instanceof Boolean) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() | ((boolean)v2.getData()? 1:0)));
      } else if (v1 instanceof Boolean && v2 instanceof Number) {
        return new Number(new BigDecimal(((boolean)v1.getData()? 1:0) | ((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NULL in |");
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

  public static class LeftShift extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public LeftShift(ValueBase v1, ValueBase v2) {
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
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() << ((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in <<");
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

  public static class RightShift extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public RightShift(ValueBase v1, ValueBase v2) {
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
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() >> ((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in >>");
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

  public static class Xor extends ValueBase implements java.io.Serializable {
    private ValueBase v1, v2;
    public Xor(ValueBase v1, ValueBase v2) {
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
      if (v1 instanceof Boolean && v2 instanceof Boolean) {
        return new Boolean((boolean)v1.getData() ^ (boolean)v2.getData());
      } else if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() ^ ((BigDecimal)v2.getData()).intValue()));
      } else if (v1 instanceof Number && v2 instanceof Boolean) {
        return new Number(new BigDecimal(((BigDecimal)v1.getData()).intValue() ^ ((boolean)v2.getData()? 0:1)));
      } else if (v1 instanceof Boolean && v2 instanceof Number) {
        return new Number(new BigDecimal(((boolean)v1.getData()? 0:1) ^ ((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | NULL in ^");
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
        return new Number(((BigDecimal)value.getData()).negate());
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
        return new Boolean((((BigDecimal)value.getData()).compareTo(BigDecimal.ZERO) == 0? true : false));
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
        return ~((BigDecimal)value.getData()).intValue();
      } else if (value instanceof Boolean) {
        return ((boolean)value.getData()? new BigDecimal(-2) : new BigDecimal(-1));
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
        System.out.print(args[i]);
        if (i < args.length - 1) System.out.print(separator);
      }
    }
  }

  public static class Exit extends ProgramBase implements java.io.Serializable {
    private ValueBase status;
    public ValueBase getStatus() {
      return this.status;
    }
    public Exit(ValueBase status) {
      this.status = status;
    }

    @Override
    void eval() {
      ValueBase status = this.status;
      if (!(status instanceof Number || status instanceof Text || status instanceof Boolean)) {
        status = (ValueBase)status.getData();
      }
      if (status instanceof Number) {
        System.exit(((BigDecimal)status.getData()).intValue());
      } else if (status instanceof Null) {
        System.exit(0);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL in exit");
      }
    }
  }

  public static class If extends ProgramBase implements java.io.Serializable {
    private ValueBase condition;
    private ProgramBase program;
    private ProgramBase elseProgram;
    public ValueBase getCondition() {
      return this.condition;
    }
    public ProgramBase getProgram() {
      return this.program;
    }
    public ProgramBase getElseProgram() {
      return this.elseProgram;
    }
    public If addElse(ProgramBase elseProgram) {
      this.elseProgram = elseProgram;
      return this;
    }
    public If(ValueBase condition, ProgramBase program) {
      this.condition = condition;
      this.program = program;
    }

    @Override
    void eval() {
      ValueBase condition = this.condition;
      if (!(condition instanceof Number || condition instanceof Text || condition instanceof Boolean)) {
        condition = (ValueBase)condition.getData();
      }
      boolean condition2 = false;
      if (condition instanceof Number) {
        condition2 = ((BigDecimal)condition.getData()).intValue() != 0;
      } else if (condition instanceof Boolean) {
        condition2 = (boolean)condition.getData();
      } else if (condition instanceof Text) {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in If");
      }
      if (condition2) {
        program.eval();
      } else {
        if (elseProgram != null) elseProgram.eval();
      }
    }
  }

  public static class While extends ProgramBase implements java.io.Serializable {
    private ValueBase condition;
    private ProgramBase program;
    public ValueBase getCondition() {
      return this.condition;
    }
    public ProgramBase getProgram() {
      return this.program;
    }
    public While(ValueBase condition, ProgramBase program) {
      this.condition = condition;
      this.program = program;
    }

    @Override
    void eval() {
      ValueBase condition2;
      if (!(condition instanceof Number || condition instanceof Text || condition instanceof Boolean)) {
        condition2 = (ValueBase)condition.getData();
      } else {
        condition2 = condition;
      }
      boolean condition3 = false;
      if (condition2 instanceof Number) {
        condition3 = ((BigDecimal)condition2.getData()).intValue() != 0;
      } else if (condition2 instanceof Boolean) {
        condition3 = (boolean)condition2.getData();
      } else if (condition2 instanceof Text) {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR in While");
      }
      while (condition3) {
        program.eval();
        if (!(condition instanceof Number || condition instanceof Text || condition instanceof Boolean)) {
          condition2 = (ValueBase)condition.getData();
        } else {
          condition2 = condition;
        }
        if (condition2 instanceof Number) {
          condition3 = ((BigDecimal)condition2.getData()).intValue() != 0;
        } else if (condition2 instanceof Boolean) {
          condition3 = (boolean)condition2.getData();
        } else if (condition2 instanceof Text) {
          Errors.error(ErrorCodes.ERROR_TYPE, "STR in While");
        }
      }
    }
  }

  public static class Repeat extends ProgramBase implements java.io.Serializable {
    private ValueBase count;
    private ProgramBase program;
    public ValueBase getCount() {
      return this.count;
    }
    public ProgramBase getProgram() {
      return this.program;
    }
    public Repeat(ValueBase count, ProgramBase program) {
      this.count = count;
      this.program = program;
    }

    @Override
    void eval() {
      ValueBase count = this.count;
      if (!(count instanceof Number || count instanceof Text || count instanceof Boolean)) {
        count = (ValueBase)count.getData();
      }
      if (count instanceof Number) {
        for (BigDecimal i = BigDecimal.ZERO; i.compareTo((BigDecimal)count.getData()) == -1; i = i.add(BigDecimal.ONE)) {
          program.eval();
        }
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in If");
      }
    }
  }
}
