package jbz.run;

import jbz.classPathStdFormatter.ClassPathStdFormatter;
import jbz.projectDescription.Parser;
import jbz.projectDescription.enums.SubProjects_enum;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * class to run java code
 * */
public class Run {
    public final Path root;
    public final String target;

    public Run(Path root, String target){
        this.root = root;
        this.target = target;
    }

    private Parser getProjectDescription() throws IOException {
        Parser projectDescriptionParser = new Parser(Paths.get(this.root.toString(), "projectDescription.yaml").toFile());
        return projectDescriptionParser;
    }

    private CommandLine runner() throws IOException {
        CommandLine cl = new CommandLine("java");
        cl.addArgument("--class-path");

        ClassPathStdFormatter classPathStdFormatter = new ClassPathStdFormatter(
                this.getProjectDescription().getClassPath(SubProjects_enum.main)
        );

        String delimitedStringClassPaths =
                (String) this.getProjectDescription().getInternal(SubProjects_enum.main).get("compileOutput") + ";" +
                classPathStdFormatter.delimitedString();

        cl.addArgument(delimitedStringClassPaths);

        cl.addArgument(this.target);

        return cl;
    }

    private org.apache.commons.exec.Executor executor(){
        org.apache.commons.exec.Executor executor = new DefaultExecutor();
        return executor;
    }

    public void run() throws IOException {
        CommandLine cl = this.runner();
        Executor executor = this.executor();
        executor.setWorkingDirectory(this.root.toFile());
        int exitCode = executor.execute(cl);
        Assert.assertEquals(
                exitCode,
                0
        );
    }
}
