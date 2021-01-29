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
    private boolean addAddFunction = false;
    private boolean addSubFunction = false;
    private boolean addMulFunction = false;
    private boolean addDivFunction = false;
    private boolean addModFunction = false;
    private boolean addPowFunction = false;
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
        } else if (val instanceof SyntaxTree.Add) {
            addAddFunction = true;
            putVales(methodWriter, ((SyntaxTree.Add) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Add) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#add", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.Sub) {
            addSubFunction = true;
            putVales(methodWriter, ((SyntaxTree.Sub) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Sub) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#sub", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.Mul) {
            addMulFunction = true;
            putVales(methodWriter, ((SyntaxTree.Mul) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Mul) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#mul", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.Div) {
            addDivFunction = true;
            putVales(methodWriter, ((SyntaxTree.Div) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Div) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#div", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.Mod) {
            addModFunction = true;
            putVales(methodWriter, ((SyntaxTree.Mod) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Mod) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#div", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.Pow) {
            addPowFunction = true;
            putVales(methodWriter, ((SyntaxTree.Pow) val).getV1(), classWriter, className);
            putVales(methodWriter, ((SyntaxTree.Pow) val).getV2(), classWriter, className);
            methodWriter.visitMethodInsn(INVOKESTATIC, className, "#pow", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        } else if (val instanceof SyntaxTree.PrintFunction) {
            syntaxTreeToJVMClass2(((SyntaxTree.PrintFunction) val).getProgram(), methodWriter, classWriter, className);
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

        if (addAddFunction) {
            MethodVisitor addMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#add", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            addMethodWriter.visitCode();
            addMethodWriter.visitVarInsn(ALOAD, 0);
            addMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            addMethodWriter.visitJumpInsn(IFEQ, label);
            addMethodWriter.visitVarInsn(ALOAD, 1);
            addMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            addMethodWriter.visitJumpInsn(IFEQ, label);
            addMethodWriter.visitVarInsn(ALOAD, 0);
            addMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            addMethodWriter.visitVarInsn(ALOAD, 1);
            addMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            addMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "add", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;", false);
            addMethodWriter.visitInsn(ARETURN);
            addMethodWriter.visitLabel(label);
            addMethodWriter.visitTypeInsn(NEW, "java/lang/StringBuilder");
            addMethodWriter.visitInsn(DUP);
            addMethodWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            addMethodWriter.visitVarInsn(ALOAD, 0);
            addMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            addMethodWriter.visitVarInsn(ALOAD, 1);
            addMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            addMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            addMethodWriter.visitInsn(ARETURN);
            addMethodWriter.visitMaxs(1, 1);
            addMethodWriter.visitEnd();
        }
        if (addSubFunction) {
            MethodVisitor subMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#sub", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            subMethodWriter.visitCode();
            subMethodWriter.visitVarInsn(ALOAD, 0);
            subMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            subMethodWriter.visitJumpInsn(IFEQ, label);
            subMethodWriter.visitVarInsn(ALOAD, 1);
            subMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            subMethodWriter.visitJumpInsn(IFEQ, label);
            subMethodWriter.visitVarInsn(ALOAD, 0);
            subMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            subMethodWriter.visitVarInsn(ALOAD, 1);
            subMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            subMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "subtract", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;", false);
            subMethodWriter.visitInsn(ARETURN);
            subMethodWriter.visitLabel(label);
            subMethodWriter.visitVarInsn(ALOAD, 0);
            subMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
            subMethodWriter.visitVarInsn(ALOAD, 1);
            subMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
            subMethodWriter.visitLdcInsn("");
            subMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            subMethodWriter.visitInsn(ARETURN);
            subMethodWriter.visitMaxs(1, 1);
            subMethodWriter.visitEnd();
        }
        if (addMulFunction) {
            MethodVisitor mulMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#mul", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            mulMethodWriter.visitCode();
            mulMethodWriter.visitVarInsn(ALOAD, 0);
            mulMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            Label label1 = new Label();
            Label label2 = new Label();
            Label labelLoop = new Label();
            Label labelLoop1 = new Label();
            Label labelLoopStart = new Label();
            Label labelLoopStart1 = new Label();
            mulMethodWriter.visitJumpInsn(IFEQ, label);
            mulMethodWriter.visitVarInsn(ALOAD, 1);
            mulMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            mulMethodWriter.visitJumpInsn(IFEQ, label);
            mulMethodWriter.visitVarInsn(ALOAD, 0);
            mulMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            mulMethodWriter.visitVarInsn(ALOAD, 1);
            mulMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "multiply", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;", false);
            mulMethodWriter.visitInsn(ARETURN);
            mulMethodWriter.visitLabel(label);
            mulMethodWriter.visitVarInsn(ALOAD, 0);
            mulMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            mulMethodWriter.visitJumpInsn(IFEQ, label1);
            mulMethodWriter.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mulMethodWriter.visitInsn(DUP);
            mulMethodWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mulMethodWriter.visitVarInsn(ASTORE, 2);
            mulMethodWriter.visitVarInsn(ALOAD, 0);
            mulMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            mulMethodWriter.visitVarInsn(ISTORE, 3);
            mulMethodWriter.visitLabel(labelLoopStart);
            mulMethodWriter.visitVarInsn(ILOAD, 3);
            mulMethodWriter.visitJumpInsn(IFLE, labelLoop);
            mulMethodWriter.visitVarInsn(ALOAD, 2);
            mulMethodWriter.visitVarInsn(ALOAD, 1);
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            mulMethodWriter.visitInsn(POP);
            mulMethodWriter.visitIincInsn(3, -1);
            mulMethodWriter.visitJumpInsn(GOTO, labelLoopStart);
            mulMethodWriter.visitLabel(labelLoop);
            mulMethodWriter.visitVarInsn(ALOAD, 2);
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mulMethodWriter.visitInsn(ARETURN);
            mulMethodWriter.visitLabel(label1);
            mulMethodWriter.visitVarInsn(ALOAD, 1);
            mulMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            mulMethodWriter.visitJumpInsn(IFEQ, label2);
            mulMethodWriter.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mulMethodWriter.visitInsn(DUP);
            mulMethodWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mulMethodWriter.visitVarInsn(ASTORE, 2);
            mulMethodWriter.visitVarInsn(ALOAD, 1);
            mulMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            mulMethodWriter.visitVarInsn(ISTORE, 3);
            mulMethodWriter.visitLabel(labelLoopStart1);
            mulMethodWriter.visitVarInsn(ILOAD, 3);
            mulMethodWriter.visitJumpInsn(IFLE, labelLoop1);
            mulMethodWriter.visitVarInsn(ALOAD, 2);
            mulMethodWriter.visitVarInsn(ALOAD, 0);
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            mulMethodWriter.visitInsn(POP);
            mulMethodWriter.visitIincInsn(3, -1);
            mulMethodWriter.visitJumpInsn(GOTO, labelLoopStart1);
            mulMethodWriter.visitLabel(labelLoop1);
            mulMethodWriter.visitVarInsn(ALOAD, 2);
            mulMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mulMethodWriter.visitInsn(ARETURN);
            mulMethodWriter.visitLabel(label2);
            mulMethodWriter.visitInsn(ACONST_NULL);
            mulMethodWriter.visitInsn(ARETURN);
            mulMethodWriter.visitMaxs(1, 1);
            mulMethodWriter.visitEnd();
        }
        if (addDivFunction) {
            MethodVisitor divMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#div", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            divMethodWriter.visitCode();
            divMethodWriter.visitVarInsn(ALOAD, 0);
            divMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            divMethodWriter.visitJumpInsn(IFEQ, label);
            divMethodWriter.visitVarInsn(ALOAD, 1);
            divMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            divMethodWriter.visitJumpInsn(IFEQ, label);
            divMethodWriter.visitVarInsn(ALOAD, 0);
            divMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            divMethodWriter.visitVarInsn(ALOAD, 1);
            divMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            divMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "divide", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;", false);
            divMethodWriter.visitInsn(ARETURN);
            divMethodWriter.visitLabel(label);
            divMethodWriter.visitInsn(ACONST_NULL);
            divMethodWriter.visitInsn(ARETURN);
            divMethodWriter.visitMaxs(1, 1);
            divMethodWriter.visitEnd();
        }
        if (addModFunction) {
            MethodVisitor divMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#div", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            divMethodWriter.visitCode();
            divMethodWriter.visitVarInsn(ALOAD, 0);
            divMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            divMethodWriter.visitJumpInsn(IFEQ, label);
            divMethodWriter.visitVarInsn(ALOAD, 1);
            divMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            divMethodWriter.visitJumpInsn(IFEQ, label);
            divMethodWriter.visitVarInsn(ALOAD, 0);
            divMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            divMethodWriter.visitVarInsn(ALOAD, 1);
            divMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            divMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "remainder", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;", false);
            divMethodWriter.visitInsn(ARETURN);
            divMethodWriter.visitLabel(label);
            divMethodWriter.visitInsn(ACONST_NULL);
            divMethodWriter.visitInsn(ARETURN);
            divMethodWriter.visitMaxs(1, 1);
            divMethodWriter.visitEnd();
        }
        if (addPowFunction) {
            MethodVisitor powMethodWriter = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "#pow", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            powMethodWriter.visitCode();
            powMethodWriter.visitVarInsn(ALOAD, 0);
            powMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            Label label = new Label();
            powMethodWriter.visitJumpInsn(IFEQ, label);
            powMethodWriter.visitVarInsn(ALOAD, 1);
            powMethodWriter.visitTypeInsn(INSTANCEOF, "java/math/BigDecimal");
            powMethodWriter.visitJumpInsn(IFEQ, label);
            powMethodWriter.visitVarInsn(ALOAD, 0);
            powMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            powMethodWriter.visitVarInsn(ALOAD, 1);
            powMethodWriter.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            powMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            powMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "pow", "(I)Ljava/math/BigDecimal;", false);
            powMethodWriter.visitInsn(ARETURN);
            powMethodWriter.visitLabel(label);
            powMethodWriter.visitInsn(ACONST_NULL);
            powMethodWriter.visitInsn(ARETURN);
            powMethodWriter.visitMaxs(1, 1);
            powMethodWriter.visitEnd();
        }
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
                    fields.add(((SyntaxTree.SetVariable) program).getVariableName());
                }
                methodVisitor.visitFieldInsn(PUTSTATIC, className, ((SyntaxTree.SetVariable) program).getVariableName(),
                        "Ljava/lang/Object;");
            }
        } else if (program instanceof SyntaxTree.Exit) {
            putVales(methodVisitor, ((SyntaxTree.Exit) program).getStatus(), classWriter, className);
            methodVisitor.visitTypeInsn(CHECKCAST, "java/math/BigDecimal");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigDecimal", "intValue", "()I", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
        } else if (program instanceof SyntaxTree.ExecuteValue) {
            putVales(methodVisitor, ((SyntaxTree.ExecuteValue) program).getValue(), classWriter, className);
        } else if (program instanceof SyntaxTree.If) {
            Label label = new Label();
            Label label1 = null;
            if (((SyntaxTree.If) program).getElseProgram() != null) {
                label1 = new Label();
            }
            putVales(methodVisitor, ((SyntaxTree.If) program).getCondition(), classWriter, className);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            methodVisitor.visitJumpInsn(IFEQ, label);
            syntaxTreeToJVMClass2(((SyntaxTree.If) program).getProgram(), methodVisitor, classWriter, className);
            if (((SyntaxTree.If) program).getElseProgram() != null)
                methodVisitor.visitJumpInsn(GOTO, label1);
            methodVisitor.visitLabel(label);
            if (((SyntaxTree.If) program).getElseProgram() != null) {
                syntaxTreeToJVMClass2(((SyntaxTree.If) program).getElseProgram(), methodVisitor, classWriter, className);
                methodVisitor.visitLabel(label1);
            }
        }
    }
}