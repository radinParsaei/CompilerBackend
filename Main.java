import java.io.FileOutputStream;
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
            new SyntaxTree.Function("func2", new SyntaxTree.Programs(new SyntaxTree.Print(vals), new SyntaxTree.Return(new SyntaxTree.Add(new SyntaxTree.Variable("a"), new SyntaxTree.Variable("b")))), "a", "b"),
            new SyntaxTree.Repeat(new SyntaxTree.Number(10),
                    new SyntaxTree.Programs(
                            new SyntaxTree.SetVariable("c", new SyntaxTree.Add(new SyntaxTree.Variable("c"), new SyntaxTree.Number(1))),
                            new SyntaxTree.Print(new SyntaxTree.Add(new SyntaxTree.Variable("c"), new SyntaxTree.Text("\n")))
                    ))
    )),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("main")), new SyntaxTree.Print(new SyntaxTree.CallFunction("func2", new SyntaxTree.Variable("b"), new SyntaxTree.Variable("c"))),
            new OpCode(SyntaxTree.objectToValue(VM.PUT), SyntaxTree.objectToValue("\nText From VM\n")/*, SyntaxTree.objectToValue(VM.PRINT)*/),
            new SyntaxTree.Print(new OpCode.PopFromVM()),
            new SyntaxTree.CreateClass("Test", new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Hello"), true, true).setCheckDeclarationInRuntime(false), new SyntaxTree.Function("setMsg", new SyntaxTree.SetVariable("msg", new SyntaxTree.Variable("arg")), "arg"), new SyntaxTree.Function("printMsg", new SyntaxTree.Print(new SyntaxTree.Variable("msg")))),
            new SyntaxTree.If(new SyntaxTree.Boolean(true), new SyntaxTree.Programs(
                    new SyntaxTree.SetVariable("a", new SyntaxTree.Null(), true, true),
                    new SyntaxTree.Print(new SyntaxTree.Variable("a"))
            )),
            new SyntaxTree.Print(new SyntaxTree.Variable("a")),
            new SyntaxTree.SetVariable("test", new SyntaxTree.CreateInstance("Test")),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("#CTestprintMsg").fromInstance(new SyntaxTree.Variable("test"))),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("#CTestsetMsg", new SyntaxTree.Text("Data From Class")).fromInstance(new SyntaxTree.Variable("test"))),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("#CTestprintMsg").fromInstance(new SyntaxTree.Variable("test")))
    );
//    program.eval();
    serializer.serialize("file.ser", program);
    serializer.deserialize("file.ser").eval();
    try {
      FileWriter writer = new FileWriter("a");
      VMTools vmTools = new VMTools();
      writer.write(vmTools.syntaxTreeToVMByteCode(program));
      writer.close();
    } catch (IOException e) {
      System.out.println("ERROR");
      e.printStackTrace();
    }
    System.out.println("\n\n-----JVMTool Test-----\n\n");
    ProgramBase program2 = new SyntaxTree.Programs(
            new SyntaxTree.Print(new SyntaxTree.Boolean(true), new SyntaxTree.Number(10), new SyntaxTree.Null(), new SyntaxTree.Text("Hello\n")),
            new SyntaxTree.SetVariable("a", new SyntaxTree.Text("Variable Data")),
            new SyntaxTree.Print(new SyntaxTree.Variable("a")),
            new SyntaxTree.If(new SyntaxTree.Boolean(true), new SyntaxTree.Print(new SyntaxTree.Text("\ntext from if\n")))
//            new SyntaxTree.SetVariable("status", new SyntaxTree.Number(10)),
//            new SyntaxTree.Exit(new SyntaxTree.Variable("status"))
    );
    try {
      JVMTool jvmTool = new JVMTool();
      byte[] out = jvmTool.syntaxTreeToJVMClass(program2, "Test");
      FileOutputStream fileOutputStream = new FileOutputStream("Test.class");
      fileOutputStream.write(out);
      fileOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
