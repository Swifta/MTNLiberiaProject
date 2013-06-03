
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author princeyekaso
 */
public class HttpClient {

    String url = "41.204.194.188:8932";
    String port = "";
    String data = "<ipayMsg client=\"MTNLonestar\" term=\"1\" seqNum=\"0\" time=\"2002-05-16 10:55:30 +0200\"><elecMsg ver=\"1.3\">\n"
            + "<custInfoReq>\n"
            + "<ref>13610570003</ref>\n"
            + "<meter>88123456ww789</meter>\n"
            + "</custInfoReq>\n"
            + "</elecMsg>\n"
            + "</ipayMsg>";

    public void sendData() throws Exception {
        Logger.getLogger(HttpClient.class).info("Connecting ... ");
        Socket socket = new Socket("41.204.194.188", 8932);
        socket.setKeepAlive(true);
        Logger.getLogger(HttpClient.class).info("Opening port ... ");
        OutputStream out = socket.getOutputStream();
        Logger.getLogger(HttpClient.class).info("Writing to port ... " + out.toString());
        out.write(wrap(data.getBytes()));
        out.flush();
        Logger.getLogger(HttpClient.class).info("Information sent ... ");

        InputStream in = socket.getInputStream();
        Logger.getLogger(HttpClient.class).info("Available : " + in.available());
        Logger.getLogger(HttpClient.class).info("InputStream : " + in.toString());
        Logger.getLogger(HttpClient.class).info("Reading ... ");
        Logger.getLogger(HttpClient.class).info("Response ... " + new String(unWrap(in)));

        in.close();
        out.close();
        socket.close();

    }

    public byte[] wrap(byte[] msg) throws Exception {
        int len = msg.length;
        if (len > 65535) {
            throw new IllegalArgumentException("Exceeds 65535 bytes.");
        }
        byte firstByte = (byte) (len >>> 8);
        byte secondByte = (byte) len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len + 2);
        baos.write(firstByte);
        baos.write(secondByte);
        baos.write(msg);
        return baos.toByteArray();
    }

    public byte[] unWrap(InputStream inputStream) throws Exception {
        int firstByte = inputStream.read();
        if (firstByte == -1) {
            throw new IOException("End of Stream while trying to read vli byte 1");
        }
        int firstByteValue = firstByte << 8;
        int secondByteValue = inputStream.read();
        if (secondByteValue == -1) {
            throw new IOException("End of Stream reading vli byte 2.");
        }
        int len = firstByteValue + secondByteValue;
        byte[] message = new byte[len];
        int requestLen;
        int readLen;
        int currentIndex = 0;
        while (true) {
            requestLen = len - currentIndex;
            readLen = inputStream.read(message, currentIndex, requestLen);
            if (readLen == requestLen) {
                break;  // Message is complete.
            }

            // Either data was not yet available, or End of Stream.
            currentIndex += readLen;
            int nextByte = inputStream.read();
            if (nextByte == -1) {
                throw new IOException("End of Stream at " + currentIndex);
            }
            message[currentIndex++] = (byte) nextByte;
        }
        return message;
    }

    public static void main(String[] args) {
        try {
            HttpClient ipay = new HttpClient();
            ipay.sendData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
