import java.util.ArrayList;

public class NameSpaces {
    private static boolean declarativeVariables = true;
    private static ArrayList<String> globals = null;

    public static void setDeclarativeVariables(boolean declarativeVariables) {
        NameSpaces.declarativeVariables = declarativeVariables;
    }

    public static SyntaxTree.Programs addNameSpaces(String nameSpace, ProgramBase program, ArrayList<String> declaredVariables) {
        if (declaredVariables == null) {
            declaredVariables = new ArrayList<>();
        }
        boolean isFirst = !declarativeVariables && globals == null;
        if (isFirst) {
            globals = new ArrayList<>();
        }
        if (program instanceof SyntaxTree.SetVariable) {
            if (declarativeVariables) {
                if (((SyntaxTree.SetVariable) program).getIsDeclaration()) {
                    declaredVariables.add(((SyntaxTree.SetVariable) program).getVariableName());
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + ":" +
                            ((SyntaxTree.SetVariable) program).getVariableName());
                } else {
                    if (declaredVariables.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                        ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + ":" + ((SyntaxTree.SetVariable) program).getVariableName());
                    }
                }
            } else {
                if (declaredVariables.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + ":" + ((SyntaxTree.SetVariable) program).getVariableName());
                } else if (!globals.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                    declaredVariables.add(((SyntaxTree.SetVariable) program).getVariableName());
                    ((SyntaxTree.SetVariable) program).setVariableName(nameSpace + ":" +
                            ((SyntaxTree.SetVariable) program).getVariableName());
                }
            }
            if (((SyntaxTree.SetVariable) program).getVariableValue() instanceof SyntaxTree.Variable) {
                SyntaxTree.Variable tmp = (SyntaxTree.Variable) ((SyntaxTree.SetVariable) program).getVariableValue();
                if (declaredVariables.contains(tmp.getVariableName())) {
                    tmp.setVariableName(nameSpace + ":" + tmp.getVariableName());
                }
            } else {
                addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable) program).getVariableValue(), declaredVariables);
            }
        } else if (!declarativeVariables && program instanceof SyntaxTree.Global) {
            globals.add(((SyntaxTree.Global)program).getVariableName());
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program2 : ((SyntaxTree.Programs)program).getPrograms()) {
                addNameSpaces(nameSpace, program2, declaredVariables);
            }
        } else if (program instanceof SyntaxTree.If) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.If)program).getCondition(), declaredVariables);
        } else if (program instanceof SyntaxTree.While) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.While)program).getCondition(), declaredVariables);
        } else if (program instanceof SyntaxTree.Repeat) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Repeat)program).getCount(), declaredVariables);
        } else if (program instanceof SyntaxTree.Exit) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Exit)program).getStatus(), declaredVariables);
        } else if (program instanceof SyntaxTree.Return) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Return)program).getValue(), declaredVariables);
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.ExecuteValue)program).getValue(), declaredVariables);
        } else if (program instanceof SyntaxTree.Print) {
            for (ValueBase value : ((SyntaxTree.Print)program).getArgs()) {
                addNameSpacesOnValue(nameSpace, value, declaredVariables);
            }
            addNameSpacesOnValue(nameSpace, ((SyntaxTree.Print)program).getSeparator(), declaredVariables);
        }
        if (isFirst) {
            globals = null;
        }
        return new SyntaxTree.Programs(program);
    }

    public static void addNameSpacesOnValue(String nameSpace, ValueBase value, ArrayList<String> declaredVariables) {
        if (declaredVariables == null || declaredVariables.size() == 0) {
            return;
        }
        if (value instanceof SyntaxTree.Variable) {
            if (declaredVariables.contains(((SyntaxTree.Variable) value).getVariableName())) {
                ((SyntaxTree.Variable) value).setVariableName(nameSpace + ":" + ((SyntaxTree.Variable) value).getVariableName());
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
            if (nameSpace.equals("F" + ((SyntaxTree.CallFunction) value).getFunctionName())) {
                ((SyntaxTree.CallFunction) value).setRecursion(true);
            }
            for (ProgramBase setVariable : ((SyntaxTree.CallFunction) value).getVariableSetters()) {
                addNameSpacesOnValue(nameSpace, ((SyntaxTree.SetVariable)setVariable).getVariableValue(), declaredVariables);
            }
        }
    }
}