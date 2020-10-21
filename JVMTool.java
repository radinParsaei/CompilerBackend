import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class JVMTool {
    private void putVals(MethodVisitor methodWriter, ValueBase... vals) {
        for (ValueBase val : vals) {
            if (val instanceof SyntaxTree.Number) {
                methodWriter.visitTypeInsn(NEW, "java/math/BigDecimal");
                methodWriter.visitInsn(DUP);
                methodWriter.visitLdcInsn(val.toString());
                methodWriter.visitMethodInsn(INVOKESPECIAL, "java/math/BigDecimal", "<init>", "(Ljava/lang/String;)V", false);
            } else if (val instanceof SyntaxTree.Text) {
                methodWriter.visitLdcInsn(val.getData());
            } else if (val instanceof SyntaxTree.Boolean) {
                if ((boolean)val.getData()) {
                    methodWriter.visitInsn(ICONST_1);
                } else {
                    methodWriter.visitInsn(ICONST_0);
                }
                methodWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if (val instanceof SyntaxTree.Null) {
                methodWriter.visitInsn(ACONST_NULL);
            }
        }
    }

    public byte[] SyntaxTreeToVMByteCode(ProgramBase program) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_8, ACC_PUBLIC, "Test", null, "java/lang/Object", null);
        SyntaxTreeToVMByteCode1(program, classWriter);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    public void SyntaxTreeToVMByteCode1(ProgramBase program, ClassWriter classWriter) {
        MethodVisitor mainMethodWriter = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mainMethodWriter.visitCode();
        Label methodStart = new Label();
        Label methodEnd = new Label();
        mainMethodWriter.visitLabel(methodStart);

        SyntaxTreeToVMByteCode2(program, mainMethodWriter);
        mainMethodWriter.visitInsn(RETURN);
        mainMethodWriter.visitLabel(methodEnd);
        mainMethodWriter.visitEnd();
        mainMethodWriter.visitMaxs(1, 1);
    }

    public void SyntaxTreeToVMByteCode2(ProgramBase program, MethodVisitor methodVisitor) {
        if (program instanceof SyntaxTree.Programs) {
            for (ProgramBase program1 : ((SyntaxTree.Programs) program).getPrograms()) {
                SyntaxTreeToVMByteCode2(program1, methodVisitor);
            }
        } else if (program instanceof SyntaxTree.Print) {
            ValueBase[] args = ((SyntaxTree.Print) program).getArgs();
            for (int i = 0; i < args.length; i++) {
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                putVals(methodVisitor, args[i]);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V", false);
                if (i < args.length - 1) {
                    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    putVals(methodVisitor, (((SyntaxTree.Print) program).getSeparator()));
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V", false);
                }
            }
        }
    }
}