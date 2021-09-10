import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    ValueBase val = new SyntaxTree.Number(10.5);
    SyntaxTreeSerializer serializer = new SyntaxTreeSerializer();
    ValueBase[] vals = {val, new SyntaxTree.Text("Hello World")};
    SyntaxTree.declareNativeFunction("test.vmso" , "test", 1);
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
            new SyntaxTree.CreateClass("Test", new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Hello"), true, true).setCheckDeclarationInRuntime(false),
                    new SyntaxTree.Function("setMsg", new SyntaxTree.SetVariable("msg", new SyntaxTree.Variable("arg")), "arg"),
                    new SyntaxTree.Function("printMsg", new SyntaxTree.Print(new SyntaxTree.Variable("msg"))),
                    new SyntaxTree.Function("<init>", new SyntaxTree.Print(new SyntaxTree.Text("Hello, World!!!"))),
                    new SyntaxTree.Function("createInstance", new SyntaxTree.Return(new SyntaxTree.CreateInstance("Test"))),
                    new SyntaxTree.Function("toString", new SyntaxTree.Return(new SyntaxTree.Text("Test.toString()")))
            ),
            new SyntaxTree.If(new SyntaxTree.Boolean(true), new SyntaxTree.Programs(
                    new SyntaxTree.SetVariable("a", new SyntaxTree.Null(), true, true),
                    new SyntaxTree.Print(new SyntaxTree.Variable("a"))
            )),
            new SyntaxTree.Print(new SyntaxTree.Variable("a")),
            new SyntaxTree.SetVariable("test", new SyntaxTree.CreateInstance("Test")),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("#CTest#printMsg").fromInstance(new SyntaxTree.Variable("test"))),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("#CTest#setMsg", new SyntaxTree.Text("Data From Class")).fromInstance(new SyntaxTree.Variable("test"))),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("printMsg").fromInstance(new SyntaxTree.Variable("test")).setAddInstanceName(true)),
            new OpCode.PutToVM(new SyntaxTree.Variable("a")),
            new OpCode(SyntaxTree.objectToValue(VM.PRINT)),
            new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Data Inserted to class")).fromInstance(new SyntaxTree.Variable("test")),
            new SyntaxTree.Print(new SyntaxTree.Variable("msg").fromInstance(new SyntaxTree.Variable("test")).setAddInstanceName(true)),
            new SyntaxTree.Print(new SyntaxTree.CallFunction("test", new SyntaxTree.Text("Data passed to function"))),
            new SyntaxTree.ExecuteValue(new SyntaxTree.CallFunction("printMsg").fromInstance(new SyntaxTree.CallFunction("createInstance").fromInstance(new SyntaxTree.Variable("test")).setAddInstanceName(true)).setAddInstanceName(true)),
            new SyntaxTree.SetVariable("instance", new SyntaxTree.CallFunction("createInstance").fromInstance(new SyntaxTree.Variable("test")).setAddInstanceName(true)),
            new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Data Inserted To Instance")).fromInstance(new SyntaxTree.Variable("instance")).setAddInstanceName(true),
            new SyntaxTree.Print(new SyntaxTree.Variable("msg").fromInstance(new SyntaxTree.Variable("instance")).setAddInstanceName(true)),
            new SyntaxTree.Print(new SyntaxTree.Text("\nlambda function test: ")),
            new SyntaxTree.SetVariable("lambdaFunc", new SyntaxTree.Lambda(new SyntaxTree.CreateLambda(new SyntaxTree.Return(new SyntaxTree.Variable("a")), "a"))),
            new SyntaxTree.Print(new SyntaxTree.CallFunctionFromPointer(new SyntaxTree.Variable("lambdaFunc"), new SyntaxTree.Text("Passed"))),
            new SyntaxTree.For(new SyntaxTree.Not(new SyntaxTree.Variable("i")), new SyntaxTree.SetVariable("i", new SyntaxTree.Boolean(true)), new SyntaxTree.SetVariable("i", new SyntaxTree.Boolean(false)), new SyntaxTree.Print(new SyntaxTree.Variable("i"))),
            new SyntaxTree.Repeat(new SyntaxTree.Number(4), new SyntaxTree.Programs(new SyntaxTree.Print(new SyntaxTree.Text(";")), new SyntaxTree.Continue(), new SyntaxTree.Print(new SyntaxTree.Text(".")))),
            new SyntaxTree.Print(new SyntaxTree.Variable("test")),
            new SyntaxTree.SetVariable("l", new SyntaxTree.List(new SyntaxTree.Number(20), new SyntaxTree.Number(10))),
            new SyntaxTree.Print(new SyntaxTree.Insert(new SyntaxTree.Variable("l"), new SyntaxTree.Null(), new SyntaxTree.Number(1))),
            new SyntaxTree.Print(new SyntaxTree.Set(new SyntaxTree.Variable("l"), new SyntaxTree.Boolean(true), new SyntaxTree.Number(1))),
            new SyntaxTree.Print(new SyntaxTree.Get(new SyntaxTree.Variable("l"), new SyntaxTree.Number(1)))
            );
//    program.eval();
    serializer.serialize("file.ser", program);
    serializer.deserialize("file.ser").eval();
//    new SyntaxTree.SetVariable("variable", new SyntaxTree.CreateInstance("Test")).eval();
//    System.out.println(new SyntaxTree.Variable("variable").getData().getClass().getName());
//    System.out.println(new SyntaxTree.CallFunction("createInstance").setAddInstanceName(true).fromInstance(new SyntaxTree.Variable("variable")).getData().getClass().getName());
//    try {
//      FileWriter writer = new FileWriter("a");
//      VMTools vmTools = new VMTools();
//      writer.write(vmTools.syntaxTreeToVMByteCode(program));
//      writer.close();
//    } catch (IOException e) {
//      System.out.println("ERROR");
//      e.printStackTrace();
//    }
//    System.out.println("\n");
    ProgramBase program_ = new SyntaxTree.Programs(new SyntaxTree.If(new SyntaxTree.Boolean(false), new SyntaxTree.Print(new SyntaxTree.Sub(new SyntaxTree.Add(new SyntaxTree.Number(10), new SyntaxTree.Number(20)), new SyntaxTree.Number(5)), new SyntaxTree.Equals(new SyntaxTree.Pow(new SyntaxTree.Number(2), new SyntaxTree.Number(2)), new SyntaxTree.Mul(new SyntaxTree.Number(2), new SyntaxTree.Number(2))))).addElse(new SyntaxTree.Print(new SyntaxTree.Text("HAHA"))), new SyntaxTree.ExecuteValue(new SyntaxTree.PrintFunction(new SyntaxTree.Print(new SyntaxTree.Null(), new SyntaxTree.List(new SyntaxTree.Number(10), new SyntaxTree.Text("Hello"))))), new SyntaxTree.Print(new SyntaxTree.Text("Hello")));
    String xml = new XMLGenerator().syntaxTreeToXML(program_);
    System.out.println(xml);
    System.out.println("\n");
    String xmlCompressed = new XMLGenerator(true).syntaxTreeToXML(program_);
    System.out.println(xmlCompressed);
    System.out.println("\n");
    try {
      new XMLToSyntaxTree().xmlToProgram(xml).eval();
      new XMLToSyntaxTree().xmlToProgram(xmlCompressed).eval();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("\n\n-----JVMTool Test-----\n\n");
    ProgramBase program2 = new SyntaxTree.Programs(
            new SyntaxTree.Print(new SyntaxTree.Boolean(true), new SyntaxTree.Number(10), new SyntaxTree.Null(), new SyntaxTree.Text("Hello\n")),
            new SyntaxTree.SetVariable("a", new SyntaxTree.Text("Variable Data")),
            new SyntaxTree.Print(new SyntaxTree.Variable("a")),
            new SyntaxTree.If(new SyntaxTree.Boolean(false), new SyntaxTree.Print(new SyntaxTree.Text("\ntext from if\n"))).addElse(new SyntaxTree.Print(new SyntaxTree.Text("\ntext from else\n"))),
            new SyntaxTree.If(new SyntaxTree.Boolean(true), new SyntaxTree.Print(new SyntaxTree.Text("text from if\n")))//,
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
