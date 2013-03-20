/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.utils.AppValues;
import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author modupealadeojebi
 */

class ConnectionReaper extends Thread {

    private JDCConnectionPool pool;
    private final long delay=300000;
    
    private Logger logger = Logger.getLogger(ConnectionReaper.class);

    ConnectionReaper(JDCConnectionPool pool) {
        this.pool=pool;
    }

    @Override
    public void run() {
        while(true) {
           try {
              sleep(delay);
           } catch( InterruptedException e) { }
           pool.reapConnections();
        }
    }
}

public class JDCConnectionPool {

    private Vector connections;
    private String url, user, password;
    final private long timeout = 60000;
    private ConnectionReaper reaper;
    private Logger logger = Logger.getLogger(ConnectionReaper.class);

    public JDCConnectionPool(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        connections = new Vector(AppValues.maxPool);
        reaper = new ConnectionReaper(this);
        reaper.start();
    }

    public synchronized void reapConnections() {
        //logger.info("Reaping connections ... Starting");
        long stale = System.currentTimeMillis() - timeout;
        Enumeration connlist = connections.elements();

        while ((connlist != null) && (connlist.hasMoreElements())) {
            JDCConnection conn = (JDCConnection) connlist.nextElement();

            if ((conn.inUse()) && (stale > conn.getLastUse())
                    && (!conn.validate())) {
                removeConnection(conn);
            }
        }
        //logger.info("Reaping connections ... Done");
    }

    public synchronized void closeConnections() {
        //logger.info("Closing connections ... Starting");
        Enumeration connlist = connections.elements();

        while ((connlist != null) && (connlist.hasMoreElements())) {
            JDCConnection conn = (JDCConnection) connlist.nextElement();
            removeConnection(conn);
        }
        //logger.info("Closing connections ... Done");
    }

    private synchronized void removeConnection(JDCConnection conn) {
        connections.removeElement(conn);
    }

    public synchronized JDCConnection getConnection() throws SQLException {
        //logger.info("Fetching a connection .... Started");
        JDCConnection c;
        
        for (int i = 0; i < connections.size(); i++) {
            c = (JDCConnection) connections.elementAt(i);
            if (c.lease()) {
                return c;
            }
        }

        Connection conn = DriverManager.getConnection(url, user, password);
        c = new JDCConnection(conn, this);
        c.lease();
        connections.addElement(c);
        //logger.info("Fetching a connection .... Done");
        return c;
    }

    public synchronized void returnConnection(JDCConnection conn) {
        conn.expireLease();
    }
}
