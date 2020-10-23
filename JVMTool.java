import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.util.HashMap;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class JVMTool {
    private static final String TYPE_NUMBER = "Ljava/math/BigDecimal;";
    private static final String TYPE_TEXT = "Ljava/lang/String;";
    private static final String TYPE_BOOLEAN = "Ljava/lang/Boolean;";
    private static final String TYPE_NULL_OR_UNKNOWN = "Ljava/lang/Object;";
    private final HashMap<String, Integer> variables = new HashMap<>();
    private int variablesCounter = 0;
    private String putVales(MethodVisitor methodWriter, ValueBase val) {
        if (val instanceof SyntaxTree.Number) {
            methodWriter.visitTypeInsn(NEW, "java/math/BigDecimal");
            methodWriter.visitInsn(DUP);
            methodWriter.visitLdcInsn(val.toString());
            methodWriter.visitMethodInsn(INVOKESPECIAL, "java/math/BigDecimal", "<init>", "(Ljava/lang/String;)V", false);
            return TYPE_NUMBER;
        } else if (val instanceof SyntaxTree.Text) {
            methodWriter.visitLdcInsn(val.getData());
            return TYPE_TEXT;
        } else if (val instanceof SyntaxTree.Boolean) {
            if ((boolean)val.getData()) {
                methodWriter.visitInsn(ICONST_1);
            } else {
                methodWriter.visitInsn(ICONST_0);
            }
            methodWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            return TYPE_BOOLEAN;
        } else if (val instanceof SyntaxTree.Null) {
            methodWriter.visitInsn(ACONST_NULL);
        } else if (val instanceof SyntaxTree.Variable) {
            Integer address;
            address = variables.get(((SyntaxTree.Variable) val).getVariableName());
            if (address == null) {
                Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, ((SyntaxTree.Variable) val).getVariableName());
            }
            methodWriter.visitVarInsn(ALOAD, address);
        }
        return TYPE_NULL_OR_UNKNOWN;
    }

    public byte[] syntaxTreeToJVMClass(ProgramBase program, String className) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null);
        syntaxTreeToJVMClass1(program, classWriter);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    public void syntaxTreeToJVMClass1(ProgramBase program, ClassWriter classWriter) {
        MethodVisitor mainMethodWriter = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mainMethodWriter.visitCode();
        Label methodStart = new Label();
        Label methodEnd = new Label();
        mainMethodWriter.visitLabel(methodStart);

        syntaxTreeToJVMClass2(program, mainMethodWriter);
        mainMethodWriter.visitInsn(RETURN);
        mainMethodWriter.visitLabel(methodEnd);
        mainMethodWriter.visitEnd();
        mainMethodWriter.visitMaxs(1, 1);
    }

    public void syntaxTreeToJVMClass2(ProgramBase program, MethodVisitor methodVisitor) {
        if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                syntaxTreeToJVMClass2(program1, methodVisitor);
            }
        } else if (program instanceof SyntaxTree.Print) {
            ValueBase[] args = ((SyntaxTree.Print) program).getArgs();
            for (int i = 0; i < args.length; i++) {
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                        ("(" + putVales(methodVisitor, args[i]) + ")V").replace("java/lang/Boolean", "java/lang/Object")
                                .replace("java/math/BigDecimal", "java/lang/Object"), false);
                if (i < args.length - 1) {
                    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                            ("(" + putVales(methodVisitor, (((SyntaxTree.Print) program).getSeparator())) + ")V")
                                    .replace("java/lang/Boolean", "java/lang/Object")
                                    .replace("java/math/BigDecimal", "java/lang/Object"), false);
                }
            }
        } else if (program instanceof SyntaxTree.SetVariable) {
            putVales(methodVisitor, ((SyntaxTree.SetVariable) program).getVariableValue());
            int address;
            if (variables.containsKey(((SyntaxTree.SetVariable) program).getVariableName())) {
                address = variables.get(((SyntaxTree.SetVariable) program).getVariableName());
            } else {
                variablesCounter++;
                address = variablesCounter++;
                variables.put(((SyntaxTree.SetVariable) program).getVariableName(), address);
            }
            methodVisitor.visitVarInsn(ASTORE, address);
        } else if (program instanceof SyntaxTree.Exit) {
            putVales(methodVisitor, ((SyntaxTree.Exit) program).getStatus());
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
        }
    }
}