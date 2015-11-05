package net.cdn.fastdfs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import net.cdn.fastdfs.command.*;
import net.cdn.fastdfs.data.GroupInfo;
import net.cdn.fastdfs.data.Result;
import net.cdn.fastdfs.data.StorageInfo;
import net.cdn.fastdfs.data.UploadStorage;
import net.cdn.fastdfs.FastdfsClientConfig;

public class TrackerClientImpl implements TrackerClient{
	
	private Socket socket;
	private String host;
	private Integer port;
	private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
	private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;
	
	public TrackerClientImpl(String address) throws IOException {
		super();
		String[] hostport = address.split(":");
		this.host = hostport[0];
		this.port = Integer.valueOf(hostport[1]);
		getSocket();
	}
	
	public TrackerClientImpl(String address,Integer connectTimeout, Integer networkTimeout) throws IOException {
		this(address);
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	
	private Socket getSocket() throws IOException{
		if(socket==null){
			socket = new Socket();
			socket.setSoTimeout(networkTimeout);
			socket.connect(new InetSocketAddress(host, port),connectTimeout);
		}
		return socket;
	}
	

	public void close() throws IOException{
		Socket socket = getSocket();
		Command<Boolean> command = new CloseCmd();
		command.exec(socket);
		socket.close();
		socket = null;
	}

	
	public Result<UploadStorage> getUploadStorage() throws IOException{
		Socket socket = getSocket();
		Command<UploadStorage> command = new QueryUploadCmd();
		return command.exec(socket);
	}

	@Override
	public Result<UploadStorage> getUploadStorage(String group) throws IOException {
		Socket socket = getSocket();
		Command<UploadStorage> command = new QueryUploadCmd(group);
		return command.exec(socket);
	}

	public Result<String> getUpdateStorageAddr(String group,String fileName) throws IOException{
		Socket socket = getSocket();
		Command<String> cmd = new QueryUpdateCmd(group,fileName);
		return cmd.exec(socket);
	}
	
	public Result<String> getDownloadStorageAddr(String group,String fileName) throws IOException{
		Socket socket = getSocket();
		Command<String> cmd = new QueryDownloadCmd(group,fileName);
		return cmd.exec(socket);
	}
	
	public Result<List<GroupInfo>> getGroupInfos() throws IOException{
		Socket socket = getSocket();
		Command<List<GroupInfo>> cmd = new GroupInfoCmd();
		return cmd.exec(socket);
	}
	
	public Result<List<StorageInfo>> getStorageInfos(String group) throws IOException{
		Socket socket = getSocket();
		Command<List<StorageInfo>> cmd = new StorageInfoCmd(group);
		return cmd.exec(socket);
	}
	@Override
	public boolean isClosed() {

		if (this.socket == null) {
			return true;
		}

		if (this.socket.isClosed()){
			return true;
		}else {
			//根据fastdfs的Active_Test_Cmd测试连通性
			ActiveTestCmd atcmd = new ActiveTestCmd();
			try {
				Result<Boolean> result = atcmd.exec(getSocket());
				//True,表示连接正常
				if(result.getData()){
					return false;
				}else {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//有异常，直接丢掉这个连接，让连接池回收
			return true;
		}
	}

}
