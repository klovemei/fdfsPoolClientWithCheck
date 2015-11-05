package net.cdn.fastdfs;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.cdn.fastdfs.client.*;
import net.cdn.fastdfs.data.GroupInfo;
import net.cdn.fastdfs.data.Result;
import net.cdn.fastdfs.data.StorageInfo;
import net.cdn.fastdfs.data.UploadStorage;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastdfsClientImpl extends AbstractClient implements FastdfsClient{
	
	private static Logger logger = LoggerFactory.getLogger(FastdfsClientImpl.class);
	private GenericObjectPool<TrackerClient> trackerClientPool;
	private GenericKeyedObjectPool<String, StorageClient> storageClientPool;
	private Map<String,String> storageIpMap = new ConcurrentHashMap<String, String>();
	
	public FastdfsClientImpl(List<String> trackerAddrs) throws Exception {
		super();
		trackerClientPool = new GenericObjectPool<TrackerClient>(new TrackerClientFactoryNoKey(trackerAddrs));
		storageClientPool = new GenericKeyedObjectPool<String, StorageClient>(new StorageClientFactory());
		updateStorageIpMap();
	}
	
	public FastdfsClientImpl(GenericObjectPool<TrackerClient> trackerClientPool,
			GenericKeyedObjectPool<String, StorageClient> storageClientPool) {
		super();
		this.trackerClientPool = trackerClientPool;
		this.storageClientPool = storageClientPool;
	}
	
	

	@Override
	public void close() {
		this.trackerClientPool.close();
		this.storageClientPool.close();
	}

	@Override
	public String getDownloadAddress(String fileId) {
		TrackerClient trackerClient = null;
		String downAddr = null;
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject();
			Result<String> result = trackerClient.getDownloadStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result.getCode()==0){
				downAddr = result.getData();
				if(FastdfsClientConfig.nginxPort==80)
				downAddr = "http://"+downAddr+"/"+fileId;else {
					downAddr = "http://"+downAddr+":"+FastdfsClientConfig.nginxPort+"/"+fileId;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerClient);
			}
		}
		return downAddr;
	}

	@Override
    public String upload(File file, String ext, Map<String, String> meta) throws Exception {
        TrackerClient trackerClient = null;
        StorageClient storageClient = null;
        String storageAddr = null;
        String fileId = null;
        try {
			long ts = System.currentTimeMillis();
            trackerClient = trackerClientPool.borrowObject();
            Result<UploadStorage> result = trackerClient.getUploadStorage();
			long te = System.currentTimeMillis();
			if(te-ts>1000){
				System.out.println("tracker:" + (te - ts));
			}
            if(result.getCode()==0){
                storageAddr = result.getData().getAddress();
                storageClient = storageClientPool.borrowObject(storageAddr);

                String extname =  ext;
                if (ext == null) {
                    extname = getFileExtName(file);
                }
                Result<String> result2 = storageClient.upload(file, extname, result.getData().getPathIndex());
                if(result2.getCode()==0){
                    fileId = result2.getData();
                    //if meta key value
                    if (meta !=null ) {
                        this.setMeta(fileId,meta);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            if(storageClient!=null){
                storageClientPool.returnObject(storageAddr, storageClient);
            }
            if(trackerClient!=null){
                trackerClientPool.returnObject(trackerClient);
            }
        }
        return fileId;
    }

	@Override
	public String upload(InputStream inputStream, String name, Map<String, String> meta) throws Exception {
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		String fileId = null;
		try {
			trackerClient = trackerClientPool.borrowObject();
			Result<UploadStorage> result = trackerClient.getUploadStorage();
			if(result.getCode()==0){
				storageAddr = result.getData().getAddress();
				storageClient = storageClientPool.borrowObject(storageAddr);

				String extname =  name;
				if (name == null) {
					return null;
				}
				Result<String> result2 = storageClient.upload(inputStream, extname, result.getData().getPathIndex());
				if(result2.getCode()==0){
					fileId = result2.getData();
					//if meta key value
					if (meta !=null ) {
						this.setMeta(fileId,meta);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerClient);
			}
		}
		return fileId;
	}

	@Override
	public String upload(File file, String ext, String group, Map<String, String> meta) throws Exception {
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		String fileId = null;
		try {
			trackerClient = trackerClientPool.borrowObject();
			Result<UploadStorage> result = trackerClient.getUploadStorage(group);
			if(result.getCode()==0){
				storageAddr = result.getData().getAddress();
				storageClient = storageClientPool.borrowObject(storageAddr);

				String extname =  ext;
				if (ext == null) {
					extname = getFileExtName(file);
				}
				Result<String> result2 = storageClient.upload(file, extname, result.getData().getPathIndex());
				if(result2.getCode()==0){
					fileId = result2.getData();
					//if meta key value
					if (meta !=null ) {
						this.setMeta(fileId,meta);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerClient);
			}
		}
		return fileId;
	}

	@Override
	public String upload(InputStream inputStream, String name, String group, Map<String, String> meta) throws Exception {
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		String fileId = null;
		try {
			trackerClient = trackerClientPool.borrowObject();
			Result<UploadStorage> result = trackerClient.getUploadStorage(group);
			if(result.getCode()==0){
				storageAddr = result.getData().getAddress();
				storageClient = storageClientPool.borrowObject(storageAddr);

				String extname =  name;
				if (name == null) {
					return null;
				}
				Result<String> result2 = storageClient.upload(inputStream, extname, result.getData().getPathIndex());
				if(result2.getCode()==0){
					fileId = result2.getData();
					//if meta key value
					if (meta !=null ) {
						this.setMeta(fileId,meta);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerClient);
			}
		}
		return fileId;
	}

	@Override
    public String uploadSlave(File file, String fileid, String prefix, String ext) throws Exception {
        TrackerClient trackerClient = null;
        StorageClient storageClient = null;
        String storageAddr = null;
        String fileId = null;
        try {
            trackerClient = trackerClientPool.borrowObject();

            if(fileid!=null){
                String[] tupple = splitFileId(fileid);
                String groupname = tupple[0];
                String filename = tupple[1];

                Result<String> result = trackerClient.getUpdateStorageAddr(groupname,filename);
                if(result.getCode() == 0) {
                    storageAddr = result.getData();
                    storageClient = storageClientPool.borrowObject(storageAddr);
                    Result<String> result2 = storageClient.uploadSlave(file, filename, prefix, ext, null);
                    if(result2.getCode()==0){
                        fileId = result2.getData();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            if(storageClient!=null){
                storageClientPool.returnObject(storageAddr, storageClient);
            }
            if(trackerClient!=null){
                trackerClientPool.returnObject(trackerClient);
            }
        }
        return fileId;
    }

    private void updateStorageIpMap() throws Exception{
		TrackerClient trackerClient = null;
		try {
			trackerClient = trackerClientPool.borrowObject();
			Result<List<GroupInfo>> result = trackerClient.getGroupInfos();
			if(result.getCode()==0){
				List<GroupInfo> groupInfos = result.getData();
				for(GroupInfo groupInfo:groupInfos){
					Result<List<StorageInfo>>  result2= trackerClient.getStorageInfos(groupInfo.getGroupName());
					if(result2.getCode()==0){
						List<StorageInfo> storageInfos = result2.getData();
						for(StorageInfo storageInfo:storageInfos){
							String hostPort = storageInfo.getDomainName();
							if(storageInfo.getStorageHttpPort()!=80){
								hostPort = hostPort + ":" + storageInfo.getStorageHttpPort();
							}
							storageIpMap.put(storageInfo.getIpAddr()+":"+storageInfo.getStoragePort(), hostPort);
						}
					}
				}
			}else{
				throw new Exception("Get getGroupInfos Error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if(trackerClient!=null){
				trackerClientPool.returnObject( trackerClient);
			}
		}
	}
	
	private String getDownloadHostPort(String storageAddr) throws Exception{
		String downloadHostPort = storageIpMap.get(storageAddr);
		if(downloadHostPort==null){
			updateStorageIpMap();
			downloadHostPort = storageIpMap.get(storageAddr);
		}
		return downloadHostPort;
	}
	
	@Override
	public Boolean setMeta(String fileId, Map<String, String> meta)
			throws Exception {
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Boolean result = null;
		String storageAddr=null;
		try{
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject();
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Boolean> result3 = storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName,meta);
				if(result3.getCode()==0||result3.getCode()==0){
					result = result3.getData();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject( trackerClient);
			}
		}
		return result;
	}

	@Override
	public Map<String, String> getMeta(String fileId) throws Exception {
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Map<String, String> meta = null;
		String storageAddr=null;
		try{
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject();
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Map<String,String>> result3 = storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
				if(result3.getCode()==0||result3.getCode()==0){
					meta = result3.getData();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerClient);
			}
		}
		return meta;
	}

	public String getUrl(String fileId) throws Exception{
		TrackerClient trackerClient = null;
		String url = null;
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject();
			Result<String> result = trackerClient.getDownloadStorageAddr(fastDfsFile.group,fastDfsFile.fileName);
			if(result.getCode()==0){
				String hostPort = getDownloadHostPort(result.getData());
				url = "http://"+hostPort+"/"+fastDfsFile.fileName;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if(trackerClient!=null){
				trackerClientPool.returnObject( trackerClient);
			}
		}
		return url;
	}
	
	public String upload(File file) throws Exception{
		String fileName = file.getName();
		return upload(file, fileName);
	}

	public String upload(File file,String fileName) throws Exception{
		return this.upload(file,fileName,null);
	}
	
	public Boolean delete(String fileId) throws Exception{
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Boolean result=false;
		String storageAddr=null;
		try{
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject();
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Boolean> result3 = storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
				if(result3.getCode()==0||result3.getCode()==0){
					result = true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject( trackerClient);
			}
		}
		return result;
	}

    private String getFileExtName(File file) {
        String name = file.getName();
        if (name!=null ) {
            int i = name.lastIndexOf('.');
            if (i>-1) {
                return name.substring(i+1);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
	
	private class FastDfsFile{
		private String group;
		private String fileName;
		
		public FastDfsFile(String fileId) {
			super();
			int pos = fileId.indexOf("/");
			group = fileId.substring(0, pos);
			fileName = fileId.substring(pos+1);
		}
		
	}
	

}
