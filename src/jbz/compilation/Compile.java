package jbz.compilation;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * class to do the compilation of a java program.
 */
public class Compile {
    /**
     * the files to be compiled.
     */
    public final List<File> filesToCompile;
    /**
     * the classPaths to be added, this class dont handle the java wildcard '*' (asterisk);
     * so all files in this parameter must exist.
     */
    public final List<File> classPath;
    /**
     * additional parameters to be passed to the compiler.
     */
    public final List<String> additionalParameters;
    /**
     * store the class path parsed delimited by dot and comma.
     */
    public String classPathString;
    /**
     * store the location to be the root for command line command.
     */
    public File root = new File(".").getAbsoluteFile();

    /**
     * @param filesToCompile:       the files to be passed at end of the compilation generator.
     * @param classPath:            files to be added to the '--class-path' parameter of the compilation generator.
     * @param additionalParameters: parameters to be passed to the generator.
     *                              it can be a empty list.
     * @param root:                 the working dir location.
     */
    public Compile(List<File> filesToCompile, List<File> classPath, List<String> additionalParameters, File root) {
        filesToCompile.parallelStream().forEach(x -> Assert.assertTrue(x.exists()));
        classPath.parallelStream().forEach(x -> Assert.assertFalse(x.getName().endsWith("*")));

        this.filesToCompile = filesToCompile;
        this.classPath = classPath;
        this.additionalParameters = additionalParameters;

        Assert.assertTrue(root.isDirectory());
        this.root = root.getAbsoluteFile();
    }

    /**
     * construct a formatted string compatible with java compiler (javac --class-path);
     */
    public void classPath_stringBuild() {
        StringBuilder classPathString = new StringBuilder();
        for (File file : this.classPath) {
            if (classPathString.length() > 0) classPathString.append(";");
            classPathString.append(file.getAbsolutePath());
        }
        this.classPathString = classPathString.toString();
    }

    /**
     * do the compilation.
     */
    public void compile() throws IOException, InterruptedException {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(this.root);

        // 'cc' means 'Compile Command'
        CommandLine cc = new CommandLine("javac");

        cc.addArguments(
                new String[]{"--class-path", this.classPathString}
        );

        for (String parameter : this.additionalParameters) {
            cc.addArguments(parameter);
        }

        for (File file : this.filesToCompile) {
            cc.addArgument(file.getAbsolutePath());
        }

        int exitCode = executor.execute(cc);

        Assert.assertEquals(exitCode, 0);
    }
}
