package jbz.javadoc;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * class to generate a javadoc from the source code.
 */
public class Javadoc {
    /**
     * the source files to be used in the javadoc
     */
    public final List<File> sources;
    /**
     * files to be added to the '--class-path' parameter
     */
    public final List<File> classPaths;
    /**
     * additional parameter to be passed to the javadoc generator.
     */
    public final List<String> additionalParameters;
    /**
     * the working dir directory intended to be executed the command
     */
    public final File root;

    /**
     * @param sources:              the files to generate the javadoc.
     * @param classPaths:           add these class paths to the generator '--class-path' parameter.
     * @param additionalParameters: parameters to be added to the generator.
     *                              it can be a empty list.
     * @param root:                 the working dir directory.
     */
    public Javadoc(List<File> sources, List<File> classPaths, List<String> additionalParameters, File root) {
        sources.parallelStream().forEach(x -> Assert.assertTrue(x.exists()));
        classPaths.parallelStream().forEach(x -> Assert.assertFalse(x.getName().endsWith("*")));

        this.sources = sources;
        this.classPaths = classPaths;
        this.additionalParameters = additionalParameters;

        this.root = root;
    }

    /**
     * generate the javadoc.
     */
    public void generate() throws IOException, InterruptedException {
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(this.root);

        CommandLine cl = new CommandLine("javadoc");

        cl.addArguments(new String[]{"--class-path", this.classPaths_cmdComplaint()});

        for (String parameter : this.additionalParameters) {
            cl.addArguments(parameter);
        }

        for (File file : this.sources) {
            cl.addArgument(file.getAbsolutePath());
        }

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);
    }

    /**
     * format the classPaths parameter to return a cmd compatible (javadoc command) string for the class path
     * the string uses ';' (dot and comma) as delimiter.
     */
    public String classPaths_cmdComplaint() {
        StringBuilder classPath = new StringBuilder();
        for (File file : this.classPaths) {
            if (classPath.length() > 0) classPath.append(";");
            classPath.append(file.getAbsolutePath());
        }
        return classPath.toString();
    }
}
