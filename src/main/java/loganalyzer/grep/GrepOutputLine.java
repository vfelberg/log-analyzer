package loganalyzer.grep;

import java.io.Serializable;

public final class GrepOutputLine implements Serializable {
    private static final long serialVersionUID = -472775444132392324L;

    private final String _host;

    private final String _fileName;

    private final int _lineNumber;
    
    private final String _message;

    GrepOutputLine(String host, String fileName, int lineNumber, String message) {
        _host = host;
        _fileName = fileName;
        _lineNumber = lineNumber;
        _message = message;
    }

    public String getHost() {
        return _host;
    }

    String getFileName() {
        return _fileName;
    }

    int getLineNumber() {
        return _lineNumber;
    }

    String getMessage() {
        return _message;
    }
}
