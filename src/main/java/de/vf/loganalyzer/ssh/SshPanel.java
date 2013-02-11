package de.vf.loganalyzer.ssh;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import de.vf.loganalyzer.config.HostGroupConfig;
import org.apache.commons.lang.StringUtils;

import de.vf.loganalyzer.OutputListener;
import de.vf.loganalyzer.appinfo.AppInfoProvider;
import de.vf.loganalyzer.config.HostConfig;

public final class SshPanel extends JPanel implements ActionListener, OutputListener {
    private static final String DEFAULT_SEARCH_PATTERN =
            "Access attempt to an invalid video" +
            "\\|BillingAccountNotFoundException" +
            "\\|ConcurrentModificationException" +
            "\\|IllegalArgumentException" +
            "\\|IllegalStateException" +
            "\\|No Hibernate Session" +
            "\\|NonUniqueObjectException" +
            "\\|NullPointerException" +
            "\\|UnsupportedOperationException" +
            "\\|UserNotVerifiedException" +
            "\\|WarningException: Counter";

    private static final int MAX_OUTPUT_LINES = 100;

    private HostGroupConfig _groupConfig;
    private final HostConfig _hostConfig;

    private final SshClient _sshClient;

    private final String DEFAULT_FILE_NAME_PATTERN;

    //grep -H -n 'NullPointerException' some.log.1 | sed 's/^/GREP_OUTPUT:/g'
    //awk 'NR >= 594520; NR == 594560 {exit}' some.log.1 | sed 's/^/VIEW_OUTPUT:/g'

    private JTextField _textField;
    private JTextArea _generalOutputArea;
    private JTextPane _appInfoArea;
    private JButton _appInfoButton;

    public SshPanel(HostGroupConfig groupConfig, HostConfig hostConfig, SshClient sshClient) {
        super(new GridBagLayout());
        _groupConfig = groupConfig;
        _hostConfig = hostConfig;
        _sshClient = sshClient;

        List<String> _recentLogFiles = new ArrayList<String>();
        _recentLogFiles.add(_groupConfig.getBaseLogFileName());
        /*
        for (int i = 1; i <= 5; i++) {
            _recentLogFiles.add(_groupConfig.getBaseLogFileName() + "." + i);
        }
        */
        DEFAULT_FILE_NAME_PATTERN = StringUtils.join(_recentLogFiles.iterator(), " ");
    }

    public void init() {
        _sshClient.addOutputListener(this);

        _textField = new JTextField(25);
        _textField.addActionListener(this);

        _generalOutputArea = new JTextArea(10, 40);
        _generalOutputArea.setEditable(false);

        JPanel leftPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        leftPanel.add(_textField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.8;
        leftPanel.add(new JScrollPane(_generalOutputArea), constraints);

        _appInfoArea = new JTextPane();
        _appInfoArea.setEditable(false);

        _appInfoButton = new JButton("Refresh app info");
        _appInfoButton.setEnabled(false);
        _appInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _appInfoButton.setEnabled(false);
                _appInfoArea.setText("Refreshing app info...");

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        refreshAppInfo();
                        _appInfoButton.setEnabled(true);
                    }
                });
            }
        });

        JPanel rightPanel = new JPanel(new GridBagLayout());

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        rightPanel.add(new JScrollPane(_appInfoArea), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(2, 5, 2, 5);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.EAST;
        rightPanel.add(_appInfoButton, constraints);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(800);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(splitPane, constraints);
    }

    public void connect() {
        _sshClient.connect();

        AppInfoProvider appInfoProvider = _hostConfig.getAppInfoProvider();
        if (appInfoProvider != null) {
            _appInfoArea.setContentType(appInfoProvider.getContentType());
            _appInfoButton.setEnabled(true);
            refreshAppInfo();
        }
    }

    private void refreshAppInfo() {
        _appInfoArea.setText(_hostConfig.getAppInfoProvider().getAppInfo());
        _appInfoArea.setCaretPosition(0);
    }

    public void destroy() {
        _sshClient.disconnect();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String text = _textField.getText() + "\n";
        _sshClient.sendText(text);
    }

    @Override
    public void onOutput(String output) {
        if (_generalOutputArea.getLineCount() >= MAX_OUTPUT_LINES) {
            _generalOutputArea.setText("");
        }
        _generalOutputArea.append(output);
        _generalOutputArea.setCaretPosition(_generalOutputArea.getDocument().getLength());
    }

    public void search(String searchPattern, String fileNamePattern) {
        HostConfig hostConfig = _sshClient.getHostConfig();
        if (!hostConfig.isEnabled()) {
            return;
        }

        if (StringUtils.isBlank(searchPattern)) {
            searchPattern = DEFAULT_SEARCH_PATTERN;
        }
        if (StringUtils.isBlank(fileNamePattern)) {
            fileNamePattern = DEFAULT_FILE_NAME_PATTERN;
        }
        String text = format("grep -H -n \"%s\" %s | sed \"s/^/GREP_OUTPUT:%s:/g\"; echo OUTPUT_END\n", searchPattern, fileNamePattern, hostConfig.getHostName());
        _sshClient.sendText(text);
    }

    public void clearOutput() {
        _generalOutputArea.setText("");
    }

    public SshClient getSshClient() {
        return _sshClient;
    }
}
