import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import static org.objectweb.asm.Opcodes.*;

public final class AsmExample {
    public static void main(String[] args) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS |
                ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "HelloWorld", null,
                "java/lang/Object", null);

        MethodVisitor constructor =
                cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        //call super()
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "java/lang/Object", "<init>", "()V");
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main", "([Ljava/lang/String;)V", null, null);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System",
                "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Hello, World!");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        byte[] bytecode = cw.toByteArray();
        File file = new File("HelloWorld.class");
        OutputStream os = new FileOutputStream(file);
        os.write(bytecode);
        os.close();

        ByteArrayClassLoader loader = new ByteArrayClassLoader();
        Class<?> test = loader.defineClass("HelloWorld", bytecode);
        try {
            test.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

/**
 * A class loader with the ability to load class from bytecode arrays.
 */
final class ByteArrayClassLoader extends ClassLoader {
    // ---------------------------------------------------------------------------------------------

    static {
        registerAsParallelCapable();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Default reusable instance of the class loader.
     */
    public static final ByteArrayClassLoader INSTANCE = new ByteArrayClassLoader();

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a class' (dot-separated) binary name and the bytecode array, load the class
     * and return the corresponding {@link Class} object.
     */
    public Class<?> defineClass(String binaryName, byte[] bytecode) {
        return defineClass(binaryName, bytecode, 0, bytecode.length);
    }

    // ---------------------------------------------------------------------------------------------
}