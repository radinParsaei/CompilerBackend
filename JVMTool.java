import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class JVMTool {
    private static final String TYPE_NUMBER = "Ljava/math/BigDecimal;";
    private static final String TYPE_TEXT = "Ljava/lang/String;";
    private static final String TYPE_BOOLEAN = "Ljava/lang/Boolean;";
    private static final String TYPE_NULL_OR_UNKNOWN = "Ljava/lang/Object;";
    private final HashMap<String, Integer> variables = new HashMap<>();
    private int variablesCounter = 0;
    private final ArrayList<String> fields = new ArrayList<>();
    private String putVales(MethodVisitor methodWriter, ValueBase val, ClassWriter classWriter, String className) {
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
            if (((SyntaxTree.Variable) val).getVariableName().contains(":")) {
                Integer address;
                address = variables.get(((SyntaxTree.Variable) val).getVariableName());
                if (address == null) {
                    Errors.error(ErrorCodes.ERROR_VARIABLE_DOES_NOT_EXISTS, ((SyntaxTree.Variable) val).getVariableName());
                }
                methodWriter.visitVarInsn(ALOAD, address);
            } else {
                methodWriter.visitFieldInsn(GETSTATIC, className, ((SyntaxTree.Variable) val).getVariableName(),
                        "Ljava/lang/Object;");
            }
        }
        return TYPE_NULL_OR_UNKNOWN;
    }

    public byte[] syntaxTreeToJVMClass(ProgramBase program, String className) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null);
        syntaxTreeToJVMClass1(program, classWriter, className);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    public void syntaxTreeToJVMClass1(ProgramBase program, ClassWriter classWriter, String className) {
        MethodVisitor mainMethodWriter = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mainMethodWriter.visitCode();
        Label methodStart = new Label();
        Label methodEnd = new Label();
        mainMethodWriter.visitLabel(methodStart);

        syntaxTreeToJVMClass2(program, mainMethodWriter, classWriter, className);
        mainMethodWriter.visitInsn(RETURN);
        mainMethodWriter.visitLabel(methodEnd);
        mainMethodWriter.visitMaxs(1, 1);
        mainMethodWriter.visitEnd();
    }

    public void syntaxTreeToJVMClass2(ProgramBase program, MethodVisitor methodVisitor, ClassWriter classWriter, String className) {
        if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                syntaxTreeToJVMClass2(program1, methodVisitor, classWriter, className);
            }
        } else if (program instanceof SyntaxTree.Print) {
            ValueBase[] args = ((SyntaxTree.Print) program).getArgs();
            for (int i = 0; i < args.length; i++) {
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                        ("(" + putVales(methodVisitor, args[i], classWriter, className) + ")V").replace("java/lang/Boolean", "java/lang/Object")
                                .replace("java/math/BigDecimal", "java/lang/Object"), false);
                if (i < args.length - 1) {
                    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                            ("(" + putVales(methodVisitor, (((SyntaxTree.Print) program).getSeparator()), classWriter, className) + ")V")
                                    .replace("java/lang/Boolean", "java/lang/Object")
                                    .replace("java/math/BigDecimal", "java/lang/Object"), false);
                }
            }
        } else if (program instanceof SyntaxTree.SetVariable) {
            if (((SyntaxTree.SetVariable) program).getVariableName().contains(":")) {
                putVales(methodVisitor, ((SyntaxTree.SetVariable) program).getVariableValue(), classWriter, className);
                int address;
                if (variables.containsKey(((SyntaxTree.SetVariable) program).getVariableName())) {
                    address = variables.get(((SyntaxTree.SetVariable) program).getVariableName());
                } else {
                    variablesCounter++;
                    address = variablesCounter++;
                    variables.put(((SyntaxTree.SetVariable) program).getVariableName(), address);
                }
                methodVisitor.visitVarInsn(ASTORE, address);
            } else {
                putVales(methodVisitor, ((SyntaxTree.SetVariable) program).getVariableValue(), classWriter, className);
                if (!fields.contains(((SyntaxTree.SetVariable) program).getVariableName())) {
                    classWriter.visitField(ACC_PRIVATE | ACC_STATIC, ((SyntaxTree.SetVariable) program).getVariableName(),
                            "Ljava/lang/Object;", null, null);
                }
                methodVisitor.visitFieldInsn(PUTSTATIC, className, ((SyntaxTree.SetVariable) program).getVariableName(),
                        "Ljava/lang/Object;");
            }
        } else if (program instanceof SyntaxTree.Exit) {
            putVales(methodVisitor, ((SyntaxTree.Exit) program).getStatus(), classWriter, className);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
        } else if (program instanceof SyntaxTree.If) {
            Label label = new Label();
            putVales(methodVisitor, ((SyntaxTree.If) program).getCondition(), classWriter, className);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            methodVisitor.visitJumpInsn(IFEQ, label);
            syntaxTreeToJVMClass2(((SyntaxTree.If) program).getProgram(), methodVisitor, classWriter, className);
            methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
            methodVisitor.visitLabel(label);
//            methodVisitor.visitFrame(F_SAME1, 0, null, 1, new Object[]{ INTEGER });
        }
    }
}