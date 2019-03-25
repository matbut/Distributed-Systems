package pl.edu.agh.ds;

import pl.edu.agh.ds.map.DistributedMap;
import pl.edu.agh.ds.map.SimpleStringMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleApp {

    private static SimpleStringMap map;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ConsoleApp consoleApp = new ConsoleApp();
        consoleApp.parseArgs(args);
        consoleApp.listen();
    }

    private void parseArgs(String[] args){
        try{
            String IPAddress = "230.100.200.17";
            if(args.length>=1)
                IPAddress = args[0];
            map = new DistributedMap(IPAddress);
        }catch (Exception e){
            System.err.println("Can't create new map: " + e.getMessage());
        }
    }

    private void listen(){
        InputStreamReader inp = new InputStreamReader(System.in);

        String msg;

        try(BufferedReader reader = new BufferedReader(inp)){
            for (boolean c = true; c; c = !msg.equals("exit")) {
                msg = reader.readLine();
                parseCommand(msg);
            }
        }catch(IOException e){
            System.err.println("Can't read from console: " + e.getMessage());
        }
    }

    private void parseCommand(String msg){
        String[] split = msg.split("\\s+");

        String command = split[0];
        if(split.length==1)
            parseNoArgsCommand(command);
        else{
            String key = split[1];
            if(split.length==2)
                parseOneArgCommand(command,key);
            else{
                if(split.length==3){
                    Integer value = Integer.valueOf(split[2]);
                    parseTwoArgsCommand(command,key,value);
                }else
                    System.err.println("Too many arguments");
            }
        }
    }

    private void parseNoArgsCommand(String command){
        switch (command){
            case "discard":
                try{
                    map.discard();
                } catch (Exception e){
                    System.err.println("Can't discard: " + e.getMessage());
                }
                return;
            case "resume":
                try{
                    map.resume();
                } catch (Exception e){
                    System.err.println("Can't resume: " + e.getMessage());
                }
                return;
            default:
                System.err.println("Illegal command");
        }
    }

    private void parseOneArgCommand(String command, String key) {
        switch (command) {
            case "get":
                Integer getValue = map.get(key);
                System.out.println(key + " " + getValue + " was get from map");
                return;
            case "remove":
                try {
                    Integer removeValue = map.remove(key);
                    System.out.println(key + " " + removeValue + " was removed from map");
                } catch (Exception e) {
                    System.err.println("Can't remove from map: " + e.getMessage());
                }
                return;
            case "contains":
                boolean contains = map.containsKey(key);
                System.out.println((contains ? "map contains " : "map doesn't contain ") + key);
                return;
            default:
                System.err.println("Illegal command");
        }
    }
    private void parseTwoArgsCommand(String command, String key, Integer value){
        switch (command) {
            case "put":
                try {
                    map.put(key, value);
                    System.out.println(key + " " + value + " was put into map");
                } catch (Exception e) {
                    System.err.println("Can't put into map: " + e.getMessage());
                }
                return;
            default:
                System.err.println("Illegal command");
        }
    }
}