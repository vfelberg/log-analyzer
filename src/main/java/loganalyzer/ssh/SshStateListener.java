package loganalyzer.ssh;

public interface SshStateListener {
    public void onInputEnabled(String host);
    public void onInputDisabled(String host);
}
