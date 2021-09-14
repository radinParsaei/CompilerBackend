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
        Node node = loadXMLFromString(xml).getFirstChild();
        if (!node.getNodeName().equals("program") && !node.getNodeName().equals("pr")) return new SyntaxTree.Programs();
        node = node.getFirstChild();
        return xmlToProgram(node);
    }

    public ProgramBase xmlToProgram(Node node) {
        ArrayList<ProgramBase> programs = new ArrayList<>();
        while (node != null) {
            if (node.getNodeName().equals("print") || node.getNodeName().equals("p")) {
                ValueBase separator = null;
                ArrayList<ValueBase> values = new ArrayList<>();
                Node node1;
                if (node.getNodeName().equals("p")) node1 = node.getChildNodes().item(0);
                else node1 = node.getChildNodes().item(1);
                while (node1 != null) {
                    if (node1.getNodeName().equals("separator"))
                        separator = getValueFromNode(node1.getChildNodes().item(1));
                    if (node1.getNodeName().equals("data"))
                        values.add(getValueFromNode(node1.getChildNodes().item(1)));
                    if (node1.getNodeName().equals("s"))
                        separator = getValueFromNode(node1.getChildNodes().item(0));
                    if (node1.getNodeName().equals("d"))
                        values.add(getValueFromNode(node1.getChildNodes().item(0)));
                    node1 = node1.getNextSibling();
                }
                ValueBase[] valuesArray = new ValueBase[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    valuesArray[i] = values.get(i);
                }
                programs.add(new SyntaxTree.Print(valuesArray).setSeparator(separator));
            } else if (node.getNodeName().equals("exit") || node.getNodeName().equals("e")) {
                Node node1;
                if (node.getNodeName().equals("p")) node1 = node.getChildNodes().item(0);
                else node1 = node.getChildNodes().item(1);
                programs.add(new SyntaxTree.Exit(getValueFromNode(node1)));
            } else if (node.getNodeName().equals("if") || node.getNodeName().equals("i")) {
                ValueBase condition;
                ProgramBase program;
                ProgramBase elseProgram = null;
                if (node.getNodeName().equals("if")) {
                    condition = getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1));
                    program = xmlToProgram(node.getChildNodes().item(3).getChildNodes().item(1));
                    try {
                        elseProgram = xmlToProgram(node.getChildNodes().item(5).getChildNodes().item(1));
                    } catch (Exception ignore) {}
                } else {
                    condition = getValueFromNode(node.getFirstChild().getFirstChild());
                    program = xmlToProgram(node.getChildNodes().item(1).getFirstChild());
                    try {
                        elseProgram = xmlToProgram(node.getChildNodes().item(2).getFirstChild());
                    } catch (Exception ignore) {}
                }
                SyntaxTree.If _if = new SyntaxTree.If(condition, program);
                if (elseProgram != null) _if.addElse(elseProgram);
                programs.add(_if);
            } else if (node.getNodeName().equals("while") || node.getNodeName().equals("w")) {
                ValueBase condition;
                ProgramBase program;
                if (node.getNodeName().equals("while")) {
                    condition = getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1));
                    program = xmlToProgram(node.getChildNodes().item(3).getChildNodes().item(1));
                } else {
                    condition = getValueFromNode(node.getFirstChild().getFirstChild());
                    program = xmlToProgram(node.getChildNodes().item(1).getFirstChild());
                }
                programs.add(new SyntaxTree.While(condition, program));
            } else if (node.getNodeName().equals("break") || node.getNodeName().equals("br")) {
                programs.add(new SyntaxTree.Break());
            } else if (node.getNodeName().equals("executeValue") || node.getNodeName().equals("ev")) {
                ValueBase value = null;
                Node node1 = node.getNodeName().equals("ev")? node.getChildNodes().item(0):node.getChildNodes().item(1);
                if (node1.getNodeName().equals("value") || node1.getNodeName().equals("v")) {
                    value = node1.getNodeName().equals("v")? getValueFromNode(node1.getChildNodes().item(0)):getValueFromNode(node1.getChildNodes().item(1));
                }
                programs.add(new SyntaxTree.ExecuteValue(value));
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
        switch (node.getNodeName()) {
            case "number":
            case "n":
                return new SyntaxTree.Number(new BigDecimal(node.getTextContent()));
            case "bool":
            case "b":
                return new SyntaxTree.Boolean(node.getTextContent().toLowerCase().equals("true"));
            case "null":
            case "nl":
                return new SyntaxTree.Null();
            case "list":
            case "l": {
                ArrayList<ValueBase> res = new ArrayList<>();
                Node node1 = node.getFirstChild();
                while (node1 != null) {
                    if (node1.getNodeName().equals("data"))
                        res.add(getValueFromNode(node1.getChildNodes().item(1)));
                    if (node1.getNodeName().equals("d"))
                        res.add(getValueFromNode(node1.getChildNodes().item(0)));
                    node1 = node1.getNextSibling();
                }
                ValueBase[] valuesArray = new ValueBase[res.size()];
                for (int i = 0; i < res.size(); i++) {
                    valuesArray[i] = res.get(i);
                }
                return new SyntaxTree.List(valuesArray);
            }
            case "text":
            case "t":
                return new SyntaxTree.Text(node.getAttributes().getNamedItem("data").getNodeValue());
            case "printFunction":
            case "pf":
                return new SyntaxTree.PrintFunction((SyntaxTree.Print) ((SyntaxTree.Programs) xmlToProgram(node.getFirstChild())).getPrograms()[0]);
            case "add":
                return new SyntaxTree.Add(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "a":
                return new SyntaxTree.Add(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "sub":
                return new SyntaxTree.Sub(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "s":
                return new SyntaxTree.Sub(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "mul":
                return new SyntaxTree.Mul(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "m":
                return new SyntaxTree.Mul(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "div":
                return new SyntaxTree.Div(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "d":
                return new SyntaxTree.Div(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "mod":
                return new SyntaxTree.Mod(getValueFromNode(node.getFirstChild().getLastChild()), getValueFromNode(node.getLastChild().getChildNodes().item(1)));
            case "m1":
                return new SyntaxTree.Mod(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "pow":
                return new SyntaxTree.Pow(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "p1":
                return new SyntaxTree.Pow(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "equals":
                return new SyntaxTree.Equals(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "eq":
                return new SyntaxTree.Equals(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "strict-equals":
                return new SyntaxTree.StrictEquals(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "seq":
                return new SyntaxTree.StrictEquals(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "greater-than":
                return new SyntaxTree.GreaterThan(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "gt":
                return new SyntaxTree.GreaterThan(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "lesser-than":
                return new SyntaxTree.LesserThan(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "lt":
                return new SyntaxTree.LesserThan(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "lesser-than-or-equal":
                return new SyntaxTree.LesserThanOrEqual(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "le":
                return new SyntaxTree.LesserThanOrEqual(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "greater-than-or-equal":
                return new SyntaxTree.GreaterThanOrEqual(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "ge":
                return new SyntaxTree.GreaterThanOrEqual(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "and":
                return new SyntaxTree.And(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "a1":
                return new SyntaxTree.And(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "or":
                return new SyntaxTree.Or(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "o":
                return new SyntaxTree.Or(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "bitwise-and":
                return new SyntaxTree.BitwiseAnd(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "ba":
                return new SyntaxTree.BitwiseAnd(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "bitwise-or":
                return new SyntaxTree.BitwiseOr(getValueFromNode(node.getChildNodes().item(1).getChildNodes().item(1)), getValueFromNode(node.getChildNodes().item(3).getChildNodes().item(1)));
            case "bo":
                return new SyntaxTree.BitwiseOr(getValueFromNode(node.getFirstChild().getFirstChild()), getValueFromNode(node.getLastChild().getFirstChild()));
            case "bitwise-not":
                return new SyntaxTree.BitwiseNot(getValueFromNode(node.getChildNodes().item(1)));
            case "bn":
                return new SyntaxTree.BitwiseNot(getValueFromNode(node.getFirstChild()));
            case "not":
                return new SyntaxTree.Not(getValueFromNode(node.getChildNodes().item(1)));
            case "n1":
                return new SyntaxTree.Not(getValueFromNode(node.getFirstChild()));
            case "exitFunction":
            case "ef":
                return new SyntaxTree.ExitFunction((SyntaxTree.Exit) ((SyntaxTree.Programs) xmlToProgram(node.getFirstChild())).getPrograms()[0]);
        }
        return new SyntaxTree.Null();
    }
}