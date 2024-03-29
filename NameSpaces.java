import java.util.ArrayList;

public class NameSpaces {
    private static String sep = ":";
    private static boolean declarativeVariables = true;
    private static ArrayList<String> globals = null;
    private static ProgramBase codeBeforeContinue;

    public static void setDeclarativeVariables(boolean declarativeVariables) {
        NameSpaces.declarativeVariables = declarativeVariables;
    }

    public static SyntaxTree.Programs addNameSpaces(String nameSpace, ProgramBase program, ArrayList<String> declaredVariables) {
        if (declaredVariables == null) {
            declaredVariables = new ArrayList<>();
        }
        if (nameSpace.startsWith("#C")) {
            sep = "";
        } else {
            sep = ":";
        }
        if (declaredVariables.size() > 0 && declaredVariables.get(declaredVariables.size() - 1).equals("%args")) {
            declaredVariables.set(declaredVariables.size() - 1, "args");
        }
        boolean isFirst = !declarativeVariables && globals == null;
        if (isFirst) {
            globals = new ArrayList<>();
        }
        if (program instanceof SyntaxTree.SetVariable && nameSpace.startsWith("#C") && ((SyntaxTree.SetVariable) program).getInstance() == null) {
            boolean hasVariableInClass = false;
            boolean publicVariableExists = false;
            boolean hasVariableInParentClass = false;
            String parent = null;
            if (!(((SyntaxTree.SetVariable) program).getInstance() instanceof SyntaxTree.Variable &&
                    ((SyntaxTree.Variable) ((SyntaxTree.SetVariable) program).getInstance()).getVariableName().equals("%"))) {
                for (String i : SyntaxTree.getVariables().keySet()) {
                    if (i.equals(nameSpace + ((SyntaxTree.SetVariable) program).getVariableName())) {
                        hasVariableInClass = true;
                    }
                    if (i.equals(((SyntaxTree.SetVariable) program).getVariableName())) {
                        publicVariableExists = true;
                    }
                    if (SyntaxTree.getClassesParents().containsKey(nameSpace.replace("#C", "").replace("#", ""))) {
                        for (String j : SyntaxTree.getClassesParents().get(nameSpace.replace("#C", "").replace("#", ""))) {
                            if (i.equals("#C" + j + "#" + ((SyntaxTree.SetVariable) program).getVariableName())) {
                                hasVariableInParentClass = true;
                                parent = j;
                                break;
                            }
                        }
                    }
                }
                hasVariableInClass &= !publicVariableExists;
                hasVariableInParentClass &= !publicVariableExists;
                hasVariableInParentClass &= !hasVariableInClass;
                if (hasVariableInParentClass) {
                    ((SyntaxTree.SetVariable) program).fromInstance(new SyntaxTree.Parent(parent)).setAddInstanceName(true);
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getInstance(), declaredVariables);
                }
                if (publicVariableExists && !((SyntaxTree.SetVariable) program).getVariableName().contains("%")
                        && !((SyntaxTree.SetVariable) program).getVariableName().contains("#") && !((SyntaxTree.SetVariable) program).getIsDeclaration()) {
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getInstance(), declaredVariables);
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getVariableValue(), declaredVariables);
                    return new SyntaxTree.Programs(program);
                }
            }
        }
        if (program instanceof SyntaxTree.SetVariable) {
            if (declarativeVariables) {
                if (((SyntaxTree.SetVariable) program).getIsDeclaration()) {
                    declaredVariables.add(((SyntaxTree.SetVariable) program).getVariableName());
                    if (nameSpace.startsWith("#C") && !((SyntaxTree.SetVariable) program).isStatic()) {
                        ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                        SyntaxTree.getClassesParameters().get(nameSpace.substring(2, nameSpace.length() - 1)).add((SyntaxTree.SetVariable) program);
                        ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep +
                                ((SyntaxTree.SetVariable) program).getVariableName());
                    } else if (nameSpace.startsWith("#C")) {
                        SyntaxTree.staticParameters.add(nameSpace + ((SyntaxTree.SetVariable) program).getVariableName());
                        ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep +
                                ((SyntaxTree.SetVariable) program).getVariableName());
                        program.eval();
                    } else {
                        ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep +
                                ((SyntaxTree.SetVariable) program).getVariableName());
                    }
                } else {
                    if (declaredVariables.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                        if (nameSpace.startsWith("#C")) ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                        if (!(nameSpace.startsWith("#F") && ((SyntaxTree.SetVariable) program).getInstance() != null)) ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep + ((SyntaxTree.SetVariable) program).getVariableName());
                    }
                }
            } else {
                if (declaredVariables.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                    if (nameSpace.startsWith("#C")) {
                        ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                        SyntaxTree.getClassesParameters().get(nameSpace.substring(2)).add((SyntaxTree.SetVariable) program);
                    }
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep + ((SyntaxTree.SetVariable) program).getVariableName());
                } else if (!globals.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                    declaredVariables.add(((SyntaxTree.SetVariable) program).getVariableName());
                    if (nameSpace.startsWith("#C")) ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep +
                            ((SyntaxTree.SetVariable) program).getVariableName());
                }
            }
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getVariableValue(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getInstance(), declaredVariables);
        } else if (!declarativeVariables && program instanceof SyntaxTree.Global) {
            globals.add(((SyntaxTree.Global)program).getVariableName());
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program2 : ((SyntaxTree.Programs)program).getPrograms()) {
                addNameSpaces(nameSpace, program2, declaredVariables);
            }
        } else if (program instanceof SyntaxTree.CreateLambda) {
            addNameSpaces(nameSpace, ((SyntaxTree.CreateLambda) program).getProgram(), declaredVariables);
        } else if (program instanceof SyntaxTree.If) {
            addNameSpaces(nameSpace, ((SyntaxTree.If)program).getProgram(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.If)program).getCondition(), declaredVariables);
            if (((SyntaxTree.If) program).getElseProgram() != null)
                addNameSpaces(nameSpace, ((SyntaxTree.If)program).getElseProgram(), declaredVariables);
        } else if (program instanceof SyntaxTree.While) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.While)program).getCondition(), declaredVariables);
            addNameSpaces(nameSpace, ((SyntaxTree.While)program).getProgram(), declaredVariables);
        } else if (program instanceof SyntaxTree.Repeat) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Repeat)program).getCount(), declaredVariables);
        } else if (program instanceof SyntaxTree.Exit) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Exit)program).getStatus(), declaredVariables);
        } else if (program instanceof SyntaxTree.Return) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Return)program).getValue(), declaredVariables);
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.ExecuteValue)program).getValue(), declaredVariables);
        } else if (program instanceof SyntaxTree.Continue) {
            if (codeBeforeContinue != null) {
                addNameSpaces(nameSpace, codeBeforeContinue, declaredVariables);
                ((SyntaxTree.Continue) program).addProgramBefore(codeBeforeContinue);
            }
        } else if (program instanceof SyntaxTree.Print) {
            for (ValueBase value : ((SyntaxTree.Print)program).getArgs()) {
                addNameSpacesOnValue(nameSpace, value, declaredVariables);
            }
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Print)program).getSeparator(), declaredVariables);
        } else if (program instanceof SyntaxTree.InitParentClass) {
            addNameSpaces(nameSpace, ((SyntaxTree.InitParentClass)program).getSetter(), declaredVariables);
        } else if (program instanceof CustomProgram) {
            for (Object i : ((CustomProgram) program).addNamespaceOn()) {
                if (i instanceof ProgramBase) {
                    addNameSpaces(nameSpace, (ProgramBase) i, declaredVariables);
                } else if (i instanceof ValueBase) {
                    addNameSpacesOnValue(nameSpace, (ValueBase) i, declaredVariables);
                }
            }
        } else if (program instanceof SyntaxTree.Function) {
            if (nameSpace.startsWith("#C")) {
                ArrayList<String> declaredVariables1 = (ArrayList<String>) declaredVariables.clone();
                for (String string : ((SyntaxTree.Function) program).getArgs()) {
                    declaredVariables1.add("#F" + ((SyntaxTree.Function) program).getFunctionName() + ":" + string);
                }
                ((SyntaxTree.Function) program).setFunctionName(nameSpace + ((SyntaxTree.Function) program).getFunctionName());
                addNameSpaces(nameSpace, ((SyntaxTree.Function) program).getProgram(), declaredVariables1);
                program.eval();
                if (((SyntaxTree.Function) program).isStatic()) {
                    SyntaxTree.staticFunctions.add(((SyntaxTree.Function) program).getFunctionName());
                }
                if (((SyntaxTree.Function) program).getFunctionName().startsWith(nameSpace + "<init>")) {
                    SyntaxTree.getClassesWithInit().add(nameSpace.replace("#C", "").substring(0, nameSpace.length() - 3));
                }
            }
        }
        if (isFirst) {
            globals = null;
        }
        return new SyntaxTree.Programs(program);
    }

    public static void addNameSpacesOnValue(String nameSpace, ValueBase value, ArrayList<String> declaredVariables) {
        if (value instanceof SyntaxTree.CallFunction && nameSpace.startsWith("#C") && ((SyntaxTree.CallFunction) value).getInstance() == null) {
            boolean hasFunctionInClass = false;
            boolean publicFunctionExists = false;
            boolean hasFunctionInParentClass = false;
            String parent = null;
            if (!(((SyntaxTree.CallFunction) value).getInstance() instanceof SyntaxTree.Variable &&
                    ((SyntaxTree.Variable) ((SyntaxTree.CallFunction) value).getInstance()).getVariableName().equals("%"))) {
                for (String i : SyntaxTree.getFunctions().keySet()) {
                    if (i.startsWith(nameSpace + ((SyntaxTree.CallFunction) value).getFunctionName() + ":")) {
                        hasFunctionInClass = true;
                    }
                    if (i.startsWith(((SyntaxTree.CallFunction) value).getFunctionName() + ":")) {
                        publicFunctionExists = true;
                    }
                    if (SyntaxTree.getClassesParents().containsKey(nameSpace.replace("#C", "").replace("#", ""))) {
                        for (String j : SyntaxTree.getClassesParents().get(nameSpace.replace("#C", "").replace("#", ""))) {
                            if (i.startsWith("#C" + j + "#" + ((SyntaxTree.CallFunction) value).getFunctionName() + ":")) {
                                hasFunctionInParentClass = true;
                                parent = j;
                                break;
                            }
                        }
                    }
                }
                hasFunctionInClass &= !publicFunctionExists;
                hasFunctionInParentClass &= !publicFunctionExists;
                hasFunctionInParentClass &= !hasFunctionInClass;
                if (hasFunctionInClass) {
                    ((SyntaxTree.CallFunction) value).setFunctionName(nameSpace + sep + ((SyntaxTree.CallFunction) value).getFunctionName());
                }
                if (hasFunctionInParentClass) {
                    ((SyntaxTree.CallFunction) value).fromInstance(new SyntaxTree.Parent(parent)).setAddInstanceName(true);
                }
            }
        }
        if (value instanceof SyntaxTree.Variable && nameSpace.startsWith("#C") && ((SyntaxTree.Variable) value).getInstance() == null) {
            boolean hasVariableInClass = false;
            boolean publicVariableExists = false;
            boolean hasVariableInParentClass = false;
            String parent = null;
            if (!(((SyntaxTree.Variable) value).getInstance() instanceof SyntaxTree.Variable &&
                    ((SyntaxTree.Variable) ((SyntaxTree.Variable) value).getInstance()).getVariableName().equals("%"))) {
                for (String i : SyntaxTree.getVariables().keySet()) {
                    if (i.equals(nameSpace + ((SyntaxTree.Variable) value).getVariableName())) {
                        hasVariableInClass = true;
                    }
                    if (i.equals(((SyntaxTree.Variable) value).getVariableName())) {
                        publicVariableExists = true;
                    }
                    if (SyntaxTree.getClassesParents().containsKey(nameSpace.replace("#C", "").replace("#", ""))) {
                        for (String j : SyntaxTree.getClassesParents().get(nameSpace.replace("#C", "").replace("#", ""))) {
                            if (i.equals("#C" + j + "#" + ((SyntaxTree.Variable) value).getVariableName())) {
                                hasVariableInParentClass = true;
                                parent = j;
                                break;
                            }
                        }
                    }
                }
                hasVariableInClass &= !publicVariableExists;
                hasVariableInParentClass &= !publicVariableExists;
                hasVariableInParentClass &= !hasVariableInClass;
                if (hasVariableInParentClass) {
                    ((SyntaxTree.Variable) value).fromInstance(new SyntaxTree.Parent(parent)).setAddInstanceName(true);
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.Variable) value).getInstance(), declaredVariables);
                }
                if (publicVariableExists && !((SyntaxTree.Variable) value).getVariableName().equals("%") && !((SyntaxTree.Variable) value).getVariableName().contains("#F")) {
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.Variable) value).getInstance(), declaredVariables);
                    return;
                }
                if (((SyntaxTree.Variable) value).getVariableName().contains("#F") && nameSpace.startsWith("#C")) {
                    if (declaredVariables.contains(((SyntaxTree.Variable) value).getVariableName())) {
                        SyntaxTree.getVariables().remove(((SyntaxTree.Variable) value).getVariableName());
                        if (nameSpace.startsWith("#C")) ((SyntaxTree.Variable) value).setUseInstanceName(!((SyntaxTree.Variable) value).getVariableName().startsWith("#F"));
                        if (!(((SyntaxTree.Variable) value).getInstance() instanceof SyntaxTree.Parent)) ((SyntaxTree.Variable) value).setVariableName((nameSpace + sep + ((SyntaxTree.Variable) value).getVariableName()).replace("##F", "#"));
                    }
                    addNameSpacesOnValue(nameSpace, ((SyntaxTree.Variable) value).getInstance(), declaredVariables);
                }
            }
        }
        if (declaredVariables == null || declaredVariables.size() == 0) {
            return;
        }
        if (value instanceof SyntaxTree.Variable) {
//            if (nameSpace.startsWith("#C") && declaredVariables.()) {
//                System.out.println(((SyntaxTree.Variable) value).getVariableName());
//            }
            if (declaredVariables.contains(((SyntaxTree.Variable) value).getVariableName())) {
                if (nameSpace.startsWith("#C")) ((SyntaxTree.Variable) value).setUseInstanceName(!((SyntaxTree.Variable) value).getVariableName().startsWith("#F"));
                if (!(((SyntaxTree.Variable) value).getInstance() instanceof SyntaxTree.Parent)) ((SyntaxTree.Variable) value).setVariableName(nameSpace + sep + ((SyntaxTree.Variable) value).getVariableName());
            }
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Variable) value).getInstance(), declaredVariables);
        } else if (value instanceof SyntaxTree.Add) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Add) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Add) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Sub) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Sub) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Sub) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Mul) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Mul) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Mul) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Div) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Div) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Div) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Mod) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Mod) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Mod) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Pow) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Pow) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Pow) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.PrintFunction) {
            addNameSpaces(nameSpace, ((SyntaxTree.PrintFunction) value).getProgram(), declaredVariables);
        } else if (value instanceof SyntaxTree.ExitFunction) {
            addNameSpaces(nameSpace, ((SyntaxTree.ExitFunction) value).getProgram(), declaredVariables);
        } else if (value instanceof SyntaxTree.Lambda) {
            addNameSpaces(nameSpace, ((SyntaxTree.Lambda) value).getCreateLambda(), declaredVariables);
        } else if (value instanceof SyntaxTree.Equals) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Equals) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Equals) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.StrictEquals) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.StrictEquals) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.StrictEquals) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.GreaterThan) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.GreaterThan) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.GreaterThan) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.GreaterThanOrEqual) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.GreaterThanOrEqual) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.GreaterThanOrEqual) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.LesserThan) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LesserThan) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LesserThan) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.LesserThanOrEqual) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LesserThanOrEqual) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LesserThanOrEqual) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Or) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Or) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Or) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.BitwiseOr) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.BitwiseOr) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.BitwiseOr) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.And) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.And) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.And) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.BitwiseAnd) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.BitwiseAnd) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.BitwiseAnd) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.LeftShift) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LeftShift) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.LeftShift) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.RightShift) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.RightShift) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.RightShift) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Xor) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Xor) value).getV1(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Xor) value).getV2(), declaredVariables);
        } else if (value instanceof SyntaxTree.Negative) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Negative) value).getValue(), declaredVariables);
        } else if (value instanceof SyntaxTree.Not) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Not) value).getValue(), declaredVariables);
        } else if (value instanceof SyntaxTree.BitwiseNot) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.BitwiseNot) value).getValue(), declaredVariables);
        } else if (value instanceof SyntaxTree.Append) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Append) value).getValue(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Append) value).getList(), declaredVariables);
        } else if (value instanceof SyntaxTree.Set) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Set) value).getValue(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Set) value).getList(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Set) value).getIndex(), declaredVariables);
        } else if (value instanceof SyntaxTree.Get) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Get) value).getIndex(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Get) value).getList(), declaredVariables);
        } else if (value instanceof SyntaxTree.GetSize) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.GetSize) value).getList(), declaredVariables);
        } else if (value instanceof SyntaxTree.IndexOf) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.IndexOf) value).getList(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.IndexOf) value).getValue(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.IndexOf) value).getFromIndex(), declaredVariables);
        } else if (value instanceof SyntaxTree.Insert) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Insert) value).getIndex(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Insert) value).getList(), declaredVariables);
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Insert) value).getValue(), declaredVariables);
        } else if (value instanceof SyntaxTree.Sort) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Sort) value).getList(), declaredVariables);
        } else if (value instanceof SyntaxTree.CreateInstance) {
            for (ValueBase arg : ((SyntaxTree.CreateInstance) value).getArgs())
                addNameSpacesOnValue(nameSpace, arg, declaredVariables);
        } else if (value instanceof SyntaxTree.CallFunction) {
            ((SyntaxTree.CallFunction) value).findFunction();
            if (nameSpace.equals("#F" + ((SyntaxTree.CallFunction) value).getFunctionName())) {
                ((SyntaxTree.CallFunction) value).setRecursion(true);
            }
            for (ProgramBase setVariable : ((SyntaxTree.CallFunction) value).getVariableSetters()) {
                if (setVariable instanceof SyntaxTree.SetVariable) addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) setVariable).getVariableValue(), declaredVariables);
                else if (setVariable instanceof OpCode.PutToVM) addNameSpacesOnValue(nameSpace, ((OpCode.PutToVM) setVariable).getValue(), declaredVariables);
            }
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.CallFunction) value).getInstance(), declaredVariables);
        } else if (value instanceof SyntaxTree.CallFunctionFromPointer) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.CallFunctionFromPointer) value).getFunctionPointer(), declaredVariables);
            for (ValueBase value1 : ((SyntaxTree.CallFunctionFromPointer) value).getValues()) {
                addNameSpacesOnValue(nameSpace, value1, declaredVariables);
            }
        } else if (value instanceof SyntaxTree.Increase) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Increase) value).getVariable(), declaredVariables);
            addNameSpaces(nameSpace, ((SyntaxTree.Increase) value).getVariableSetter(), declaredVariables);
        } else if (value instanceof SyntaxTree.Decrease) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Decrease) value).getVariable(), declaredVariables);
            addNameSpaces(nameSpace, ((SyntaxTree.Decrease) value).getVariableSetter(), declaredVariables);
        } else if (value instanceof SyntaxTree.Parent) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Parent) value).getVariable(), declaredVariables);
        } else if (value instanceof CustomValue) {
            for (Object i : ((CustomValue) value).addNamespaceOn()) {
                if (i instanceof ProgramBase) {
                    addNameSpaces(nameSpace, (ProgramBase) i, declaredVariables);
                } else if (i instanceof ValueBase) {
                    addNameSpacesOnValue(nameSpace, (ValueBase) i, declaredVariables);
                }
            }
        }
    }

    public static void addCodeBeforeContinue(ProgramBase program) {
        NameSpaces.codeBeforeContinue = program;
    }
}