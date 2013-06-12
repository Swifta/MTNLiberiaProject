/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package afrinnovaelectric.util;

import java.net.Socket;

/**
 *
 * @author princeyekaso
 */
public interface ConnectionListener {

    public boolean processConnection(Socket socket);
}
