import java.util.*;
import java.math.BigDecimal;

public class SyntaxTree {
  public static ValueBase objectToValue(Object object) {
    try {
      if (object == null) {
        return new SyntaxTree.Null();
      } else if (object instanceof BigDecimal) {
        return new SyntaxTree.Number((BigDecimal)object);
      } else if (object instanceof String) {
        return new SyntaxTree.Text((String)object);
      } else if (object instanceof Integer) {
        return new SyntaxTree.Number((int)object);
      } else if (object instanceof Byte) {
        return new SyntaxTree.Number((byte)object);
      } else if (object instanceof ArrayList) {
        return List.fromArrayList((ArrayList<ValueBase>) object);
      }  else if (object instanceof Boolean || (boolean) object || !((boolean) object)) {
        return new SyntaxTree.Boolean((boolean)object);
      }
    } catch (ClassCastException ignore) {}
    assert object instanceof ValueBase;
    return (ValueBase)object;
  }

  private static final HashMap<String, ValueBase> variables = new HashMap<>();
  public final static ArrayList<String> staticFunctions = new ArrayList<>();
  public final static ArrayList<String> staticParameters = new ArrayList<>();
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
  private static int id = 0;

  public static int getId() {
    return id;
  }

  public static void setId(int id) {
    SyntaxTree.id = id;
  }

  public static void touchFunctionsFromClass(ProgramBase program, String className) {
    if (program instanceof Programs) {
      for (ProgramBase program1 : ((Programs) program).getPrograms()) {
        touchFunctionsFromClass(program1, className);
      }
    } else if (program instanceof Function) {
      functions.put("#C" + className + "#" + ((Function) program).getFunctionName(), null);
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

  public static void deleteNativeFunction(String parent, String name, int argumentCount) {
    data.getFunctions().remove(name + ":N#" + argumentCount + "#" + parent);
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


  public static class List extends ValueBase {
    public List(ValueBase... data) {
      this.setData(new ArrayList<>(Arrays.asList(data)));
    }

    @Override
    public String toString() {
      StringBuilder stringBuilder = new StringBuilder("[");
      boolean isFirst = true;
      for (ValueBase value : (ArrayList<ValueBase>) getData()) {
        if (isFirst) {
          isFirst = false;
        } else {
          stringBuilder.append(", ");
        }
        if (value instanceof CreateInstance) value = getTextFromInstance(value);
        stringBuilder.append(value.toString());
      }
      return stringBuilder.append("]").toString();
    }

    public static List fromArrayList(ArrayList<ValueBase> arrayList) {
      List list = new List();
      list.setData(arrayList);
      return list;
    }
  }

  public static class Append extends ValueBase {
    private final ValueBase list;
    private final ValueBase value;
    public Append(ValueBase list, ValueBase value) {
      this.list = list;
      this.value = value;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      ValueBase value = this.value;
      list.setConfigData(getConfigData());
      value.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      if (list instanceof List) {
        ArrayList<ValueBase> arrayList = (ArrayList<ValueBase>) ((ArrayList<ValueBase>) list.getData()).clone();
        arrayList.add(value);
        return List.fromArrayList(arrayList);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List (Append)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public ValueBase getValue() {
      return value;
    }
  }

  public static class Insert extends ValueBase {
    private final ValueBase list;
    private final ValueBase value;
    private final ValueBase index;
    public Insert(ValueBase list, ValueBase value, ValueBase index) {
      this.list = list;
      this.value = value;
      this.index = index;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      ValueBase value = this.value;
      ValueBase index = this.index;
      list.setConfigData(getConfigData());
      value.setConfigData(getConfigData());
      index.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      if (!(index instanceof Number || index instanceof Text || index instanceof Boolean || index instanceof Null || index instanceof List)) {
        index = (ValueBase)index.getData();
      }
      if (list instanceof List && index instanceof Number) {
        ArrayList<ValueBase> arrayList = (ArrayList<ValueBase>) ((ArrayList<ValueBase>) list.getData()).clone();
        int i = ((BigDecimal)index.getData()).intValue();
        if (i > arrayList.size()) {
          Null _null = new Null();
          arrayList.ensureCapacity(i);
          while (arrayList.size() < i) {
            arrayList.add(_null);
          }
        }
        arrayList.add(i, value);
        return List.fromArrayList(arrayList);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List AND/OR index is not Number (Insert)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public ValueBase getValue() {
      return value;
    }

    public ValueBase getIndex() {
      return index;
    }
  }

  public static class Set extends ValueBase {
    private final ValueBase list;
    private final ValueBase value;
    private final ValueBase index;
    public Set(ValueBase list, ValueBase value, ValueBase index) {
      this.list = list;
      this.value = value;
      this.index = index;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      ValueBase value = this.value;
      ValueBase index = this.index;
      list.setConfigData(getConfigData());
      value.setConfigData(getConfigData());
      index.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      if (!(index instanceof Number || index instanceof Text || index instanceof Boolean || index instanceof Null || index instanceof List)) {
        index = (ValueBase)index.getData();
      }
      if (list instanceof List && index instanceof Number) {
        ArrayList<ValueBase> arrayList = (ArrayList<ValueBase>) ((ArrayList<ValueBase>) list.getData()).clone();
        arrayList.set(((BigDecimal)index.getData()).intValue(), value);
        return List.fromArrayList(arrayList);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List AND/OR index is not Number (Set)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public ValueBase getValue() {
      return value;
    }

    public ValueBase getIndex() {
      return index;
    }
  }

  public static class Get extends ValueBase {
    private final ValueBase list;
    private final ValueBase index;
    public Get(ValueBase list, ValueBase index) {
      this.list = list;
      this.index = index;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      ValueBase index = this.index;
      list.setConfigData(getConfigData());
      index.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (!(index instanceof Number || index instanceof Text || index instanceof Boolean || index instanceof Null || index instanceof List)) {
        index = (ValueBase)index.getData();
      }
      if (list instanceof List && index instanceof Number) {
        return ((ArrayList<ValueBase>) list.getData()).get(((BigDecimal)index.getData()).intValue());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List AND/OR index is not Number (Get)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public ValueBase getIndex() {
      return index;
    }
  }

  public static class GetSize extends ValueBase {
    private final ValueBase list;
    public GetSize(ValueBase list) {
      this.list = list;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      list.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (list instanceof List) {
        return new Number(((ArrayList) list.getData()).size());
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List (GetSize)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }
  }

  public static class IndexOf extends ValueBase {
    private final ValueBase list;
    private ValueBase fromIndex;
    private final ValueBase value;
    private boolean last = false;
    public IndexOf(ValueBase list, ValueBase data) {
      this.list = list;
      this.value = data;
    }

    public IndexOf(ValueBase list, ValueBase data, ValueBase fromIndex) {
      this.list = list;
      this.value = data;
      this.fromIndex = fromIndex;
    }

    public boolean isLastIndexOf() {
      return last;
    }

    public IndexOf setLastIndexOf(boolean last) {
      this.last = last;
      return this;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      list.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      ValueBase data = this.value;
      data.setConfigData(getConfigData());
      if (!(data instanceof Number || data instanceof Text || data instanceof Boolean || data instanceof Null || data instanceof List)) {
        data = (ValueBase)data.getData();
      }
      ValueBase fromIndex = this.fromIndex;
      if (fromIndex != null) {
        fromIndex.setConfigData(getConfigData());
      }
      if (fromIndex != null && !(fromIndex instanceof Number || fromIndex instanceof Text || fromIndex instanceof Boolean || fromIndex instanceof Null || fromIndex instanceof List)) {
        fromIndex = (ValueBase)fromIndex.getData();
      }
      if (list instanceof List) {
        if (!last) {
          if (this.fromIndex == null) {
            return new Number(((ArrayList) list.getData()).indexOf(data));
          } else if (fromIndex instanceof Number) {
            int res = ((ArrayList) list.getData()).subList(((BigDecimal) fromIndex.getData()).intValue(), ((ArrayList) list.getData()).size()).indexOf(data);
            if (res != -1) {
              res += ((BigDecimal) fromIndex.getData()).intValue();
            }
            return new Number(res);
          } else {
            Errors.error(ErrorCodes.ERROR_TYPE, "fromIndex is not Number (IndexOf)");
            return new Null();
          }
        } else {
          if (this.fromIndex == null) {
            return new Number(((ArrayList) list.getData()).lastIndexOf(data));
          } else if (fromIndex instanceof Number) {
            int res = ((ArrayList) list.getData()).subList(0, ((BigDecimal) fromIndex.getData()).intValue() + 1).lastIndexOf(data);
            return new Number(res);
          } else {
            Errors.error(ErrorCodes.ERROR_TYPE, "fromIndex is not Number (IndexOf)");
            return new Null();
          }
        }
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List (IndexOf)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public ValueBase getValue() {
      return value;
    }

    public ValueBase getFromIndex() {
      return fromIndex;
    }
  }

  public static class Sort extends ValueBase {
    private ValueBase list;
    private boolean sortByNumber;
    public Sort(ValueBase list, boolean sortByNumber) {
      this.list = list;
      this.sortByNumber = sortByNumber;
    }

    @Override
    public ValueBase getData() {
      ValueBase list = this.list;
      list.setConfigData(getConfigData());
      if (!(list instanceof Number || list instanceof Text || list instanceof Boolean || list instanceof Null || list instanceof List)) {
        list = (ValueBase)list.getData();
      }
      if (list instanceof List) {
        ArrayList<ValueBase> arrayList = (ArrayList<ValueBase>) ((ArrayList<ValueBase>) list.getData()).clone();
        if (sortByNumber) {
          arrayList.sort((o1, o2) -> {
            if (o1 instanceof Number && o2 instanceof Number) {
              return ((BigDecimal) o1.getData()).compareTo((BigDecimal) o2.getData());
            } else {
              return -1;
            }
          });
        } else {
          arrayList.sort(Comparator.comparing(Object::toString));
        }
        return List.fromArrayList(arrayList);
      } else {
        Errors.error(ErrorCodes.ERROR_TYPE, "data is not List (Sort)");
        return new SyntaxTree.Null();
      }
    }

    public ValueBase getList() {
      return list;
    }

    public boolean isSortByNumber() {
      return sortByNumber;
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
      if (instance != null) {
        String[] splitInstance = instance.toString().split(":");
        if (addInstanceName && !variableName.startsWith("#C"))
          variableName = "#C" + splitInstance[1] + "#" + variableName;
        getConfigData().setInstanceName(splitInstance[0]);
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
    private boolean isStatic = false;

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
      if (Targets.useAnalyzer) Analyzer.setVariable(variableName, value);
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
      if (Targets.useAnalyzer) Analyzer.setVariable(variableName, value);
    }

    public SetVariable(String variableName, ValueBase value) {
      this.variableName = variableName;
      this.value = value;
      if (!data.getVariables().containsKey(variableName)) data.getVariables().put(variableName, null);
      if (Targets.useAnalyzer) Analyzer.setVariable(variableName, value);
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

    public boolean isStatic() {
      return isStatic;
    }

    public SetVariable setStatic(boolean aStatic) {
      isStatic = aStatic;
      return this;
    }

    @Override
    void eval() {
      if (instance != null) {
        String[] splitInstance = instance.toString().split(":");
        getData().setInstanceName(splitInstance[0]);
        if (addInstanceName && !variableName.startsWith("#C"))
          variableName = "#C" + splitInstance[1] + "#" + variableName;
      }
      if (variableName.startsWith("#C")) variableName = variableName.replace("#F", "");
      if (checkDeclarationInRuntime) checkDeclaration();
      ValueBase value = this.value;
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof CreateInstance || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof CreateInstance) {
        value.getData();
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
    private boolean error = true;
    private boolean isStatic = false;
    public Function(String functionName, ProgramBase program, boolean error, String... args) {
      this.args = args;
      this.error = error;
      StringBuilder finalFunctionName = new StringBuilder(functionName).append(":");
      for (String string : args) {
        finalFunctionName.append(",").append(string);
      }
      this.functionName = finalFunctionName.toString();
      if (data.getFunctions().containsKey(this.functionName)) touchedVariables.add(this.functionName);
      else data.getFunctions().put(this.functionName, null);
      this.program = NameSpaces.addNameSpaces("#F" + this.functionName, program, new ArrayList<>(Arrays.asList(args)));
    }

    public Function(String functionName, ProgramBase program, String... args) {
      this.args = args;
      StringBuilder finalFunctionName = new StringBuilder(functionName).append(":");
      for (String string : args) {
        finalFunctionName.append(",").append(string);
      }
      this.functionName = finalFunctionName.toString();
      if (data.getFunctions().containsKey(this.functionName)) touchedVariables.add(this.functionName);
      else data.getFunctions().put(this.functionName, null);
      this.program = NameSpaces.addNameSpaces("#F" + this.functionName, program, new ArrayList<>(Arrays.asList(args)));
    }

    @Override
    void eval() {
      if (error) {
        String[] splitFunctionName = functionName.split(":");
        for (String name : data.getFunctions().keySet()) {
          String[] splitName = name.split(":");
          if (name.startsWith(splitFunctionName[0] + ":") && splitName.length > 1 && splitFunctionName.length > 1 &&
                  splitName[1].split(",").length == splitFunctionName[1].split(",").length &&
                  !name.equals(functionName)) {
            Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, this.functionName);
          }
        }
        if (data.getFunctions().get(this.functionName) != null) {
          Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, this.functionName);
        }
      }
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

    public Function setStatic(boolean aStatic) {
      isStatic = aStatic;
      return this;
    }

    public boolean isStatic() {
      return isStatic;
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
      if (instance != null) {
        ValueBase instance = this.instance;
        if (!(instance instanceof Number || instance instanceof Text || instance instanceof Boolean || instance instanceof Null || instance instanceof List || instance instanceof CreateInstance)) {
          instance = (ValueBase) instance.getData();
        }
        if (instance instanceof Text) {
          String string = (String) instance.getData();
          if (functionName.equals("replace")) {
            if (args.length != 2) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.replace(args[0].toString(), args[1].toString()));
          } else if (functionName.equals("charAt")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (!(args[0] instanceof Number || args[0] instanceof Text || args[0] instanceof Boolean || args[0] instanceof Null || args[0] instanceof List || args[0] instanceof CreateInstance)) {
              args[0] = (ValueBase) args[0].getData();
            }
            if (args[0] instanceof Number) {
              return new Text("" + string.charAt(((BigDecimal) args[0].getData()).intValue()));
            } else {
              Errors.error(ErrorCodes.ERROR_TYPE, "ARG0 MUST BE NUMBER");
              return new Null();
            }
          } else if (functionName.equals("charAtFromEnd")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (!(args[0] instanceof Number || args[0] instanceof Text || args[0] instanceof Boolean || args[0] instanceof Null || args[0] instanceof List || args[0] instanceof CreateInstance)) {
              args[0] = (ValueBase) args[0].getData();
            }
            if (args[0] instanceof Number) {
              return new Text("" + string.charAt(string.length() - 1 - ((BigDecimal) args[0].getData()).intValue()));
            } else {
              Errors.error(ErrorCodes.ERROR_TYPE, "ARG0 MUST BE NUMBER");
              return new Null();
            }
          } else if (functionName.equals("toUpper") || functionName.equals("toUpperCase")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.toUpperCase());
          } else if (functionName.equals("toLower") || functionName.equals("toLowerCase")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.toLowerCase());
          } else if (functionName.equals("toTitle") || functionName.equals("toTitleCase")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            StringBuilder stringBuilder = new StringBuilder(string.length());
            boolean spaceFound = true;
            for (char c : string.toCharArray()) {
              if (Character.isSpaceChar(c)) {
                spaceFound = true;
              } else if (spaceFound) {
                c = Character.toUpperCase(c);
                spaceFound = false;
              }
              stringBuilder.append(c);
            }
            return new Text(stringBuilder.toString());
          } else if (functionName.equals("startsWith")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.startsWith(args[0].toString()));
          } else if (functionName.equals("length")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Number(string.length());
          } else if (functionName.equals("isEmpty")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.isEmpty());
          } else if (functionName.equals("endsWith")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.endsWith(args[0].toString()));
          } else if (functionName.equals("contains") || functionName.equals("includes")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.contains(args[0].toString()));
          } else if (functionName.equals("equalsIgnoreCase")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.equalsIgnoreCase(args[0].toString()));
          } else if (functionName.equals("matches")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Boolean(string.matches(args[0].toString()));
          } else if (functionName.equals("codePointAt")) {
            if (args.length != 1) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (!(args[0] instanceof Number || args[0] instanceof Text || args[0] instanceof Boolean || args[0] instanceof Null || args[0] instanceof List || args[0] instanceof CreateInstance)) {
              args[0] = (ValueBase) args[0].getData();
            }
            if (args[0] instanceof Number) {
              return new Number(string.codePointAt(((BigDecimal) args[0].getData()).intValue()));
            } else {
              Errors.error(ErrorCodes.ERROR_TYPE, "ARG0 MUST BE NUMBER");
              return new Null();
            }
          } else if (functionName.equals("substring")) {
            if (args.length != 1 && args.length != 2) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (!(args[0] instanceof Number || args[0] instanceof Text || args[0] instanceof Boolean || args[0] instanceof Null || args[0] instanceof List || args[0] instanceof CreateInstance)) {
              args[0] = (ValueBase) args[0].getData();
            }
            if (args.length == 2) {
              if (!(args[1] instanceof Number || args[1] instanceof Text || args[1] instanceof Boolean || args[1] instanceof Null || args[1] instanceof List || args[1] instanceof CreateInstance)) {
                args[1] = (ValueBase) args[1].getData();
              }
            }
            if (args[0] instanceof Number) {
              if (args.length == 2 && args[1] instanceof Number) {
                return new Text(string.substring(((BigDecimal) args[0].getData()).intValue(), ((BigDecimal) args[1].getData()).intValue()));
              } else if (args.length == 1) {
                return new Text(string.substring(((BigDecimal) args[0].getData()).intValue()));
              } else {
                Errors.error(ErrorCodes.ERROR_TYPE, "ARG1 MUST BE NUMBER");
                return new Null();
              }
            } else {
              Errors.error(ErrorCodes.ERROR_TYPE, "ARG0 MUST BE NUMBER");
              return new Null();
            }
          } else if (functionName.equals("trim")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.trim());
          } else if (functionName.equals("reverse")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return (ValueBase) new Negative(instance).getData();
          } else if (functionName.equals("getFirstCharacter")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.charAt(0) + "");
          } else if (functionName.equals("getLastCharacter")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.charAt(string.length() - 1) + "");
          } else if (functionName.equals("getRandomCharacter")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.charAt(Math.abs(new Random().nextInt()) % string.length()) + "");
          } else if (functionName.equals("trimLeft")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.replaceAll("^\\s+", ""));
          } else if (functionName.equals("trimRight")) {
            if (args.length != 0) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            return new Text(string.replaceAll("\\s+$", ""));
          } else if (functionName.equals("indexOf")) {
            if (args.length != 1 && args.length != 2) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (args.length == 1) {
              return new Number(string.indexOf(args[0].toString()));
            } else {
              if (!(args[1] instanceof Number || args[1] instanceof Text || args[1] instanceof Boolean || args[1] instanceof Null || args[1] instanceof List || args[1] instanceof CreateInstance)) {
                args[1] = (ValueBase) args[1].getData();
              }
              if (args[1] instanceof Number) {
                return new Number(string.indexOf(args[0].toString(), ((BigDecimal) args[1].getData()).intValue()));
              } else {
                Errors.error(ErrorCodes.ERROR_TYPE, "ARG1 MUST BE NUMBER");
                return new Null();
              }
            }
          } else if (functionName.equals("lastIndexOf")) {
            if (args.length != 1 && args.length != 2) {
              Errors.error(ErrorCodes.ERROR_ARGS_NOT_MATCH, functionName);
              return new Null();
            }
            if (args.length == 1) {
              return new Number(string.lastIndexOf(args[0].toString()));
            } else {
              if (!(args[1] instanceof Number || args[1] instanceof Text || args[1] instanceof Boolean || args[1] instanceof Null || args[1] instanceof List || args[1] instanceof CreateInstance)) {
                args[1] = (ValueBase) args[1].getData();
              }
              if (args[1] instanceof Number) {
                return new Number(string.lastIndexOf(args[0].toString(), ((BigDecimal) args[1].getData()).intValue()));
              } else {
                Errors.error(ErrorCodes.ERROR_TYPE, "ARG1 MUST BE NUMBER");
                return new Null();
              }
            }
          }
          return new Null();
        }
        String[] splitInstance = instance.toString().split(":");
        getConfigData().setInstanceName(splitInstance[0]);
        if (addInstanceName) {
          functionName = "#C" + splitInstance[1] + "#" + functionName;
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
        if (!(tmp2 instanceof Number || tmp2 instanceof Text || tmp2 instanceof Boolean || tmp2 instanceof Null || tmp2 instanceof CreateInstance || tmp2 instanceof List)) {
          tmp2 = (ValueBase) tmp2.getData();
        }
        if (isRecursion)
          data.getVariables().putAll(tmp);
        return tmp2;
      } else {
        for (ProgramBase program : programs) {
          program.eval();
        }
        return (ValueBase) new OpCode.PopFromVM().getData();
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

    public ValueBase[] getArgs() {
      return args;
    }
  }


  public static ValueBase getTextFromInstance(ValueBase instance) {
    String className = ((CreateInstance) instance).getClassName();
    if (functions.containsKey("#C" + className + "#toString:"))
      instance = new SyntaxTree.CallFunction("toString").fromInstance(instance).setAddInstanceName(true).getData();
    if (instance instanceof CreateInstance && !((CreateInstance) instance).getClassName().equals(className)) instance = getTextFromInstance(instance);
    return new Text(instance.toString());
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean hasAdd = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Add:,((?!,).)+")) {
            hasAdd = true;
            break;
          }
        }
        if (hasAdd) {
          return new SyntaxTree.CallFunction("Add", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean hasSub = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Add:,((?!,).)+")) {
            hasSub = true;
            break;
          }
        }
        if (hasSub) {
          return new SyntaxTree.CallFunction("Add", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean hasSub = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Subtract:,((?!,).)+")) {
            hasSub = true;
            break;
          }
        }
        if (hasSub) {
          return new SyntaxTree.CallFunction("Subtract", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean hasSub = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Subtract:,((?!,).)+")) {
            hasSub = true;
            break;
          }
        }
        if (hasSub) {
          return new SyntaxTree.CallFunction("Subtract", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean hasMul = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Multiply:,((?!,).)+")) {
            hasMul = true;
            break;
          }
        }
        if (hasMul) {
          return new SyntaxTree.CallFunction("Multiply", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean hasMul = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Multiply:,((?!,).)+")) {
            hasMul = true;
            break;
          }
        }
        if (hasMul) {
          return new SyntaxTree.CallFunction("Multiply", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v1 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean hasDiv = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Divide:,((?!,).)+")) {
            hasDiv = true;
            break;
          }
        }
        if (hasDiv) {
          return new SyntaxTree.CallFunction("Divide", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean hasDiv = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Divide:,((?!,).)+")) {
            hasDiv = true;
            break;
          }
        }
        if (hasDiv) {
          return new SyntaxTree.CallFunction("Divide", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v1 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean hasMod = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Modulo:,((?!,).)+")) {
            hasMod = true;
            break;
          }
        }
        if (hasMod) {
          return new SyntaxTree.CallFunction("Modulo", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean hasMod = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Modulo:,((?!,).)+")) {
            hasMod = true;
            break;
          }
        }
        if (hasMod) {
          return new SyntaxTree.CallFunction("Modulo", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Power:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Power", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Power:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Power", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#LooksEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LooksEqual", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#LooksEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LooksEqual", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Equals:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Equals", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Equals:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Equals", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#GreaterThan:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("GreaterThan", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "z3GreaterThan:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("GreaterThan", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#GreaterThanOrEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("GreaterThanOrEqual", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#GreaterThanOrEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("GreaterThanOrEqual", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#LesserThan:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LesserThan", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#LesserThan:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LesserThan", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#LesserThanOrEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LesserThanOrEqual", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#LesserThanOrEqual:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("LesserThanOrEqual", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#And:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("And", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#And:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("And", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#Or:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Or", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#Or:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("Or", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#BitwiseAnd:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("BitwiseAnd", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#BitwiseAnd:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("BitwiseAnd", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List || v1 instanceof CreateInstance)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List || v2 instanceof CreateInstance)) {
        v2 = (ValueBase)v2.getData();
      }
      if (v1 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v1).getClassName() + "#BitwiseOr:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("BitwiseOr", v2).fromInstance(v1).setAddInstanceName(true).getData();
        }
      }
      if (v2 instanceof CreateInstance) {
        boolean overloaded = false;
        for (Map.Entry<String, ProgramBase> entry : functions.entrySet()) {
          if (entry.getKey().matches("#C" + ((CreateInstance) v2).getClassName() + "#BitwiseOr:,((?!,).)+")) {
            overloaded = true;
            break;
          }
        }
        if (overloaded) {
          return new SyntaxTree.CallFunction("BitwiseOr", v1).fromInstance(v2).setAddInstanceName(true).getData();
        }
      }
      if (v1 instanceof CreateInstance) v1 = getTextFromInstance(v1);
      if (v2 instanceof CreateInstance) v2 = getTextFromInstance(v2);
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List)) {
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List)) {
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
      if (!(v1 instanceof Number || v1 instanceof Text || v1 instanceof Boolean || v1 instanceof Null || v1 instanceof List)) {
        v1 = (ValueBase)v1.getData();
      }
      if (!(v2 instanceof Number || v2 instanceof Text || v2 instanceof Boolean || v2 instanceof Null || v2 instanceof List)) {
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
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      if (value instanceof Number) {
        return new Number(((BigDecimal)value.getData()).negate());
      } else if (value instanceof Text) {
        return new Text(new StringBuilder((String)value.getData()).reverse().toString());
      } else if (value instanceof List) {
        ArrayList<ValueBase> res = (ArrayList<ValueBase>) ((ArrayList<ValueBase>) value.getData()).clone();
        Collections.reverse(res);
        return List.fromArrayList(res);
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
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
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
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
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
      int id = getId();
      for (ProgramBase program : programs) {
        if (data.isContinued()) {
          break;
        }
        program.setData(data);
        if (program instanceof Continue) {
          program.eval();
          break;
        }
        if (id != getId()) break;
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
          if (functions.containsKey("#C" + ((CreateInstance) itemToPrint).getClassName() + "#toString:"))
            itemToPrint = new SyntaxTree.CallFunction("toString").fromInstance(itemToPrint).setAddInstanceName(true);
        }
        if (separator2 instanceof Variable) {
          separator2 = (ValueBase) separator2.getData();
        }
        if (separator2 instanceof CallFunction) {
          separator2 = (ValueBase) separator2.getData();
        }
        if (separator2 instanceof CreateInstance) {
          if (functions.containsKey("#C" + ((CreateInstance) separator2).getClassName() + "#toString:"))
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
      if (Targets.isWeb) {
        setId(getId() + 1);
      } else {
        ValueBase status = this.status;
        if (!(status instanceof Number || status instanceof Text || status instanceof Boolean || status instanceof Null)) {
          status = (ValueBase) status.getData();
        }
        if (status instanceof Number) {
          System.exit(((BigDecimal) status.getData()).intValue());
        } else if (status instanceof Null) {
          System.exit(0);
        } else {
          Errors.error(ErrorCodes.ERROR_TYPE, "STR | BOOL in exit");
        }
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
        int id = getId();
        while (condition3 && getId() == id) {
          if (Targets.isWeb && Targets.isInThread) {
            try {
              Thread.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
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
      int id = getId();
      if (count instanceof Number) {
        for (BigDecimal i = BigDecimal.ZERO; ((i.compareTo((BigDecimal)count.getData()) == -1) && getId() == id); i = i.add(BigDecimal.ONE)) {
          if (Targets.isWeb && Targets.isInThread) {
            try {
              Thread.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
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

  public static class PrintFunction extends ValueBase implements java.io.Serializable {
    private final Print program;
    public PrintFunction(Print program) {
      this.program = program;
    }

    @Override
    public ValueBase getData() {
      program.eval();
      return new Null();
    }

    public Print getProgram() {
      return program;
    }
  }

  public static class ExitFunction extends ValueBase implements java.io.Serializable {
    private final Exit program;
    public ExitFunction(Exit program) {
      this.program = program;
    }

    @Override
    public ValueBase getData() {
      program.eval();
      return new Null();
    }

    public Exit getProgram() {
      return program;
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
      variables.add(new SetVariable("%", new Null()).setIsDeclaration(true));
      classesParameters.put(className, variables);
      Programs programs1 = new Programs(programs);
      touchFunctionsFromClass(programs1, className);
      this.programs = NameSpaces.addNameSpaces("#C" + className + "#", programs1, new ArrayList<String>(Collections.singleton("%")));
    }

    public ProgramBase getPrograms() {
      return programs;
    }

    public String getClassName() {
      return className;
    }
  }

  public static class CreateInstance extends ValueBase implements java.io.Serializable {
    private final ArrayList<SetVariable> parameters = new ArrayList<>();
    private final String className;
    private CallFunction callInit;
    private final ValueBase[] args;
    private boolean isFirst = true;
    private Text instance;
    String instanceNameSpace;
    public CreateInstance(String className, ValueBase... args) {
      this.className = className;
      this.args = args;
      instanceNameSpace = nextNameSpace();
//      Data data = new Data();
//      data.setInstanceName(instanceNameSpace);
//      setConfigData(data);
      getConfigData().setInstanceName(instanceNameSpace);
    }

    @Override
    public Object getData() {
      getConfigData().setInstanceName(instanceNameSpace);
      if (isFirst) {
        for (SetVariable setVariable : classesParameters.get(className)) {
          SetVariable setVariable1 = ((SetVariable) setVariable.clone()).setVariableName(setVariable.getVariableName() + getConfigData().getInstanceName());
          setVariable1.setData(getConfigData());
          parameters.add(setVariable1);
        }
        for (SetVariable setVariable : parameters) {
          setVariable.eval();
        }
        if (classesWithInit.contains(className)) this.callInit = new CallFunction("#C" + className + "#<init>", args);
        instance = new SyntaxTree.Text(instanceNameSpace + ":" + className);
        isFirst = false;
        new SetVariable("#C" + className + "#%", this).fromInstance(this).setIsDeclaration(true).eval();
        if (callInit != null) callInit.fromInstance(this).getData();
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

  public static class This extends Variable implements java.io.Serializable {
    public This() {
      super("%");
    }
  }

  public static class CreateLambda extends Function implements java.io.Serializable {
    private static int i = 0;
    public CreateLambda(ProgramBase program, boolean error, String... args) {
      super("l#" + i++, program, error, args);
    }

    public CreateLambda(ProgramBase program, String... args) {
      super("l#" + i++, program, args);
    }

    @Override
    void eval() {
      if (functions.containsKey(getFunctionName())) {
        setFunctionName("l#" + i++);
      }
      super.eval();
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

  //why this class exists? when we can do it in "frontend" (just replacing import <something> to it's code)
//
//because this class can be used in "more high level" code generators to know when we have import (like https://github.com/radinParsaei/Blockly-test)
  public static class Import extends ProgramBase {
    public interface DoImport {
      ProgramBase doImport(String fileName);
    }
    private final String fileName;
    public static DoImport doImport;
    private ProgramBase program;
    public Import(String fileName) {
      this.fileName = fileName;
    }

    @Override
    void eval() {
      getProgram().eval();
    }

    public ProgramBase getProgram() {
      if (program == null) {
        program = doImport.doImport(fileName);
      }
      return program;
    }

    public String getFileName() {
      return fileName;
    }
  }

  public static class AwaitedProgram extends ProgramBase {
    public interface FetchProgram {
      ProgramBase fetch();
    }

    private final FetchProgram fetchProgram;
    public AwaitedProgram(FetchProgram fetchProgram) {
      this.fetchProgram = fetchProgram;
    }

    @Override
    void eval() {
      super.eval();
      fetchProgram().eval();
    }

    public ProgramBase fetchProgram() {
      return fetchProgram.fetch();
    }
  }

  public static class AwaitedValue extends ValueBase {
    public interface FetchValue {
      ValueBase fetch();
    }

    private final FetchValue fetchValue;
    public AwaitedValue(FetchValue fetchProgram) {
      this.fetchValue = fetchProgram;
    }

    @Override
    public Object getData() {
      ValueBase value = fetchValue.fetch();
      if (!(value instanceof Number || value instanceof Text || value instanceof Boolean || value instanceof Null || value instanceof List)) {
        value = (ValueBase)value.getData();
      }
      return value;
    }

    public ValueBase fetchValue() {
      return fetchValue.fetch();
    }
  }
}
