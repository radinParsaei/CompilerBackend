import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Map;

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
      } else if (object instanceof Boolean || (boolean) object || !((boolean) object)) {
        return new SyntaxTree.Boolean((boolean)object);
      } else if (object == null) {
        return new SyntaxTree.Null();
      }
    } catch (ClassCastException ignore) {}
    assert object instanceof ValueBase;
    return (ValueBase)object;
  }

  private static final HashMap<String, ValueBase> variables = new HashMap<>();
  public static HashMap<String, ValueBase> getVariables() {
    return variables;
  }
  private static final HashMap<String, ProgramBase> functions = new HashMap<>();
  public static HashMap<String, ProgramBase> getFunctions() {
    return functions;
  }
  private static final HashMap<String, ArrayList<SetVariable>> classesParameters = new HashMap<>();
  public static HashMap<String, ArrayList<SetVariable>> getClassesParameters() {
    return classesParameters;
  }
  private static final ArrayList<String> classesWithInit = new ArrayList<>();
  private static final ArrayList<String> touchedVariables = new ArrayList<>();

  public static void touchFunctionsFromClass(ProgramBase program, String className) {
    if (program instanceof Programs) {
      for (ProgramBase program1 : ((Programs) program).getPrograms()) {
        touchFunctionsFromClass(program1, className);
      }
    } else if (program instanceof Function) {
      functions.put("#C" + className + ((Function) program).getFunctionName(), null);
    }
  }

  public static ArrayList<String> getClassesWithInit() {
    return classesWithInit;
  }

  private static Data data = new Data();

  public static Data getData() {
    return data;
  }

  public static void setData(Data data) {
    SyntaxTree.data = data;
  }

  private static String lastNameSpace = "@";
  private static int location = 0;
  private static String nextNameSpace() {
    if (lastNameSpace.charAt(location) == 'z') {
      location++;
      lastNameSpace += "@";
    }
    StringBuilder stringBuilder = new StringBuilder(lastNameSpace);
    char toReplace = (char) (lastNameSpace.charAt(location) + 1);
    if (toReplace == '[') {
      toReplace += 6;
    }
    stringBuilder.setCharAt(location, toReplace);
    lastNameSpace = stringBuilder.toString();
    return lastNameSpace;
  }

  public static void resetNameSpaces() {
    lastNameSpace = "@";
    location = 0;
  }

  public static void declareNativeFunction(String parent, String name, int argumentCount) {
    name += ":N#" + argumentCount + "#" + parent;
    if (data.getFunctions().containsKey(name)) {
      Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, name);
    }
    data.getFunctions().put(name, null);
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
    private boolean error = true;
    private boolean useInstanceName = false;
    private ValueBase instance;
    private boolean addInstanceName = false;

    public boolean isUseInstanceName() {
      return useInstanceName;
    }

    public void setUseInstanceName(boolean useInstanceName) {
      this.useInstanceName = useInstanceName;
    }
    public Variable(String variableName) {
      this.variableName = variableName;
    }

    public Variable setAddInstanceName(boolean addInstanceName) {
      this.addInstanceName = addInstanceName;
      return this;
    }

    public boolean isAddInstanceName() {
      return addInstanceName;
    }

    public Variable fromInstance(ValueBase value) {
      useInstanceName = true;
      instance = value;
      return this;
    }

    public ValueBase getInstance() {
      return instance;
    }

    @Override
    public Object getData() {
      if (instance != null && !(instance instanceof This)) {
        String[] splitInstance = instance.toString().split(":");
        getConfigData().setInstanceName(splitInstance[0]);
        if (addInstanceName && !variableName.startsWith("#C"))
          variableName = "#C" + splitInstance[1] + variableName;
      }
      if (variableName.startsWith("#C")) variableName = variableName.replace("#F", "");
      ValueBase tmp = data.getVariables().get(variableName + (useInstanceName? getConfigData().getInstanceName():""));
      if (error && tmp == null) Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, variableName);
      if (!error && tmp == null) tmp = new SyntaxTree.Null();
      return tmp;
    }

    public Variable setVariableName(String variableName) {
      this.variableName = variableName;
      return this;
    }

    public String getVariableName() {
      return variableName;
    }

    public Variable setError(boolean error) {
      this.error = error;
      return this;
    }
  }

  public static class SetVariable extends ProgramBase implements java.io.Serializable {
    private String variableName;
    private final ValueBase value;
    private boolean isDeclaration = false;
    private boolean checkDeclarationInRuntime = false;
    private boolean useInstanceName = false;
    private ValueBase instance = null;
    private boolean addInstanceName = false;

    public boolean getIsDeclaration() {
      return isDeclaration;
    }

    public SetVariable setIsDeclaration(boolean isDeclaration) {
      this.isDeclaration = isDeclaration;
      return this;
    }

    public SetVariable setAddInstanceName(boolean addInstanceName) {
      this.addInstanceName = addInstanceName;
      return this;
    }

    public boolean isAddInstanceName() {
      return addInstanceName;
    }

    public boolean getCheckDeclarationInRuntime() {
      return checkDeclarationInRuntime;
    }

    public SetVariable setCheckDeclarationInRuntime(boolean checkDeclarationInRuntime) {
      this.checkDeclarationInRuntime = checkDeclarationInRuntime;
      return this;
    }
    public SetVariable(String variableName, ValueBase value, boolean isDeclaration) {
      this.variableName = variableName;
      this.value = value;
      this.isDeclaration = isDeclaration;
      checkDeclaration();
      if (!data.getVariables().containsKey(variableName)) data.getVariables().put(variableName, null);
    }

    public SetVariable(String variableName, ValueBase value, boolean isDeclaration, boolean checkDeclarationInRuntime) {
      this.variableName = variableName;
      this.value = value;
      this.isDeclaration = isDeclaration;
      this.checkDeclarationInRuntime = checkDeclarationInRuntime;
      if (!checkDeclarationInRuntime) {
        checkDeclaration();
      }
      if (!data.getVariables().containsKey(variableName)) data.getVariables().put(variableName, null);
    }

    public SetVariable(String variableName, ValueBase value) {
      this.variableName = variableName;
      this.value = value;
      if (!data.getVariables().containsKey(variableName)) data.getVariables().put(variableName, null);
    }

    public void checkDeclaration() {
      if (!variableName.startsWith("#C")) {
        if (isDeclaration && data.getVariables().get(variableName) != null) {
          Errors.error(ErrorCodes.ERROR_VARIABLE_REDECLARATION, variableName);
        }
      }
      if (!isDeclaration && data.getVariables().get(variableName + (useInstanceName? data.getInstanceName():"")) == null) {
        Errors.error(ErrorCodes.ERROR_VARIABLE_NOT_DECLARED, variableName);
      }
    }

    @Override
    void eval() {
      if (instance != null && !(instance instanceof This)) {
        String[] splitInstance = instance.toString().split(":");
        getData().setInstanceName(splitInstance[0]);
        if (addInstanceName && !variableName.startsWith("#C"))
          variableName = "#C" + splitInstance[1] + variableName;
      }
      if (variableName.startsWith("#C")) variableName = variableName.replace("#F", "");
      if (checkDeclarationInRuntime) checkDeclaration();
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof CreateInstance)) {
        value = (ValueBase)value.getData();
      }
      data.getVariables().put(variableName + (useInstanceName? data.getInstanceName():""), value);
    }

    public String getVariableName() {
      return variableName;
    }

    public SetVariable setVariableName(String variableName) {
      data.getVariables().remove(this.variableName);
      this.variableName = variableName;
      data.getVariables().put(variableName, null);
      return this;
    }

    public ValueBase getVariableValue() {
      return value;
    }

    @Override
    protected Object clone() {
      return new SetVariable(variableName, value)
              .setIsDeclaration(isDeclaration)
              .setCheckDeclarationInRuntime(checkDeclarationInRuntime);
    }

    public boolean isUseInstanceName() {
      return useInstanceName;
    }

    public void setUseInstanceName(boolean useInstanceName) {
      this.useInstanceName = useInstanceName;
    }

    public SetVariable fromInstance(ValueBase instance) {
      this.instance = instance;
      useInstanceName = true;
      return this;
    }

    public ValueBase getInstance() {
      return instance;
    }
  }

  public static class Function extends ProgramBase implements java.io.Serializable {
    private String functionName;
    private final ProgramBase program;
    private final String[] args;
    public Function(String functionName, ProgramBase program, boolean error, String... args) {
      this.args = args;
      StringBuilder finalFunctionName = new StringBuilder(functionName).append(":");
      for (String string : args) {
        finalFunctionName.append(",").append(string);
      }
      this.functionName = finalFunctionName.toString();
      if (error && data.getFunctions().get(this.functionName) != null) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, this.functionName);
      }
      if (data.getFunctions().containsKey(this.functionName)) touchedVariables.add(this.functionName);
      else data.getFunctions().put(this.functionName, null);
      this.program = NameSpaces.addNameSpaces("#F" + this.functionName, program, new ArrayList<>(Arrays.asList(args)));
    }

    public Function(String functionName, ProgramBase program, String... args) {
      this.args = args;
      StringBuilder finalFunctionName = new StringBuilder(functionName).append(":");
      for (String string : args) {
        finalFunctionName.append(",").append(string);
        data.getVariables().put(string, new SyntaxTree.Null());
      }
      this.functionName = finalFunctionName.toString();
      if (data.getFunctions().get(this.functionName) != null) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, this.functionName);
      }
      if (data.getFunctions().containsKey(this.functionName)) touchedVariables.add(this.functionName);
      else data.getFunctions().put(this.functionName, null);
      this.program = NameSpaces.addNameSpaces("#F" + this.functionName, program, new ArrayList<>(Arrays.asList(args)));
    }

    @Override
    void eval() {
      data.getFunctions().put(functionName, program);
    }

    public String getFunctionName() {
      return functionName;
    }

    public void setFunctionName(String functionName) {
      if (functionName.startsWith("#C"))
        data.getFunctions().remove(this.functionName);
      for (String string : touchedVariables) {
        data.getFunctions().put(string, null);
      }
      this.functionName = functionName;
      if (data.getFunctions().get(this.functionName) != null) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, this.functionName);
      }
      data.getFunctions().put(this.functionName, null);
    }

    public ProgramBase getProgram() {
      return program;
    }

    public String[] getArgs() {
      return args;
    }
  }

  public static class CallFunction extends ValueBase implements java.io.Serializable {
    private String functionName;
    private ProgramBase[] programs;
    private final ValueBase[] args;
    private boolean isRecursion = false;
    private ValueBase instance = null;
    private boolean addInstanceName = false;
    private boolean nativeFunction = false;
    public CallFunction(String functionName, ValueBase... args) {
      this.functionName = functionName;
      this.args = args;
    }

    void findFunction() {
      if (functionName.contains(":")) return;
      programs = new ProgramBase[args.length];
      ArrayList<String> params = new ArrayList<>();
      for (Map.Entry<String, ProgramBase> entry : data.getFunctions().entrySet()) {
        if (entry.getKey().split(":")[0].equals(functionName)) {
          String previousFunctionName = functionName;
          this.functionName = entry.getKey();
          if (this.functionName.split(":").length > 1) {
            if (this.functionName.split(":")[1].startsWith("N#")) {
              if (!("" + args.length).equals(this.functionName.split(":")[1].substring(2).split("#")[0])) {
                this.functionName = previousFunctionName;
                continue;
              }
              programs = new ProgramBase[args.length + 3];
              int i = 0;
              for (; i < args.length; i++) {
                programs[i] = new OpCode.PutToVM(args[i]);
              }
              programs[i] = new OpCode.PutToVM(new SyntaxTree.Text(functionName.split(":")[0]));
              String[] tmp = functionName.split("#");
              programs[++i] = new OpCode.PutToVM(new SyntaxTree.Text(tmp[tmp.length - 1]));
              programs[++i] = new OpCode(new SyntaxTree.Number(VM.DLCALL));
              nativeFunction = true;
            } else {
              for (String string : this.functionName.split(":")[1].split(",")) {
                if (string.equals("")) continue;
                params.add(string);
              }
            }
          }
          if (!nativeFunction && (params.size() != args.length)) {
            this.functionName = previousFunctionName;
            params.clear();
          }
        }
      }
      if (!nativeFunction && params.size() != args.length) {
        Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
      }
      if (!nativeFunction) {
        int i = 0;
        boolean hasF = true;
        for (ValueBase value : args) {
          if (this.functionName.startsWith("#C")) hasF = false;
          programs[i] = new SyntaxTree.SetVariable((hasF ? "#F" : "") + this.functionName + ":" + params.get(i++), value);
        }
      }
    }

    @Override
    public ValueBase getData() {
      if (instance != null && !(instance instanceof This)) {
        String[] splitInstance = instance.toString().split(":");
        getConfigData().setInstanceName(splitInstance[0]);
        if (addInstanceName) {
          functionName = "#C" + splitInstance[1] + functionName;
        }
      }
      findFunction();
      if (!nativeFunction) {
        HashMap<String, ValueBase> tmp = null;
        if (isRecursion) {
          tmp = new HashMap<>();
          for (Map.Entry<String, ValueBase> entry : data.getVariables().entrySet()) {
            if (entry.getKey().startsWith("#F" + this.functionName) && entry.getValue() != null)
              tmp.put(entry.getKey(), entry.getValue());
          }
        }
        if (programs != null) {
          for (ProgramBase program : programs) {
            program.getData().setInstanceName(getConfigData().getInstanceName());
            program.eval();
          }
        }
        ProgramBase program = data.getFunctions().get(functionName);
        if (program == null) {
          Errors.error(ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS, functionName);
          return new Null();
        }
        program.setData(getConfigData());
        program.eval();
        ValueBase tmp2;
        if (program.getData().getReturnedData() != null) {
          tmp2 = program.getData().getReturnedData();
          program.getData().setReturnedData(null);
        } else {
          tmp2 = new Null();
        }
        if (!(tmp2 instanceof Number || tmp2 instanceof Text || tmp2 instanceof Boolean || tmp2 instanceof Null || tmp2 instanceof CreateInstance)) {
          tmp2 = (ValueBase) tmp2.getData();
        }
        if (isRecursion)
          data.getVariables().putAll(tmp);
        return tmp2;
      } else {
        for (ProgramBase program : programs) {
          program.eval();
        }
        return new OpCode.PopFromVM();
      }
    }

    public String getFunctionName() {
      return functionName;
    }

    public ProgramBase[] getVariableSetters() {
      return programs;
    }

    public boolean isAddInstanceName() {
      return addInstanceName;
    }

    public CallFunction setAddInstanceName(boolean addInstanceName) {
      this.addInstanceName = addInstanceName;
      return this;
    }

    public void setRecursion(boolean recursion) {
      isRecursion = recursion;
    }

    public boolean isRecursion() {
      return isRecursion;
    }

    public CallFunction fromInstance(ValueBase instance) {
      this.instance = instance;
      return this;
    }

    public boolean isFromInstance() {
      return instance != null;
    }

    public ValueBase getInstance() {
      return instance;
    }

    public boolean isNativeFunction() {
      return nativeFunction;
    }

    public void setFunctionName(String functionName) {
      this.functionName = functionName;
    }
  }

  public static class Add extends ValueBase implements java.io.Serializable {
    private final ValueBase v1;
    private final ValueBase v2;
    public Add(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Sub(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Mul(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Div(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Mod(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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

  public static class Pow extends ValueBase implements java.io.Serializable {
    private final ValueBase v1;
    private final ValueBase v2;
    public Pow(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Number && v2 instanceof Number) {
        return new Number(((BigDecimal)v1.getData()).pow(((BigDecimal)v2.getData()).intValue()));
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in ^");
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Equals(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof Boolean && v2 instanceof Number) {
        if (((boolean)v1.getData())) {
          if (!v2.getData().equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        } else {
          if (v2.getData().equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        }
      }
      if (v2 instanceof Boolean && v1 instanceof Number) {
        if (((boolean)v2.getData())) {
          if (!v1.getData().equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        } else {
          if (v1.getData().equals(BigDecimal.ZERO)) {
            return new Boolean(true);
          }
        }
      }
      return new SyntaxTree.Boolean(v2.toString().equals(v1.toString()) ||
              (v1 instanceof Number && v2 instanceof Number && ((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData()) == 0));
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class StrictEquals extends ValueBase implements java.io.Serializable {
    private final ValueBase v1;
    private final ValueBase v2;
    public StrictEquals(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null)) {
        v2 = (ValueBase)v2.getData();
      }
      return new Boolean((v1.toString().equals(v2.toString()) && v1 instanceof Number == v2 instanceof Number) ||
              (v1 instanceof Number && v2 instanceof Number && ((BigDecimal)v1.getData()).compareTo((BigDecimal)v2.getData()) == 0));
    }

    public ValueBase getV1() {
      return v1;
    }

    public ValueBase getV2() {
      return v2;
    }
  }

  public static class GreaterThan extends ValueBase implements java.io.Serializable {
    private final ValueBase v1;
    private final ValueBase v2;
    public GreaterThan(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public GreaterThanOrEqual(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public LesserThan(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public LesserThanOrEqual(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public And(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Or(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public BitwiseAnd(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public BitwiseOr(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public LeftShift(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public RightShift(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase v1;
    private final ValueBase v2;
    public Xor(ValueBase v1, ValueBase v2) {
      this.v1 = v1;
      this.v2 = v2;
    }

    @Override
    public Object getData() {
      ValueBase v1 = this.v1, v2 = this.v2;
      v1.setConfigData(data);
      v2.setConfigData(data);
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
    private final ValueBase value;
    public Negative(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      value.setConfigData(data);
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
    private final ValueBase value;
    public Not(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      value.setConfigData(data);
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return new Boolean((((BigDecimal) value.getData()).compareTo(BigDecimal.ZERO) == 0));
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
    private final ValueBase value;
    public BitwiseNot(ValueBase value) {
      this.value = value;
    }

    @Override
    public Object getData() {
      ValueBase value = this.value;
      value.setConfigData(data);
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
    private final ProgramBase[] programs;
    public Programs(ProgramBase... programs) {
      this.programs = programs;
    }
    public ProgramBase[] getPrograms() {
      return programs;
    }

    @Override
    void eval() {
      for (ProgramBase program : programs) {
        if (data.isContinued()) {
          break;
        }
        program.setData(data);
        if (program instanceof Continue) {
          program.eval();
          break;
        }
        program.getData().setInstanceName(getData().getInstanceName());
        program.eval();
        if (data.isBroken()) break;
        if (data.getReturnedData() != null) break;
      }
    }
  }

  public static class Print extends ProgramBase implements java.io.Serializable {
    private final ValueBase[] args;
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
      separator.setConfigData(data);
      for (int i = 0; i < args.length; i++) {
        ValueBase itemToPrint = args[i];
        ValueBase separator2 = separator;
        separator2.setConfigData(this.getData());
        itemToPrint.setConfigData(this.getData());
        if (itemToPrint instanceof Variable) {
          itemToPrint = (ValueBase) itemToPrint.getData();
        }
        if (itemToPrint instanceof CallFunction) {
          itemToPrint = (ValueBase) itemToPrint.getData();
        }
        if (itemToPrint instanceof CreateInstance) {
          if (functions.containsKey("#C" + ((CreateInstance) itemToPrint).getClassName() + "toString:"))
            itemToPrint = new SyntaxTree.CallFunction("toString").fromInstance(itemToPrint).setAddInstanceName(true);
        }
        if (separator2 instanceof Variable) {
          separator2 = (ValueBase) separator2.getData();
        }
        if (separator2 instanceof CallFunction) {
          separator2 = (ValueBase) separator2.getData();
        }
        if (separator2 instanceof CreateInstance) {
          if (functions.containsKey("#C" + ((CreateInstance) separator2).getClassName() + "toString:"))
            separator2 = new SyntaxTree.CallFunction("toString").fromInstance(separator2).setAddInstanceName(true);
        }
        if (Targets.systemPrint) {
          System.out.print(itemToPrint);
        } else {
          Targets.print(itemToPrint);
        }
        if (i < args.length - 1) {
          if (Targets.systemPrint) {
            System.out.print(separator2);
          } else {
            Targets.print(separator2);
          }
        }
      }
    }
  }

  public static class Exit extends ProgramBase implements java.io.Serializable {
    private final ValueBase status;
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
    private final ValueBase condition;
    private final ProgramBase program;
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
      if (elseProgram instanceof If) {
        this.elseProgram = elseProgram;
      } else {
        this.elseProgram = NameSpaces.addNameSpaces(nextNameSpace(), elseProgram, null);
      }
      return this;
    }
    public If(ValueBase condition, ProgramBase program) {
      this.condition = condition;
      this.program = NameSpaces.addNameSpaces(nextNameSpace(), program, null);
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

  public static class For extends ProgramBase implements java.io.Serializable {
    private final Programs code;

    public For(ValueBase condition, ProgramBase step, ProgramBase init, ProgramBase program) {
      NameSpaces.addCodeBeforeContinue(step);
      this.code = NameSpaces.addNameSpaces(nextNameSpace(), new Programs(init, new While(condition, new Programs(program, step))), null);
      NameSpaces.addCodeBeforeContinue(null);
    }

    @Override
    void eval() {
      code.eval();
    }

    public Programs getCode() {
      return code;
    }
  }

  public static class While extends ProgramBase implements java.io.Serializable {
    private final ValueBase condition;
    private final ProgramBase program;
    public ValueBase getCondition() {
      return this.condition;
    }
    public ProgramBase getProgram() {
      return this.program;
    }
    public While(ValueBase condition, ProgramBase program) {
      this.condition = condition;
      this.program = NameSpaces.addNameSpaces(nextNameSpace(), program, null);
    }

    @Override
    void eval() {
      if (Targets.customWhile) {
        Targets._while(() -> {
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
          if (!condition3) return false;
          program.eval();
          if (data.isBroken()) {
            data.setBroken(false);
            return false;
          }
          data.setContinued(false);
          if (data.getReturnedData() != null) {
            return false;
          }
          if (!(condition instanceof Number || condition instanceof Text || condition instanceof Boolean)) {
            condition2 = (ValueBase) condition.getData();
          } else {
            condition2 = condition;
          }
          if (condition2 instanceof Number) {
            condition3 = ((BigDecimal) condition2.getData()).intValue() != 0;
          } else if (condition2 instanceof Boolean) {
            condition3 = (boolean) condition2.getData();
          } else if (condition2 instanceof Text) {
            Errors.error(ErrorCodes.ERROR_TYPE, "STR in While");
          }
          return condition3;
        });
      } else {
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
          if (data.isBroken()) {
            data.setBroken(false);
            return;
          }
          data.setContinued(false);
          if (data.getReturnedData() != null) {
            return;
          }
          if (!(condition instanceof Number || condition instanceof Text || condition instanceof Boolean)) {
            condition2 = (ValueBase) condition.getData();
          } else {
            condition2 = condition;
          }
          if (condition2 instanceof Number) {
            condition3 = ((BigDecimal) condition2.getData()).intValue() != 0;
          } else if (condition2 instanceof Boolean) {
            condition3 = (boolean) condition2.getData();
          } else if (condition2 instanceof Text) {
            Errors.error(ErrorCodes.ERROR_TYPE, "STR in While");
          }
        }
      }
    }
  }

  public static class Repeat extends ProgramBase implements java.io.Serializable {
    private final ValueBase count;
    private final ProgramBase program;
    public ValueBase getCount() {
      return this.count;
    }
    public ProgramBase getProgram() {
      return this.program;
    }
    public Repeat(ValueBase count, ProgramBase program) {
      this.count = count;
      this.program = NameSpaces.addNameSpaces(nextNameSpace(), program, null);
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
          if (data.isBroken()) {
            data.setBroken(false);
            return;
          }
          data.setContinued(false);
          if (data.getReturnedData() != null) {
            return;
          }
        }
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL | NULL in If");
      }
    }
  }

  public static class Break extends ProgramBase implements java.io.Serializable {
    @Override
    void eval() {
      data.setBroken(true);
    }
  }

  public static class Continue extends ProgramBase implements java.io.Serializable {
    private ProgramBase program;

    @Override
    void eval() {
      if (program != null) program.eval();
      data.setContinued(true);
    }

    public void addProgramBefore(ProgramBase program) {
      this.program = program;
    }

    public ProgramBase getProgram() {
      return program;
    }
  }

  public static class Return extends ProgramBase implements java.io.Serializable {
    ValueBase value;
    public Return(ValueBase value) {
      this.value = value;
    }

    @Override
    void eval() {
      data.setReturnedData(value);
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class ExecuteValue extends ProgramBase implements java.io.Serializable {
    ValueBase value;
    public ExecuteValue(ValueBase value) {
      this.value = value;
    }

    @Override
    void eval() {
      value.getData();
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class Global extends ProgramBase implements java.io.Serializable {
    String variableName;
    public Global(String variableName) {
      this.variableName = variableName;
    }

    public String getVariableName() {
      return variableName;
    }
  }

  public static class CreateClass extends ProgramBase implements java.io.Serializable {
    private final Programs programs;
    private final String className;
    public CreateClass(String className, ProgramBase... programs) {
      this.className = className;
      ArrayList<SetVariable> variables = new ArrayList<>();
      classesParameters.put(className, variables);
      Programs programs1 = new Programs(programs);
      touchFunctionsFromClass(programs1, className);
      this.programs = NameSpaces.addNameSpaces("#C" + className, programs1, null);
    }

    public ProgramBase getPrograms() {
      return programs;
    }

    public String getClassName() {
      return className;
    }
  }

  public static class CreateInstance extends ValueBase implements java.io.Serializable {
    private static final ArrayList<SetVariable> parameters = new ArrayList<>();
    private final String className;
    private CallFunction callInit;
    private final ValueBase[] args;
    private boolean isFirst = true;
    private Text instance;
    public CreateInstance(String className, ValueBase... args) {
      this.className = className;
      this.args = args;
      String instanceNameSpace = nextNameSpace();
//      Data data = new Data();
//      data.setInstanceName(instanceNameSpace);
//      setConfigData(data);
      getConfigData().setInstanceName(instanceNameSpace);
    }

    @Override
    public Object getData() {
      if (isFirst) {
        for (SetVariable setVariable : classesParameters.get(className)) {
          SetVariable setVariable1 = ((SetVariable) setVariable.clone()).setVariableName(setVariable.getVariableName() + getConfigData().getInstanceName());
          setVariable1.setData(getConfigData());
          parameters.add(setVariable1);
        }
        for (SetVariable setVariable : parameters) {
          setVariable.eval();
        }
        if (classesWithInit.contains(className)) this.callInit = new CallFunction("#C" + className + "<init>", args);
        instance = new SyntaxTree.Text(getConfigData().getInstanceName() + ":" + className);
        if (callInit != null) callInit.fromInstance(instance).getData();
        isFirst = false;
      }
      return instance;
    }

    public String getClassName() {
      return className;
    }

    public CallFunction getCallInit() {
      return callInit;
    }

    public ValueBase[] getArgs() {
      return args;
    }
  }

  public static class This extends ValueBase implements java.io.Serializable {

  }

  public static class CreateLambda extends Function implements java.io.Serializable {
    private static int i = 0;
    public CreateLambda(ProgramBase program, boolean error, String... args) {
      super("l#" + i++, program, error, args);
    }

    public CreateLambda(ProgramBase program, String... args) {
      super("l#" + i++, program, args);
    }

    public static void setCounter(int i) {
      CreateLambda.i = i;
    }
  }

  public static class Lambda extends ValueBase implements java.io.Serializable {
    private final CreateLambda createLambda;
    public Lambda(CreateLambda createLambda) {
      this.createLambda = createLambda;
    }

    @Override
    public Object getData() {
      createLambda.eval();
      return new Text(createLambda.getFunctionName());
    }

    public CreateLambda getCreateLambda() {
      return createLambda;
    }
  }

  public static class CallFunctionFromPointer extends ValueBase implements java.io.Serializable {
    private final ValueBase functionPointer;
    private final ValueBase[] values;
    public CallFunctionFromPointer(ValueBase functionPointer, ValueBase... values) {
      this.functionPointer = functionPointer;
      this.values = values;
    }

    @Override
    public Object getData() {
      int i = 0;
      String functionPointerString = functionPointer.toString();
      if (functionPointerString.split(":").length != 1) {
        for (String string : functionPointerString.split(":")[1].split(",")) {
          if (string.isEmpty()) continue;
          new SetVariable("#F" + functionPointerString + ":" + string, values[i++]).eval();
        }
      }
      ProgramBase program = data.getFunctions().get(functionPointerString);
      if (program == null) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS, functionPointerString.split(":")[0]);
        return new Null();
      }
      program.setData(getConfigData());
      program.eval();
      ValueBase tmp2;
      if (program.getData().getReturnedData() != null) {
        tmp2 = program.getData().getReturnedData();
        program.getData().setReturnedData(null);
      } else {
        tmp2 = new Null();
      }
      if (!(tmp2 instanceof Number || tmp2 instanceof Text || tmp2 instanceof Boolean || tmp2 instanceof Null)) {
        tmp2 = (ValueBase) tmp2.getData();
      }
      return tmp2;
    }

    public ValueBase getFunctionPointer() {
      return functionPointer;
    }

    public ValueBase[] getValues() {
      return values;
    }
  }
}
