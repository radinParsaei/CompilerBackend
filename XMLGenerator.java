public class XMLGenerator {
    boolean isFirst = true;
    int tabCount = 1;

    String getTabs(int add) {
        tabCount += add;
        return new SyntaxTree.Mul(new SyntaxTree.Text("\t"), new SyntaxTree.Number(tabCount)).toString();
    }

    String syntaxTreeToXML(ProgramBase program) {
        boolean isFirst1 = false;
        StringBuilder stringBuilder = new StringBuilder(isFirst? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<program>\n":"");
        if (isFirst) {
            isFirst = false;
            isFirst1 = true;
        }
        if (program instanceof SyntaxTree.Print) {
            stringBuilder.append(getTabs(0)).append("<print>\n").append(getTabs(1)).append("<separator>\n")
                    .append(getValueAsXMLString(((SyntaxTree.Print) program).getSeparator())).append(getTabs(-1)).append("</separator>\n");
            for (ValueBase value : ((SyntaxTree.Print) program).getArgs()) {
                stringBuilder.append(getTabs(0)).append("<data>\n").append(getValueAsXMLString(value)).append(getTabs(-1)).append("</data>\n");
            }
            stringBuilder.append(getTabs(-1)).append("</print>\n");
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                stringBuilder.append(syntaxTreeToXML(program1));
            }
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            stringBuilder.append(getTabs(0)).append("<executeValue>\n").append(getTabs(1)).append("<value>\n")
                    .append(getValueAsXMLString(((SyntaxTree.ExecuteValue) program).getValue()))
                    .append(getTabs(-1)).append("</value>\n").append(getTabs(-1)).append("</executeValue>\n");
        }
        if (isFirst1) {
            stringBuilder.append(getTabs(-1)).append("</program>");
        }
        return stringBuilder.toString();
    }

    private String getValueAsXMLString(ValueBase value) {
        if (value instanceof SyntaxTree.Number) {
            return getTabs(1) + "<number>" + value.getData() + "</number>\n";
        } else if (value instanceof SyntaxTree.Text) {
            return getTabs(1) + "<text data=\"" + value.getData() + "\"/>\n";
        } else if (value instanceof SyntaxTree.PrintFunction) {
            String string = getTabs(1) + "<printFunction>\n";
            tabCount++;
            string += syntaxTreeToXML(((SyntaxTree.PrintFunction) value).getProgram()) + getTabs(-1) + "</printFunction>\n";
            return string;
        }
        tabCount--;
        return "";
    }
}