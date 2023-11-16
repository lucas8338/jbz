package jbz.directorial;

import jbz.util.BasicUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * class which is used to initialize the project at given directory.
 */
public class DefaultStructure {
    /**
     * store the root location of the project
     */
    public final File projectLocation;

    public DefaultStructure(File projectLocation) {
        Assert.assertFalse(projectLocation.isFile());
        if (!projectLocation.exists()) {
            Assert.assertTrue(
                    projectLocation.mkdirs(),
                    "was not possible to create the directory for the project."
            );
        }
        this.projectLocation = projectLocation;
    }

    /**
     * copy the content of a directory located in the 'resources/defaultStructure'.
     *
     * @param dirName the name of the directory to copy to.
     *                the 'name' must be a name of a directory in 'resources/defaultStructure'.
     */
    public void copyResourceDefaultStructureDir(String dirName) throws URISyntaxException, IOException {
        File dir =
                new File(
                        this.getClass().getClassLoader().getResource("defaultStructure/" + dirName).toURI()
                );

        Assert.assertTrue(dir.isDirectory());

        BasicUtil.copyDirectoryContents(dir, this.projectLocation, true, false);
    }

    /**
     * delete recursively something with a specific name.
     *
     * @param name:     the name of the file or directory to remove.
     * @param fileOnly: only will delete if is a file. not a directory.
     */
    public void deleteRecursive(String name, boolean fileOnly) {
        List<File> files = FileUtils.listFiles(this.projectLocation, null, true).stream()
                .filter(x -> x.getName().equals(name))
                .toList();
        if (fileOnly) {
            files.stream().filter(x -> x.isFile()).forEach(x -> x.delete());
        } else {
            files.stream().forEach(x -> FileUtils.deleteQuietly(x));
        }
    }

}
