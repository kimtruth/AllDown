package google;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class Downloader {
    static final int BUFFER_SIZE = 4096;
    static final String USER_AGENT = "Mozilla/5.0";

    public static String getFileSize(String fileURL) {
        URL url = null;
        try {
            url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("HEAD");
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            int contentLength = httpConn.getContentLength();

            return formatFileSize(contentLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int getFileFromUrl(String fileName, String fileURL, String saveDir) {

        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = httpConn.getResponseCode();
            int contentLength = httpConn.getContentLength();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);

                System.out.println("fileName : " + fileName);

                InputStream in = httpConn.getInputStream();
                String saveFilePath = saveDir + fileName;

                FileOutputStream out = new FileOutputStream(saveFilePath);

                int tmp = -1;

                byte[] buf = new byte[BUFFER_SIZE];
                int downLength = 0;

                while ((tmp = in.read(buf)) != -1) {
                    out.write(buf, 0, tmp);
                    downLength += tmp;
                    System.out.println(fileName + " : Download (" + (int) ((double) downLength / contentLength * 100) + ")%");
                }

                out.close();
                in.close();

            } else {
                System.out.println("[*] File Not Found(" + fileName + "), Response code : " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String formatFileSize(long size) {
        if (size == -1)
            return "ERROR";

        String hrSize = null;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}
