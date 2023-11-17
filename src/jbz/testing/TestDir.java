package jbz.testing;

import jbz.util.BasicUtil;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.testng.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * a class to run all tests knowing a directory which contain the tests or packages.
 */
public class TestDir {

    /**
     * the xml string of a testng suite to test all classes recursively in the classpath.
     */
    public final String testAll =
            """
                    <suite name="suite" verbose="1">
                        <test name="allPackages">
                    		<packages>
                    			<package name="tests.*"/>
                    		</packages>
                        </test>
                    </suite>
                    """;
    /**
     * the directory containing the compiled of 'testsSrcDir'.
     * this directory must contains the '.class' files. can have directory (packages) with '.class'...
     */
    public File testsClassDir;
    /**
     * contains the suites to be run in execution.
     */
    public List<String> suites = new ArrayList<String>(List.of(this.testAll));

    public TestDir(File testsClassDir) throws URISyntaxException {
        Assert.assertTrue(testsClassDir.isDirectory());
        this.testsClassDir = testsClassDir;
    }

    /**
     * writes the suites to the temporary directory.
     *
     * @return a list with the suites locations ordered by supplied order.
     */
    private List<File> writeSuites() throws IOException {
        List<File> suitesFiles = new ArrayList<File>();
        for (String suite : this.suites) {
            File path = new File(
                    System.getProperty("java.io.tmpdir") + "/suite" + (int) (Math.random() * 1000000) + ".xml"
            );
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(suite.getBytes());
            fos.close();
            suitesFiles.add(path);
        }
        return suitesFiles;
    }

    /**
     * run the tests
     *
     * @param additionalClassPaths: the additional class paths to be added to the 'java' command.
     *                              look the parameter 'testClassDir' is already added to the class path.
     * @param additionalArgs:       additional arguments. this must not contains the 'class path' arguments.
     * @param testngArgs:           the arguments of testng passed after the suites declaration.
     */
    public void run(List<File> additionalClassPaths, List<String> additionalArgs, List<String> testngArgs, File workingDir) throws IOException {
        List<String> classPaths = new ArrayList<String>();

        classPaths.add(this.testsClassDir.getAbsolutePath());

        classPaths.addAll(
                additionalClassPaths.stream().map(x -> x.getAbsolutePath()).toList()
        );

        List<String> testngSuites = new ArrayList<String>();
        testngSuites.addAll(
                this.writeSuites().stream().map(x -> x.getAbsolutePath()).toList()
        );

        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(workingDir);
        CommandLine cl = new CommandLine("java");

        String classPaths_stringDelimited = BasicUtil.listToDelimitedString(classPaths, ";");

        cl.addArgument("--class-path");
        cl.addArgument(classPaths_stringDelimited);

        for (String additionalArg : additionalArgs) {
            cl.addArguments(additionalArg);
        }

        cl.addArgument("org.testng.TestNG");

        cl.addArguments(testngSuites.toArray(new String[0]));

        for (String testngArg : testngArgs) {
            cl.addArguments(testngArg);
        }

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);
    }
}
