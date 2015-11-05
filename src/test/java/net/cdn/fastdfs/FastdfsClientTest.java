package net.cdn.fastdfs;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class FastdfsClientTest {
	

	@Test
	public void testFastdfsClient() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		String fileId = fastdfsClient.upload(file);
		System.out.println("fileId:"+fileId);
		assertNotNull(fileId);
		String url = fastdfsClient.getUrl(fileId);
		assertNotNull(url);
		System.out.println("url:"+url);
		Map<String,String> meta = new HashMap<String, String>();
		meta.put("fileName", file.getName());
		boolean result = fastdfsClient.setMeta(fileId, meta);
		assertTrue(result);
		Map<String,String> meta2 = fastdfsClient.getMeta(fileId);
		assertNotNull(meta2);
		System.out.println(meta2.get("fileName"));
		result = fastdfsClient.delete(fileId);
		assertTrue(result);
		fastdfsClient.close();
	}


    @Test
    public void testUploadSlave() throws Exception {
        FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
        URL fileUrl = this.getClass().getResource("/Koala.jpg");
        File file = new File(fileUrl.getPath());
        String fileId = fastdfsClient.upload(file);
        System.out.println("fileId:"+fileId);
        assertNotNull(fileId);

        String result = fastdfsClient.uploadSlave(file,fileId,"_200x200","jpg");
        assertNotNull(result);
        System.out.println(result);
        fastdfsClient.close();
    }
    @Test
    public void testUpload120() throws Exception{
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30,30,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        while (true){
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
                    try {
                        String fileId = fastdfsClient.upload(new File("D:/usr/local/testfile/20151009142049_5.000.ts"));
                        System.out.println("fileId:"+fileId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread.sleep(50);
        }
    }

    @Test
    public void testUploadMeta() throws Exception {
        FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
        URL fileUrl = this.getClass().getResource("/Koala.jpg");
        File file = new File(fileUrl.getPath());
        Map<String,String> meta = new HashMap<String, String>();
        meta.put("size","200x200");

        String fileId = fastdfsClient.upload(file,null,meta);
        System.out.println("fileId:"+fileId);
        assertNotNull(fileId);

        //set second meta
        meta.put("size","300x300");
        meta.put("nickname","nickname");
        fastdfsClient.setMeta(fileId,meta);

        Map<String,String> a = fastdfsClient.getMeta(fileId);
        assertNotNull(a);
        assertEquals(a.get("size"),"300x300");
        assertEquals(a.get("nickname"),"nickname");
        fastdfsClient.close();
    }

}
