package loganalyzer.grep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public final class GrepOutputTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -7864808322666036934L;

    static final String[] COLUMN_NAMES = {"Host", "File", "Line #", "Line"};

    static final int HOST_COLUMN = 0;
    static final int FILE_NAME_COLUMN = 1;
    static final int LINE_NUMBER_COLUMN = 2;
    static final int MESSAGE_COLUMN = 3;

    private List<GrepOutputLine> _lines = new ArrayList<GrepOutputLine>();

    @Override
    public int getRowCount() {
        return _lines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == MESSAGE_COLUMN;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GrepOutputLine line = _lines.get(rowIndex);
        switch (columnIndex) {
            case HOST_COLUMN:
                return line.getHost();
            case FILE_NAME_COLUMN:
                return line.getFileName();
            case LINE_NUMBER_COLUMN:
                return line.getLineNumber();
            case MESSAGE_COLUMN:
                return line.getMessage();
            default:
                return null;
        }
    }

    void addLine(GrepOutputLine line) {
        _lines.add(line);

        int rowIndex = _lines.size() - 1;
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    GrepOutputLine getLine(int rowIndex) {
        return _lines.get(rowIndex);
    }

    void deleteLine(int rowIndex) {
        _lines.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    void deleteLinesContainingSubstring(String substring) {
        int rowIndex = 0;
        for (Iterator<GrepOutputLine> iterator = _lines.iterator(); iterator.hasNext(); rowIndex++) {
            String message = iterator.next().getMessage();
            if (message.contains(substring)) {
                iterator.remove();
                fireTableRowsDeleted(rowIndex, rowIndex);
            }
        }
    }

    void clear() {
        int rowCount = _lines.size();
        if (rowCount > 0) {
            _lines.clear();
            fireTableRowsDeleted(0, rowCount - 1);
        }
    }
}
