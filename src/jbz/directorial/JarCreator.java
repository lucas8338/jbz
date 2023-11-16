package jbz.directorial;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * class to generate a jar file.
 */
public class JarCreator {
    public final File tmpdir = new File(
            System.getProperty("java.io.tmpdir") + "/jarCreator_tmpdir_" + (int) (Math.random() * 1000000)
    );

    public final File tmpdir_assembly = new File(
            tmpdir.getPath() + "/assembly"
    );

    public final File tmpdir_jar = new File(
            tmpdir.getPath() + "/jar"
    );

    public final File tmpdir_manifest = new File(
            tmpdir.getPath() + "/manifest.txt"
    );

    public JarCreator() {
        Assert.assertTrue(this.tmpdir.mkdir());
        Assert.assertTrue(this.tmpdir_assembly.mkdir());
        Assert.assertTrue(this.tmpdir_jar.mkdir());
    }

    /**
     * create a manifest
     */
    public void createManifest(Map<String, String> manifest) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String key : manifest.keySet()) {
            sb.append(key);
            sb.append(": ");
            sb.append(manifest.get(key));
            sb.append("\n");
        }

        FileOutputStream manifestOStream = new FileOutputStream(this.tmpdir_manifest);
        manifestOStream.write(sb.toString().getBytes());
        manifestOStream.close();
    }

    public List<File> extractJar(File jarPath) throws IOException {
        Assert.assertTrue(jarPath.isFile());

        File tmpdir = new File(
                System.getProperty("java.io.tmpdir") + "/tmpExtractJar_" + (int) (Math.random() * 1000000)
        );

        Assert.assertTrue(tmpdir.mkdir());

        File tmpjar = new File(
                tmpdir, "tempjarToExtract.jar"
        );

        Files.copy(jarPath.toPath(), tmpjar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpdir);
        CommandLine cl = new CommandLine("jar");

        cl.addArgument("xf");

        cl.addArgument(tmpjar.getName());

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);

        Assert.assertTrue(tmpjar.delete());

        return Arrays.stream(tmpdir.listFiles()).toList();
    }

    /**
     * copy all files in the supplied list to the assembly directory.
     */
    public void copyFilesToAssembly(List<File> files) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                FileUtils.copyToDirectory(file, this.tmpdir_assembly);
            } else {
                File newFile = new File(
                        tmpdir_assembly,
                        file.getName()
                );
            }
        }
    }

    /**
     * do all stuffs and create the .jar in the tmp dir directory
     *
     * @return the path to the create jar file.
     */
    public void createJar(File jarPath) throws IOException {
        ListUtils.emptyIfNull(Arrays.stream(this.tmpdir_jar.listFiles()).toList()).parallelStream()
                .forEach(x -> {
                    try {
                        List<File> extracteds = this.extractJar(x);
                        this.copyFilesToAssembly(extracteds);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpdir_assembly);
        CommandLine cl = new CommandLine("jar");

        if (this.tmpdir_manifest.exists()) {
            cl.addArgument("cmf");
            cl.addArgument(this.tmpdir_manifest.getAbsolutePath());
        } else {
            cl.addArgument("cf");
        }

        cl.addArgument(jarPath.getAbsolutePath());

        cl.addArguments(
                Arrays.stream(this.tmpdir_assembly.listFiles())
                        .filter(x -> !x.getName().equals("META-INF"))
                        .map(x -> x.getName())
                        .toArray(String[]::new)
        );

        int exitCode = executor.execute(cl);

        Assert.assertEquals(exitCode, 0);
    }
}
