package pl.edu.agh.sr.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ZooWatcher implements Watcher {

    private static final Logger log = LoggerFactory.getLogger(ZooWatcher.class);

    private static final String NODE_PATH = "/z";

    private final ZooKeeper zooKeeper;

    private String[] appArgs;

    private Process appProcess = null;

    public ZooWatcher(ZooKeeper zooKeeper, String[] appArgs) throws KeeperException, InterruptedException{
        this.zooKeeper = zooKeeper;
        this.appArgs = appArgs;

        if (zooKeeper.exists(NODE_PATH, this) == null)
            waitForExistence();
        else
            printlnChildren();
    }

    @Override
    public void process(WatchedEvent event) {
        assert event.getPath().equals(NODE_PATH);
        switch (event.getType()) {
            case NodeDeleted:
                log.info("Node deleted");
                stopApp();
                waitForExistence();
                break;
            case NodeCreated:
                log.info("Node created");
                startApp();
                printlnChildren();
                break;
            case NodeChildrenChanged:
                printlnChildren();
                break;
        }
    }

    private void waitForExistence() {
        zooKeeper.exists(NODE_PATH, this, null, null);
    }

    private void printlnChildren() {
        try {
            int count = zooKeeper.getChildren(NODE_PATH, this).size();
            System.out.println(String.format("Path %s has %d children", NODE_PATH, count));
        } catch (KeeperException | InterruptedException e) {
            log.warn("ZooKeeper getChildren failed:" + e.getMessage());
            stopApp();
            waitForExistence();
        }
    }

    private void stopApp() {
        if (appProcess != null && appProcess.isAlive()) {
            appProcess.destroy();
        }
    }

    private void startApp() {
        if (appProcess == null || !appProcess.isAlive()) {
            try {
                appProcess = Runtime.getRuntime().exec(appArgs);
            } catch (IOException e) {
                log.warn("Run app failed:" + e.getMessage());
            }
        }
    }
}
