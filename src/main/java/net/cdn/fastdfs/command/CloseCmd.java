package net.cdn.fastdfs.command;

import java.io.IOException;
import java.net.Socket;

import net.cdn.fastdfs.data.Result;

public class CloseCmd extends AbstractCmd<Boolean> {
	
	public CloseCmd() {
		super();
		this.requestCmd = FDFS_PROTO_CMD_QUIT;
		this.responseCmd = TRACKER_PROTO_CMD_RESP;
	}

	@Override
	public Result<Boolean> exec(Socket socket) throws IOException {
		request(socket.getOutputStream());
		return new Result<Boolean>(SUCCESS_CODE,true);
	}


}
