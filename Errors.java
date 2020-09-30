public class Errors {
  static void error(byte errorCode, String... args) {
    switch (errorCode) {
      case ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS:
        System.err.println("Variable " + args[0] + " does not exists");
        break;
      case ErrorCodes.ERROR_FUNCTION_DOES_NOT_EXISTS:
        System.err.println("Function " + args[0] + " does not exists");
        break;
      case ErrorCodes.ERROR_FUNCTION_REDECLARATION:
        System.err.println("Function " + args[0] + " already exists");
        break;
      case ErrorCodes.ERROR_VARIABLE_REDECLARATION:
        System.err.println("Variable " + args[0] + " already exists");
        break;
      case ErrorCodes.ERROR_VARIABLE_NOT_DECLARED:
        System.err.println("use of undeclared variable " + args[0]);
        break;
      case ErrorCodes.ERROR_TYPE:
        System.out.print("Type Error");
        if (args.length != 0) System.out.println(":\t" + args[0]);
        else System.out.println();
        break;
    }
    System.exit(1);
  }
}
