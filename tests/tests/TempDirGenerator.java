package tests;

import org.testng.Assert;

import java.io.File;

/**
 * class to generate a temporary directory where to run tests
 * */
public class TempDirGenerator {
    public final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    public File generate(){
        File root = new File(tmpDir.getPath() + "/" + "tmpDir_testsJbz_" + (int) (Math.random() * 1000000)).getAbsoluteFile();
        Assert.assertTrue(root.mkdir());
        return root;
    }
}
