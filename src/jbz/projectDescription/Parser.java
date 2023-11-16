package jbz.projectDescription;

import com.esotericsoftware.yamlbeans.YamlReader;
import jbz.projectDescription.enums.SubProjects_enum;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * this contains the reader for the project description, this allows to read a specific
 * field of the project description file
 */
public class Parser {
    public Map<String, Object> projectDescription;

    public Parser(File projectDescriptionFile) throws IOException {
        Assert.assertTrue(projectDescriptionFile.isFile());
        String projectDescriptionString = Files.readString(projectDescriptionFile.toPath());
        YamlReader yreader = new YamlReader(projectDescriptionString);
        this.projectDescription = yreader.read(Map.class);
    }

    /**
     * return the 'internal' field of the project description.
     */
    public Map<String, Object> getInternal(SubProjects_enum subField) {
        Map<String, Map<String, Object>> internal = (Map<String, Map<String, Object>>) this.projectDescription.get("internal");
        return internal.get(subField.toString());
    }

    /**
     * return the 'class-path' field of the project description.
     */
    public List<Map<String, Object>> getClassPath(SubProjects_enum subField) {
        Map<String, List<Map<String, Object>>> classPath = (Map<String, List<Map<String, Object>>>) this.projectDescription.get("class-path");
        return classPath.get(subField.toString());
    }

    /**
     * return the 'compileArgs' field of the projectDescription.
     */
    public List<Map<String, Object>> getCompileArgs(SubProjects_enum subField) {
        Map<String, List<Map<String, Object>>> compileArgs = (Map<String, List<Map<String, Object>>>) this.projectDescription.get("compileArgs");
        return compileArgs.get(subField.toString());
    }

    /**
     * return the 'javadocArgs' field of the projectDescription.
     */
    public List<Map<String, Object>> getJavadocArgs(SubProjects_enum subField) {
        Map<String, List<Map<String, Object>>> javadocArgs = (Map<String, List<Map<String, Object>>>) this.projectDescription.get("javadocArgs");
        return javadocArgs.get(subField.toString());
    }

    /**
     * return the 'testng' field of the projectDescription.
     */
    public List<Map<String, Object>> getTestNg(SubProjects_enum subField) {
        Map<String, List<Map<String, Object>>> testngField = (Map<String, List<Map<String, Object>>>) this.projectDescription.get("testngArgs");
        return testngField.get(subField.toString());
    }
}
