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
        } else if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                stringBuilder.append(syntaxTreeToXML(program1));
            }
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            stringBuilder.append(getTabs(0)).append(compressed? "<ev>":"<executeValue>").append(getTabs(1)).append(compressed? "<v>":"<value>")
                    .append(getValueAsXMLString(((SyntaxTree.ExecuteValue) program).getValue()))
                    .append(getTabs(-1)).append(compressed? "<v>":"</value>").append(getTabs(-1)).append(compressed? "</ev>":"</executeValue>");
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
        } else if (value instanceof SyntaxTree.PrintFunction) {
            String string = getTabs(1) + (compressed? "<pf>":"<printFunction>");
            tabCount++;
            string += syntaxTreeToXML(((SyntaxTree.PrintFunction) value).getProgram()) + getTabs(-1) + (compressed? "</pf>":"</printFunction>");
            return string;
        }
        tabCount--;
        return "";
    }
}