package jbz.frontEnd;

import jbz.compilation.Compile;
import jbz.directorial.DefaultStructure;
import jbz.directorial.JarCreator;
import jbz.frontEnd.logger.Logger;
import jbz.frontEnd.logger.Logger_level;
import jbz.javadoc.Javadoc;
import jbz.projectDescription.FormatedString;
import jbz.projectDescription.Parser;
import jbz.projectDescription.classPath.ClassPathStringParser;
import jbz.projectDescription.enums.SubProjects_enum;
import jbz.testing.TestDir;
import jbz.util.BasicUtil;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

@CommandLine.Command(name = "jbz", mixinStandardHelpOptions = true)
public class Cli {


    /**
     * the location of the 'current' directory.
     * one of the usage of this is to format name of paths and to use
     * to set the 'working dir' of command line stuffs.
     */
    public final File root;
    /**
     * store the 'file' to the projectDescription.yaml file.
     */
    public final File projectDescriptionFile;
    /**
     * store the parsed projectDescription
     */
    public Parser projectDescription;
    @CommandLine.Option(names = {"-v", "--verbose"}, arity = "0")
    public boolean verbose;

    public Cli(File workingDir) throws IOException {
        Assert.assertTrue(workingDir.isDirectory());
        this.root = workingDir;

        this.projectDescriptionFile = new File(this.root.getAbsolutePath() + "/projectDescription.yaml").getAbsoluteFile();

        if (this.projectDescriptionFile.exists()) {
            this.projectDescription = new Parser(this.projectDescriptionFile.getAbsoluteFile());
        }
    }

    /**
     * obtain the logger configured.
     */
    public Logger getLogger(String methodName) {
        Logger log = new Logger("jbz.frontEnd.Cli." + methodName, this.verbose ? Logger_level.debug : Logger_level.info);
        return log;
    }

    @CommandLine.Command(description = "initialize a new project in given location or in current directory.")
    public void init(@CommandLine.Parameters(defaultValue = "basic") String structure) throws IOException, URISyntaxException {
        Logger log = this.getLogger("init");

        DefaultStructure defaultStructureGen = new DefaultStructure(this.root);
        if (this.root.listFiles().length == 0) {
            log.debug("generating the default structure, copying files.");
            defaultStructureGen.copyResourceDefaultStructureDir(structure);
            log.debug("deleting all '.gitkeep_initDelete' files in the project location.");
            defaultStructureGen.deleteRecursive(".gitkeep_initDelete", true);
        } else {
            log.info("the directory is not empty, then init cant be called.");
        }

        this.projectDescription = new Parser(this.projectDescriptionFile.getAbsoluteFile());
    }

    @CommandLine.Command(description = "compile the 'main' project.")
    public void compile(@CommandLine.Option(names = {"--subProject"}, defaultValue = "main") String subProject) throws IOException, InterruptedException {
        Logger log = this.getLogger("compile");

        // for hour the objective is just to fill these three variables required by the 'jbz.compilation.Compile' class.
        // the files to be compiled.
        List<File> filesToCompile = new ArrayList<File>();
        // files to be added to the class path.
        List<File> classPath = new ArrayList<File>();
        // the additional parameters to be passed to the compiler.
        List<String> additionalParameters = new ArrayList<String>();

        Map<String, Object> internal = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject));

        ////////////////// fill the files to compile list //////////////////


        String srcLocation = internal.get("src").toString();

        filesToCompile.addAll(
                FileUtils.listFiles(new File(this.root, srcLocation), new String[]{"java"}, true).stream().toList()
        );
        ///////////////////////////////////////////////////////////
        //////////////// fill the 'classPath' list ///////////////

        List<Map<String, Object>> cp_entries = this.projectDescription.getClassPath(SubProjects_enum.valueOf(subProject))
                .stream()
                .filter(x -> x.containsKey("path"))
                .toList();

        for (Map<String, Object> entry : cp_entries) {
            String entryPath = entry.get("path").toString();
            ClassPathStringParser parser = new ClassPathStringParser(entryPath);
            classPath.addAll(
                    parser.parse()
                            .stream()
                            .map(x -> new File(this.root, x.getPath()))
                            .toList()
            );
        }

        //////////////////////////////////////////////////////////
        /////////////// fill the 'additionalParameter' list //////////////////////

        additionalParameters.addAll(
                this.projectDescription.getCompileArgs(SubProjects_enum.valueOf(subProject))
                        .stream()
                        .filter(x -> x.containsKey("string"))
                        .map(x -> x.get("string").toString())
                        .toList()
        );

        ////////////////////////////////////////////////////////////////
        Compile compile = new Compile(filesToCompile, classPath, additionalParameters, this.root);

        compile.classPath_stringBuild();

        log.info("compiling the files for the project: " + subProject);
        compile.compile();

    }

    @CommandLine.Command(
            description = "generate the javadoc documentation."
    )
    public void javadoc(@CommandLine.Option(names = {"--subProject"}, defaultValue = "main") String subProject) throws IOException, InterruptedException {
        Logger log = this.getLogger("javadoc");

        List<File> sources = new ArrayList<File>();
        List<File> classPaths = new ArrayList<File>();
        List<String> additionalParameters = new ArrayList<String>();

        Map<String, Object> internal_main = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject));

        /////// fill the 'sources' list /////////

        String internal_main_src = internal_main.get("src").toString();
        sources.addAll(
                FileUtils.listFiles(new File(root + "/" + internal_main_src), new String[]{"java"}, true)
        );

        //////////////////////////////////
        ///////// fill the 'classPaths' list ///////////

        List<Map<String, Object>> cp_entries = this.projectDescription.getClassPath(SubProjects_enum.valueOf(subProject))
                .stream()
                .filter(x -> x.containsKey("path"))
                .toList();

        for (Map<String, Object> entry : cp_entries) {
            String entryPath = entry.get("path").toString();
            ClassPathStringParser parser = new ClassPathStringParser(entryPath);
            classPaths.addAll(parser.parse());
        }

        ///////////////////////////////////////
        ////////// fill the 'additionalParameters' list ///////

        List<String> javadocArgs = this.projectDescription.getJavadocArgs(SubProjects_enum.valueOf(subProject))
                .stream()
                .filter(x -> x.containsKey("string"))
                .map(x -> x.get("string").toString())
                .toList();

        additionalParameters.addAll(javadocArgs);

        ////////////////

        Javadoc javadocGenerator = new Javadoc(sources, classPaths, additionalParameters, this.root);
        javadocGenerator.classPaths_cmdComplaint();

        log.info("generating javadoc for the project: " + subProject);
        javadocGenerator.generate();
    }

    @CommandLine.Command(description = "run the tests in the 'tests' project.")
    public void test(@CommandLine.Option(names = {"--subProject"}, defaultValue = "tests") String subProject) throws URISyntaxException, IOException, InterruptedException {
        Logger log = this.getLogger("test");

        Map<String, Object> internal_tests = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject));

        this.compile(subProject);

        TestDir testDir = new TestDir(
                new File(
                        this.root,
                        internal_tests.get("compileOutput").toString()
                )
        );

        List<Map<String, Object>> classPaths = this.projectDescription.getClassPath(SubProjects_enum.valueOf(subProject))
                .stream()
                .filter(x -> x.containsKey("path"))
                .toList();

        List<File> classPaths_file = new ArrayList<File>();

        for (Map<String, Object> entry : classPaths) {
            String entryPath = entry.get("path").toString();
            ClassPathStringParser parser = new ClassPathStringParser(entryPath);
            classPaths_file.addAll(parser.parse());
        }

        List<Map<String, Object>> additionalArgs_javaArg =
                ListUtils.emptyIfNull(
                        this.projectDescription.getTestNg(SubProjects_enum.valueOf(subProject))
                                .stream()
                                .filter(x -> x.containsKey("string"))
                                .filter(x -> x.containsKey("type"))
                                .filter(x -> x.get("type").toString().equals("javaArg"))
                                .toList()
                );

        List<String> additionalArgs_javaArg_listString = additionalArgs_javaArg
                .stream()
                .map(x -> x.get("string").toString())
                .toList();

        List<Map<String, Object>> additionalArgs_testngArg =
                ListUtils.emptyIfNull(
                        this.projectDescription.getTestNg(SubProjects_enum.valueOf(subProject))
                                .stream()
                                .filter(x -> x.containsKey("string"))
                                .filter(x -> x.containsKey("type"))
                                .filter(x -> x.get("type").toString().equals("testngArg"))
                                .toList()
                );

        List<String> additionalArgs_testngArg_listString = additionalArgs_testngArg
                .stream()
                .map(x -> x.get("string").toString())
                .toList();

        log.info("running tests for the project: " + subProject);
        testDir.run(classPaths_file, additionalArgs_javaArg_listString, additionalArgs_testngArg_listString, this.root);
    }

    /**
     * generate a manifest map for the 'Jar' class.
     * this generated map is general purpose.
     */
    private Map<String, String> defaultManifestMap() {
        Map<String, String> manifest = new HashMap<String, String>();

        String pd_name = this.projectDescription.projectDescription.get("name").toString();
        String pd_mainClass = this.projectDescription.projectDescription.get("mainClass").toString();
        String pd_version = this.projectDescription.projectDescription.get("version").toString();
        String pd_description = this.projectDescription.projectDescription.get("description").toString();
        String pd_author = this.projectDescription.projectDescription.get("author").toString();
        String pd_url = this.projectDescription.projectDescription.get("url").toString();

        manifest.put("name", pd_name);

        if (!pd_mainClass.equals("")) {
            manifest.put("Main-Class", pd_mainClass);
        }

        manifest.put("Specification-Title", pd_description);

        manifest.put("Specification-Version", pd_version);

        manifest.put("Specification-Vendor", pd_author);

        return manifest;
    }

    @CommandLine.Command(description = "generate a '.jar' file from a project without dependencies.")
    public void jar(@CommandLine.Option(names = {"--subProject"}, defaultValue = "main") String subProject) throws IOException, InterruptedException {
        Logger log = this.getLogger("jar");

        Map<String, Object> internal = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject));

        Map<String, String> manifest = this.defaultManifestMap();

        JarCreator jc = new JarCreator();

        List<String> classPaths_addToJarCp = this.projectDescription.getClassPath(SubProjects_enum.valueOf(subProject)).stream()
                .filter(x -> x.containsKey("path"))
                .filter(x->Boolean.parseBoolean((String) x.get("addToJarCp")) == true)
                .map(x -> x.get("path").toString())
                .toList();

        List<String> classPaths_addToJarCp_wilHandled = this.handleReplaceWildcardsInList(classPaths_addToJarCp);

        StringBuilder metafileClassPathBuilder = new StringBuilder();

        for ( String path: classPaths_addToJarCp_wilHandled ){
            // append a line break and two empty spaces to allow a lot of entries
            // because the size of a line is limited
            metafileClassPathBuilder.append("\n");
            metafileClassPathBuilder.append("  ");
            metafileClassPathBuilder.append(path.replace("\\", "/"));
        }

        manifest.put("Class-Path", metafileClassPathBuilder.toString());

        File compileOutputDir = new File(this.root, internal.get("compileOutput").toString());

        FileUtils.copyDirectory(compileOutputDir, jc.tmpdir_assembly);

        jc.createManifest(manifest);

        FormatedString fs = new FormatedString(internal.get("jarPath").toString(), this.projectDescription.projectDescription);

        String jarPath = fs.replace();

        File jarPath_file = new File(
                this.root,
                jarPath
        );

        jc.createJar(jarPath_file);
    }

    /**
     * function to handle wildcards found in a list of string. will replace the
     * strings which ends with wildcard and add beginning at that position the file paths.
     * */
    private List<String> handleReplaceWildcardsInList(List<String> input){
        List<String> copyInput = new ArrayList<String>();
        copyInput.addAll(input);
        int index = 0;
        while (true){
            String value = copyInput.get(index);
            if ( value.endsWith("*") ){
                List<String> files_string = new ArrayList<String>();
                ClassPathStringParser parser = new ClassPathStringParser(value);
                files_string.addAll(parser.parse().stream()
                        .map(x-> x.getPath())
                        .toList()
                );
                copyInput.remove(index);
                for ( int i = 0; i < files_string.size(); i++ ){
                    copyInput.add(index + i, files_string.get(i));
                }
            }
            index++;
            if ( index >= copyInput.size() ){break;}
        }
        return copyInput;
    }

    /**
     * transform all paths into relative paths with the 'root' parameter.
     * */
    private String relativizePathString(String path){
        return this.root.toPath().relativize(new File(path).toPath()).toFile().getPath();
    }

    @CommandLine.Command(description = "generate a '.jar' file from a project with dependencies.")
    public void fatJar(@CommandLine.Option(names = {"--subProject"}, defaultValue = "main") String subProject) throws IOException, InterruptedException {
        Logger log = this.getLogger("jar");

        Map<String, Object> internal = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject));

        Map<String, String> manifest = this.defaultManifestMap();

        JarCreator jc = new JarCreator();

        List<String> classPaths_copyC = this.projectDescription.getClassPath(SubProjects_enum.valueOf(subProject)).stream()
                .filter(x -> x.containsKey("path"))
                .filter(x->Boolean.parseBoolean((String) x.get("copyCToJarWithDependencies")) == true)
                // here using the java.nio Paths doesnt work, because the Files class accepts the asterisk
                // and the Path class doest.
                .map(x->new File(this.root.getPath(), (String) x.get("path")).getPath())
                .toList();

        List<String> classPaths_copyC_wildHandled = this.handleReplaceWildcardsInList(classPaths_copyC);

        List<Path> jars = new ArrayList<Path>(
                classPaths_copyC_wildHandled.stream()
                        .filter(x->x.endsWith(".jar"))
                        .map(x->Paths.get(x))
                        .toList()
        );

        for (Path path : jars) {
            Path target = Paths.get(
                    jc.tmpdir_jar.getPath() + "/" + path.getFileName()
            );
            Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        }

        File compileOutputDir = new File(this.root, internal.get("compileOutput").toString());

        FileUtils.copyDirectory(compileOutputDir, jc.tmpdir_assembly);

        // will only add the content of the 'resources' directory if there the
        // 'resources' key in the config file.

        Object resourcesConfigPath = internal.get("resources");

        if ( resourcesConfigPath != null ) {
            File resourcesDir = new File(
                    this.root,
                    resourcesConfigPath.toString()
            );
            FileUtils.copyDirectory(resourcesDir, jc.tmpdir_assembly);
        }

        jc.createManifest(manifest);

        FormatedString fs = new FormatedString(internal.get("jarPathWithDependencies").toString(), this.projectDescription.projectDescription);

        String jarPath = fs.replace();

        File jarPath_file = new File(
                this.root,
                jarPath
        );

        jc.createJar(jarPath_file);
    }

    /**
     * delete all files in the directory
     */
    public void deleteDirContents(File dir) {
        if (!dir.isDirectory()) {
            return;
        }
        List<File> filesInDir = ListUtils.emptyIfNull(
                Arrays.stream(dir.listFiles()).toList()
        );

        filesInDir.parallelStream()
                .forEach(x -> {
                    if (x.isDirectory()) {
                        try {
                            FileUtils.forceDelete(x);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        x.delete();
                    }
                });
    }

    @CommandLine.Command(description = "delete output files and dirs")
    public void clean(@CommandLine.Option(names = {"--subProject"}, defaultValue = "main") String subProject) {
        File dirCompileOutputFile = new File(
                this.root,
                this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject)).get("compileOutput").toString()
        );

        File dirJavadocOutputFile = new File(
                this.root,
                this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject)).get("javadocOutput").toString()
        );

        String jarPath_asread = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject)).get("jarPath").toString();

        FormatedString jarPath_fs = new FormatedString(jarPath_asread, this.projectDescription.projectDescription);

        String jarPath_replaced = jarPath_fs.replace();

        File jarPathFile = new File(
                this.root,
                jarPath_replaced
        );

        String jarPathWithDependencies_asread = this.projectDescription.getInternal(SubProjects_enum.valueOf(subProject)).get("jarPathWithDependencies").toString();

        FormatedString jarPathWithDependencies_fs = new FormatedString(jarPathWithDependencies_asread, this.projectDescription.projectDescription);

        String jarPathWithDependencies_replaced = jarPathWithDependencies_fs.replace();

        File jarPathWithDependenciesFile = new File(
                this.root,
                jarPathWithDependencies_replaced
        );

        this.deleteDirContents(dirCompileOutputFile);
        this.deleteDirContents(dirJavadocOutputFile);
        if (jarPathFile.isFile()) {
            jarPathFile.delete();
        }
        if (jarPathWithDependenciesFile.isFile()) {
            jarPathWithDependenciesFile.delete();
        }
    }
}
