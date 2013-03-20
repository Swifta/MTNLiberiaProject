/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.utils.AppValues;
import java.io.IOException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
@ManagedBean
@SessionScoped
public class PortalSession {

    /**
     * Creates a new instance of PortalSession
     */
    
    private HttpSession appSession;
    private Logger logger = Logger.getLogger(PortalSession.class);    
    
    public PortalSession() {
        FacesContext fc = FacesContext.getCurrentInstance();
        appSession = (HttpSession) fc.getExternalContext().getSession(true);
        appSession.setAttribute("pageid", 0);
        appSession.setMaxInactiveInterval(AppValues.timeOut);                
        PortalInitBean.initApp();
    }
    
    public HttpSession getAppSession() {
        return appSession;
    }

    public void setAppSession(HttpSession appSession) {
        this.appSession = appSession;
    }    
    
    public void redirect(int id) throws IOException{
        //redirects the page
        FacesContext fc = FacesContext.getCurrentInstance();
        this.appSession.setAttribute("pageid",id);
        logger.info("Redirecting to page id : "+id);
        fc.getExternalContext().redirect("index.xhtml");                
    }    
    
}
