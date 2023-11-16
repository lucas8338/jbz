package jbz.projectDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class to replace values of a string using a map
 * a 'string template' substitutor.
 */
public class FormatedString {
    /**
     * store the source string. which contains the variables to replace.
     */
    public final String source;
    public Map<String, Object> replacement;
    Pattern pattern = Pattern.compile("\\\\\\{(.*?)}");

    public FormatedString(String source, Map<String, Object> replacement) {
        this.source = source;

        this.replacement = replacement;
    }

    /**
     * replace the matched strings in the map using the map supplied to replace the values.
     */
    public String replace() {
        String result = this.source;

        Matcher mat = this.pattern.matcher(this.source);
        while (mat.find()) {
            String entire = mat.group(0);
            String value = mat.group(1);

            String replacementValue = this.mapNavigateWithString(value, ".");
            result = result.replaceFirst(Pattern.quote(entire), replacementValue);
        }
        return result;
    }

    /**
     * navigate in the 'replacement' map using a delimited string.
     *
     * @param navigator: a string which is delimited and will be used to navigate.
     * @param delimiter: the string delimiter.
     * @return the value of the last key of the map.
     */
    private String mapNavigateWithString(String navigator, String delimiter) {
        List<String> keys = new ArrayList<String>();
        keys.addAll(
                Arrays.stream(navigator.split(delimiter)).toList()
        );
        // this fix when there no delimiter in the 'navigator' text.
        if (keys.size() == 0) {
            keys.add(navigator);
        }
        Object node = "";
        for (String key : keys) {
            node = this.replacement.get(key);
        }
        return node.toString();
    }
}
