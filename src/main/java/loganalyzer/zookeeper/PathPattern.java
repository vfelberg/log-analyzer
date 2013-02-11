package loganalyzer.zookeeper;

import java.util.Arrays;
import java.util.List;

public class PathPattern {
    private final List<String> _elements;

    public PathPattern(String pattern) {
        _elements = Arrays.asList(pattern.split("/"));
        _elements.set(0, "/");
    }

    public boolean matches(Object[] elements) {
        if (_elements.size() != elements.length) {
            return false;
        }
        for (int i = 0; i < _elements.size(); i++) {
            String pattern = _elements.get(i);
            String element = elements[i].toString();
            if (!pattern.equals("*") && !pattern.equals(element)) {
                return false;
            }
        }
        return true;
    }
}
