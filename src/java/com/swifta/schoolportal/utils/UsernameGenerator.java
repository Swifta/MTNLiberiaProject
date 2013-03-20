/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

import java.util.Random;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class UsernameGenerator {
    
    Logger logger = Logger.getLogger(UsernameGenerator.class);
    
    public String generateUsername(String schoolName){
        StringTokenizer str = new StringTokenizer(schoolName," ");
        String username = str.nextToken();
        logger.info(username);
        return username+generatePin(AppValues.pinLength);
    }

    public String generatePin(int length){
            // random PIN number
            Random r = new Random();
            int digit = 1000 + r.nextInt((int)(Math.pow(10, length)));
            if (String.valueOf(digit).length() == length) {
                return (String.valueOf(digit));
            } else {
                return generatePin(length);
            }
        
    }    
    
}
