public class XMLGenerator {
    boolean isFirst = true;
    int tabCount = 1;

    String getTabs(int add) {
        tabCount += add;
        return "\n" + new SyntaxTree.Mul(new SyntaxTree.Text("\t"), new SyntaxTree.Number(tabCount)).toString();
    }

    String syntaxTreeToXML(ProgramBase program) {
        boolean isFirst1 = false;
        StringBuilder stringBuilder = new StringBuilder(isFirst? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<program>":"");
        if (isFirst) {
            isFirst = false;
            isFirst1 = true;
        }
        if (program instanceof SyntaxTree.Print) {
            stringBuilder.append(getTabs(0)).append("<print>").append(getTabs(1)).append("<separator>")
                    .append(getValueAsXMLString(((SyntaxTree.Print) program).getSeparator())).append(getTabs(-1)).append("</separator>");
            for (ValueBase value : ((SyntaxTree.Print) program).getArgs()) {
                stringBuilder.append(getTabs(0)).append("<data>").append(getValueAsXMLString(value)).append(getTabs(-1)).append("</data>");
            }
            stringBuilder.append(getTabs(-1)).append("</print>");
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                stringBuilder.append(syntaxTreeToXML(program1));
            }
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            stringBuilder.append(getTabs(0)).append("<executeValue>").append(getTabs(1)).append("<value>")
                    .append(getValueAsXMLString(((SyntaxTree.ExecuteValue) program).getValue()))
                    .append(getTabs(-1)).append("</value>").append(getTabs(-1)).append("</executeValue>");
        }
        if (isFirst1) {
            stringBuilder.append(getTabs(-1)).append("</program>");
        }
        return stringBuilder.toString();
    }

    private String getValueAsXMLString(ValueBase value) {
        if (value instanceof SyntaxTree.Number) {
            return getTabs(1) + "<number>" + value.getData() + "</number>";
        } else if (value instanceof SyntaxTree.Text) {
            return getTabs(1) + "<text data=\"" + value.getData() + "\"/>";
        } else if (value instanceof SyntaxTree.PrintFunction) {
            String string = getTabs(1) + "<printFunction>";
            tabCount++;
            string += syntaxTreeToXML(((SyntaxTree.PrintFunction) value).getProgram()) + getTabs(-1) + "</printFunction>";
            return string;
        }
        tabCount--;
        return "";
    }
}