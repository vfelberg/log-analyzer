package loganalyzer.grep;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;

import loganalyzer.OutputListener;
import loganalyzer.config.ApplicationConfig;
import loganalyzer.config.HostGroupConfig;
import static loganalyzer.grep.GrepOutputTableModel.FILE_NAME_COLUMN;
import static loganalyzer.grep.GrepOutputTableModel.LINE_NUMBER_COLUMN;
import static loganalyzer.grep.GrepOutputTableModel.MESSAGE_COLUMN;
import static loganalyzer.grep.LineContextDialog.LINES_AFTER_CONTEXT;
import static loganalyzer.grep.LineContextDialog.LINES_BEFORE_CONTEXT;

public final class GrepOutputPanel extends JPanel implements OutputListener {
    private final ApplicationConfig _applicationConfig;
    private final HostGroupConfig _hostGroupConfig;

    private final GrepOutputTableModel _tableModel = new GrepOutputTableModel();

    public GrepOutputPanel(ApplicationConfig applicationConfig, HostGroupConfig hostGroupConfig) {
        super(new GridBagLayout());
        _applicationConfig = applicationConfig;
        _hostGroupConfig = hostGroupConfig;
    }

    public void init() {
        final JTable table = new JTable(_tableModel);
        table.setFillsViewportHeight(true);

        TableColumnModel columnModel = table.getColumnModel();

        TableColumn hostColumn = columnModel.getColumn(GrepOutputTableModel.HOST_COLUMN);
        hostColumn.setPreferredWidth(100);
        hostColumn.setMaxWidth(100);

        TableColumn fileNameColumn = columnModel.getColumn(FILE_NAME_COLUMN);
        fileNameColumn.setPreferredWidth(160);
        fileNameColumn.setMaxWidth(160);

        TableColumn lineNumberColumn = columnModel.getColumn(LINE_NUMBER_COLUMN);
        lineNumberColumn.setPreferredWidth(80);
        lineNumberColumn.setMaxWidth(80);

        TableColumn messageColumn = columnModel.getColumn(MESSAGE_COLUMN);
        messageColumn.setPreferredWidth(400);
        messageColumn.setMinWidth(400);

        JScrollPane scrollPane = new JScrollPane(table);

        JLabel emptyLabel = new JLabel("                    ");

        final JTextField contextBeforeField = new JTextField(Integer.toString(LINES_BEFORE_CONTEXT), 5);
        final JTextField contextAfterField = new JTextField(Integer.toString(LINES_AFTER_CONTEXT), 5);

        ActionListener lineContextListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow > -1) {
                    int contextBefore;
                    try {
                        contextBefore = Integer.parseInt(contextBeforeField.getText());
                    } catch (NumberFormatException e) {
                        contextBefore = LINES_BEFORE_CONTEXT;
                    }
                    int contextAfter;
                    try {
                        contextAfter = Integer.parseInt(contextAfterField.getText());
                    } catch (NumberFormatException e) {
                        contextAfter = LINES_AFTER_CONTEXT;
                    }

                    LineContextDialog lineContextDialog = new LineContextDialog(
                            _applicationConfig.getRootFrame(),
                            _hostGroupConfig,
                            _tableModel.getLine(selectedRow),
                            contextBefore,
                            contextAfter);
                    lineContextDialog.init();
                }
            }
        };

        contextBeforeField.addActionListener(lineContextListener);
        contextAfterField.addActionListener(lineContextListener);

        JButton viewButton = new JButton("View line context");
        viewButton.addActionListener(lineContextListener);

        JButton deleteSelectedLinesButton = new JButton("Delete selected lines");
        deleteSelectedLinesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow;
                do {
                    selectedRow = table.getSelectedRow();
                    if (selectedRow > -1) {
                        _tableModel.deleteLine(selectedRow);
                    }
                } while (selectedRow > -1);
            }
        });

        final JTextField substringField = new JTextField(80);
        ActionListener deleteSubstringListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtils.isNotBlank(substringField.getText())) {
                    _tableModel.deleteLinesContainingSubstring(substringField.getText());
                    substringField.selectAll();
                }
            }
        };
        substringField.addActionListener(deleteSubstringListener);

        JButton deleteSubstringButton = new JButton("Delete lines containing substring");
        deleteSubstringButton.addActionListener(deleteSubstringListener);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.4;
        constraints.weighty = 0.0;
        add(emptyLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.1;
        constraints.weighty = 0.0;
        add(contextBeforeField, constraints);

        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.1;
        constraints.weighty = 0.0;
        add(contextAfterField, constraints);

        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(viewButton, constraints);

        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(deleteSelectedLinesButton, constraints);

        constraints.gridx = 5;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.4;
        constraints.weighty = 0.0;
        add(substringField, constraints);

        constraints.gridx = 6;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 5);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(deleteSubstringButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 7;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(scrollPane, constraints);
    }

    @Override
    public void onOutput(String output) {
        String[] lineParts = output.split(":", 5);
        String host = lineParts[1];
        String fileName = lineParts[2];
        int lineNumber = Integer.parseInt(lineParts[3]);
        String message = lineParts[4];
        _tableModel.addLine(new GrepOutputLine(host, fileName, lineNumber, message));
    }

    public void clear() {
        _tableModel.clear();
    }
}
