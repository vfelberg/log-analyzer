package de.vf.loganalyzer.grep;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static java.lang.String.format;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import de.vf.loganalyzer.OutputListener;
import de.vf.loganalyzer.config.HostGroupConfig;
import de.vf.loganalyzer.ssh.SshClient;

public class LineContextDialog extends JDialog implements OutputListener {

    private static final String VIEW_OUTPUT_PREFIX = "VIEW_OUTPUT";

    static final int LINES_BEFORE_CONTEXT = 120;

    static final int LINES_AFTER_CONTEXT = 120;

    private final HostGroupConfig _hostGroupConfig;
    private final GrepOutputLine _line;
    private final int _contextBefore;
    private final int _contextAfter;

    private JTextArea _outputArea;

    private int _start;
    private int _end;

    public LineContextDialog(JFrame rootFrame, HostGroupConfig hostGroupConfig, GrepOutputLine line, int contextBefore, int contextAfter) {
        super(rootFrame, true);
        _hostGroupConfig = hostGroupConfig;
        _line = line;
        _contextBefore = contextBefore;
        _contextAfter = contextAfter;
    }

    public void init() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SshClient sshClient = _hostGroupConfig.getSshClient(_line.getHost());
                sshClient.removeOutputListener(LineContextDialog.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                dispose();
            }
        });

        JPanel contentPane = new JPanel(new GridBagLayout());
        setContentPane(contentPane);

        JButton copyButton = new JButton("Copy selection to clipboard");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = _outputArea.getSelectedText();
                if (selectedText != null) {
                    selectedText = selectedText.replaceAll("\\n", "");

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(selectedText), new ClipboardOwner() {
                        @Override
                        public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        }
                    });
                }
            }
        });

        _outputArea = new JTextArea(40, 80);
        _outputArea.setEditable(false);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        contentPane.add(copyButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        contentPane.add(new JScrollPane(_outputArea), constraints);

        SshClient sshClient = _hostGroupConfig.getSshClient(_line.getHost());
        sshClient.addOutputListener(VIEW_OUTPUT_PREFIX, this);

        int lineNumber = _line.getLineNumber();
        _start = lineNumber - _contextBefore;
        if (_start < 1) {
            _start = 1;
        }
        _end = lineNumber + _contextAfter;

        sshClient.sendText(format(
                "awk 'BEGIN {OFS = \":\"} NR == %d, NR == %d {print \"%4$s\", NR, $0} END {print \"%4$s\", -1, \"\"}' %s; echo OUTPUT_END\n",
                _start,
                _end,
                _line.getFileName(),
                VIEW_OUTPUT_PREFIX));

        pack();
        setVisible(true);
    }

    private static final Highlighter.HighlightPainter highlightPainter = new HighlightPainter(Color.YELLOW);

    private static class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public HighlightPainter(Color color) {
            super(color);
        }
    }

    @Override
    public void onOutput(String output) {
        String[] outputParts = output.split(":", 3);

        int lineNumber = Integer.valueOf(outputParts[1]).intValue();
        String message = outputParts[2];

        if (lineNumber == -1) {
            // end of view output
            try {
                Highlighter highlighter = _outputArea.getHighlighter();

                int startOffset = _outputArea.getLineStartOffset(_line.getLineNumber() - _start);
                int endOffset = startOffset + _line.getMessage().length();

                highlighter.addHighlight(startOffset, endOffset, highlightPainter);

                _outputArea.setCaretPosition(startOffset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            _outputArea.append(message + "\n");

            // useful in an IDE console to link to the source code
            //System.out.println(message);
        }
    }
}
