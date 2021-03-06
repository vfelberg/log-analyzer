package loganalyzer.config;

import loganalyzer.appinfo.AppInfoProvider;

import org.springframework.beans.factory.annotation.Required;

public final class HostConfig {
    private String _hostName;
    private AppInfoProvider _appInfoProvider;
    private boolean _enabled = true;

    public String getHostName() {
        return _hostName;
    }

    @Required
    public void setHostName(String hostName) {
        _hostName = hostName;
    }

    public AppInfoProvider getAppInfoProvider() {
        return _appInfoProvider;
    }

    public void setAppInfoProvider(AppInfoProvider appInfoProvider) {
        _appInfoProvider = appInfoProvider;
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }
}
