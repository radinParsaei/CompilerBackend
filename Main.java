import java.io.FileWriter;
import java.io.IOException;
public class Main {
  public static void main(String[] args) {
    ValueBase val = new SyntaxTree.Number(10.5);
    SyntaxTreeSerializer serializer = new SyntaxTreeSerializer();
    ValueBase[] vals = {val, new SyntaxTree.Text("Hello World")};
    ProgramBase program = new SyntaxTree.Programs(
          new SyntaxTree.SetVariable("a", new SyntaxTree.Number(10.5)),
          new SyntaxTree.SetVariable("b", new SyntaxTree.Text("Hello")),
          new SyntaxTree.Print(new SyntaxTree.Variable("b"), new SyntaxTree.Mul(new SyntaxTree.Number(5), new SyntaxTree.Add(new SyntaxTree.Variable("b"), new SyntaxTree.Number(10))), new SyntaxTree.Text(".")).setSeparator(new SyntaxTree.Text("\t"))
    );
    // program.eval();
    serializer.serialize("file.ser", program);
    serializer.deserialize("file.ser").eval();
    System.out.println();
    try {
      FileWriter writer = new FileWriter("a");
      VMTools vmTools = new VMTools();
      writer.write(vmTools.SyntaxTreeToVMByteCode(program));
      writer.close();
    } catch (IOException e) {
      System.out.println("ERROR");
      e.printStackTrace();
    }
  }
}
