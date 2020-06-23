import java.util.HashMap;
public class VMTools {
  private HashMap<String, Integer> variables = new HashMap<>();
  private int variablesCounter = 0;
  private String putVals(ValueBase... vals) {
    StringBuilder output = new StringBuilder();
    for (ValueBase val : vals) {
      if (val instanceof SyntaxTree.Number) {
        output.append("PUT\tNUM").append(val.getData()).append("\n");
      } else if (val instanceof SyntaxTree.Text) {
        output.append("PUT\tTXT").append(val.getData()).append("\n");
      } else if (val instanceof SyntaxTree.Variable) {
        output.append("REC\nPUT\tNUM0\nMEMGET\nPUT\tNUM-1\nEQ\nEND\nPUT\tNUM0\nMEMGET\nPUT\tNUM-1\nEQ\nWFRUN\nPUT NUM1\nPUT\tNUM0\nMEMSET\n");
        output.append("REC\nPUT\tNUM0\nMEMGET\nPUT\tNUM1\nADD\nPUT\tNUM0\nMEMSET\nBREAK\nEND\nREC\nPUT\tNUM0\nMEMGET\nMEMGET\n");
        Integer a = variables.get(((SyntaxTree.Variable)val).getVariableName());
        if (a == null) {
           Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, ((SyntaxTree.Variable)val).getVariableName());
        }
        output.append("PUT\tNUM").append(a);
        output.append("\nEQ\nIFTRUN\nPOP\nPUT\tNUM0\nMEMGET\nPUT\tNUM2\nADD\nPUT\tNUM0\nMEMSET\nEND\nPUT\tNUM2\nMEMSIZE\nDIV\nREPEAT\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPUT\tNUM0\nMEMGET\nMEMGET\nPUT\tNUM-1\nPUT\tNUM0\nMEMSET\n");
      } else if (val instanceof SyntaxTree.Add) {
        output.append(putVals(((SyntaxTree.Add)val).getV2()));
        output.append(putVals(((SyntaxTree.Add)val).getV1()));
        output.append("ADD\n");
      } else if (val instanceof SyntaxTree.Sub) {
        output.append(putVals(((SyntaxTree.Sub)val).getV2()));
        output.append(putVals(((SyntaxTree.Sub)val).getV1()));
        output.append("SUB\n");
      } else if (val instanceof SyntaxTree.Mul) {
        output.append(putVals(((SyntaxTree.Mul)val).getV2()));
        output.append(putVals(((SyntaxTree.Mul)val).getV1()));
        output.append("MUL\n");
      } else if (val instanceof SyntaxTree.Div) {
        output.append(putVals(((SyntaxTree.Div)val).getV2()));
        output.append(putVals(((SyntaxTree.Div)val).getV1()));
        output.append("DIV\n");
      }
    }
    return output.toString();
  }

  public String SyntaxTreeToVMByteCode(ProgramBase program) {
    return new StringBuilder("PUT\tNUM-1\nMEMPUT\n").append(SyntaxTreeToVMByteCode2(program)).toString();
  }

  private String SyntaxTreeToVMByteCode2(ProgramBase program) {
    StringBuilder output = new StringBuilder();
    if (program instanceof SyntaxTree.Programs) {
      for (ProgramBase program2 : ((SyntaxTree.Programs)program).getPrograms()) {
        output.append(SyntaxTreeToVMByteCode2(program2));
      }
    } else if (program instanceof SyntaxTree.Print) {
      ValueBase[] args = ((SyntaxTree.Print)program).getArgs();
      for (int i = 0; i < args.length; i++) {
        output.append(putVals(args[i]));
        output.append("PRINT\n");
        if (i < args.length - 1) {
          output.append(putVals((((SyntaxTree.Print)program).getSeparator())));
          output.append("PRINT\n");
        }
      }
    } else if (program instanceof SyntaxTree.SetVariable) {
      if (variables.get(((SyntaxTree.SetVariable)program).getVariableName()) == null) {
        variablesCounter++;
        variables.put(((SyntaxTree.SetVariable)program).getVariableName(), variablesCounter);
        output.append("PUT\tNUM").append(variablesCounter).append("\nMEMPUT\n");
        output.append(putVals(((SyntaxTree.SetVariable)program).getVariableValue())).append("MEMPUT\n");
      } else {
        output.append(putVals(((SyntaxTree.SetVariable)program).getVariableValue()));
        output.append("REC\nPUT\tNUM0\nMEMGET\nPUT\tNUM-1\nEQ\nEND\nPUT\tNUM0\nMEMGET\nPUT\tNUM-1\nEQ\nWFRUN\nPUT NUM1\nPUT\tNUM0\nMEMSET\n");
        output.append("REC\nPUT\tNUM0\nMEMGET\nPUT\tNUM1\nADD\nPUT\tNUM0\nMEMSET\nBREAK\nEND\nREC\nPUT\tNUM0\nMEMGET\nMEMGET\n");
        output.append("PUT\tNUM").append(variables.get(((SyntaxTree.SetVariable)program).getVariableName()));
        output.append("\nEQ\nIFTRUN\nPOP\nPUT\tNUM0\nMEMGET\nPUT\tNUM2\nADD\nPUT\tNUM0\nMEMSET\nEND\nPUT\tNUM2\nMEMSIZE\nDIV\nREPEAT\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPOP\nPUT\tNUM0\nMEMGET\nPUT\tNUM-1\nPUT\tNUM0\nMEMSET\nMEMSET\n");
      }
    }
    return output.toString();
  }
}
