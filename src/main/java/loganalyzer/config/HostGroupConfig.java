package loganalyzer.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loganalyzer.ssh.SshClient;

import org.springframework.beans.factory.annotation.Required;

public final class HostGroupConfig {
    private static final String DEFAULT_SEARCH_PATTERN = "exception|error|warn|fatal";

    private String _groupName;

    private List<HostConfig> _hostConfigs;

    private String _logDirectory;
    private String _baseLogFileName;
    private String _defaultSearchPattern = DEFAULT_SEARCH_PATTERN;

    private final Map<String, SshClient> _sshClients = new HashMap<String, SshClient>();

    public String getGroupName() {
        return _groupName;
    }

    @Required
    public void setGroupName(String groupName) {
        _groupName = groupName;
    }

    public List<HostConfig> getHostConfigs() {
        return _hostConfigs;
    }

    @Required
    public void setHostConfigs(List<HostConfig> hostConfigs) {
        _hostConfigs = hostConfigs;
    }

    public String getLogDirectory() {
        return _logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        _logDirectory = logDirectory;
    }

    public String getBaseLogFileName() {
        return _baseLogFileName;
    }

    @Required
    public void setBaseLogFileName(String baseLogFileName) {
        _baseLogFileName = baseLogFileName;
    }

    public String getDefaultSearchPattern() {
        return _defaultSearchPattern;
    }

    public void setDefaultSearchPattern(final String defaultSearchPattern) {
        _defaultSearchPattern = defaultSearchPattern;
    }

    public void addSshClient(SshClient sshClient) {
        _sshClients.put(sshClient.getHostConfig().getHostName(), sshClient);
    }

    public SshClient getSshClient(String host) {
        return _sshClients.get(host);
    }
}
