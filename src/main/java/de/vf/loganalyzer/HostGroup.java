package de.vf.loganalyzer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;

import de.vf.loganalyzer.config.ApplicationConfig;
import de.vf.loganalyzer.config.HostConfig;
import de.vf.loganalyzer.config.HostGroupConfig;
import de.vf.loganalyzer.grep.GrepOutputPanel;
import de.vf.loganalyzer.ssh.SshClient;
import de.vf.loganalyzer.ssh.SshPanel;
import de.vf.loganalyzer.ssh.SshStateListener;

public final class HostGroup extends JPanel implements SshStateListener {
    private final ApplicationConfig _applicationConfig;
    private final HostGroupConfig _hostGroupConfig;

    private JTextField _searchPatternField;
    private JTextField _fileNamePatternField;

    private GrepOutputPanel _grepOutputPanel;

    private Map<String, SshPanel> _panels = new HashMap<String, SshPanel>();
    private JTabbedPane _tabbedPane;

    private final ImageIcon _iconEnabled = new ImageIcon(getClass().getClassLoader().getResource("img/enabled.png"));
    private final ImageIcon _iconDisabled = new ImageIcon(getClass().getClassLoader().getResource("img/disabled.png"));

    HostGroup(ApplicationConfig applicationConfig, HostGroupConfig hostGroupConfig) {
        super(new GridBagLayout());
        _applicationConfig = applicationConfig;
        _hostGroupConfig = hostGroupConfig;
    }

    void init() {
        final JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                connectButton.setEnabled(false);
            }
        });

        _searchPatternField = new JTextField(25);
        _fileNamePatternField = new JTextField(25);

        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _grepOutputPanel.clear();
                for (SshPanel panel : _panels.values()) {
                    panel.search(_searchPatternField.getText(), _fileNamePatternField.getText());
                }
            }
        };

        _searchPatternField.addActionListener(searchListener);
        _fileNamePatternField.addActionListener(searchListener);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(searchListener);

        JButton clearOutputButton = new JButton("Clear output");
        clearOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (SshPanel panel : _panels.values()) {
                    panel.clearOutput();
                }
            }
        });

        _grepOutputPanel = new GrepOutputPanel(_applicationConfig, _hostGroupConfig);
        _grepOutputPanel.init();

        _tabbedPane = new JTabbedPane();

        List<HostConfig> hosts = _hostGroupConfig.getHostConfigs();
        for (int i = 0; i < hosts.size(); i++) {
            HostConfig hostConfig = hosts.get(i);

            SshClient sshClient = new SshClient(_applicationConfig, _hostGroupConfig, hostConfig);
            sshClient.addOutputListener("GREP_OUTPUT", _grepOutputPanel);
            sshClient.addStateListener(this);

            SshPanel panel = new SshPanel(_hostGroupConfig, hostConfig, sshClient);
            panel.init();
            _panels.put(hostConfig.getHostName(), panel);

            _tabbedPane.addTab(hostConfig.getHostName(), _iconDisabled, panel);
            _tabbedPane.setMnemonicAt(i, KeyEvent.VK_1 + i);

            _hostGroupConfig.addSshClient(sshClient);
        }
        _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                _grepOutputPanel,
                _tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(400);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(connectButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 2);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        add(_searchPatternField, constraints);

        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 2);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        add(_fileNamePatternField, constraints);

        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 2);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(searchButton, constraints);

        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 2, 5, 5);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(clearOutputButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(splitPane, constraints);
    }

    private void connect() {
        for (SshPanel panel : _panels.values()) {
            panel.connect();
        }
    }

    void destroy() {
        try {
            for (SshPanel panel : _panels.values()) {
                panel.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInputEnabled(String host) {
        int i = _tabbedPane.indexOfTab(host);
        if (i >= 0) {
            _tabbedPane.setIconAt(i, _iconEnabled);
        }
    }

    @Override
    public void onInputDisabled(String host) {
        int i = _tabbedPane.indexOfTab(host);
        if (i >= 0) {
            _tabbedPane.setIconAt(i, _iconDisabled);
        }
    }
}
