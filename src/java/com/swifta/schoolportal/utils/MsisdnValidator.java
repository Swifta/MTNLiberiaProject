/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

/**
 *
 * @author Opeyemi
 */
public class MsisdnValidator {

    public MsisdnValidator() {
        
    }
    
    public boolean validLength(String msisdn) {
        return msisdn.length() == 13 ? true : false;
    }

    public String transform(String msisdn) {
        if (msisdn.length() == 11) {
            String num = msisdn.substring(1, 11);
            return "234" + num;
        } else {
            return msisdn;
        }
    }

    public boolean validNumber(String msisdn) {
        boolean valid = false;
        if (msisdn.length() == 13) {
            String prefix = msisdn.substring(0, 6);
            valid = prefix.equals("234803") || prefix.equals("234806") || prefix.equals("234813") || prefix.equals("234816") || prefix.equals("234703") || prefix.equals("234706") || prefix.equals("234810");
        } else {
            String prefix = msisdn.substring(0, 4);
            valid = prefix.equals("0803") || prefix.equals("0806") || prefix.equals("0813") || prefix.equals("0816") || prefix.equals("0703") || prefix.equals("0706") || prefix.equals("0810");
        }
        return valid;
    }
    
    public int getAgentType(String msisdn){
        if(Character.isDigit(msisdn.charAt(0)) || Character.isDigit(msisdn.charAt(2))){
            return AppValues.AGENT_PAGE;
        }else{
            return AppValues.MERCHANT_PAGE;
        }
    }    
    
}
