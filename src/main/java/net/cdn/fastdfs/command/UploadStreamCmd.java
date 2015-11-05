package net.cdn.fastdfs.command;

import net.cdn.fastdfs.data.Result;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by lk on 2015/10/27.
 */
public class UploadStreamCmd extends AbstractCmd<String>{
    private InputStream inputStream;
    @Override
    public Result<String> exec(Socket socket) throws IOException {
        request(socket.getOutputStream(), this.inputStream);
        Response response = response(socket.getInputStream());
        if(response.isSuccess()){
            byte[] data = response.getData();
            String group = new String(data, 0,	FDFS_GROUP_NAME_MAX_LEN).trim();
            String remoteFileName = new String(data,FDFS_GROUP_NAME_MAX_LEN, data.length - FDFS_GROUP_NAME_MAX_LEN);
            Result<String> result = new Result<String>(response.getCode());
            result.setData(group + "/" + remoteFileName);
            return result;
        }else{
            Result<String> result = new Result<String>(response.getCode());
            result.setMessage("Error");
            return result;
        }
    }

    /**
     *
     * @param input
     * @param name 文件名，如果传入一个完整文件名，将截取扩展名
     * @param storePathIndex
     */
    public UploadStreamCmd(InputStream input,String name,byte storePathIndex) throws IOException {
        super();
        this.inputStream = input;
        this.requestCmd = STORAGE_PROTO_CMD_UPLOAD_FILE;
        this.body2Len = input.available();
        this.responseCmd = STORAGE_PROTO_CMD_RESP;
        this.responseSize = -1;
        this.body1 = new byte[15];
        Arrays.fill(body1, (byte) 0);
        this.body1[0] = storePathIndex;
        byte[] fileSizeByte = long2buff(this.body2Len);
        byte[] fileExtNameByte = getFileExtNameByte(name);
        int fileExtNameByteLen = fileExtNameByte.length;
        if(fileExtNameByteLen>FDFS_FILE_EXT_NAME_MAX_LEN){
            fileExtNameByteLen = FDFS_FILE_EXT_NAME_MAX_LEN;
        }
        System.arraycopy(fileSizeByte, 0, body1, 1, fileSizeByte.length);
        System.arraycopy(fileExtNameByte, 0, body1, fileSizeByte.length + 1, fileExtNameByteLen);
    }

    private byte[] getFileExtNameByte(String fileName) {
        String fileExtName = null;
        int nPos = fileName.lastIndexOf('.');
        if (nPos > 0 && fileName.length() - nPos <= FDFS_FILE_EXT_NAME_MAX_LEN + 1) {
            fileExtName = fileName.substring(nPos + 1);
            if (fileExtName != null && fileExtName.length() > 0) {
                return fileExtName.getBytes(charset);
            }else{
                return new byte[0];
            }
        } else {
            //传入的即为扩展名
            return fileName.getBytes(charset);
        }

    }
}
