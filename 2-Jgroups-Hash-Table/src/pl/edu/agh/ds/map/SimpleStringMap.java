package pl.edu.agh.ds.map;

public interface SimpleStringMap {

    boolean containsKey(String key);

    Integer get(String key);

    void put(String key, Integer value) throws Exception;

    Integer remove(String key) throws Exception;
}
