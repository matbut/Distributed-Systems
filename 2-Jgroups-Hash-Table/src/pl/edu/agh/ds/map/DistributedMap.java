package pl.edu.agh.ds.map;

import java.util.concurrent.ConcurrentHashMap;

public class DistributedMap implements SimpleStringMap {

    private final ConcurrentHashMap<String, Integer> hashMap;

    private final MapCommunication mapCommunication;

    public DistributedMap() throws Exception{
        hashMap = new ConcurrentHashMap<>();
        mapCommunication = new MapCommunication(hashMap,"230.100.200.17");
    }

    @Override
    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return hashMap.get(key);
    }

    @Override
    public void put(String key, Integer value) throws Exception{
        mapCommunication.putUpdate(key,value);
    }

    @Override
    public Integer remove(String key) throws Exception{
        Integer value = get(key);
        mapCommunication.removeUpdate(key);
        return value;
    }
}
