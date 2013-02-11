package de.vf.loganalyzer.config;

import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Required;

public class ApplicationConfig {
    private Environment _environment;

    private String _privateKeyPassword;

    private JFrame _rootFrame;

    public Environment getEnvironment() {
        return _environment;
    }

    @Required
    public void setEnvironment(Environment environment) {
        _environment = environment;
    }

    public String getPrivateKeyPassword() {
        return _privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        _privateKeyPassword = privateKeyPassword;
    }

    public JFrame getRootFrame() {
        return _rootFrame;
    }

    public void setRootFrame(JFrame rootFrame) {
        _rootFrame = rootFrame;
    }

}
