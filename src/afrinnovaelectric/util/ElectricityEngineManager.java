package afrinnovaelectric.util;

import java.io.InputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author princeyekaso
 */
public class ElectricityEngineManager {

    private Vector drivers;
    private Map pools = new HashMap();
    private static int clients = 0;
    private static Logger logger = Logger.getLogger(ElectricityEngine.class);
    private static ElectricityEngineManager electricityEngineManager = null;

    private ElectricityEngineManager() {
        init();
    }

    public static synchronized ElectricityEngineManager getInstance() {

        if (null == electricityEngineManager) {
            logger.info("GOT NEW ELECTRIC MANAGER.....=====================================");
            electricityEngineManager = new ElectricityEngineManager();
        }
        clients++;
        return electricityEngineManager;
    }

    private void init() {
        InputStream is = getClass().getResourceAsStream("/db.properties");
        Properties dbProps = new Properties();
        try {
            dbProps.load(is);
        } catch (Exception e) {
            System.err.println("Can't read the properties file. "
                    + "Make sure db.properties is in the CLASSPATH");
            logger.info("Exception LOADING FILE ==============================================");
            return;
        }
        String logFile = dbProps.getProperty("logfile",
                "DBConnectionManager.log");

        createPools(dbProps);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        throw new CloneNotSupportedException();

    }

    public Socket getConnection(String name) {
        ElectricityEngine pool = (ElectricityEngine) pools.get(name);
        if (pool != null) {
            logger.info("Socket Pool exists......");
            return pool.getConnection();
        }
        return null;
    }

    public ElectricityEngine getConnectionEngine(String name) {
        ElectricityEngine pool = (ElectricityEngine) pools.get(name);
        if (pool != null) {
            logger.info("Socket Pool exists......");
            return pool;
        }
        return null;
    }

    public Socket getConnection(String name, long time) {
        ElectricityEngine pool = (ElectricityEngine) pools.get(name);
        if (pool != null) {
            return pool.getConnection(time);
        }
        return null;
    }

    public void freeConnection(String name, Socket con) {
        ElectricityEngine pool = (ElectricityEngine) pools.get(name);
        if (pool != null) {
            logger.info("Socket Pool freed.....");
            pool.freeConnection(con);
            logger.info("Socket pool freed and returning......");
        }
    }

    public synchronized void release() {
        // Wait until called by the last client
        if (--clients != 0) {
            return;
        }

        Collection allPools = pools.values();
        if (allPools != null) {
            while (allPools.iterator().hasNext()) {
                ElectricityEngine pool = (ElectricityEngine) allPools.iterator().next();
                pool.release();
            }
        }

    }

    private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        logger.info("AFTER GETTING PROPS LIST..............");
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".ipaddress")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String ipAddress = props.getProperty(poolName + ".ipaddress");
                if (ipAddress == null) {
                    logger.debug("No ip address specified for " + poolName);
                    continue;
                }
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                String portNumber = props.getProperty(poolName + ".portno", "0");
                int max, portNo;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                    portNo = Integer.valueOf(portNumber).intValue();
                } catch (NumberFormatException e) {
                    logger.debug("Invalid maxconn value " + maxconn + " for "
                            + poolName);
                    max = 0;
                    portNo = 0;
                }
                ElectricityEngine pool =
                        new ElectricityEngine(poolName, ipAddress, portNo, max);
                pools.put(poolName, pool);
                logger.debug("Initialized pool " + poolName);
            }
        }
        logger.info("AFTER GETTING POOLS");
    }
}
