import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;

public class XMLToSyntaxTree {
    private Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xml));
        return builder.parse(inputSource);
    }

    public ProgramBase xmlToProgram(String xml) throws Exception {
        ArrayList<ProgramBase> programs = new ArrayList<>();
        Node node = loadXMLFromString(xml).getFirstChild();
        if (!node.getNodeName().equals("program")) return new SyntaxTree.Programs();
        node = node.getFirstChild();
        while (node != null) {
            if (node.getNodeName().equals("print")) {
                ValueBase separator = null;
                ArrayList<ValueBase> values = new ArrayList<>();
                Node node1 = node.getChildNodes().item(1);
                if (node1.getNodeName().equals("separator")) {
                    separator = getValueFromNode(node1.getChildNodes().item(1));
                }
                node1 = node1.getNextSibling();
                while (node1 != null) {
                    if (node1.getNodeName().equals("data"))
                        values.add(getValueFromNode(node1.getChildNodes().item(1)));
                    node1 = node1.getNextSibling();
                }
                ValueBase[] valuesArray = new ValueBase[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    valuesArray[i] = values.get(i);
                }
                programs.add(new SyntaxTree.Print(valuesArray).setSeparator(separator));
            }
            node = node.getNextSibling();
        }
        ProgramBase[] programsArray = new ProgramBase[programs.size()];
        for (int i = 0; i < programs.size(); i++) {
            programsArray[i] = programs.get(i);
        }
        return new SyntaxTree.Programs(programsArray);
    }

    private ValueBase getValueFromNode(Node node) {
        if (node.getNodeName().equals("number")) {
            return new SyntaxTree.Number(new BigDecimal(node.getTextContent()));
        } else if (node.getNodeName().equals("text")) {
            return new SyntaxTree.Text(node.getAttributes().getNamedItem("data").getNodeValue());
        }
        return new SyntaxTree.Null();
    }
}