/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

/**
 *
 * @author princeyekaso
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidator {

    private Pattern pattern;
    private Matcher matcher;
    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
    private static final String SQL_INJECTION_PATTERN = "((?=.*[-!~#$%^&/\\|])(?=.*\\s))";

    public FieldValidator() {
    }

    /**
     * Validate password with regular expression
     *
     * @param password password for validation
     * @return true valid password, false invalid password
     */
    public boolean validatePassword(final String password) {
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();

    }

    public boolean validatePossibleInjection(final String stringLiteral) {
        pattern = Pattern.compile(SQL_INJECTION_PATTERN);
        matcher = pattern.matcher(stringLiteral);
        return matcher.matches();
    }
}
