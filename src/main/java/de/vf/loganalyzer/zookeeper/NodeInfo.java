package de.vf.loganalyzer.zookeeper;

//import de.vf.zookeeper.ZooKeeperPath;

public class NodeInfo {
    //private final ZooKeeperPath _path;
    //private final String _name;
    private boolean _childrenInitialized = false;
    private boolean _dataInitialized = false;

    /*
    public NodeInfo(ZooKeeperPath path, String name) {
        _path = path;
        _name = name;
    }

    public ZooKeeperPath getPath() {
        return _path;
    }

    public String getName() {
        return _name;
    }
    */

    public boolean isChildrenInitialized() {
        return _childrenInitialized;
    }

    public void setChildrenInitialized(boolean childrenInitialized) {
        _childrenInitialized = childrenInitialized;
    }

    public boolean isDataInitialized() {
        return _dataInitialized;
    }

    public void setDataInitialized(boolean dataInitialized) {
        _dataInitialized = dataInitialized;
    }

    /*
    @Override
    public String toString() {
        return _name;
    }
    */
}
