package pl.edu.agh.ds;

import pl.edu.agh.ds.map.DistributedMap;
import pl.edu.agh.ds.map.SimpleStringMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        SimpleStringMap map = new DistributedMap();

        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inp);
        String msg;

        for (boolean c = true; c; c = !msg.equals("quit")) {
            msg = reader.readLine();
            execute(map, msg);
        }
        reader.close();
    }

    private static void execute(SimpleStringMap map,String msg) throws Exception{
        String[] split = msg.split("\\s+");

        String cmd = "nothing", key = null;
        Integer value = null;

        if (split.length >= 2) {
            cmd = split[0];
            key = split[1];
        }
        if (split.length >= 3)
            value = Integer.parseInt(split[2]);

        switch (cmd){
            case "put":
                map.put(key, value);
                System.out.println(key + " " + value + " was put to map");
                break;
            case "get":
                value = map.get(key);
                System.out.println(key + " " + value + " was get from map");
                break;
            case "remove":
                value = map.remove(key);
                System.out.println(key + " " + value + " was removed from map");
                break;
            case "contains":
                boolean contains = map.containsKey(key);
                System.out.println((contains ?  "map contains " : "map doesn't contain ") + key);
                break;
        }
    }
}