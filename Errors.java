public class Errors {
  static void error(byte errorCode, String... args) {
    switch (errorCode) {
      case ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS:
        System.err.println("Variable " + args[0] + " does not exists");
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
