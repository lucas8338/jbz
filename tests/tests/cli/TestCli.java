package tests.cli;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import tests.TempDirGenerator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * contains tests for the 'cli'.
 * */
public class TestCli {
    public File tmpDir = new TempDirGenerator().generate();

    public picocli.CommandLine commandLine = new CommandLine(new jbz.frontEnd.Cli(this.tmpDir));

    public TestCli() throws IOException {}

    @BeforeClass
    public void printTmpDir(){
        System.out.println("tmpdir at: " + tmpDir.getAbsolutePath());
    }

    @Test
    public void test_init(){
        try {
            commandLine.execute("init");
        }catch (Exception e){
            Assert.fail();
        }
    }

    @Test(dependsOnMethods = "test_init")
    public void test_compile(){
        try {
            commandLine.execute("compile");
            commandLine.execute("compile", "--subProject", "tests");
        }catch (Exception e){
            Assert.fail();
        }
        Assert.assertTrue(
                new File(tmpDir.getAbsolutePath() + "/srcOut/compile/helloWorld/HelloWorld.class").exists()
        );
        Assert.assertTrue(
                new File(tmpDir.getAbsolutePath() + "/testsOut/compile/tests/TestExample.class").exists()
        );
    }

    @Test(dependsOnMethods = "test_compile")
    public void test_javadoc(){
        try {
            commandLine.execute("javadoc");
            commandLine.execute("javadoc", "--subProject", "tests");
        }catch (Exception e){
            Assert.fail();
        }
        Assert.assertTrue(
                new File(tmpDir.getAbsolutePath() + "./srcOut/javadoc/index.html").exists(),
                "the javadoc command will create a 'index.html' file in a directory. this file" +
                        "must exists."
        );
        Assert.assertTrue(
                new File(tmpDir.getAbsolutePath() + "./testsOut/javadoc/index.html").exists(),
                "the javadoc command will create a 'index.html' file in a directory. this file" +
                        "must exists."
        );
    }

    @Test(dependsOnMethods = "test_compile")
    public void test_test() throws ParserConfigurationException, IOException, SAXException {
        try {
            // delete all files in the temp dir. this is required by a clean test.
            for ( File file: ListUtils.emptyIfNull(Arrays.stream(tmpDir.listFiles()).toList()) ){
                FileUtils.forceDelete(file);
            }
            commandLine.execute("init");
            commandLine.execute("test");
        }catch (Exception e){
            Assert.fail();
        }
        Assert.assertTrue(
                new File(this.tmpDir, "testsOut/etc/testngOutput/index.html").exists()
        );

        DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document all = builder.parse(
                new File(this.tmpDir, "testsOut/etc/testngOutput/testng-results.xml")
        );

        Node testngResultTestMethod0 = all.getElementsByTagName("test-method").item(0);

        Assert.assertEquals(
                testngResultTestMethod0.getAttributes().getNamedItem("name").getNodeValue(),
                "testExample"
        );

        Assert.assertEquals(
                testngResultTestMethod0.getAttributes().getNamedItem("status").getNodeValue(),
                "PASS"
        );
    }

    @Test(dependsOnMethods = "test_test")
    public void test_jar() throws IOException {
        try {
            commandLine.execute("compile");
            commandLine.execute("jar");
        }catch (Exception e){
            Assert.fail();
        }

        File jarLocation = new File(this.tmpDir, "srcOut/artifact/helloWorldProject-0.0.1.0.jar");

        Assert.assertTrue(
                jarLocation.exists(),
                "seems the jar was not created"
        );

        // here to test if the "Class-Path" in the manifest of the jar
        // is right will move the generated jar to the root then
        // will run it and it needs to work.

        File newJarLocation = new File(
                this.tmpDir,
                jarLocation.getName()
        );

        Files.copy(jarLocation.toPath(), newJarLocation.toPath());

        // backup the current print stream.
        PrintStream defaultPrintStream = System.out;

        ByteArrayOutputStream outputSave = new ByteArrayOutputStream();

        OutputStream outStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputSave.write(b);
            }
        };

        PrintStream printStream = new PrintStream(outStream, true);

        System.setOut(printStream);

        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(this.tmpDir);
        org.apache.commons.exec.CommandLine cl = new org.apache.commons.exec.CommandLine("java");

        cl.addArgument("-jar");
        cl.addArgument(newJarLocation.getName());

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);

        Assert.assertTrue(
                outputSave.toString().contains("hello world")
        );

        Assert.assertTrue(
                outputSave.toString().contains("the world ended in 2010.")
        );

        System.setOut(defaultPrintStream);
    }

    @Test(dependsOnMethods = "test_jar")
    public void test_fatJar() throws IOException {
        try {
            commandLine.execute("compile");
            commandLine.execute("fatJar");
        }catch (Exception e){
            Assert.fail();
        }

        File jarLocation = new File(this.tmpDir, "srcOut/artifact/helloWorldProject-0.0.1.0-withDependencies.jar");

        Assert.assertTrue(
            jarLocation.exists(),
                "seems the jar was not created"
        );

        // backup the current print stream.
        PrintStream defaultPrintStream = System.out;

        ByteArrayOutputStream outputSave = new ByteArrayOutputStream();

        OutputStream outStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputSave.write(b);
            }
        };

        PrintStream printStream = new PrintStream(outStream, true);

        System.setOut(printStream);

        Executor executor = new DefaultExecutor();
        org.apache.commons.exec.CommandLine cl = new org.apache.commons.exec.CommandLine("java");

        cl.addArgument("-jar");
        cl.addArgument(jarLocation.getAbsolutePath());

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);

        Assert.assertTrue(
                outputSave.toString().contains("hello world")
        );

        Assert.assertTrue(
                outputSave.toString().contains("the world ended in 2010.")
        );

        System.setOut(defaultPrintStream);
    }

    @Test(dependsOnMethods = {"test_jar", "test_fatJar"})
    public void test_clean() throws IOException {
        commandLine.execute("clean");

        File dirCompileOutput = new File(
                this.tmpDir,
                "srcOut/compile"
        );
        Assert.assertEquals(Files.list(dirCompileOutput.toPath()).toList().size(), 0);

        File dirJavadocOutput = new File(
                this.tmpDir,
                "srcOut/javadoc"
        );
        Assert.assertEquals(Files.list(dirJavadocOutput.toPath()).toList().size(), 0);

        File jarPath = new File(
                this.tmpDir,
                "srcOut/artifact/helloWorldProject-0.0.1.0.jar"
        );
        Assert.assertFalse(jarPath.exists());

        File jarPathWithDependencies = new File(
                this.tmpDir,
                "srcOut/artifact/helloWorldProject-0.0.1.0-withDependencies.jar"
        );
        Assert.assertFalse(jarPathWithDependencies.exists());
    }

    @Test(dependsOnMethods = {"test_compile"})
    public void test_run(){
        PrintStream defaultPrintStrem = System.out;

        ByteArrayOutputStream captureString = new ByteArrayOutputStream();

        OutputStream sendStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                captureString.write(b);
            }
        };

        PrintStream capturePrintStrem = new PrintStream(sendStream);

        System.setOut(capturePrintStrem);

        commandLine.execute("run");

        Assert.assertTrue(
                captureString.toString().contains("hello world")
        );

        Assert.assertTrue(
                captureString.toString().contains("the world ended in 2010.")
        );

        System.setOut(defaultPrintStrem);
    }
}
