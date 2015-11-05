package net.cdn.fastdfs.client;

import java.io.IOException;
import java.util.List;

import net.cdn.fastdfs.data.GroupInfo;
import net.cdn.fastdfs.data.Result;
import net.cdn.fastdfs.data.StorageInfo;
import net.cdn.fastdfs.data.UploadStorage;

public interface TrackerClient {

	public Result<UploadStorage> getUploadStorage() throws IOException;
	public Result<UploadStorage> getUploadStorage(String group) throws IOException;
	public Result<String> getUpdateStorageAddr(String group,String fileName) throws IOException;
	public Result<String> getDownloadStorageAddr(String group,String fileName) throws IOException;
	public Result<List<GroupInfo>> getGroupInfos() throws IOException;
	public Result<List<StorageInfo>> getStorageInfos(String group) throws IOException;
	public void close() throws IOException;

	public boolean isClosed();
}
