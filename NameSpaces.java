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
        boolean isFirst = !declarativeVariables && globals == null;
        if (isFirst) {
            globals = new ArrayList<>();
        }
        if (program instanceof SyntaxTree.SetVariable) {
            if (declarativeVariables) {
                if (((SyntaxTree.SetVariable) program).getIsDeclaration()) {
                    declaredVariables.add(((SyntaxTree.SetVariable) program).getVariableName());
                    if (nameSpace.startsWith("#C")) {
                        ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                        SyntaxTree.getClassesParameters().get(nameSpace.substring(2)).add((SyntaxTree.SetVariable) program);
                    }
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep +
                            ((SyntaxTree.SetVariable) program).getVariableName());
                } else {
                    if ((!(((SyntaxTree.SetVariable) program).getInstance() instanceof SyntaxTree.This) || nameSpace.startsWith("#C")) && declaredVariables.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                        if (nameSpace.startsWith("#C")) ((SyntaxTree.SetVariable) program).setUseInstanceName(!((SyntaxTree.SetVariable) program).getVariableName().startsWith("#F"));
                        ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + sep + ((SyntaxTree.SetVariable) program).getVariableName());
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
        } else if (!declarativeVariables && program instanceof SyntaxTree.Global) {
            globals.add(((SyntaxTree.Global)program).getVariableName());
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program2 : ((SyntaxTree.Programs)program).getPrograms()) {
                addNameSpaces(nameSpace, program2, declaredVariables);
            }
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
        } else if (program instanceof SyntaxTree.Function) {
            if (nameSpace.startsWith("#C")) {
                ArrayList<String> declaredVariables1 = (ArrayList<String>) declaredVariables.clone();
                for (String string : ((SyntaxTree.Function) program).getArgs()) {
                    declaredVariables1.add("#F" + ((SyntaxTree.Function) program).getFunctionName() + ":" + string);
                }
                ((SyntaxTree.Function) program).setFunctionName(nameSpace + ((SyntaxTree.Function) program).getFunctionName());
                addNameSpaces(nameSpace, ((SyntaxTree.Function) program).getProgram(), declaredVariables1);
                program.eval();
                if (((SyntaxTree.Function) program).getFunctionName().startsWith(nameSpace + "<init>")) {
                    SyntaxTree.getClassesWithInit().add(nameSpace.replace("#C", ""));
                }
            }
        }
        if (isFirst) {
            globals = null;
        }
        return new SyntaxTree.Programs(program);
    }

    public static void addNameSpacesOnValue(String nameSpace, ValueBase value, ArrayList<String> declaredVariables) {
        if (value instanceof SyntaxTree.CallFunction && nameSpace.startsWith("#C")) {
            boolean hasFunctionInClass = false;
            boolean publicFunctionExists = false;
            for (String i : SyntaxTree.getFunctions().keySet()) {
                if (i.startsWith(nameSpace + ((SyntaxTree.CallFunction) value).getFunctionName() + ":")) {
                    hasFunctionInClass = true;
                }
                if (i.startsWith(((SyntaxTree.CallFunction) value).getFunctionName() + ":")) {
                    publicFunctionExists = true;
                }
            }
            hasFunctionInClass &= !publicFunctionExists;
            if (((SyntaxTree.CallFunction) value).getInstance() instanceof SyntaxTree.This || hasFunctionInClass) {
                ((SyntaxTree.CallFunction) value).setFunctionName(nameSpace + sep + ((SyntaxTree.CallFunction) value).getFunctionName());
            }
        }
        if (declaredVariables == null || declaredVariables.size() == 0) {
            return;
        }
        if (value instanceof SyntaxTree.Variable) {
            if ((!(((SyntaxTree.Variable) value).getInstance() instanceof SyntaxTree.This) || nameSpace.startsWith("#C")) && declaredVariables.contains(((SyntaxTree.Variable) value).getVariableName())) {
                if (nameSpace.startsWith("#C")) ((SyntaxTree.Variable) value).setUseInstanceName(!((SyntaxTree.Variable) value).getVariableName().startsWith("#F"));
                ((SyntaxTree.Variable) value).setVariableName(nameSpace + sep + ((SyntaxTree.Variable) value).getVariableName());
            }
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
        } else if (value instanceof SyntaxTree.CallFunction) {
            ((SyntaxTree.CallFunction) value).findFunction();
            if (nameSpace.equals("#F" + ((SyntaxTree.CallFunction) value).getFunctionName())) {
                ((SyntaxTree.CallFunction) value).setRecursion(true);
            }
            for (ProgramBase setVariable : ((SyntaxTree.CallFunction) value).getVariableSetters()) {
                addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) setVariable).getVariableValue(), declaredVariables);
            }
        } else if (value instanceof SyntaxTree.CallFunctionFromPointer) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.CallFunctionFromPointer) value).getFunctionPointer(), declaredVariables);
            for (ValueBase value1 : ((SyntaxTree.CallFunctionFromPointer) value).getValues()) {
                addNameSpacesOnValue(nameSpace, value1, declaredVariables);
            }
        }
    }

    public static void addCodeBeforeContinue(ProgramBase program) {
        NameSpaces.codeBeforeContinue = program;
    }
}