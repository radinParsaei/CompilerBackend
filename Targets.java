public class Targets {
    public static final boolean systemPrint = true;
    public static final boolean customWhile = false;
    public static final boolean isWeb = false;
    public static final boolean isInThread = false;

    public interface CustomWhileInterface {
        boolean run();
    }

    public static void _while(CustomWhileInterface customWhileInterface) {
        while (customWhileInterface.run());
    }

    public static void print(ValueBase value) {
    }
}