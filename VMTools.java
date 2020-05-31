import java.util.HashMap;
public class VMTools {
  private String putVals(ValueBase... vals) {
    StringBuilder output = new StringBuilder();
    for (ValueBase val : vals) {
      if (val instanceof SyntaxTree.Number) {
        output.append("PUT\tNUM").append(val.getData()).append("\n");
      } else if (val instanceof SyntaxTree.Text) {
        output.append("PUT\tTXT").append(val.getData()).append("\n");
      }
    }
    return output.toString();
  }

  public String SyntaxTreeToVMByteCode(ProgramBase program) {
    StringBuilder output = new StringBuilder();
    if (program instanceof SyntaxTree.Programs) {
      for (ProgramBase program2 : ((SyntaxTree.Programs)program).getPrograms()) {
        output.append(SyntaxTreeToVMByteCode(program2));
      }
    } else if (program instanceof SyntaxTree.Print) {
      ValueBase[] args = ((SyntaxTree.Print)program).getArgs();
      for (int i = 0; i < args.length; i++) {
        output.append(putVals(args[i]));
        output.append("PRINT\n");
        if (i < args.length - 1){
          output.append(putVals((((SyntaxTree.Print)program).getSeparator())));
          output.append("PRINT\n");
        }
      }
    }
    return output.toString();
  }
}
