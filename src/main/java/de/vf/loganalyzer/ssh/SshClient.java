package de.vf.loganalyzer.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.vf.loganalyzer.config.ApplicationConfig;
import de.vf.loganalyzer.config.HostGroupConfig;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.Assert;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.vf.loganalyzer.OutputListener;
import de.vf.loganalyzer.config.HostConfig;

public final class SshClient {
    private final ApplicationConfig _applicationConfig;
    private final HostGroupConfig _hostGroupConfig;
    private final HostConfig _hostConfig;

    private List<SshStateListener> _stateListeners = new ArrayList<SshStateListener>();

    private List<OutputListener> _outputListeners = new ArrayList<OutputListener>();

    private Map<String, List<OutputListener>> _prefixListeners = new HashMap<String, List<OutputListener>>();

    private Session _session;
    private Channel _channel;

    private InputStream _in;
    private OutputStream _out;

    private boolean _connected = false;

    public SshClient(ApplicationConfig applicationConfig, HostGroupConfig hostGroupConfig, HostConfig hostConfig) {
        Assert.notNull(applicationConfig, "Application config must not be null");
        Assert.notNull(hostGroupConfig, "Host group config must not be null");
        Assert.notNull(hostConfig, "Host config must not be null");
        _applicationConfig = applicationConfig;
        _hostGroupConfig = hostGroupConfig;
        _hostConfig = hostConfig;
    }

    public HostConfig getHostConfig() {
        return _hostConfig;
    }

    public void addStateListener(SshStateListener listener) {
        _stateListeners.add(listener);
    }

    public void removeStateListener(SshStateListener listener) {
        _stateListeners.remove(listener);
    }

    public void addOutputListener(OutputListener listener) {
        _outputListeners.add(listener);
    }

    public void removeOutputListener(OutputListener listener) {
        _outputListeners.remove(listener);
    }

    public void addOutputListener(String prefix, OutputListener listener) {
        List<OutputListener> listeners = _prefixListeners.get(prefix);
        if (listeners == null) {
            listeners = new ArrayList<OutputListener>();
            _prefixListeners.put(prefix, listeners);
        }
        listeners.add(listener);
    }

    public void removeOutputListener(String prefix, OutputListener listener) {
        List<OutputListener> listeners = _prefixListeners.get(prefix);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void connect() {
        try {
            JSch shell = new JSch();
            if (new File(System.getProperty("user.home") + "/.ssh/id_rsa").exists()) {
                shell.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa", _applicationConfig.getPrivateKeyPassword());
            } else {
                shell.addIdentity(System.getProperty("user.home") + "/.ssh/id_dsa", _applicationConfig.getPrivateKeyPassword());
            }
            shell.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");

            Properties sessionConfig = new Properties();
            sessionConfig.setProperty("StrictHostKeyChecking", "no");

            _session = shell.getSession(System.getProperty("user.name"), _hostConfig.getHostName());
            _session.setConfig(sessionConfig);
            _session.connect();

            _channel = _session.openChannel("shell");
            _channel.connect();

            _in = _channel.getInputStream();
            _out = _channel.getOutputStream();

            _connected = true;

            sendText(String.format("cd %s; echo OUTPUT_END\n", _hostGroupConfig.getLogDirectory()));

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        StringBuilder builder = new StringBuilder();

                        int n;
                        byte[] buffer = new byte[8 * 1024];
                        while ((n = _in.read(buffer, 0, buffer.length)) > -1) {
                            String s = new String(buffer, 0, n);
                            onOutput(s);

                            int i;
                            do {
                                i = s.indexOf("\n");
                                if (i > -1) {
                                    builder.append(s.substring(0, i));
                                    s = s.substring(i + 1);

                                    String line = builder.toString();
                                    if (line.startsWith("OUTPUT_END")) {
                                        enableInput(_hostConfig.getHostName());
                                    }
                                    for (String prefix : _prefixListeners.keySet()) {
                                        if (line.startsWith(prefix)) {
                                            onOutput(prefix, line);
                                        }
                                    }

                                    builder = new StringBuilder();
                                } else {
                                    builder.append(s);
                                }
                            } while (i > -1);
                        }
                    } catch (IOException e) {
                        onOutput(ExceptionUtils.getFullStackTrace(e));
                    }
                }
            };
            Thread thread = new Thread(runnable, "SshClient-" + _hostConfig.getHostName());
            thread.setDaemon(true);
            thread.start();
        } catch (JSchException e) {
            onOutput(ExceptionUtils.getFullStackTrace(e));
        } catch (IOException e) {
            onOutput(ExceptionUtils.getFullStackTrace(e));
        }
    }

    void disconnect() {
        if (_channel != null) {
            _channel.disconnect();
        }
        if (_session != null) {
            _session.disconnect();
        }
        _connected = false;
    }

    public void sendText(String text) {
        if (!_connected) {
            return;
        }

        disabledInput(_hostConfig.getHostName());
        try {
            _out.write(text.getBytes("UTF-8"));
            _out.flush();
        } catch (IOException e) {
            onOutput(ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void disabledInput(String host) {
        for (SshStateListener listener : _stateListeners) {
            listener.onInputDisabled(host);
        }
    }

    private void enableInput(String host) {
        for (SshStateListener listener : _stateListeners) {
            listener.onInputEnabled(host);
        }
    }

    private void onOutput(String output) {
        for (OutputListener listener : _outputListeners) {
            listener.onOutput(output);
        }
    }

    private void onOutput(String prefix, String output) {
        for (OutputListener listener : _prefixListeners.get(prefix)) {
            listener.onOutput(output);
        }
    }
}
