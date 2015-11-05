package net.cdn.fastdfs;

import net.cdn.fastdfs.utils.PropertyUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lk on 2015/10/16.
 */
public class Upload {
    static FileOutputStream fileOutputStream;
    static String fileName;
    public static AtomicInteger poolSize = new AtomicInteger(0);
    public static AtomicInteger runSize = new AtomicInteger(0);
    public static void main(String[] args) throws Exception {
        PropertyUtil.init(args[0], "info");
        PropertyUtil propertyUtil = PropertyUtil.getInstance("info");
        String timeSpanStr = propertyUtil.getProperty("timeSpan");
        int timeSpan = Integer.valueOf(timeSpanStr);
        fileOutputStream = new FileOutputStream(new File(propertyUtil.getProperty("logUrl")));
        fileName = propertyUtil.getProperty("tsFile");
        new Thread(new Runnable() {
            class HandlerThread implements Runnable {
                private Socket socket;
                public HandlerThread(Socket client) {
                    socket = client;
                    new Thread(this).start();
                }
                public void run() {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream= socket.getOutputStream();
                        ByteArrayOutputStream baos   =   new   ByteArrayOutputStream();
                        byte[] bytes = new byte[1000];
                        inputStream.read(bytes);
                        baos.write(bytes);
                        String str = baos.toString();
                        String need = str.substring(str.indexOf("GET")+3,str.indexOf("HTTP")).trim();
                        need = URLDecoder.decode(need, "utf-8");
                        if("/count".equals(need)){
                            System.out.println(poolSize.get());
                            outputStream.write(("poolSize"+poolSize.get()+"runSize"+runSize.get()).getBytes());
                        }
                        inputStream.close();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (Exception e) {
                                socket = null;
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(12345);
                    while (true) {
                        // 一旦有堵塞, 则表示服务器与客户端获得了连接
                        Socket client = serverSocket.accept();
                        // 处理这次连接
                        new HandlerThread(client);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30,30,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        while (true){
            Thread.sleep(timeSpan);
            if(poolSize.get()>30000){
                continue;}
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runSize.incrementAndGet();
                    long start = System.currentTimeMillis();
                    FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
                    try {
                        String fileId = fastdfsClient.upload(new File(fileName));
                        long end = System.currentTimeMillis();
                        addName(fileId, end - start);
                        runSize.decrementAndGet();
                        poolSize.decrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            poolSize.incrementAndGet();
        }
    }
    synchronized static void addName(String fileId,long needTime){
        try {
            fileOutputStream.write((fileId+","+needTime+"\r\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(needTime>5000){
            System.out.println(needTime);
        }
    }
}
