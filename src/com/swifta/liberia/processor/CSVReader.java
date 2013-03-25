/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.liberia.processor;

import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class CSVReader {

    public static void main(String[] args) {
        
        Logger.getLogger(CSVReader.class).info("Starting ... ");
        
        try {
            new Utils().browseDirectory(DBUtils.fr.getProperty("csvPath"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Logger.getLogger(CSVReader.class).info("Updated ... ");
    }
}
