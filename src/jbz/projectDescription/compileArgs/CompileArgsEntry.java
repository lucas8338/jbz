package jbz.projectDescription.compileArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * parse an entry in the 'compileArgs' key in projectDescription.
 */
public class CompileArgsEntry {

    Map<String, Object> entry;

    public CompileArgsEntry(Map<String, Object> entry) {
        this.entry = entry;
    }

    // split the entry into a processBuilder compatible.
    // it can return a empty list, case the entry not provide the requirement
    // for the processBuilder
    public List<String> parse_processBuilder() {
        List<String> pbString = new ArrayList<String>();
        String entry_string = (String) this.entry.get("string");
        if (!(entry_string == null)) {
            pbString.addAll(List.of(entry_string.split(" ")));
        }
        return pbString;
    }
}
