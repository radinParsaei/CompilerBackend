public class Errors {
  static void error(byte errorCode, String... args) {
    switch (errorCode) {
      case ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS:
        System.err.println("Variable " + args[0] + " does not exists");
        break;
    }
    System.exit(1);
  }
}
