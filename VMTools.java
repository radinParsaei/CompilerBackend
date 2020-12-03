import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Map;

public class VMTools {
  private HashMap<String, Integer> variables = new HashMap<>();
  private int variablesCounter = 0;
  private int functionsCounter = 0;
  private boolean checkVariables = true;
  private final HashMap<String, Integer> functions = new HashMap<>();
  private final HashMap<String, Integer> sizes = new HashMap<>();
  private final HashMap<String, String> classesParameters = new HashMap<>();
  private String putVals(ValueBase... vals) {
    StringBuilder output = new StringBuilder();
    for (ValueBase val : vals) {
      if (val instanceof SyntaxTree.Number) {
        output.append("PUT\tNUM").append((BigDecimal) val.getData()).append("\n");
      } else if (val instanceof SyntaxTree.Text) {
        output.append("PUT\tTXT").append(val.getData().toString().replace("\n", "\\n").replace("\t", "\\t")).append("\n");
      } else if (val instanceof SyntaxTree.Boolean) {
        output.append("PUT\tBOOL").append(val.getData().toString()).append("\n");
      } else if (val instanceof SyntaxTree.Null) {
        output.append("PUT\tNULL\n");
      } else if (val instanceof SyntaxTree.Variable) {
        if (((SyntaxTree.Variable) val).getInstance() != null) {
          output.append(putVals(((SyntaxTree.Variable) val).getInstance()))
                  .append("PUT\tNUM0\nSTCKMOV\n");
        }
        if (((SyntaxTree.Variable) val).isUseInstanceName()) {
          output.append("PUT\tNUM0\nSTCKGET2\n");
        }
        if (((SyntaxTree.Variable) val).getVariableName().startsWith("#C") ||
                ((SyntaxTree.Variable) val).getVariableName().startsWith("#F")) {
          if (!variables.containsKey(((SyntaxTree.Variable) val).getVariableName())) {
            variables.put(((SyntaxTree.Variable) val).getVariableName(), variablesCounter++);
          }
        }
        if (((SyntaxTree.Variable) val).getInstance() == null) {
          output.append("PUT\tNUM&").append(((SyntaxTree.Variable) val).getVariableName());
        } else {
          output.append("PUT\tTXT").append(((SyntaxTree.Variable) val).getVariableName())
                  .append("\nPUT\tNUM0\nSTCKGET\nMEMGET\nPUT\tTXT#C\nADD\nADD\nPUT\tTXTgpm\nDLCALL\n");
        }
        if (((SyntaxTree.Variable) val).isUseInstanceName()) {
          output.append("\nADD");
        }
        output.append("\nMEMGET\n");
      } else if (val instanceof SyntaxTree.Add) {
        output.append(putVals(((SyntaxTree.Add) val).getV2()));
        output.append(putVals(((SyntaxTree.Add) val).getV1()));
        output.append("ADD\n");
      } else if (val instanceof SyntaxTree.Sub) {
        output.append(putVals(((SyntaxTree.Sub) val).getV2()));
        output.append(putVals(((SyntaxTree.Sub) val).getV1()));
        output.append("SUB\n");
      } else if (val instanceof SyntaxTree.Mul) {
        output.append(putVals(((SyntaxTree.Mul) val).getV2()));
        output.append(putVals(((SyntaxTree.Mul) val).getV1()));
        output.append("MUL\n");
      } else if (val instanceof SyntaxTree.Div) {
        output.append(putVals(((SyntaxTree.Div) val).getV2()));
        output.append(putVals(((SyntaxTree.Div) val).getV1()));
        output.append("DIV\n");
      } else if (val instanceof SyntaxTree.Mod) {
        output.append(putVals(((SyntaxTree.Mod) val).getV2()));
        output.append(putVals(((SyntaxTree.Mod) val).getV1()));
        output.append("MOD\n");
      } else if (val instanceof SyntaxTree.Equals) {
        output.append(putVals(((SyntaxTree.Equals) val).getV2()));
        output.append(putVals(((SyntaxTree.Equals) val).getV1()));
        output.append("EQ\n");
      } else if (val instanceof SyntaxTree.StrictEquals) {
        output.append(putVals(((SyntaxTree.StrictEquals) val).getV2()));
        output.append(putVals(((SyntaxTree.StrictEquals) val).getV1()));
        output.append("FEQ\n");
      } else if (val instanceof SyntaxTree.GreaterThan) {
        output.append(putVals(((SyntaxTree.GreaterThan) val).getV2()));
        output.append(putVals(((SyntaxTree.GreaterThan) val).getV1()));
        output.append("GT\n");
      } else if (val instanceof SyntaxTree.GreaterThanOrEqual) {
        output.append(putVals(((SyntaxTree.GreaterThanOrEqual) val).getV2()));
        output.append(putVals(((SyntaxTree.GreaterThanOrEqual) val).getV1()));
        output.append("GE\n");
      } else if (val instanceof SyntaxTree.LesserThan) {
        output.append(putVals(((SyntaxTree.LesserThan) val).getV2()));
        output.append(putVals(((SyntaxTree.LesserThan) val).getV1()));
        output.append("LT\n");
      } else if (val instanceof SyntaxTree.LesserThanOrEqual) {
        output.append(putVals(((SyntaxTree.LesserThanOrEqual) val).getV2()));
        output.append(putVals(((SyntaxTree.LesserThanOrEqual) val).getV1()));
        output.append("LE\n");
      } else if (val instanceof SyntaxTree.And) {
        output.append(putVals(((SyntaxTree.And) val).getV2()));
        output.append(putVals(((SyntaxTree.And) val).getV1()));
        output.append("LAND\n");
      } else if (val instanceof SyntaxTree.Or) {
        output.append(putVals(((SyntaxTree.Or) val).getV2()));
        output.append(putVals(((SyntaxTree.Or) val).getV1()));
        output.append("LOR\n");
      } else if (val instanceof SyntaxTree.Xor) {
        output.append(putVals(((SyntaxTree.Xor) val).getV2()));
        output.append(putVals(((SyntaxTree.Xor) val).getV1()));
        output.append("XOR\n");
      } else if (val instanceof SyntaxTree.BitwiseAnd) {
        output.append(putVals(((SyntaxTree.BitwiseAnd) val).getV2()));
        output.append(putVals(((SyntaxTree.BitwiseAnd) val).getV1()));
        output.append("AND\n");
      } else if (val instanceof SyntaxTree.LeftShift) {
        output.append(putVals(((SyntaxTree.LeftShift) val).getV2()));
        output.append(putVals(((SyntaxTree.LeftShift) val).getV1()));
        output.append("LSHIFT\n");
      } else if (val instanceof SyntaxTree.RightShift) {
        output.append(putVals(((SyntaxTree.RightShift) val).getV2()));
        output.append(putVals(((SyntaxTree.RightShift) val).getV1()));
        output.append("RSHIFT\n");
      } else if (val instanceof SyntaxTree.BitwiseOr) {
        output.append(putVals(((SyntaxTree.BitwiseOr) val).getV2()));
        output.append(putVals(((SyntaxTree.BitwiseOr) val).getV1()));
        output.append("OR\n");
      } else if (val instanceof SyntaxTree.Negative) {
        output.append(putVals(((SyntaxTree.Negative) val).getValue()));
        output.append("NEG\n");
      } else if (val instanceof SyntaxTree.Not) {
        output.append(putVals(((SyntaxTree.Not) val).getValue()));
        output.append("LNOT\n");
      } else if (val instanceof SyntaxTree.BitwiseNot) {
        output.append(putVals(((SyntaxTree.BitwiseNot) val).getValue()));
        output.append("NOT\n");
      } else if (val instanceof SyntaxTree.CreateInstance) {
        output.append("MEMSIZE\nMEMSIZE\nPUT\tNUM0\nSTCKMOV\nPUT\tTXT").append(((SyntaxTree.CreateInstance) val).getClassName())
                .append("\nMEMPUT\n//PUT VARIABLES OF CLASS ").append(((SyntaxTree.CreateInstance) val).getClassName()).append("PUT\tNUM0\nSTCKDEL\n");
      } else if (val instanceof SyntaxTree.Lambda) {
        output.append(syntaxTreeToVMByteCode2(((SyntaxTree.Lambda) val).getCreateLambda()));
        output.append("PUT\tNUM").append(functions.get(((SyntaxTree.Lambda) val).getCreateLambda().getFunctionName())).append("\n");
      } else if (val instanceof SyntaxTree.CallFunction) {
        ((SyntaxTree.CallFunction) val).findFunction();
        if (((SyntaxTree.CallFunction) val).isNativeFunction()) {
          output.append(syntaxTreeToVMByteCode2(new SyntaxTree.Programs(((SyntaxTree.CallFunction) val).getVariableSetters())));
        } else {
          Integer functionCode = functions.get(((SyntaxTree.CallFunction) val).getFunctionName());
          if ((!functions.containsKey(((SyntaxTree.CallFunction) val).getFunctionName())) && !((SyntaxTree.CallFunction) val).isAddInstanceName()) {
            Errors.error(ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS, ((SyntaxTree.CallFunction) val).getFunctionName());
          }
          if (((SyntaxTree.CallFunction) val).isRecursion()) {
            output.append("//save ").append(((SyntaxTree.CallFunction) val).getFunctionName()).append(" variables\n");
          }
          if (((SyntaxTree.CallFunction) val).isFromInstance()) {
            output.append(putVals(((SyntaxTree.CallFunction) val).getInstance()));
            output.append("PUT\tNUM0\nSTCKMOV\n");
          }
          if (((SyntaxTree.CallFunction) val).isAddInstanceName()) {
            output.append(syntaxTreeToVMByteCode2(new SyntaxTree.Programs(((SyntaxTree.CallFunction) val).getVariableSetters())))
                    .append("PUT\tTXT").append(((SyntaxTree.CallFunction) val).getFunctionName())
                    .append("\nPUT\tNUM0\nSTCKGET2\nMEMGET\nPUT\tTXT#C\nADD")
                    .append("\nADD\nCALLFN\n");
          } else {
            output.append(syntaxTreeToVMByteCode2(new SyntaxTree.Programs(((SyntaxTree.CallFunction) val).getVariableSetters())))
                    .append("PUT\tNUM").append(functionCode).append("\nCALLFN\n");
          }
          if (((SyntaxTree.CallFunction) val).isFromInstance()) {
            output.append("PUT\tNUM0\nSTCKDEL\n");
          }
          if (((SyntaxTree.CallFunction) val).isRecursion()) {
            output.append("//load ").append(((SyntaxTree.CallFunction) val).getFunctionName()).append(" variables\n");
          }
        }
      } else if (val instanceof SyntaxTree.CallFunctionFromPointer) {
        output.append(putVals(((SyntaxTree.CallFunctionFromPointer) val).getValues()))
                .append(putVals(((SyntaxTree.CallFunctionFromPointer) val).getFunctionPointer()))
                .append("PUT\tTXTfp:c:\nADD\nDLCALL\n");
      }
    }
    return output.toString();
  }

  public String syntaxTreeToVMByteCode(ProgramBase program) {
    String result = syntaxTreeToVMByteCode2(program);
    return "PUT NULL\n" +
            "PUT NUM" + variablesCounter + "\n" +
            "MEMSET\n" + result;
  }

  public String syntaxTreeToVMByteCode2(ProgramBase program) {
    boolean checkVariables2 = checkVariables;
    if (checkVariables2) checkVariables = false;
    StringBuilder output = new StringBuilder();
    if (program instanceof SyntaxTree.Programs) {
      for (ProgramBase program2 : ((SyntaxTree.Programs) program).getPrograms()) {
        output.append(syntaxTreeToVMByteCode2(program2));
      }
    } else if (program instanceof SyntaxTree.Print) {
      ValueBase[] args = ((SyntaxTree.Print) program).getArgs();
      for (int i = 0; i < args.length; i++) {
        output.append(putVals(args[i]));
        output.append("PRINT\n");
        if (i < args.length - 1) {
          output.append(putVals((((SyntaxTree.Print) program).getSeparator())));
          output.append("PRINT\n");
        }
      }
    } else if (program instanceof SyntaxTree.If) {
      ProgramBase programBase = ((SyntaxTree.If) program).getElseProgram();
      String elseByteCode = syntaxTreeToVMByteCode2(programBase);
      output.append(putVals(new SyntaxTree.Number(new BigDecimal((elseByteCode.split("\n").length + 2 - (programBase == null ? 1 : 0)) - elseByteCode.split("//").length + 1))));
      for (String string : elseByteCode.split("\n")) {
        if (string.startsWith("//load")) {
          output.append("//add size of").append(string.replace("//load", "")).append('\n');
        }
      }
      output.append(putVals(((SyntaxTree.If) program).getCondition()));
      output.append("TOBOOL\nIFSKIP\n");
      output.append(elseByteCode);
      String byteCode = syntaxTreeToVMByteCode2(((SyntaxTree.If) program).getProgram());
      output.append(putVals(new SyntaxTree.Number(new BigDecimal(byteCode.split("\n").length - byteCode.split("//").length + 1))));
      for (String string : byteCode.split("\n")) {
        if (string.startsWith("//load")) {
          output.append("//add size of").append(string.replace("//load", "")).append('\n');
        }
      }
      output.append("SKIP\n");
      output.append(byteCode);
    } else if (program instanceof SyntaxTree.While) {
      output.append("REC\n");
      output.append(syntaxTreeToVMByteCode2(((SyntaxTree.While) program).getProgram()));
      output.append(putVals(((SyntaxTree.While) program).getCondition()));
      output.append("END\n");
      output.append(putVals(((SyntaxTree.While) program).getCondition()));
      output.append("WTRUN\nPOP\n");
    } else if (program instanceof OpCode) {
      VM vm = new VM();
      ValueBase[] byteCodes = ((OpCode) program).getProgram();
      for (int i = 0; i < byteCodes.length; i++) {
        if (byteCodes[i] instanceof SyntaxTree.Number) {
          byte tmp = (byte) ((BigDecimal) ((SyntaxTree.Number) byteCodes[i]).getData()).intValue();
          if (tmp == VM.PUT) {
            i++;
            if (byteCodes[i] instanceof SyntaxTree.Number) {
              output.append(vm.disassemble(tmp, (BigDecimal) ((SyntaxTree.Number) byteCodes[i]).getData()));
            } else if (byteCodes[i] instanceof SyntaxTree.Text) {
              output.append(vm.disassemble(tmp, (String) ((SyntaxTree.Text) byteCodes[i]).getData()).replace("\n", "\\n").replace("\f", "\\f").replace("\r", "\\r"));
            } else if (byteCodes[i] instanceof SyntaxTree.Boolean) {
              output.append(vm.disassemble(tmp, (boolean) ((SyntaxTree.Boolean) byteCodes[i]).getData()));
            } else {
              output.append(vm.disassemble(tmp));
            }
          } else {
            output.append(vm.disassemble(tmp));
          }
        }
        output.append("\n");
      }
    } else if (program instanceof OpCode.PutToVM) {
      output.append(putVals(((OpCode.PutToVM) program).getValue()));
    } else if (program instanceof SyntaxTree.Function) {
      if (functions.containsKey(((SyntaxTree.Function) program).getFunctionName())) {
        Errors.error(ErrorCodes.ERROR_FUNCTION_REDECLARATION, ((SyntaxTree.Function) program).getFunctionName());
      }
      functions.put(((SyntaxTree.Function) program).getFunctionName(), functionsCounter);
      functionsCounter++;
      String functionCode = syntaxTreeToVMByteCode2(((SyntaxTree.Function) program).getProgram());
      output.append("REC\n").append(functionCode)
              .append("END\nPUT\tTXT").append(functions.get(((SyntaxTree.Function) program).getFunctionName()));
      for (String arg : ((SyntaxTree.Function) program).getArgs()) {
        output.append(",").append(variables.get((((SyntaxTree.Function) program).getFunctionName().startsWith("#C")? "":"#F") +
                ((SyntaxTree.Function) program).getFunctionName() + ":" + arg));
      }
      output.append("\nMKFN - ").append(((SyntaxTree.Function) program).getFunctionName()).append("\n");
    } else if (program instanceof SyntaxTree.ExecuteValue) {
      output.append(putVals(((SyntaxTree.ExecuteValue) program).getValue()));//.append("POP\n");
    } else if (program instanceof SyntaxTree.Repeat) {
      output.append("REC\n");
      output.append(syntaxTreeToVMByteCode2(((SyntaxTree.Repeat) program).getProgram()));
      output.append("END\n");
      output.append(putVals(((SyntaxTree.Repeat) program).getCount()));
      output.append("REPEAT\n");
    } else if (program instanceof SyntaxTree.Exit) {
      output.append(putVals(((SyntaxTree.Exit) program).getStatus()));
      output.append("EXIT\n");
    } else if (program instanceof SyntaxTree.SetVariable) {
      if (((SyntaxTree.SetVariable) program).getInstance() != null) {
        output.append(putVals(((SyntaxTree.SetVariable) program).getVariableValue()));
        output.append(putVals(((SyntaxTree.SetVariable) program).getInstance())).append("PUT\tNUM0\nSTCKMOV\n");
        output.append("PUT\tTXT").append(((SyntaxTree.SetVariable) program).getVariableName())
                .append("\nPUT\tNUM0\nSTCKGET2\nMEMGET\nPUT\tTXT#C\nADD\nADD\nPUT\tTXTgpm\nDLCALL\n");
        output.append("PUT\tNUM0\nSTCKGET\nADD\nMEMSET\n");
      } else {
        if (variables.get(((SyntaxTree.SetVariable) program).getVariableName()) == null) {
          variables.put(((SyntaxTree.SetVariable) program).getVariableName(), variablesCounter);
          variablesCounter++;
          output.append(putVals(((SyntaxTree.SetVariable) program).getVariableValue()));
          output.append("PUT\tNUM").append(variables.get(((SyntaxTree.SetVariable) program).getVariableName()));
          if (((SyntaxTree.SetVariable) program).isUseInstanceName()) {
            output.append("\nPUT\tNUM0\nSTCKGET2\nADD");
          }
          output.append("\nMEMSET\n");
        } else {
          output.append(putVals(((SyntaxTree.SetVariable) program).getVariableValue()));
          output.append("PUT\tNUM").append(variables.get(((SyntaxTree.SetVariable) program).getVariableName()));
          if (((SyntaxTree.SetVariable) program).isUseInstanceName()) {
            output.append("\nPUT\tNUM0\nSTCKGET2\nADD");
          }
          output.append("\nMEMSET\n");
        }
      }
    } else if (program instanceof SyntaxTree.Break) {
      output.append("BREAK\n");
    } else if (program instanceof SyntaxTree.Return) {
      output.append(putVals(((SyntaxTree.Return) program).getValue())).append("EXITFN\n");
    } else if (program instanceof SyntaxTree.CreateClass) {
      VMTools vmTools = new VMTools();
      vmTools.setVariables((HashMap<String, Integer>) variables.clone());
      vmTools.setFunctionsCounter(functionsCounter);
      vmTools.setVariablesCounter(1);
      output.append(createClass(((SyntaxTree.CreateClass) program).getPrograms(), vmTools, ((SyntaxTree.CreateClass) program).getClassName()));
    }
    String result = output.toString();
    if (checkVariables2) {
      for (Map.Entry<String, Integer> entry : variables.entrySet()) {
        result = result.replace("PUT\tNUM&" + entry.getKey() + "\n", "PUT\tNUM" + entry.getValue() + "\n");
        for (Map.Entry<String, Integer> entry1 : functions.entrySet()) {
          if (entry.getKey().startsWith("#F" + entry1.getKey())) {
            int tmp = result.length();
            result = result.replace("\n//save " + entry1.getKey() + " variables\n", "\nPUT\tNUM" +
                    variables.get(entry.getKey()) + "\nMEMGET\nPUT\tNUM0\nSTCKMOV\n//save " + entry1.getKey() + " variables\n");
            if (!sizes.containsKey("//add size of " + entry1.getKey() + " variables"))
              sizes.put("//add size of " + entry1.getKey() + " variables", 0);
            if (tmp != result.length())
              sizes.put("//add size of " + entry1.getKey() + " variables", sizes.get("//add size of " + entry1.getKey() + " variables") + 8);
            result = result.replace("\n//load " + entry1.getKey() + " variables\n", "\nPUT\tNUM0\nSTCKGET\nPUT\tNUM" +
                    variables.get(entry.getKey()) + "\nMEMSET\n//load " + entry1.getKey() + " variables\n");
          }
        }
      }
      int size = result.length();
      for (Map.Entry<String, Integer> entry : sizes.entrySet()) {
        int tmp = result.indexOf("\n" + entry.getKey());
        if (tmp < 0) continue;
        int tmp2 = tmp--;
        while (result.charAt(tmp) >= 48 && result.charAt(tmp) <= 57) tmp--;
        tmp++;
        int i = Integer.parseInt(result.substring(tmp, tmp2));
        result = result.replaceFirst(i + "\n" + entry.getKey(), (i + entry.getValue()) + "");
      }
      while (size != result.length()) {
        size = result.length();
        for (Map.Entry<String, Integer> entry : sizes.entrySet()) {
          int tmp = result.indexOf("\n" + entry.getKey());
          if (tmp < 0) continue;
          int tmp2 = tmp--;
          while (result.charAt(tmp) >= 48 && result.charAt(tmp) <= 57) tmp--;
          tmp++;
          int i = Integer.parseInt(result.substring(tmp, tmp2));
          result = result.replaceFirst(i + "\n" + entry.getKey(), (i + entry.getValue()) + "");
        }
      }
      for (Map.Entry<String, String> entry : classesParameters.entrySet()) {
        result = result.replace("\n//PUT VARIABLES OF CLASS " + entry.getKey(), "//PUT VARIABLES OF CLASS" + entry.getKey() + "\n" + entry.getValue());
      }
      int index = result.indexOf("PUT\tNUM&");
      if (index != -1) {
        String value = result.substring(index + 8, result.indexOf("\n", index));
        Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, value);
      }
    }
    return result;
  }

  private String createClass(ProgramBase program, VMTools vmTools, String className) {
    if (program instanceof SyntaxTree.Function) {
      String result = vmTools.syntaxTreeToVMByteCode2(program);
      functions.putAll(vmTools.getFunctions());
      variables.putAll(vmTools.getVariables());
      functionsCounter = vmTools.functionsCounter;
      return result + "PUT\tTXTnf" + ((SyntaxTree.Function) program).getFunctionName().split(":")[0] + ":" +
              functions.get(((SyntaxTree.Function) program).getFunctionName()) + "\nDLCALL\n";
    } else if (program instanceof SyntaxTree.SetVariable) {
      if (classesParameters.containsKey(className)) {
        classesParameters.put(className, classesParameters.get(className) + vmTools.syntaxTreeToVMByteCode2(program));
      } else {
        classesParameters.put(className, vmTools.syntaxTreeToVMByteCode2(program));
      }
      variables.putAll(vmTools.getVariables());
      return "PUT\tTXTnm" + ((SyntaxTree.SetVariable) program).getVariableName() + ":" +
              variables.get(((SyntaxTree.SetVariable) program).getVariableName()) + "\nDLCALL\n";
    } else if (program instanceof SyntaxTree.Programs) {
      StringBuilder stringBuilder = new StringBuilder();
      for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
        stringBuilder.append(createClass(program1, vmTools, className));
      }
      return stringBuilder.toString();
    }
    return "";
  }

  public void setVariables(HashMap<String, Integer> variables) {
    this.variables = variables;
  }

  public void setVariablesCounter(int variablesCounter) {
    this.variablesCounter = variablesCounter;
  }

  public HashMap<String, Integer> getVariables() {
    return variables;
  }

  public HashMap<String, Integer> getFunctions() {
    return functions;
  }

  public void setFunctionsCounter(int functionsCounter) {
    this.functionsCounter = functionsCounter;
  }
}
