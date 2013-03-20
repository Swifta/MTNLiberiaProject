/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author Opeyemi
 */
public class SendUserPin {
    
    private Logger logger = Logger.getLogger(SendUserPin.class);
    
    public boolean sendPin(String pin, String destination){
        try {
            String message = "Your one time PIN is " + pin + ". Please note that your PIN expires in 60 seconds";
            return this.sendMessage(message, destination);
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    public boolean sendMessage(String message, String destination) throws IOException, JSONException {

        Message msg = new Message();
        msg.setDestination(destination);
        msg.setMessage(message);
        msg.setMessageID(0);

        String json = new Message().toJson(msg);
        
        logger.info("Connecting to Swifta SMS Gateway to send SMS .... ");
        
        Socket socket = new Socket(AppValues.gatewayAddress, Integer.parseInt(AppValues.gatewayPort));
        socket.setSoTimeout(AppValues.gatewayTimeOut);

        logger.info("Connection status : " + socket.isConnected());
        logger.info("Connection close status : " + socket.isClosed());
        logger.info("Socket connected ... " + socket.toString());


        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String m = new Message().toJson(msg);
        dos.writeUTF(m);
        dos.flush();

        logger.info("Message sent to server ... "+m);

        //read
        DataInputStream din = new DataInputStream(socket.getInputStream());
        m = din.readUTF();
        logger.info("Message Response : " + m);

        JSONObject j = new JSONObject(m);

        socket.close();
        if (m.length() > 0 && m != null) {
            return j.getBoolean("sent");
        } else {
            return false;
        }
    
    }
}
