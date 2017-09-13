package org.apache.hadoop.hbase.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperListener;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto detect pub-network  realHostname <=> virtualHostname,
 * The virtualHostname is offen a address that can be connect all over the internet,
 * While the realHostname is offen a local hostname that the hbase rs/ms really use.
 * Example:
 *   hb-01928382.example.com <=> hb-proxy-pub-01928382.example.com
 */
public class VHostMapListener extends ZooKeeperListener {
    static final Log LOG = LogFactory.getLog(VHostMapListener.class);

    private String node;
    private Map<String,String> vhostMap ;
    private static VHostMapListener vHostMapListener ;

    public static VHostMapListener getInstance(String root, Configuration conf) {
        if (vHostMapListener == null) {
            synchronized (VHostMapListener.class) {
                if (vHostMapListener == null) {
                    try {
                        vHostMapListener = new VHostMapListener(root,
                                new ZooKeeperWatcher(conf, "VHostMapListener", null));
                    } catch (IOException e) {
                        LOG.warn("vhostmap listener create failed,will not enable pub-conn.");
                    }
                }
            }
        }
        return vHostMapListener;
    }

    /**
     * Construct a ZooKeeper event listener.
     * @param path
     * @param watcher
     */
    private VHostMapListener(String path, ZooKeeperWatcher watcher) {
        super(watcher);
        this.node = path;
        this.vhostMap = new HashMap<String,String>();
        init();
    }

    private void init(){
        try {
            ZKUtil.createWithParents(watcher, node);
        } catch (KeeperException e) {
            LOG.warn("create vhost root node failed, pub-network still disabled.", e);
            return;
        }
        this.watcher.registerListener(this);
        updateVhostMap();
    }

    private void watchUpdate(){
        try {
            ZKUtil.watchAndCheckExists(watcher,node);
        } catch (KeeperException e) {
            LOG.warn("watch vhost node failed, vhostmap will not update later.", e);
        }
    }

    @Override
    public void nodeChildrenChanged(String path){
        if(!path.startsWith(node)) return;
        updateVhostMap();
    }

    private void updateVhostMap(){
        try {
            List<String> children = ZKUtil.listChildrenAndWatchThem(watcher,node);
            if(children != null && children.size() > 0 ){
                synchronized (vhostMap){
                    vhostMap.clear();
                    for(String childNode : children){
                        byte[] data = ZKUtil.getDataAndWatch(watcher, ZKUtil.joinZNode(node,childNode));
                        if(data != null && data.length > 0){
                            String child = Bytes.toString(data);
                            //replace old value if exists
                            vhostMap.put(childNode,child);
                        }
                    }
                }
            }
        } catch (KeeperException e) {
            LOG.warn("encount error while getting data from " + node);
            e.printStackTrace();
        }
        watchUpdate();
    }

    public String getVhost(String host){
        return this.vhostMap.get(host);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("vhostMap=[ ");
        for(Map.Entry<String,String> e : vhostMap.entrySet()){
            sb.append(e.getKey() + ":" + e.getValue() + " ");
        }
        sb.append("]");
        return sb.toString();
    }
}
