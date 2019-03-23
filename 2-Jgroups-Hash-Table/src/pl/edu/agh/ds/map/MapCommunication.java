package pl.edu.agh.ds.map;


import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class MapCommunication {

    private final JChannel mapsChannel;

    private final ConcurrentHashMap<String, Integer> hashMap;

    private final String IPAddress;

    MapCommunication(ConcurrentHashMap<String, Integer> hashMap, String IPAddress) throws Exception{
        mapsChannel = new JChannel(false);
        this.hashMap = hashMap;
        this.IPAddress = IPAddress;
        init();
    }

    private void init() throws Exception{
        ProtocolStack stack=new ProtocolStack();
        mapsChannel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr",InetAddress.getByName(IPAddress)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new STATE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2());

        stack.init();
        mapsChannel.connect("operation");

        mapsChannel.setReceiver( new Receiver(mapsChannel,hashMap));

        mapsChannel.getState(null, 1000);
    }

    void putUpdate(String key, Integer value) throws Exception{
        MessageContent messageContent = new MessageContent(MessageContent.Type.PUT,key,value);
        update(messageContent);
    }

    void removeUpdate(String key) throws Exception{
        MessageContent messageContent = new MessageContent(MessageContent.Type.REMOVE,key,null);
        update(messageContent);
    }

    void close(){
        mapsChannel.close();
    }

    private void update(MessageContent messageContent) throws Exception{
        Message msg=new Message(null, null, messageContent);
        mapsChannel.send(msg);
    }

}

