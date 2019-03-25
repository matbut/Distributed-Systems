package pl.edu.agh.ds.map;

import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver extends ReceiverAdapter {

    private final JChannel mapsChannel;

    private final ConcurrentHashMap<String, Integer> hashMap;

    Receiver(JChannel mapsChannel, ConcurrentHashMap<String, Integer> hashMap) {
        this.mapsChannel = mapsChannel;
        this.hashMap = hashMap;
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("[COMUNICATION]received view " + view);
        if (view instanceof MergeView) {
            System.out.println("[COMUNICATION]MergeView!");
            ViewHandler handler = new ViewHandler(mapsChannel, (MergeView) view);
            handler.start();
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception{
        Util.objectToStream(hashMap, new DataOutputStream(output));
    }

    @Override
    public void setState(InputStream input) throws Exception{
        hashMap.clear();
        hashMap.putAll((ConcurrentHashMap<String, Integer>) Util.objectFromStream(new DataInputStream(input)));
        System.out.println("[COMUNICATION] Update map state, " + hashMap.size() + " values added");
    }

    @Override
    public void receive(Message msg) {
        MessageContent messageContent = (MessageContent) msg.getObject();

        switch (messageContent.type) {
            case PUT: {
                hashMap.put(messageContent.key, messageContent.value);
                System.out.println("[COMUNICATION] " + messageContent.key + " " + messageContent.value + " was put to map");
                break;
            }
            case REMOVE: {
                Integer value = hashMap.remove(messageContent.key);
                System.out.println("[COMUNICATION] " + messageContent.key + " " + value + " was removed from map");
                break;
            }
        }
    }

    private static class ViewHandler extends Thread {
        JChannel ch;
        MergeView view;

        private ViewHandler(JChannel ch, MergeView view) {
            this.ch = ch;
            this.view = view;
        }

        public void run() {
            View tmp_view = view.getSubgroups().get(0);
            Address local_addr = ch.getAddress();
            if (!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("[COMUNICATION]Not member of the new primary partition ("
                        + tmp_view + "), will re-acquire the state");
                try {
                    ch.getState(null, 30000);
                } catch (Exception ignored) {
                }
            } else {
                System.out.println("[COMUNICATION]Not member of the new primary partition ("
                        + tmp_view + "), will do nothing");
            }
        }
    }
}
