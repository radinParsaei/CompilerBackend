import java.util.ArrayList;

public class XMLGenerator {
    private boolean isFirst = true;
    private int tabCount = 1;
    private boolean compressed = false;

    public XMLGenerator(boolean compressed) {
        this.compressed = compressed;
    }

    public XMLGenerator() {}

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    String getTabs(int add) {
        tabCount += add;
        return compressed? "":("\n" + new SyntaxTree.Mul(new SyntaxTree.Text("\t"), new SyntaxTree.Number(tabCount)).toString());
    }

    String syntaxTreeToXML(ProgramBase program) {
        boolean isFirst1 = false;
        StringBuilder stringBuilder = new StringBuilder(isFirst? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + (compressed? "<pr>":"<program>"):"");
        if (isFirst) {
            isFirst = false;
            isFirst1 = true;
        }
        if (program instanceof SyntaxTree.Print) {
            stringBuilder.append(getTabs(0)).append(compressed? "<p>":"<print>").append(getTabs(1)).append(compressed? "<s>":"<separator>")
                    .append(getValueAsXMLString(((SyntaxTree.Print) program).getSeparator())).append(getTabs(-1)).append(compressed? "</s>":"</separator>");
            for (ValueBase value : ((SyntaxTree.Print) program).getArgs()) {
                stringBuilder.append(getTabs(0)).append(compressed? "<d>":"<data>").append(getValueAsXMLString(value)).append(getTabs(-1)).append(compressed? "</d>":"</data>");
            }
            stringBuilder.append(getTabs(-1)).append(compressed? "</p>":"</print>");
        } else if (program instanceof SyntaxTree.If) {
            stringBuilder.append(getTabs(0)).append(compressed? "<i>":"<if>").append(getTabs(1)).append(compressed? "<c>":"<condition>")
                    .append(getValueAsXMLString(((SyntaxTree.If) program).getCondition())).append(getTabs(-1)).append(compressed? "</c>":"</condition>")
                    .append(getTabs(0)).append(compressed? "<p>":"<program>");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.If) program).getProgram())).append(getTabs(-1)).append(compressed? "</p>":"</program>");
            if (((SyntaxTree.If) program).getElseProgram() != null) {
                stringBuilder.append(getTabs(0)).append(compressed? "<e>":"<else>");
                tabCount++;
                stringBuilder.append(syntaxTreeToXML(((SyntaxTree.If) program).getElseProgram())).append(getTabs(-1)).append(compressed? "</e>":"</else>");
            }
            stringBuilder.append(getTabs(-1)).append(compressed? "</i>":"</if>");
        } else if (program instanceof SyntaxTree.While) {
            stringBuilder.append(getTabs(0)).append(compressed? "<w>":"<while>").append(getTabs(1)).append(compressed? "<c>":"<condition>")
                    .append(getValueAsXMLString(((SyntaxTree.While) program).getCondition())).append(getTabs(-1)).append(compressed? "</c>":"</condition>")
                    .append(getTabs(0)).append(compressed? "<p>":"<program>");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.While) program).getProgram())).append(getTabs(-1)).append(compressed? "</p>":"</program>");
            stringBuilder.append(getTabs(-1)).append(compressed? "</w>":"</while>");
        } else if (program instanceof SyntaxTree.Break) {
            stringBuilder.append(getTabs(0)).append(compressed? "<br/>":"<break/>");
        } else if (program instanceof SyntaxTree.Continue) {
            stringBuilder.append(getTabs(0)).append(compressed? "<con/>":"<continue/>");
        } else if (program instanceof SyntaxTree.Repeat) {
            stringBuilder.append(getTabs(0)).append(compressed? "<r>":"<repeat>").append(getTabs(1))
                    .append(compressed? "<p>":"<program>");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.Repeat) program).getProgram()))
                    .append(getTabs(-1)).append(compressed? "</p>":"</program>").append(getTabs(0)).append(compressed? "<c>":"<count>")
                    .append(getValueAsXMLString(((SyntaxTree.Repeat) program).getCount())).append(getTabs(-1))
                    .append(compressed? "</c>":"</count>").append(getTabs(-1)).append(compressed? "</r>":"</repeat>");
        } else if (program instanceof SyntaxTree.Function) {
            stringBuilder.append(getTabs(0)).append(compressed? "<fun n=\"":"<function name=\"")
                    .append(((SyntaxTree.Function) program).getFunctionName().split(":")[0].split("#")[((SyntaxTree.Function) program).getFunctionName().split("#").length - 1].replace("<", "#")).append(compressed? "\" a=\"":"\" args=\"");
            if (((SyntaxTree.Function) program).getArgs().length > 0) {
                for (String i : ((SyntaxTree.Function) program).getArgs()) {
                    stringBuilder.append(i).append(",");
                }
                stringBuilder.setCharAt(stringBuilder.length() - 1, '"');
            } else {
                stringBuilder.append('"');
            }
            stringBuilder.append(">");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.Function) program).getProgram()))
                    .append(getTabs(-1)).append(compressed? "</fun>":"</function>");
        } else if (program instanceof SyntaxTree.For) {
            stringBuilder.append(getTabs(0)).append(compressed? "<f>":"<for>").append(getTabs(1))
                    .append(compressed? "<i>":"<init>");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.For) program).getInit()))
                    .append(getTabs(-1)).append(compressed? "</i>":"</init>").append(getTabs(0));
            tabCount++;
            stringBuilder.append(compressed? "<s>":"<step>").append(syntaxTreeToXML(((SyntaxTree.For) program).getStep()))
                    .append(getTabs(-1)).append(compressed? "</s>":"</step>").append(getTabs(0));
            tabCount++;
            stringBuilder.append(compressed? "<pr>":"<program>").append(syntaxTreeToXML(((SyntaxTree.For) program).getProgram()))
                    .append(getTabs(-1)).append(compressed? "</pr>":"</program>").append(getTabs(0))
                    .append(compressed? "<c>":"<condition>").append(getValueAsXMLString(((SyntaxTree.For) program).getCondition()))
                    .append(getTabs(-1)).append(compressed? "</c>":"</condition>");
            stringBuilder.append(getTabs(-1)).append(compressed? "</f>":"</for>");
        } else if (program instanceof SyntaxTree.SetVariable) {
            stringBuilder.append(getTabs(0)).append(compressed? "<set n=\"":"<set-variable name=\"")
                    .append(((SyntaxTree.SetVariable) program).getVariableName().split("#")[((SyntaxTree.SetVariable) program).getVariableName().split("#").length - 1]).append("\" ")
                    .append(compressed? "ain=\"":"addInstanceName=\"").append(((SyntaxTree.SetVariable) program).isAddInstanceName()).append("\" ").append(compressed? "d=\"":"declaration=\"").append(((SyntaxTree.SetVariable) program).getIsDeclaration()).append("\">")
                    .append(getTabs(1)).append(compressed? "<v>":"<value>")
                    .append(getValueAsXMLString(((SyntaxTree.SetVariable) program).getVariableValue()))
                    .append(getTabs(-1)).append(compressed? "</v>":"</value>").append(((SyntaxTree.SetVariable) program).getInstance() != null? getTabs(0) + "<instance>" + getValueAsXMLString(((SyntaxTree.SetVariable) program).getInstance()) + getTabs(-1) + "</instance>":"")
                    .append(getTabs(-1)).append(compressed? "</set>":"</set-variable>");
        } else if (program instanceof SyntaxTree.CreateClass) {
            stringBuilder.append(getTabs(0)).append(compressed? "<cl n=\"":"<class name=\"")
                    .append(((SyntaxTree.CreateClass) program).getClassName()).append("\">");
            tabCount++;
            stringBuilder.append(syntaxTreeToXML(((SyntaxTree.CreateClass) program).getPrograms()));
            stringBuilder.append(getTabs(-1)).append(compressed? "</cl>":"</class>");
        } else if (program instanceof SyntaxTree.Exit) {
            stringBuilder.append(getTabs(0)).append(compressed? "<e>":"<exit>")
                    .append(getValueAsXMLString(((SyntaxTree.Exit) program).getStatus()));
            stringBuilder.append(getTabs(-1)).append(compressed? "</e>":"</exit>");
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                stringBuilder.append(syntaxTreeToXML(program1));
            }
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            stringBuilder.append(getTabs(0)).append(compressed? "<ev>":"<executeValue>").append(getTabs(1)).append(compressed? "<v>":"<value>")
                    .append(getValueAsXMLString(((SyntaxTree.ExecuteValue) program).getValue()))
                    .append(getTabs(-1)).append(compressed? "</v>":"</value>").append(getTabs(-1)).append(compressed? "</ev>":"</executeValue>");
        }
        if (isFirst1) {
            stringBuilder.append(getTabs(-1)).append(compressed? "</pr>":"</program>");
        }
        return stringBuilder.toString();
    }

    private String getValueAsXMLString(ValueBase value) {
        if (value instanceof SyntaxTree.Number) {
            return getTabs(1) + (compressed? "<n>":"<number>") + value.getData() + (compressed? "</n>":"</number>");
        } else if (value instanceof SyntaxTree.Variable) {
            return getTabs(1) + (compressed? "<v n=\"":"<variable name=\"") + ((SyntaxTree.Variable) value).getVariableName().split("#")[((SyntaxTree.Variable) value).getVariableName().split("#").length - 1] + "\" " + (compressed? "ain=\"":"addInstanceName=\"") + ((SyntaxTree.Variable) value).isAddInstanceName() + "\">" + (((SyntaxTree.Variable) value).getInstance() != null? getTabs(1) + "<instance>" + getValueAsXMLString(((SyntaxTree.Variable) value).getInstance()) + getTabs(-1) + "</instance>" + getTabs(-1):"") + (compressed? "</v>":"</variable>");
        } else if (value instanceof SyntaxTree.Increase) {
            return getTabs(1) + (compressed? "<in p=\"":"<increase is-postfix=\"") + ((SyntaxTree.Increase) value).isPostfix() + "\">" + getValueAsXMLString(((SyntaxTree.Increase) value).getVariable()) + getTabs(-1) + (compressed? "</in>":"</increase>");
        } else if (value instanceof SyntaxTree.Decrease) {
            return getTabs(1) + (compressed? "<de p=\"":"<decrease is-postfix=\"") + ((SyntaxTree.Decrease) value).isPostfix() + "\">" + getValueAsXMLString(((SyntaxTree.Decrease) value).getVariable()) + getTabs(-1) + (compressed? "</de>":"</decrease>");
        } else if (value instanceof SyntaxTree.Text) {
            return getTabs(1) + "<text data=\"".replace("text", compressed? "t":"text") + value.getData() + "\"/>";
        } else if (value instanceof SyntaxTree.Boolean) {
            return getTabs(1) + (compressed? "<b>":"<bool>") + value.getData() + (compressed? "</b>":"</bool>");
        } else if (value instanceof SyntaxTree.Null) {
            return getTabs(1) + (compressed? "<nl/>":"<null/>");
        } else if (value instanceof SyntaxTree.List) {
            StringBuilder stringBuilder = new StringBuilder(getTabs(1) + (compressed? "<l>":"<list>"));
            tabCount++;
            for (ValueBase valueBase : (ArrayList<ValueBase>) value.getData()) {
                stringBuilder.append(compressed? "<d>":getTabs(0) + "<data>").append(getValueAsXMLString(valueBase)).append(compressed? "</d>":getTabs(-1) + "</data>");
            }
            return stringBuilder + (compressed? "</l>":getTabs(-1) + "</list>");
        } else if (value instanceof SyntaxTree.Add) {
            return getTabs(1) + (compressed? "<a><d1>":"<add>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Add) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Add) value).getV2()) + (compressed? "</d2></a>":getTabs(-1) + "</data2>" + getTabs(-1) +  "</add>");
        } else if (value instanceof SyntaxTree.Sub) {
            return getTabs(1) + (compressed? "<s><d1>":"<sub>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Sub) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Sub) value).getV2()) + (compressed? "</d2></s>":getTabs(-1) + "</data2>" + getTabs(-1) + "</sub>");
        } else if (value instanceof SyntaxTree.Mul) {
            return getTabs(1) + (compressed? "<m><d1>":"<mul>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Mul) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Mul) value).getV2()) + (compressed? "</d2></m>":getTabs(-1) + "</data2>" + getTabs(-1) + "</mul>");
        } else if (value instanceof SyntaxTree.Div) {
            return getTabs(1) + (compressed? "<d><d1>":"<div>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Div) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Div) value).getV2()) + (compressed? "</d2></d>":getTabs(-1) + "</data2>" + getTabs(-1) + "</div>");
        } else if (value instanceof SyntaxTree.Mod) {
            return getTabs(1) + (compressed? "<m1><d1>":"<mod>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Mod) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Mod) value).getV2()) + (compressed? "</d2></m1>":getTabs(-1) + "</data2>" + getTabs(-1) + "</mod>");
        } else if (value instanceof SyntaxTree.Pow) {
            return getTabs(1) + (compressed? "<p1><d1>":"<pow>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Pow) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Pow) value).getV2()) + (compressed? "</d2></p1>":getTabs(-1) + "</data2>" + getTabs(-1) + "</pow>");
        } else if (value instanceof SyntaxTree.Equals) {
            return getTabs(1) + (compressed? "<eq><d1>":"<equals>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Equals) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Equals) value).getV2()) + (compressed? "</d2></eq>":getTabs(-1) + "</data2>" + getTabs(-1) + "</equals>");
        } else if (value instanceof SyntaxTree.StrictEquals) {
            return getTabs(1) + (compressed? "<seq><d1>":"<strict-equals>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.StrictEquals) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.StrictEquals) value).getV2()) + (compressed? "</d2></seq>":getTabs(-1) + "</data2>" + getTabs(-1) + "</strict-equals>");
        } else if (value instanceof SyntaxTree.GreaterThan) {
            return getTabs(1) + (compressed? "<gt><d1>":"<greater-than>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.GreaterThan) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.GreaterThan) value).getV2()) + (compressed? "</d2></gt>":getTabs(-1) + "</data2>" + getTabs(-1) + "</greater-than>");
        } else if (value instanceof SyntaxTree.LesserThan) {
            return getTabs(1) + (compressed? "<lt><d1>":"<lesser-than>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.LesserThan) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.LesserThan) value).getV2()) + (compressed? "</d2></lt>":getTabs(-1) + "</data2>" + getTabs(-1) + "</lesser-than>");
        } else if (value instanceof SyntaxTree.GreaterThanOrEqual) {
            return getTabs(1) + (compressed? "<ge><d1>":"<greater-than-or-equal>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.GreaterThanOrEqual) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.GreaterThanOrEqual) value).getV2()) + (compressed? "</d2></ge>":getTabs(-1) + "</data2>" + getTabs(-1) + "</greater-than-or-equal>");
        } else if (value instanceof SyntaxTree.LesserThanOrEqual) {
            return getTabs(1) + (compressed? "<le><d1>":"<lesser-than-or-equal>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.LesserThanOrEqual) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.LesserThanOrEqual) value).getV2()) + (compressed? "</d2></le>":getTabs(-1) + "</data2>" + getTabs(-1) + "</lesser-than-or-equal>");
        } else if (value instanceof SyntaxTree.And) {
            return getTabs(1) + (compressed? "<a1><d1>":"<and>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.And) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.And) value).getV2()) + (compressed? "</d2></a1>":getTabs(-1) + "</data2>" + getTabs(-1) + "</and>");
        } else if (value instanceof SyntaxTree.Or) {
            return getTabs(1) + (compressed? "<o><d1>":"<or>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.Or) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.Or) value).getV2()) + (compressed? "</d2></o>":getTabs(-1) + "</data2>" + getTabs(-1) + "</or>");
        } else if (value instanceof SyntaxTree.BitwiseAnd) {
            return getTabs(1) + (compressed? "<ba><d1>":"<bitwise-and>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.BitwiseAnd) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.BitwiseAnd) value).getV2()) + (compressed? "</d2></ba>":getTabs(-1) + "</data2>" + getTabs(-1) + "</bitwise-and>");
        } else if (value instanceof SyntaxTree.BitwiseOr) {
            return getTabs(1) + (compressed? "<bo><d1>":"<bitwise-or>" + getTabs(1) + "<data1>") + getValueAsXMLString(((SyntaxTree.BitwiseOr) value).getV1()) + (compressed? "</d1><d2>":getTabs(-1) + "</data1>" + getTabs(0) + "<data2>") + getValueAsXMLString(((SyntaxTree.BitwiseOr) value).getV2()) + (compressed? "</d2></bo>":getTabs(-1) + "</data2>" + getTabs(-1) + "</bitwise-or>");
        } else if (value instanceof SyntaxTree.BitwiseNot) {
            return getTabs(1) + (compressed? "<bn>":"<bitwise-not>") + getValueAsXMLString(((SyntaxTree.BitwiseNot) value).getValue()) + (compressed? "</bn>":getTabs(-1) + "</bitwise-not>");
        } else if (value instanceof SyntaxTree.Not) {
            return getTabs(1) + (compressed? "<n1>":"<not>") + getValueAsXMLString(((SyntaxTree.Not) value).getValue()) + (compressed? "</n1>":getTabs(-1) + "</not>");
        } else if (value instanceof SyntaxTree.CallFunction) {
            StringBuilder builder = new StringBuilder();
            int c = -1;
            tabCount++;
            for (ValueBase i : ((SyntaxTree.CallFunction) value).getArgs()) {
                builder.append(getTabs(1)).append("<arg").append(++c).append(">").append(getValueAsXMLString(i)).append(getTabs(-1)).append("</arg").append(c).append(">");
                tabCount--;
            }
            tabCount--;
            if (((SyntaxTree.CallFunction) value).getArgs().length > 0) {
                builder.append(getTabs(1));
                tabCount--;
            }
            return getTabs(1) + (compressed? "<c n=\"":"<call-function name=\"") + ((SyntaxTree.CallFunction) value).getFunctionName() + "\" " + (compressed? "ain=\"":"addInstanceName=\"") + ((SyntaxTree.CallFunction) value).isAddInstanceName() + "\">" + builder + (((SyntaxTree.CallFunction) value).getInstance() != null? getTabs(1) + "<instance>" + getValueAsXMLString(((SyntaxTree.CallFunction) value).getInstance()) + getTabs(-1) + "</instance>" + getTabs(-1):"") + (compressed? "</c>":"</call-function>");
        } else if (value instanceof SyntaxTree.PrintFunction) {
            String string = getTabs(1) + (compressed? "<pf>":"<printFunction>");
            tabCount++;
            string += syntaxTreeToXML(((SyntaxTree.PrintFunction) value).getProgram()) + getTabs(-1) + (compressed? "</pf>":"</printFunction>");
            return string;
        } else if (value instanceof SyntaxTree.ExitFunction) {
            String string = getTabs(1) + (compressed? "<ef>":"<exitFunction>");
            tabCount++;
            string += syntaxTreeToXML(((SyntaxTree.ExitFunction) value).getProgram()) + getTabs(-1) + (compressed? "</ef>":"</exitFunction>");
            return string;
        } else if (value instanceof SyntaxTree.CreateInstance) {
            StringBuilder builder = new StringBuilder();
            int c = -1;
            tabCount++;
            for (ValueBase i : ((SyntaxTree.CreateInstance) value).getArgs()) {
                builder.append(getTabs(1)).append("<arg").append(++c).append(">").append(getValueAsXMLString(i)).append(getTabs(-1)).append("</arg").append(c).append(">");
                tabCount--;
            }
            tabCount--;
            if (((SyntaxTree.CreateInstance) value).getArgs().length > 0) {
                builder.append(getTabs(1));
                tabCount--;
            }
            return getTabs(1) + (compressed? "<ci n=\"":"<createInstance name=\"") + ((SyntaxTree.CreateInstance) value).getClassName() + "\">" + builder + (compressed? "</ci>":"</createInstance>");
        }
        tabCount--;
        return "";
    }
}