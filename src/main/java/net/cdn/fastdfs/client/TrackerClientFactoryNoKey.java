package net.cdn.fastdfs.client;

import net.cdn.fastdfs.FastdfsClientConfig;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by lk on 2015/10/23.
 */
public class TrackerClientFactoryNoKey implements PooledObjectFactory<TrackerClient>{

    private List<String> trackAddresses;
    private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
    private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;

    public TrackerClientFactoryNoKey(List<String> trackAddresses) {
        super();
        this.trackAddresses = trackAddresses;
    }

    public TrackerClientFactoryNoKey(List<String> trackAddresses, Integer connectTimeout, Integer networkTimeout) {
        super();
        this.trackAddresses = trackAddresses;
        this.connectTimeout = connectTimeout;
        this.networkTimeout = networkTimeout;
    }

    @Override
    public PooledObject makeObject() throws Exception {
        int i=0;
        PooledObject<TrackerClient> pooledTrackerClient=null;
        do{
            try {
                String trackAddr = getTrackerAddr();
                TrackerClient trackerClient = new TrackerClientImpl(trackAddr,connectTimeout,networkTimeout);
                pooledTrackerClient = new DefaultPooledObject<TrackerClient>(trackerClient);
            } catch (IOException e) {
                if(i++>5){
                    throw e;
                }
            }
        }while (pooledTrackerClient==null||!validateObject(pooledTrackerClient));
        return pooledTrackerClient;
    }

    @Override
    public void destroyObject(PooledObject<TrackerClient> pooledObject) throws Exception {
        TrackerClient trackerClient = pooledObject.getObject();
        trackerClient.close();
    }

    @Override
    public boolean validateObject(PooledObject<TrackerClient> pooledObject) {
        TrackerClient trackerClient= pooledObject.getObject();
        if (trackerClient.isClosed()) {
            //return false to ignore this closed client
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void activateObject(PooledObject<TrackerClient> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<TrackerClient> pooledObject) throws Exception {

    }
    private String getTrackerAddr(){
        Random r = new Random();
        int i = r.nextInt(trackAddresses.size());
        return trackAddresses.get(i);
    }
}
