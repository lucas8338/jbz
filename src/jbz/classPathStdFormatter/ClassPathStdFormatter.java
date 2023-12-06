package jbz.classPathStdFormatter;

import jbz.projectDescription.classPath.ClassPathStringParser;

import java.util.*;

/**
 * class to return the class path from the projectDescription file already formatted to a java compatible format
 * */
public class ClassPathStdFormatter {
    public final List<Map<String, Object>> classPathFieldEntries;

    public ClassPathStdFormatter(List<Map<String, Object>> classPathFieldEntries){
        this.classPathFieldEntries = classPathFieldEntries;
    }

    /**
     * retrieve all parsed paths which don't contains the wildcard
     * */
    private List<String> formatWithoutWildcard(){
        List<String> result = new ArrayList<String>();
        for ( Map<String, Object> entry: this.classPathFieldEntries.stream().filter(x->x.containsKey("path")).toList() ){
            String path = (String) entry.get("path");
            if ( !path.endsWith("*") ){
                ClassPathStringParser parser = new ClassPathStringParser(path);
                result.addAll(
                        parser.parse().stream()
                                .map(x->x.getPath())
                                .toList()
                );
            }
        }

        return result;
    }

    /**
     * retrieve all paths which contains the wildcard;
     * */
    private List<String> formatWithWildcard(){
        List<String> result = new ArrayList<String>();
        for ( Map<String, Object> entry: this.classPathFieldEntries.stream().filter(x->x.containsKey("path")).toList() ){
            String path = (String) entry.get("path");
            if ( path.endsWith("*") ){
                ClassPathStringParser parser = new ClassPathStringParser(path);
                result.addAll(
                        parser.parse().stream()
                                .map(x->x.getPath())
                                .toList()
                );
            }
        }

        return result;
    }

    /**
     * retrieve the class path delimited by ";" dot and virgule.
     * */
    public String delimitedString(){
        List<String> withoutWildcard = this.formatWithoutWildcard();
        List<String> withWildcard = this.formatWithWildcard();

        List<String> result = new ArrayList<String>();
        result.addAll(withoutWildcard);
        result.addAll(withWildcard);

        Set<String> uniquicity = new HashSet<String>();
        List<String> uniqueResult = result.stream()
                .filter(x->uniquicity.add(x))
                .toList();

        String delimitedString = jbz.util.BasicUtil.listToDelimitedString(uniqueResult, ";");

        return delimitedString;
    }
}
