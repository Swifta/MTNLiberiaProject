/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.liberia.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class Utils {

    private Logger logger = Logger.getLogger(Utils.class);
    private DBUtils dbUtils = new DBUtils();

    public String getSchoolTag(String schoolName) {
        StringTokenizer str = new StringTokenizer(schoolName, " ");
        String tag = "";
        while (str.hasMoreElements()) {
            tag += str.nextToken().toUpperCase().charAt(0);
            if (tag.length() == 2) {
                break;
            }
        }
        return tag;
    }

    public String readCSVFile(File file) throws FileNotFoundException,
            IOException, SQLException, ClassNotFoundException {
        String schoolName = file.getName().substring(0, file.getName().length() - 4);
        dbUtils.createSchool(schoolName);
        if (this.getFileExtension(file.getName()).equals("csv")) {
            FileInputStream fin = new FileInputStream(file);
            byte[] b = new byte[fin.available()];
            int i = 1;
            String tag = this.getSchoolTag(schoolName);
            int schoolID = dbUtils.findSchoolID(schoolName);
            while (fin.read(b) != -1) {
                StringTokenizer strToken = new StringTokenizer(new String(b), "\n");
                while (strToken.hasMoreElements()) {
                    dbUtils.createStudent(tag + "00" + i, strToken.nextToken(), schoolID);
                    i++;
                }
            }
        } else {
            logger.info("An unknown content type found.");
        }
        return null;
    }

    public void browseDirectory(String fileDir) throws IOException, SQLException, FileNotFoundException, ClassNotFoundException {
        File file = new File(fileDir);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                this.readCSVFile(f);
            }
        }
    }

    public String getFileExtension(String fileName) {
        return fileName.substring(fileName.length() - 3, fileName.length());
    }
}
