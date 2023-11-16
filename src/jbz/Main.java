package jbz;

import org.testng.Assert;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

/**
 * the class with the 'main' method of the package.
 */
public class Main {
    public static void main(String... args) throws IOException {
        File workingDir = new File(".").getAbsoluteFile();
        CommandLine cl = new CommandLine(new jbz.frontEnd.Cli(workingDir));
        int exitCode = cl.execute(args);
        Assert.assertEquals(exitCode, 0);
    }
}
