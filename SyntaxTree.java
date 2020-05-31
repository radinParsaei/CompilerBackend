public class SyntaxTree {
  public static class Number extends ValueBase {
    public Number(java.lang.Number number){
      this.setData(number);
    }
  }

  public static class Text extends ValueBase {
   public Text(String text) {
     this.setData(text);
   }
  }

  public static class Programs extends ProgramBase implements java.io.Serializable {
    private ProgramBase[] programs;
    public Programs(ProgramBase... programs) {
      this.programs = programs;
    }
    public ProgramBase[] getPrograms() {
      return programs;
    }

    @Override
    void eval() {
      for (ProgramBase program : programs) {
        program.eval();
      }
    }
  }

  public static class Print extends ProgramBase implements java.io.Serializable {
    private ValueBase[] args;
    private ValueBase separator = new Text(" ");
    public ValueBase[] getArgs() {
      return this.args;
    }
    public ValueBase getSeparator() {
      return this.separator;
    }
    public Print setSeparator(ValueBase separator) {
      this.separator = separator;
      return this;
    }
    public Print(ValueBase... args) {
      this.args = args;
    }

    @Override
    void eval() {
      for (int i = 0; i < args.length; i++) {
        System.out.print(args[i].getData());
        if (i < args.length - 1) System.out.print(separator);
      }
    }
  }
}
