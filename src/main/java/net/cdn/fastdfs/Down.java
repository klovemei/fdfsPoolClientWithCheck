package net.cdn.fastdfs;


/**
 * Created by lk on 2015/10/26.
 */
public class Down {
    public static void main(String[] args) {
        System.out.print(FastdfsClientFactory.getFastdfsClient().getDownloadAddress("group2/M00/AE/79/wKgBslYtorKAH0dUAA_mpOYB9TI6711.ts"));
    }
}
