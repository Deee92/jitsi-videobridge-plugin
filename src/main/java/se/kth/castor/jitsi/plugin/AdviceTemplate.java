package se.kth.castor.jitsi.plugin;

import com.thoughtworks.xstream.XStream;

public interface AdviceTemplate {
    XStream xStream = new XStream();

    static String[] setUpFiles(String path) {
        String storagePath = "/tmp/jitsi-object-data/" + path;
        String receivingObjectFilePath = storagePath + "-receiving.xml";
        String paramObjectsFilePath = storagePath + "-params.xml";
        String returnedObjectFilePath = storagePath + "-returned.xml";
        return new String[]{receivingObjectFilePath, paramObjectsFilePath, returnedObjectFilePath};
    }
}
