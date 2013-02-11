package de.vf.loganalyzer.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

//import de.vf.zookeeper.ZooKeeper;

public class Environment {
    private String _environmentName;

    private List<HostGroupConfig> _hostGroupConfigs;

    //private ZooKeeper _zooKeeper;

    public String getEnvironmentName() {
        return _environmentName;
    }

    @Required
    public void setEnvironmentName(String environmentName) {
        _environmentName = environmentName;
    }

    public List<HostGroupConfig> getHostGroupConfigs() {
        return _hostGroupConfigs;
    }

    @Required
    public void setHostGroupConfigs(List<HostGroupConfig> hostGroupConfigs) {
        _hostGroupConfigs = hostGroupConfigs;
    }

    /*
    public ZooKeeper getZooKeeper() {
        return _zooKeeper;
    }

    @Required
    public void setZooKeeper(ZooKeeper zooKeeper) {
        _zooKeeper = zooKeeper;
    }
    */
}
