import java.math.BigDecimal;

public class OpCode extends ProgramBase implements java.io.Serializable {
  private static final VM vm = new VM();
  public static class PopFromVM extends ValueBase implements java.io.Serializable {
    @Override
    public Object getData() {
      String tmp = vm.pop();
      if (tmp.charAt(0) == 'T') {
        return new SyntaxTree.Text(tmp.substring(1));
      } else if (tmp.charAt(0) == 'N') {
        return new SyntaxTree.Number(new BigDecimal(tmp.substring(1)));
      } else if (tmp.charAt(0) == 'B') {
        return new SyntaxTree.Boolean(tmp.charAt(1) == '1');
      }
      return new SyntaxTree.Null();
    }
  }

  public static class PutToVM extends ProgramBase implements java.io.Serializable {
    private final ValueBase value;
    public PutToVM(ValueBase data) {
      this.value = data;
    }

    @Override
    public void eval() {
      ValueBase value = this.value;
      if (!(value instanceof SyntaxTree.Number || value instanceof SyntaxTree.Text || value instanceof SyntaxTree.Boolean || value instanceof SyntaxTree.Null)) {
        value = (ValueBase) value.getData();
      }
      if (value instanceof SyntaxTree.Number) {
        vm.run(VM.PUT, (BigDecimal)((SyntaxTree.Number) value).getData());
      } else if (value instanceof SyntaxTree.Text) {
        vm.run(VM.PUT, (String)((SyntaxTree.Text) value).getData());
      } else if (value instanceof SyntaxTree.Boolean) {
        vm.run(VM.PUT, (boolean)((SyntaxTree.Boolean) value).getData());
      } else {
        vm.run(VM.PUT);
      }
    }

    public ValueBase getValue() {
      return value;
    }
  }
  private ValueBase[] program;
  public ValueBase[] getProgram() {
    return this.program;
  }
  public OpCode(ValueBase... program) {
    this.program = program;
  }

  @Override
  void eval() {
    for (int i = 0; i < program.length; i++) {
      if (program[i] instanceof SyntaxTree.Number) {
        byte tmp = (byte)((BigDecimal)((SyntaxTree.Number)program[i]).getData()).intValue();
        if (tmp == VM.PUT) {
          i++;
          if (program[i] instanceof SyntaxTree.Number) {
            vm.run(tmp, (BigDecimal)((SyntaxTree.Number)program[i]).getData());
          } else if (program[i] instanceof SyntaxTree.Text) {
            vm.run(tmp, (String)((SyntaxTree.Text)program[i]).getData());
          } else if (program[i] instanceof SyntaxTree.Boolean) {
            vm.run(tmp, (boolean)((SyntaxTree.Boolean)program[i]).getData());
          } else {
            vm.run(tmp);
          }
        } else {
          vm.run(tmp);
        }
      }
    }
  }
}
