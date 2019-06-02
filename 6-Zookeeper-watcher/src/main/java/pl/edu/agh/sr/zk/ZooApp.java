package pl.edu.agh.sr.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ZooApp {

    private static final String NODE_PATH = "/z";

    private final ZooKeeper zooKeeper;

    private final Watcher watcher;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        if (args.length < 2) {
            System.out.println("Illegal arguments number.");
            printHelp();
        } else {
            String host = args[0];
            String[] appArgs = Arrays.copyOfRange(args, 1, args.length);
            ZooApp zooApp = new ZooApp(host, appArgs);
            zooApp.run();
        }

    }

    ZooApp(String host, String[] appArgs) throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(host, 3000, (ignore) -> {});
        watcher = new ZooWatcher(zooKeeper, appArgs);
    }

    void run() throws IOException, KeeperException, InterruptedException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine().trim();

            switch (line) {
                case "p":
                case "print":
                    printTree(NODE_PATH);
                    break;
                case "q":
                case "quit":
                    System.exit(0);
                case "h":
                case "help":
                    printHelp();
                    break;
            }
        }
    }

    void printTree(String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) != null) {
            System.out.println(path);
            for (String child : zooKeeper.getChildren(path, false)) {
                printTree(path + "/" + child);
            }
        } else
            System.out.println(path + " does not exists.");
    }

    private static void printHelp() {
        System.out.println("Usage:\n" +
                "p/print - print znodes tree /z\n" +
                "g/quit - exit\n" +
                "h/help - print help\n");
    }

}

