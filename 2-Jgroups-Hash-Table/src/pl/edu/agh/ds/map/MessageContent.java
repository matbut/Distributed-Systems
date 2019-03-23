package pl.edu.agh.ds.map;

import java.io.Serializable;

class MessageContent implements Serializable {

    final Type type;
    final String key;
    final Integer value;

    MessageContent(Type type, String key, Integer value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    enum Type {
        PUT,
        REMOVE;
    }
}
