package jbz.projectDescription.classPath;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * a class to parse the string format of a class path, returning a list of file.
 * one of the purpose of this class is to work with the asterisk '*' (java wildcard).
 */
public class ClassPathStringParser {
    /**
     * the string path. which is a location to be included to a 'classpath' argument of java.
     */
    String stringPath;

    public ClassPathStringParser(String pathString) {
        this.stringPath = pathString;
    }

    /**
     * parse the supplied string and return the files which the path refers to.
     */
    public List<File> parse() {
        List<File> result = new ArrayList<File>();
        if (this.stringPath.endsWith("*")) {
            File path_File = new File(this.stringPath.substring(0, this.stringPath.length() - 2));
            result.addAll(FileUtils.listFiles(path_File, new String[]{"jar"}, false));
        } else {
            result.add(new File(this.stringPath));
        }
        return result;
    }
}
