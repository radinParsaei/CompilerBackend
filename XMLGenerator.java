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
        } else if (value instanceof SyntaxTree.Text) {
            return getTabs(1) + "<text data=\"".replace("text", compressed? "t":"text") + value.getData() + "\"/>";
        } else if (value instanceof SyntaxTree.Boolean) {
            return getTabs(1) + (compressed? "<b>":"<bool>") + value.getData() + (compressed? "</b>":"</bool>");
        } else if (value instanceof SyntaxTree.Null) {
            return getTabs(1) + (compressed? "<nl/>":"<null/>");
        } else if (value instanceof SyntaxTree.List) {
            StringBuilder stringBuilder = new StringBuilder(getTabs(1) + (compressed? "<l>":"<list>"));
            for (ValueBase valueBase : (ArrayList<ValueBase>) value.getData()) {
                stringBuilder.append(compressed? "<d>":"<data>").append(getValueAsXMLString(valueBase)).append(compressed? "</d>":"</data>");
            }
            return stringBuilder + (compressed? "</l>":"</list>");
        } else if (value instanceof SyntaxTree.Add) {
            return getTabs(1) + (compressed? "<a><d1>":"<add><data1>") + getValueAsXMLString(((SyntaxTree.Add) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Add) value).getV2()) + (compressed? "</d2></a>":"></data2></add>");
        } else if (value instanceof SyntaxTree.Sub) {
            return getTabs(1) + (compressed? "<s><d1>":"<sub><data1>") + getValueAsXMLString(((SyntaxTree.Sub) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Sub) value).getV2()) + (compressed? "</d2></s>":"></data2></sub>");
        } else if (value instanceof SyntaxTree.Mul) {
            return getTabs(1) + (compressed? "<m><d1>":"<mul><data1>") + getValueAsXMLString(((SyntaxTree.Mul) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Mul) value).getV2()) + (compressed? "</d2></m>":"></data2></mul>");
        } else if (value instanceof SyntaxTree.Div) {
            return getTabs(1) + (compressed? "<d><d1>":"<div><data1>") + getValueAsXMLString(((SyntaxTree.Div) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Div) value).getV2()) + (compressed? "</d2></d>":"></data2></div>");
        } else if (value instanceof SyntaxTree.Pow) {
            return getTabs(1) + (compressed? "<p1><d1>":"<pow><data1>") + getValueAsXMLString(((SyntaxTree.Pow) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Pow) value).getV2()) + (compressed? "</d2></p1>":"></data2></pow>");
        } else if (value instanceof SyntaxTree.Equals) {
            return getTabs(1) + (compressed? "<eq><d1>":"<equals><data1>") + getValueAsXMLString(((SyntaxTree.Equals) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.Equals) value).getV2()) + (compressed? "</d2></eq>":"></data2></equals>");
        } else if (value instanceof SyntaxTree.GreaterThan) {
            return getTabs(1) + (compressed? "<gt><d1>":"<greater-than><data1>") + getValueAsXMLString(((SyntaxTree.GreaterThan) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.GreaterThan) value).getV2()) + (compressed? "</d2></gt>":"></data2></greater-than>");
        } else if (value instanceof SyntaxTree.LesserThan) {
            return getTabs(1) + (compressed? "<lt><d1>":"<lesser-than><data1>") + getValueAsXMLString(((SyntaxTree.LesserThan) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.LesserThan) value).getV2()) + (compressed? "</d2></lt>":"></data2></lesser-than>");
        } else if (value instanceof SyntaxTree.GreaterThanOrEqual) {
            return getTabs(1) + (compressed? "<ge><d1>":"<greater-than-or-equal><data1>") + getValueAsXMLString(((SyntaxTree.GreaterThanOrEqual) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.GreaterThanOrEqual) value).getV2()) + (compressed? "</d2></ge>":"></data2></greater-than-or-equal>");
        } else if (value instanceof SyntaxTree.LesserThanOrEqual) {
            return getTabs(1) + (compressed? "<le><d1>":"<lesser-than-or-equal><data1>") + getValueAsXMLString(((SyntaxTree.LesserThanOrEqual) value).getV1()) + (compressed? "</d1><d2>":"</data1><data2>") + getValueAsXMLString(((SyntaxTree.LesserThanOrEqual) value).getV2()) + (compressed? "</d2></le>":"></data2></lesser-than-or-equal>");
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
        }
        tabCount--;
        return "";
    }
}