package loganalyzer.zookeeper;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
/*
import de.vf.protobuf.IServiceInfoDeserializer;
import de.vf.util.FmtLogger;
import de.vf.util.StringUtils;
import de.vf.zookeeper.ZooKeeper;
import de.vf.zookeeper.ZooKeeperConnectionListener;
import de.vf.zookeeper.ZooKeeperNode;
import de.vf.zookeeper.ZooKeeperPath;
*/

public class ZooKeeperPanel extends JPanel /* implements ZooKeeperConnectionListener */ {

    /*
    private static final FmtLogger LOG = FmtLogger.getLogger(ZooKeeperPanel.class);

    private final ZooKeeper _zooKeeper;
    */

    private DefaultTreeModel _treeModel;
    private JTree _tree;

    private Map<PathPattern, Class<?>> _deserializerMap = new HashMap<PathPattern, Class<?>>();

    //public ZooKeeperPanel(ZooKeeper zooKeeper) {
    //    super(new GridBagLayout());

    //    _zooKeeper = zooKeeper;
    //    _zooKeeper.addConnectionListener(this);

    //    _deserializerMap.put(new PathPattern("/Services/*/*"), IServiceInfoDeserializer.class);
    //}

    /*
    public void init() {
        NodeInfo rootInfo = new NodeInfo(ZooKeeperPath.ROOT, "/");

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootInfo);
        root.setAllowsChildren(true);

        _treeModel = new DefaultTreeModel(root, true);

        _tree = new JTree(_treeModel);
        _tree.setShowsRootHandles(true);
        _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (node.getUserObject() instanceof NodeInfo) {
                    NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
                    if (!nodeInfo.isChildrenInitialized()) {
                        addChildren(node);
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        _tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) _tree.getLastSelectedPathComponent();
                if (node == null || !(node.getUserObject() instanceof NodeInfo)) {
                    return;
                }
                NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
                if (!nodeInfo.isDataInitialized()) {
                    setData(node);
                }
            }
        });

        JPanel topPanel = new JPanel(new GridBagLayout());
        final JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                connectButton.setEnabled(false);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        topPanel.add(connectButton, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        topPanel.add(new JLabel(""), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        add(topPanel, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(new JScrollPane(_tree), constraints);
    }

    private void connect() {
        _zooKeeper.init();
    }

    private void addChildren(DefaultMutableTreeNode node) {
        NodeInfo nodeInfo = (NodeInfo) node.getUserObject();

        List<String> childNames = _zooKeeper.getChildren(nodeInfo.getPath());
        Collections.sort(childNames);

        for (String childName : childNames) {
            NodeInfo childNodeInfo = new NodeInfo(ZooKeeperPath.childPath(nodeInfo.getPath(), childName), childName);
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(childNodeInfo);
            child.setAllowsChildren(true);
            _treeModel.insertNodeInto(child, node, node.getChildCount());
        }

        nodeInfo.setChildrenInitialized(true);
    }

    private void setData(DefaultMutableTreeNode node) {
        NodeInfo nodeInfo = (NodeInfo) node.getUserObject();

        ZooKeeperNode zooKeeperNode = _zooKeeper.getNode(nodeInfo.getPath());
        if (zooKeeperNode == null) {
            // stale data
            _treeModel.removeNodeFromParent(node);
            return;
        }

        byte[] data = zooKeeperNode.getData();
        Object object = null;

        if (data != null) {
            for (PathPattern pattern : _deserializerMap.keySet()) {
                if (pattern.matches(node.getPath())) {
                    Class<?> deserializerClass = _deserializerMap.get(pattern);
                    try {
                        Method method = deserializerClass.getDeclaredMethod("fromByteArray", byte[].class);
                        object = method.invoke(null, (Object) data);

                        if (object != null) {
                            addObject(node, object);
                            _tree.expandPath(new TreePath(node.getPath()));
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (object == null) {
                String stringData = zooKeeperNode.getStringData();
                if (StringUtils.isNotBlank(stringData)) {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(stringData);
                    child.setAllowsChildren(false);
                    _treeModel.insertNodeInto(child, node, 0);
                    _tree.expandPath(new TreePath(node.getPath()));
                }
            }
        }

        nodeInfo.setDataInitialized(true);
    }

    private void addObject(DefaultMutableTreeNode node, Object object) {
        if (Iterable.class.isAssignableFrom(object.getClass())) {
            Iterable<?> iterable = (Iterable<?>) object;
            int i = 0;
            for (Object element : iterable) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(i);
                child.setAllowsChildren(true);
                _treeModel.insertNodeInto(child, node, node.getChildCount());
                addObject(child, element);
                i++;
            }
            return;
        }

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);
        PropertyDescriptor[] descriptors = beanWrapper.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Object value = beanWrapper.getPropertyValue(propertyName);
                if (value == null || Number.class.isAssignableFrom(value.getClass()) || value.getClass().equals(String.class)) {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(propertyName + "=" + value);
                    child.setAllowsChildren(false);
                    _treeModel.insertNodeInto(child, node, node.getChildCount());
                } else {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(propertyName);
                    child.setAllowsChildren(true);
                    _treeModel.insertNodeInto(child, node, node.getChildCount());
                    addObject(child, value);
                }
            }
        }
    }

    private volatile boolean _connected = false;

    @Override
    public void onClientConnected() {
        LOG.debug("onClientConnected()");
        if (!_connected) {
            try {
                addChildren((DefaultMutableTreeNode) _treeModel.getRoot());
            } finally {
                _connected = true;
            }
        }
    }

    @Override
    public void onClientDisconnected() {
        LOG.debug("onClientDisconnected()");
    }

    @Override
    public void onSessionExpired() {
        LOG.debug("onSessionExpired()");
        try {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) _treeModel.getRoot();
            root.removeAllChildren();
            _treeModel.nodeStructureChanged(root);
        } finally {
            _connected = false;
        }
    }
    */
}
