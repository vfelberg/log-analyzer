package loganalyzer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import loganalyzer.config.ApplicationConfig;
import loganalyzer.config.Environment;
import loganalyzer.config.HostGroupConfig;

public class LogAnalyzer extends JPanel {

    private final ApplicationConfig _applicationConfig;

    private final List<HostGroup> _hostGroups = new ArrayList<HostGroup>();

    public LogAnalyzer(ApplicationConfig applicationConfig) {
        super(new GridBagLayout());
        _applicationConfig = applicationConfig;
    }

    private void init() {
        JTabbedPane tabbedPane = new JTabbedPane();

        Environment environment = _applicationConfig.getEnvironment();
        for (HostGroupConfig hostGroupConfig : environment.getHostGroupConfigs()) {
            HostGroup hostGroup = new HostGroup(_applicationConfig, hostGroupConfig);
            hostGroup.init();

            _hostGroups.add(hostGroup);
            tabbedPane.addTab(hostGroupConfig.getGroupName(), hostGroup);
        }

        /*
        ZooKeeperPanel zooKeeperPanel = new ZooKeeperPanel(environment.getZooKeeper());
        zooKeeperPanel.init();
        tabbedPane.addTab("ZooKeeper", zooKeeperPanel);
        */

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(tabbedPane, constraints);
    }

    private void destroy() {
        for (HostGroup hostGroup : _hostGroups) {
            hostGroup.destroy();
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/log-analyzer-context.xml");
        ApplicationConfig applicationConfig = (ApplicationConfig) applicationContext.getBean("appConfig");

        String privateKeyPassword = getPrivateKeyPassword();
        if (StringUtils.isBlank(privateKeyPassword)) {
            System.err.println("No password to the SSH private key store provided");
            System.exit(1);
        }

        applicationConfig.setPrivateKeyPassword(privateKeyPassword);

        final LogAnalyzer logAnalyzer = new LogAnalyzer(applicationConfig);
        logAnalyzer.init();

        //Create and set up the window.
        final JFrame frame = new JFrame("LogAnalyzer: " + applicationConfig.getEnvironment().getEnvironmentName());

        WindowAdapter windowAdapter = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logAnalyzer.destroy();

                frame.setVisible(false);
                frame.dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        };
        frame.addWindowListener(windowAdapter);

        //Add content to the window.
        frame.add(logAnalyzer, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        applicationConfig.setRootFrame(frame);
    }

    private static String getPrivateKeyPassword() {
        final JPasswordField passwordField = new JPasswordField();
        JOptionPane optionPane = new JOptionPane(
                new Object[]{new JLabel("Enter password to the SSH private key store:"), passwordField},
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = optionPane.createDialog("LogAnalyzer: SSH password");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        passwordField.requestFocusInWindow();
                    }
                });
            }
        });
        passwordField.addFocusListener(new java.awt.event.FocusListener() {
            public void focusGained(FocusEvent e) {
                passwordField.selectAll();
            }

            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.requestFocusInWindow();
                }
            }
        });
        dialog.setVisible(true);

        Integer result = (Integer) optionPane.getValue();
        dialog.dispose();

        if (result != null && result == JOptionPane.OK_OPTION) {
            return new String(passwordField.getPassword());
        }
        return null;
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                try {
                    createAndShowGUI();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}
