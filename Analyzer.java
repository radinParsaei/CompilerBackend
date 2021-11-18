import java.util.ArrayList;
import java.util.HashMap;

public class Analyzer {
    public static final int NUMBER = 0b00000001;
    public static final int TEXT = 0b00000010;
    public static final int BOOLEAN = 0b00000100;
    public static final int NULL = 0b00001000;
    public static final int LIST = 0b00010000;
    public static final int INSTANCE = 0b00100000;
    public static final int UNKNOWN = 0b11111111;

    public static void setFunction(String functionName, int argCount, ArrayList<ValueBase> returnedValues) {
        functions.put(functionName + ":" + argCount, returnedValues);
    }

    private static class Type {
        int type = 0;
        final ArrayList<String> instances = new ArrayList<>();

        void addType(int type) {
            this.type |= type;
        }

        void addInstanceName(String name) {
            instances.add(name);
        }
    }

    static void clear() {
        variables.clear();
        functions.clear();
    }

    static boolean functionExists(String functionName) {
        return functions.containsKey(functionName);
    }

    static boolean variableExists(String variableName) {
        return variables.containsKey(variableName);
    }

    static void initTypeFromValue(Type type, ValueBase value) {
        if (value instanceof SyntaxTree.Number) {
            type.addType(NUMBER);
        } else if (value instanceof SyntaxTree.Text) {
            type.addType(TEXT);
        } else if (value instanceof SyntaxTree.Boolean) {
            type.addType(BOOLEAN);
        } else if (value instanceof SyntaxTree.Null) {
            type.addType(NULL);
        } else if (value instanceof SyntaxTree.CreateInstance) {
            type.addType(INSTANCE);
            type.addInstanceName(((SyntaxTree.CreateInstance) value).getClassName());
//        } else if (value instanceof SyntaxTree.Variable) {
//            Type type1 = variables.get(((SyntaxTree.Variable) value).getVariableName());
//            type.connect(type1);
        } else if (value instanceof SyntaxTree.Add) {
            if (matches(((SyntaxTree.Add) value).getV1(), TEXT) || matches(((SyntaxTree.Add) value).getV2(), TEXT)) {
                type.addType(TEXT);
            } else if (matches(((SyntaxTree.Add) value).getV1(), NUMBER) && matches(((SyntaxTree.Add) value).getV1(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Sub) {
            if (matches(((SyntaxTree.Sub) value).getV1(), TEXT) || matches(((SyntaxTree.Sub) value).getV2(), TEXT)) {
                type.addType(TEXT);
            } else if (matches(((SyntaxTree.Sub) value).getV1(), NUMBER) && matches(((SyntaxTree.Sub) value).getV1(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Mul) {
            if (matches(((SyntaxTree.Mul) value).getV1(), TEXT) || matches(((SyntaxTree.Mul) value).getV2(), TEXT)) {
                type.addType(TEXT);
            } else if (matches(((SyntaxTree.Mul) value).getV1(), NUMBER) && matches(((SyntaxTree.Mul) value).getV1(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Div) {
            if (matches(((SyntaxTree.Div) value).getV1(), NUMBER) && matches(((SyntaxTree.Div) value).getV2(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Mod) {
            if (matches(((SyntaxTree.Mod) value).getV1(), NUMBER) && matches(((SyntaxTree.Mod) value).getV2(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Pow) {
            if (matches(((SyntaxTree.Pow) value).getV1(), NUMBER) && matches(((SyntaxTree.Pow) value).getV2(), NUMBER)) {
                type.addType(NUMBER);
            } else {
                type.addType(UNKNOWN);
            }
        } else if (value instanceof SyntaxTree.Append || value instanceof SyntaxTree.Insert || value instanceof SyntaxTree.Set || value instanceof SyntaxTree.Get) {
            type.addType(LIST);
        } else if (value instanceof SyntaxTree.Equals || value instanceof SyntaxTree.StrictEquals || value instanceof SyntaxTree.GreaterThan || value instanceof SyntaxTree.GreaterThanOrEqual || value instanceof SyntaxTree.LesserThan || value instanceof SyntaxTree.LesserThanOrEqual || value instanceof SyntaxTree.And || value instanceof SyntaxTree.Or || value instanceof SyntaxTree.Not) {
            type.addType(BOOLEAN);
        } else if (value instanceof SyntaxTree.BitwiseAnd || value instanceof SyntaxTree.BitwiseOr || value instanceof SyntaxTree.BitwiseNot || value instanceof SyntaxTree.LeftShift || value instanceof SyntaxTree.RightShift || value instanceof SyntaxTree.Xor) {
            type.addType(NUMBER);
        } else if (value instanceof SyntaxTree.Negative) {
            if (matches(((SyntaxTree.Negative) value).getValue(), TEXT)) {
                type.addType(TEXT);
            } else if (matches(((SyntaxTree.Negative) value).getValue(), NUMBER)) {
                type.addType(NUMBER);
            }
            type.addType(UNKNOWN);
        } else if (value instanceof SyntaxTree.CallFunction) {
            for (ValueBase value1 : functions.get(((SyntaxTree.CallFunction) value).getFunctionName() + ":" + ((SyntaxTree.CallFunction) value).getArgs().length)) {
                initTypeFromValue(type, value1);
            }
        } else {
            type.addType(UNKNOWN);
        }
    }
    private static final HashMap<String, Type> variables = new HashMap<>();
    private static final HashMap<String, ArrayList<ValueBase>> functions = new HashMap<>();

    static void setVariable(String variableName, ValueBase value) {
        Type type;
        if (variables.containsKey(variableName)) {
            type = variables.get(variableName);
        } else {
            type = new Type();
        }
        initTypeFromValue(type, value);
        variables.put(variableName, type);
    }

    public static boolean matches(ValueBase value, int type) {
        if (value instanceof SyntaxTree.Variable) {
            if ((variables.get(((SyntaxTree.Variable) value).getVariableName()).type | type) == type) {
                return true;
            }
        }
        Type type1 = new Type();
        initTypeFromValue(type1, value);
        return (type1.type | type) == type;
    }

    public static ArrayList<String> getPossibleInstanceNames(ValueBase value) {
        if (value instanceof SyntaxTree.Variable) {
            return variables.get(((SyntaxTree.Variable) value).getVariableName()).instances;
        }
        Type type1 = new Type();
        initTypeFromValue(type1, value);
        return type1.instances;
    }
}