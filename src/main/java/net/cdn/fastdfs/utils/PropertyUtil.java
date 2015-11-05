package net.cdn.fastdfs.utils;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * 类说明：资源文件读取工具类
 * @文件名：PropertyUtil.java
 * @创建时间：2014年8月14日 下午6:25:22
 */

public class PropertyUtil {
	
   private static Map instance = Collections.synchronizedMap(new HashMap());
   private static Object lock = new Object();
   protected String sourceUrl;
   protected Properties properties;
   
   protected PropertyUtil(String sourceUrl){
	   this.sourceUrl = sourceUrl;
	   load();
   }
  
   public static PropertyUtil getInstance(String id){
	   synchronized (lock) {
          PropertyUtil manager = (PropertyUtil) instance.get(id);
          if(manager ==null){
        	  return null;
          }
          return manager;
	}
   }

	public static void init(String url,String id){
		synchronized (lock) {
			PropertyUtil manager = (PropertyUtil) instance.get(id);
			if(manager ==null){
				manager = new PropertyUtil(url);
				instance.put(id,manager);
			}
		}
	}

   private synchronized void load(){
	   try{
		   properties = new Properties();
		   properties.load(new FileInputStream(sourceUrl));
	   }catch(Exception e){
		   throw new RuntimeException("sourceUrl = " + sourceUrl + " file load error!",e);
	   }
   }
   
   public synchronized String getProperty(String key) {
         return properties.getProperty(key);
}
   
}
