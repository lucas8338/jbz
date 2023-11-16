package jbz.util;

import org.apache.commons.collections4.ListUtils;
import org.testng.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class BasicUtil {
    /**
     * transform a list into a delimited string.
     */
    public static String listToDelimitedString(List<String> x, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String element : x) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(element);
        }
        return sb.toString();
    }

    /**
     * class to copy the content of a directory to another one.
     */
    public static void copyDirectoryContents(File source, File destination, boolean recursive, boolean overwrite) throws IOException {
        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(destination.isDirectory());

        List<File> sourceFiles = ListUtils.emptyIfNull(Arrays.stream(source.listFiles()).toList());
        List<File> destinationFiles = ListUtils.emptyIfNull(Arrays.stream(destination.listFiles()).toList());

        for (File file : sourceFiles) {
            String fileName = file.getName();
            // check if the file already exits in the destination.
            if (destinationFiles.parallelStream().filter(x -> x.isFile()).anyMatch(x -> x.getName().equals(fileName))) {
                if (!overwrite) {
                    continue;
                }
            }

            // start the recursion or copy the file to the destination.
            if (file.isDirectory() && recursive) {
                File newDestination = new File(destination.getPath() + "/" + file.getName());
                newDestination.mkdir();
                copyDirectoryContents(file, newDestination, true, overwrite);
            } else {
                FileOutputStream destination_stream = new FileOutputStream(destination.getPath() + "/" + file.getName());
                Files.copy(file.toPath(), destination_stream);
                destination_stream.close();
            }
        }
    }
}
