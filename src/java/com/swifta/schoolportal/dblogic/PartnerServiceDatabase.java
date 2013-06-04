/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.PartnerService;
import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.managedbeans.PortalSession;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author princeyekaso
 */
public class PartnerServiceDatabase {

    Logger logger = Logger.getLogger(SchoolDatabase.class);
    private PortalSession portalSession;

    public List<PartnerService> getAllPartnerServices() throws SQLException {
        String sqlQuery = "select * from Partner_service";
        logger.info(sqlQuery);

        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);

        ArrayList<PartnerService> partnerServices = new ArrayList<PartnerService>();

        while (res.next()) {
            PartnerService ps = new PartnerService();

            ps.setName(res.getString("name"));
            ps.setId(res.getInt(1));

            partnerServices.add(ps);
        }
        return partnerServices;
    }

   
}
