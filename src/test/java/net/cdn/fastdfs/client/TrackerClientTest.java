package net.cdn.fastdfs.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import net.cdn.fastdfs.data.GroupInfo;
import net.cdn.fastdfs.data.Result;
import net.cdn.fastdfs.data.UploadStorage;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;

import org.junit.Assert;
import org.junit.Test;

public class TrackerClientTest {

	@Test
	public void testGetUploadStorageAddr() throws NumberFormatException, UnknownHostException, IOException {
		TrackerClient trackerClient = new TrackerClientImpl("10.125.176.138:22122");
		Result<UploadStorage> result = trackerClient.getUploadStorage();
		assertEquals(0, result.getCode());
		Assert.assertEquals("10.125.176.138:23000", result.getData().getAddress());
		trackerClient.close();
	}
	
	@Test
	public void testGetDownloadStorageAddr() throws IOException {
		TrackerClient trackerClient = new TrackerClientImpl("10.125.176.138:22122");
		Result<String> result = trackerClient.getDownloadStorageAddr("group1","M00/00/00/Cn2wilM00puAa0xSAANVQ4eIxAM143.jpg");
		assertEquals(0, result.getCode());
		assertEquals("10.125.176.138:23000",result.getData());
		trackerClient.close();
	}
	
	@Test
	public void testGetUpdateStorageAddr() throws IOException {
		TrackerClient trackerClient = new TrackerClientImpl("10.125.176.138:22122");
		Result<String> result = trackerClient.getUpdateStorageAddr("group1","M00/00/00/Cn2wilM00puAa0xSAANVQ4eIxAM143.jpg");
		assertEquals(0, result.getCode());
		assertEquals("10.125.176.138:23000",result.getData());
		trackerClient.close();
	}
	
	@Test
	public void testGetGroupInfos() throws NumberFormatException, UnknownHostException, IOException{
		TrackerClient trackerClient = new TrackerClientImpl("10.125.176.138:22122");
		Result<List<GroupInfo>> groupInfos = trackerClient.getGroupInfos();
		assertNotNull(groupInfos);
		trackerClient.close();
	}
	@Test
	public void testPool() throws Exception {
		GenericObjectPool genericObjectPool = new GenericObjectPool(new PooledObjectFactory(){
			@Override
			public PooledObject makeObject() throws Exception {
				System.out.println("make");
				PooledObject object = new DefaultPooledObject(1);
				return object;
			}

			@Override
			public void destroyObject(PooledObject pooledObject) throws Exception {
				System.out.println("destroy");
			}

			@Override
			public boolean validateObject(PooledObject pooledObject) {
				System.out.println("test");
				if(((Integer) pooledObject.getObject())<2)
				return true;else return false;
			}

			@Override
			public void activateObject(PooledObject pooledObject) throws Exception {
				System.out.println("activate");
			}

			@Override
			public void passivateObject(PooledObject pooledObject) throws Exception {
				System.out.println("passivate");
			}
		});
		genericObjectPool.setTestOnBorrow(true);
		genericObjectPool.setTestOnCreate(true);
		Integer o = (Integer)genericObjectPool.borrowObject();
		o++;
		genericObjectPool.returnObject(o);
		genericObjectPool.borrowObject();
	}
}
