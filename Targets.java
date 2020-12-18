public class Targets {
    public static final boolean systemPrint = true;
    public static final boolean customWhile = false;
    public interface CustomWhileInterface {
        boolean run();
    }

    public static void _while(CustomWhileInterface customWhileInterface) {
        while (customWhileInterface.run());
    }

    public static void print(ValueBase value) {
    }
}