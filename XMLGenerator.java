public class XMLGenerator {
    StringBuilder stringBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    int tabCount = 0;

    String getTabs(int add) {
        tabCount += add;
        return new SyntaxTree.Mul(new SyntaxTree.Text("\t"), new SyntaxTree.Number(tabCount)).toString();
    }

    String syntaxTreeToXML(ProgramBase program) {
        if (program instanceof SyntaxTree.Print) {
            stringBuilder.append(getTabs(0)).append("<print>\n").append(getTabs(1)).append("<separator>\n")
                    .append(getValueAsXMLString(((SyntaxTree.Print) program).getSeparator())).append(getTabs(-1)).append("</separator>\n");
            for (ValueBase value : ((SyntaxTree.Print) program).getArgs()) {
                stringBuilder.append(getTabs(0)).append("<data>\n").append(getValueAsXMLString(value)).append(getTabs(-1)).append("</data>\n");
            }
            stringBuilder.append(getTabs(-1)).append("</print>");
        }
        return stringBuilder.toString();
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