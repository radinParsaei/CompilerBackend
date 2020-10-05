import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Map;

public class VMTools {
  private static boolean variablesHasDeclaration = false;
  public static void setVariablesHasDeclaration(boolean variablesHasDeclaration1) {
    variablesHasDeclaration = variablesHasDeclaration1;
  }
  private final HashMap<String, Integer> variables = new HashMap<>();
  private int variablesCounter = 0;
  private int functionsCounter = 0;
  private final HashMap<String, Integer> functions = new HashMap<>();
  private String putVals(ValueBase... vals) {
    StringBuilder output = new StringBuilder();
    for (ValueBase val : vals) {
      if (val instanceof SyntaxTree.Number) {
        output.append("PUT\tNUM").append((BigDecimal)val.getData()).append("\n");
      } else if (val instanceof SyntaxTree.Text) {
        output.append("PUT\tTXT").append(val.getData().toString().replace("\n", "\\n")).append("\n");
      } else if (val instanceof SyntaxTree.Boolean) {
        output.append("PUT\tBOOL").append(val.getData().toString()).append("\n");
      } else if (val instanceof SyntaxTree.Null) {
        output.append("PUT\tNULL\n");
      } else if (val instanceof SyntaxTree.Variable) {
        if (variables.get(((SyntaxTree.Variable)val).getVariableName()) != null) {
          Integer a = variables.get(((SyntaxTree.Variable)val).getVariableName());
          output.append("PUT\tNUM").append(a).append("\nMEMGET\n");
        } else {
          Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, ((SyntaxTree.Variable)val).getVariableName());
        }
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
      } else if (val instanceof SyntaxTree.Mod) {
        output.append(putVals(((SyntaxTree.Mod)val).getV2()));
        output.append(putVals(((SyntaxTree.Mod)val).getV1()));
        output.append("MOD\n");
      } else if (val instanceof SyntaxTree.Equals) {
        output.append(putVals(((SyntaxTree.Equals)val).getV2()));
        output.append(putVals(((SyntaxTree.Equals)val).getV1()));
        output.append("EQ\n");
      } else if (val instanceof SyntaxTree.StrictEquals) {
        output.append(putVals(((SyntaxTree.StrictEquals)val).getV2()));
        output.append(putVals(((SyntaxTree.StrictEquals)val).getV1()));
        output.append("FEQ\n");
      } else if (val instanceof SyntaxTree.GreaterThan) {
        output.append(putVals(((SyntaxTree.GreaterThan)val).getV2()));
        output.append(putVals(((SyntaxTree.GreaterThan)val).getV1()));
        output.append("GT\n");
      } else if (val instanceof SyntaxTree.GreaterThanOrEqual) {
        output.append(putVals(((SyntaxTree.GreaterThanOrEqual)val).getV2()));
        output.append(putVals(((SyntaxTree.GreaterThanOrEqual)val).getV1()));
        output.append("GE\n");
      } else if (val instanceof SyntaxTree.LesserThan) {
        output.append(putVals(((SyntaxTree.LesserThan)val).getV2()));
        output.append(putVals(((SyntaxTree.LesserThan)val).getV1()));
        output.append("LT\n");
      } else if (val instanceof SyntaxTree.LesserThanOrEqual) {
        output.append(putVals(((SyntaxTree.LesserThanOrEqual)val).getV2()));
        output.append(putVals(((SyntaxTree.LesserThanOrEqual)val).getV1()));
        output.append("LE\n");
      } else if (val instanceof SyntaxTree.And) {
        output.append(putVals(((SyntaxTree.And)val).getV2()));
        output.append(putVals(((SyntaxTree.And)val).getV1()));
        output.append("LAND\n");
      } else if (val instanceof SyntaxTree.Or) {
        output.append(putVals(((SyntaxTree.Or)val).getV2()));
        output.append(putVals(((SyntaxTree.Or)val).getV1()));
        output.append("LOR\n");
      } else if (val instanceof SyntaxTree.Xor) {
        output.append(putVals(((SyntaxTree.Xor)val).getV2()));
        output.append(putVals(((SyntaxTree.Xor)val).getV1()));
        output.append("XOR\n");
      } else if (val instanceof SyntaxTree.BitwiseAnd) {
        output.append(putVals(((SyntaxTree.BitwiseAnd)val).getV2()));
        output.append(putVals(((SyntaxTree.BitwiseAnd)val).getV1()));
        output.append("AND\n");
      } else if (val instanceof SyntaxTree.LeftShift) {
        output.append(putVals(((SyntaxTree.LeftShift)val).getV2()));
        output.append(putVals(((SyntaxTree.LeftShift)val).getV1()));
        output.append("LSHIFT\n");
      } else if (val instanceof SyntaxTree.RightShift) {
        output.append(putVals(((SyntaxTree.RightShift)val).getV2()));
        output.append(putVals(((SyntaxTree.RightShift)val).getV1()));
        output.append("RSHIFT\n");
      } else if (val instanceof SyntaxTree.BitwiseOr) {
        output.append(putVals(((SyntaxTree.BitwiseOr)val).getV2()));
        output.append(putVals(((SyntaxTree.BitwiseOr)val).getV1()));
        output.append("OR\n");
      } else if (val instanceof SyntaxTree.Negative) {
        output.append(putVals(((SyntaxTree.Negative)val).getValue()));
        output.append("NEG\n");
      } else if (val instanceof SyntaxTree.Not) {
        output.append(putVals(((SyntaxTree.Not)val).getValue()));
        output.append("LNOT\n");
      } else if (val instanceof SyntaxTree.BitwiseNot) {
        output.append(putVals(((SyntaxTree.BitwiseNot)val).getValue()));
        output.append("NOT\n");
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
        if (i < args.length - 1) {
          output.append(putVals((((SyntaxTree.Print)program).getSeparator())));
          output.append("PRINT\n");
        }
      }
    } else if (program instanceof SyntaxTree.If) {
      ProgramBase programBase = ((SyntaxTree.If)program).getElseProgram();
      String elseByteCode = SyntaxTreeToVMByteCode(programBase);
      output.append(putVals(new SyntaxTree.Number(new BigDecimal(elseByteCode.split("\n").length + 2 - (programBase == null? 1:0)))));
      output.append(putVals(((SyntaxTree.If)program).getCondition()));
      output.append("TOBOOL\nIFSKIP\n");
      output.append(elseByteCode);
      String byteCode = SyntaxTreeToVMByteCode(((SyntaxTree.If)program).getProgram());
      output.append(putVals(new SyntaxTree.Number(new BigDecimal(byteCode.split("\n").length))));
      output.append("SKIP\n");
      output.append(byteCode);
    } else if (program instanceof SyntaxTree.While) {
      output.append("REC\n");
      output.append(SyntaxTreeToVMByteCode(((SyntaxTree.While)program).getProgram()));
      output.append(putVals(((SyntaxTree.While)program).getCondition()));
      output.append("END\n");
      output.append(putVals(((SyntaxTree.While)program).getCondition()));
      output.append("WTRUN\nPOP\n");
    } else if (program instanceof OpCode) {
      VM vm = new VM();
      ValueBase[] byteCodes = ((OpCode)program).getProgram();
      for (int i = 0; i < byteCodes.length; i++) {
        if (byteCodes[i] instanceof SyntaxTree.Number) {
          byte tmp = (byte)((BigDecimal)((SyntaxTree.Number)byteCodes[i]).getData()).intValue();
          if (tmp == VM.PUT) {
            i++;
            if (byteCodes[i] instanceof SyntaxTree.Number) {
              output.append(vm.disassemble(tmp, (BigDecimal)((SyntaxTree.Number)byteCodes[i]).getData()));
            } else if (byteCodes[i] instanceof SyntaxTree.Text) {
              output.append(vm.disassemble(tmp, (String)((SyntaxTree.Text)byteCodes[i]).getData()).replace("\n", "\\n").replace("\f", "\\f").replace("\r", "\\r"));
            } else if (byteCodes[i] instanceof SyntaxTree.Boolean) {
              output.append(vm.disassemble(tmp, (boolean)((SyntaxTree.Boolean)byteCodes[i]).getData()));
            } else {
              output.append(vm.disassemble(tmp));
            }
          } else {
            output.append(vm.disassemble(tmp));
          }
        }
        output.append("\n");
      }
    } else if (program instanceof SyntaxTree.Function) {
      if (functions.containsKey(((SyntaxTree.Function)program).getFunctionName())) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, ((SyntaxTree.Function)program).getFunctionName());
      }
      functions.put(((SyntaxTree.Function)program).getFunctionName(), functionsCounter);
      functionsCounter++;
      String functionCode = SyntaxTreeToVMByteCode(((SyntaxTree.Function)program).getProgram());
      output.append("REC\n").append(functionCode)
              .append("END\nPUT\tNUM").append(functions.get(((SyntaxTree.Function)program).getFunctionName()))
              .append("\nMKFN\n");
    } else if (program instanceof SyntaxTree.CallFunction) {
      Integer functionCode = functions.get(((SyntaxTree.CallFunction)program).getFunctionName());
      if (!functions.containsKey(((SyntaxTree.CallFunction)program).getFunctionName())) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS, ((SyntaxTree.CallFunction)program).getFunctionName());
      }
      output.append("PUT\tNUM").append(functionCode).append("\nCALLFN\n");
    } else if (program instanceof SyntaxTree.Repeat) {
      output.append("REC\n");
      output.append(SyntaxTreeToVMByteCode(((SyntaxTree.Repeat)program).getProgram()));
      output.append("END\n");
      output.append(putVals(((SyntaxTree.Repeat)program).getCount()));
      output.append("REPEAT\n");
    } else if (program instanceof SyntaxTree.Exit) {
      output.append(putVals(((SyntaxTree.Exit)program).getStatus()));
      output.append("EXIT\n");
    } else if (program instanceof SyntaxTree.SetVariable) {
      if (variables.get(((SyntaxTree.SetVariable)program).getVariableName()) == null) {
        variables.put(((SyntaxTree.SetVariable)program).getVariableName(), variablesCounter);
        output.append(putVals(((SyntaxTree.SetVariable)program).getVariableValue()));
        output.append("PUT\tNUM").append(variablesCounter).append("\nMEMSET\n");
      } else {
        output.append(putVals(((SyntaxTree.SetVariable)program).getVariableValue()));
        output.append("PUT\tNUM").append(variables.get(((SyntaxTree.SetVariable)program).getVariableName())).append("\nMEMSET\n");
      }
      variablesCounter++;
    } else if (program instanceof SyntaxTree.Break) {
      output.append("BREAK\n");
    }
    return output.toString();
  }
}
