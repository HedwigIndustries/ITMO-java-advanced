package info.kgeorgiy.ja.kadyrov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link JarImpler}.
 * This class implements the interface or the jar file for interface's implementation.
 *
 * @author Kadyrov Rustam.
 */

public class Implementor implements JarImpler {

    /**
     * Implementing class header template.
     */
    private static final String CLASS_TEMPLATE = "public class %s implements %s {";
    /**
     * Method header template.
     */
    private static final String METHOD_TEMPLATE = "\tpublic %s %s(%s) {";
    /**
     * Return statement template.
     */
    private static final String RETURN_TEMPLATE = "\t\treturn%s;";
    /**
     * Suffix, which append to implementing class name.
     */
    private static final String IMPL = "Impl";
    /**
     * Resolution of file.
     */
    private static final String RESOLUTION_JAVA = ".java";
    /**
     * Resolution of implementing class.
     */
    private static final String RESOLUTION_CLASS = ".class";
    /**
     * Standard encoding.
     */
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * {@inheritDoc}
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Invalid input. Arguments can't be null.");
        }
        if (!token.isInterface()) {
            throw new ImplerException("You can implement only interfaces.");
        }
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Interface is private.");
        }
        Path filePath = getCorrectFilePath(token, root);
        createDirs(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, UTF_8)) {
            try {
                writePackage(token, writer);
                writeClassHeader(token, writer);
                for (Iterator<Method> it = Arrays.stream(token.getMethods()).filter(x -> !x.isDefault()).iterator(); it.hasNext(); ) {
                    writeMethod(writer, it.next());
                }
                writer.write("}");
            } catch (IOException e) {
                throw new ImplerException("An error occurs while writing a file." + " " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new ImplerException("An error occurs while opening a file." + " " + e.getMessage(), e);
        }
    }

    /**
     * Create directories if they do not exist in the given path.
     *
     * @param file path to file.
     * @throws ImplerException if  I/O errors occurs.
     */
    private void createDirs(Path file) throws ImplerException {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot creating directories." + " " + e.getMessage());
        }
    }

    /**
     * Changes the file path to the correct one.
     *
     * @param token reflection of implementing interface.
     * @param root  path to implementing file.
     * @return path to file.
     */
    private Path getCorrectFilePath(Class<?> token, Path root) {
        return resolvePath(token, root, RESOLUTION_JAVA);
    }

    /**
     * Changes the class path to the correct one.
     *
     * @param token reflection of implementing interface.
     * @param root  path to implementing class.
     * @return path to class.
     */
    private Path getCorrectClassPath(Class<?> token, Path root) {
        return resolvePath(token, root, RESOLUTION_CLASS);
    }

    /**
     * Resolves path, transforms package class to path and appends string to the end.
     *
     * @param token reflection of implementing interface.
     * @param root  path to resolve.
     * @param end   string, which append to path.
     * @return path to file.
     */
    private Path resolvePath(Class<?> token, Path root, String end) {
        return root.resolve(replace(token, File.separator)).resolve(getClassName(token) + end);
    }

    /**
     * Replaces the dot to your character in the class name.
     *
     * @param token       reflection of implementing interface.
     * @param replacement character to replace.
     * @return path with replacement.
     */
    private String replace(Class<?> token, String replacement) {
        return token.getPackageName().replace(".", replacement);
    }

    /**
     * Writes package of implementing class.
     *
     * @param token  reflection of implementing interface.
     * @param writer buffered writer.
     * @throws IOException if an I/O error occurs.
     */
    private void writePackage(Class<?> token, BufferedWriter writer) throws IOException {
        Package classPackage = token.getPackage();
        if (classPackage != null) {
            writer.write(classPackage + ";");
            writer.newLine();
            writer.newLine();
        }
    }

    /**
     * Writes header string of implementing class.
     *
     * @param token  reflection of implementing interface.
     * @param writer buffered writer.
     * @throws IOException if an I/O error occurs.
     */
    private void writeClassHeader(Class<?> token, BufferedWriter writer) throws IOException {
        writer.write(String.format(CLASS_TEMPLATE, getClassName(token), token.getCanonicalName()));
        writer.newLine();
    }

    /**
     * Gets implementing class name.
     *
     * @param token reflection of implementing interface.
     * @return class name.
     */
    private String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL;
    }


    /**
     * Writes method of implementing class.
     *
     * @param writer buffered writer.
     * @param m      method of implementing class.
     * @throws IOException if an I/O error occurs.
     */
    private void writeMethod(BufferedWriter writer, Method m) throws IOException {
        writeMethodHeader(writer, m);
        writeReturn(writer, m);
        writer.write("\t" + "}");
        writer.newLine();
        writer.newLine();
    }

    /**
     * Writes header of implementing method.
     *
     * @param m      method of implementing class.
     * @param writer buffered writer.
     * @throws IOException if an I/O error occurs.
     */
    private void writeMethodHeader(BufferedWriter writer, Method m) throws IOException {
        String returnType = m.getReturnType().getCanonicalName();
        String methodName = m.getName();
        Parameter[] methodParameters = m.getParameters();
        StringBuilder methodParametersSB = parametersToSB(methodParameters);
        writer.write(String.format(METHOD_TEMPLATE, returnType, methodName, methodParametersSB));
        writer.newLine();
    }

    /**
     * Gets arguments of implementing method.
     *
     * @param methodParameters parameters of method of implementing class.
     * @return parameters of method in format string.
     */
    private StringBuilder parametersToSB(Parameter[] methodParameters) {
        StringBuilder methodParametersSB = new StringBuilder();
        int pos = 0;
        for (Parameter p : methodParameters) {
            String methodParameter;
            if (pos == methodParameters.length - 1) {
                methodParameter = (p.getType().getCanonicalName() + " " + p.getName());
            } else {
                methodParameter = (p.getType().getCanonicalName() + " " + p.getName() + "," + " ");
            }
            pos++;
            methodParametersSB.append(methodParameter);
        }
        return methodParametersSB;
    }

    /**
     * Writes return statement of method.
     *
     * @param m      method of implementing class.
     * @param writer buffered writer.
     * @throws IOException if an I/O error occurs.
     */
    private void writeReturn(BufferedWriter writer, Method m) throws IOException {
        String returnType;
        if (!m.getReturnType().isPrimitive()) {
            returnType = String.format(RETURN_TEMPLATE, " " + "null");
        } else if (m.getReturnType().equals(boolean.class)) {
            returnType = String.format(RETURN_TEMPLATE, " " + "false");
        } else if (m.getReturnType().equals(void.class)) {
            returnType = String.format(RETURN_TEMPLATE, "");
        } else {
            returnType = String.format(RETURN_TEMPLATE, " " + "0");
        }
        writer.write(returnType);
        writer.newLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Invalid input. Arguments can't be null.");
        }
        createDirs(jarFile);
        Path tempDir = getPathOfTempDir(jarFile);
        implement(token, tempDir);
        compileFiles(token, tempDir, getCorrectFilePath(token, tempDir).toString());
        createJar(token, tempDir, jarFile);
    }

    /**
     * Creates temporary directory and get path to itself.
     *
     * @param jarFile path to jar file.
     * @return path to temporary directory.
     * @throws ImplerException if occurs problems with create temporally directory.
     */
    private Path getPathOfTempDir(Path jarFile) throws ImplerException {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tempDir");
        } catch (IOException e) {
            throw new ImplerException("Can't create temporary directory." + " " + e.getMessage());
        }
        return tempDir;
    }

    /**
     * Generates manifest for the jar file.
     *
     * @return manifest
     */
    private Manifest generateManifest() {
        return new Manifest();
    }

    /**
     * Compiles files.
     *
     * @param token reflection of implementing interface.
     * @param root  path to files to compile.
     * @param file  list of files to compile.
     * @throws ImplerException if occurs troubles with compile file.
     */
    private static void compileFiles(Class<?> token, final Path root, final String file) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath.");
        }
        final String classpath = root + File.pathSeparator + getClassPath(token);
        final int exitCode = compiler.run(null, null, null, file, "-cp", classpath, "-encoding", UTF_8.name());
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code:" + exitCode);
        }
    }

    /**
     * Gets class path.
     *
     * @param token reflection of implementing interface.
     * @return path
     */
    private static String getClassPath(Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a jar file, convert files, manifest.
     *
     * @param token   reflection of implementing interface.
     * @param tempDir path to temporary directory with converting files.
     * @param jarFile path to jar file.
     * @throws ImplerException if occurs troubles with creating jar file.
     */
    private void createJar(Class<?> token, Path tempDir, Path jarFile) throws ImplerException {
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), generateManifest())) {
            try {
                String name = replace(token, "/") + "/" + getClassName(token) + RESOLUTION_CLASS;
                jarOutputStream.putNextEntry(new ZipEntry(name));
                Files.copy(getCorrectClassPath(token, tempDir), jarOutputStream);
            } catch (IOException e) {
                throw new ImplerException("Can't put class file." + " " + e.getMessage());
            }
        } catch (IOException e) {
            throw new ImplerException("Can't create jar." + " " + e.getMessage());
        }
    }

    /**
     * Implements interfaces using the implementor or jar-implementor, witch depends on the passed arguments.
     * If first argument equals "--jar", second argument should be the token class, the third argument should be the path for jar-file.
     * Default: First argument should be the token class, the second argument should be the path for implementation class.
     *
     * @param args arguments
     */

    public static void main(String[] args) {
        if (args == null || args.length > 3 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect input. Arguments can't be null.");
            return;
        }
        Implementor impl = new Implementor();
        if (args[0].equals("--jar")) {
            if (args[2] != null) {
                try {
                    impl.implementJar(args[1].getClass(), Path.of(args[2]));
                } catch (ImplerException e) {
                    System.err.println(e.getMessage());
                }
            } else System.err.println("Incorrect input. Arguments can't be null.");
        } else {
            try {
                impl.implement(args[0].getClass(), Path.of(args[1]));
            } catch (ImplerException e) {
                System.err.println(e.getMessage());
            }
        }

    }
}
