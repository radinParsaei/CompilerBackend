import java.io.FileWriter;
import java.io.IOException;
public class Main {
  public static void main(String[] args) {
    ValueBase val = new SyntaxTree.Number(10.5);
    SyntaxTreeSerializer serializer = new SyntaxTreeSerializer();
    ValueBase[] vals = {val, new SyntaxTree.Text("Hello World")};
    ProgramBase program = new SyntaxTree.Programs(new SyntaxTree.Function("main", new SyntaxTree.Programs(
        new SyntaxTree.SetVariable("a", new SyntaxTree.Number(10.5)),
        new SyntaxTree.SetVariable("b", new SyntaxTree.Text("Hello")),
        new SyntaxTree.Print(new SyntaxTree.Variable("b"), new SyntaxTree.Mul(new SyntaxTree.Number(5), new SyntaxTree.Add(new SyntaxTree.Variable("b"), new SyntaxTree.Number(10))), new SyntaxTree.Text(".")).setSeparator(new SyntaxTree.Text("\t")),
        new SyntaxTree.SetVariable("c", new SyntaxTree.Number(0)),
        new SyntaxTree.Print(new SyntaxTree.Text("Hello\n")),
        new SyntaxTree.Function("func2", new SyntaxTree.Programs(new SyntaxTree.Print(vals), new SyntaxTree.Return(new SyntaxTree.Number(10)))),
        new SyntaxTree.Repeat(new SyntaxTree.Number(10),
            new SyntaxTree.Programs(
                new SyntaxTree.SetVariable("c", new SyntaxTree.Add(new SyntaxTree.Variable("c"), new SyntaxTree.Number(1))),
                new SyntaxTree.Print(new SyntaxTree.Add(new SyntaxTree.Variable("c"), new SyntaxTree.Text("\n")))
            ))
    )),
    new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("main")), new SyntaxTree.Print(new SyntaxTree.CallFunction("func2")),
    new OpCode(SyntaxTree.objectToValue(VM.PUT), SyntaxTree.objectToValue("\nText From VM\n")/*, SyntaxTree.objectToValue(VM.PRINT)*/),
    new SyntaxTree.Print(new OpCode.PopFromVM()),
            new SyntaxTree.If(new SyntaxTree.Boolean(true), new SyntaxTree.Programs(
                    new SyntaxTree.SetVariable("a", new SyntaxTree.Null(), true),
                    new SyntaxTree.Print(new SyntaxTree.Variable("a"))
            )),
            new SyntaxTree.Print(new SyntaxTree.Variable("a"))
    );
//     program.eval();
    serializer.serialize("file.ser", program);
    serializer.deserialize("file.ser").eval();
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
