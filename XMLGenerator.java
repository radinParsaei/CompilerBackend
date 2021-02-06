public class XMLGenerator {
    StringBuilder stringBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<program>\n");
    int tabCount = 1;

    String getTabs(int add) {
        tabCount += add;
        return new SyntaxTree.Mul(new SyntaxTree.Text("\t"), new SyntaxTree.Number(tabCount)).toString();
    }

    XMLGenerator syntaxTreeToXML(ProgramBase program) {
        if (program instanceof SyntaxTree.Print) {
            stringBuilder.append(getTabs(0)).append("<print>\n").append(getTabs(1)).append("<separator>\n")
                    .append(getValueAsXMLString(((SyntaxTree.Print) program).getSeparator())).append(getTabs(-1)).append("</separator>\n");
            for (ValueBase value : ((SyntaxTree.Print) program).getArgs()) {
                stringBuilder.append(getTabs(0)).append("<data>\n").append(getValueAsXMLString(value)).append(getTabs(-1)).append("</data>\n");
            }
            stringBuilder.append(getTabs(-1)).append("</print>\n");
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                syntaxTreeToXML(program1);
            }
        }
        return this;
    }

    String getResult() {
        return stringBuilder.append("</program>\n").toString();
    }

    private String getValueAsXMLString(ValueBase value) {
        if (value instanceof SyntaxTree.Number) {
            return getTabs(1) + "<number>" + value.getData() + "</number>\n";
        } else if (value instanceof SyntaxTree.Text) {
            return getTabs(1) + "<text data=\"" + value.getData() + "\"></text>\n";
        }
        tabCount--;
        return "";
    }
}