package net.cdn.fastdfs;

import java.util.List;

import net.cdn.fastdfs.client.StorageClient;
import net.cdn.fastdfs.client.StorageClientFactory;
import net.cdn.fastdfs.client.TrackerClient;
import net.cdn.fastdfs.client.TrackerClientFactoryNoKey;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastdfsClientFactory {

    private static Logger logger = LoggerFactory.getLogger(FastdfsClientFactory.class);
	
	private static volatile FastdfsClient fastdfsClient;
    private static FastdfsClientConfig config = null;

    private final static String configFile = "FastdfsClient.properties";

    public FastdfsClientFactory() {
    }

    public static FastdfsClient getFastdfsClient(){
        if (fastdfsClient == null) {
            synchronized (FastdfsClient.class) {
                if (fastdfsClient == null) {
                    try {
                        config = new FastdfsClientConfig(configFile);
                    } catch (ConfigurationException e) {
                        logger.warn("Load fastdfs config failed.",e);
                    }
                    int connectTimeout = config.getConnectTimeout();
                    int networkTimeout = config.getNetworkTimeout();
                    List<String> trackerAddrs = config.getTrackerAddrs();
                    TrackerClientFactoryNoKey trackerClientFactory = new TrackerClientFactoryNoKey(trackerAddrs,connectTimeout, networkTimeout);
                    StorageClientFactory storageClientFactory = new StorageClientFactory(connectTimeout, networkTimeout);
                    GenericObjectPoolConfig trackerClientPoolConfig = config.getTrackerClientPoolConfig();
                    trackerClientPoolConfig.setTestOnBorrow(true);
                    GenericKeyedObjectPoolConfig storageClientPoolConfig = config.getStorageClientPoolConfig();
                    storageClientPoolConfig.setTestOnBorrow(true);
                    GenericObjectPool<TrackerClient> trackerClientPool = new GenericObjectPool<TrackerClient>(trackerClientFactory, trackerClientPoolConfig);
                    GenericKeyedObjectPool<String,StorageClient> storageClientPool = new GenericKeyedObjectPool<String,StorageClient>(storageClientFactory, storageClientPoolConfig);
                    fastdfsClient = new FastdfsClientImpl(trackerClientPool,storageClientPool);
                }
            }
        }
        return fastdfsClient;
    }


	

}
