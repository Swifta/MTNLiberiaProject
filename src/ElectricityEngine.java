
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author princeyekaso
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;

import afrinnovaelectric.IpayMsg;
import afrinnovaelectric.MTNLiberiaCompressor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.Marshaller;
import org.apache.commons.io.IOUtils;

/**
 * * Servlet implementation class FileCounter
 */
public class ElectricityEngine extends HttpServlet {

    private static final long serialVersionUID = 1L;
    int count;
    Logger logger = Logger.getLogger(ElectricityEngine.class);

    public void init() throws ServletException {
    }

    public void service(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        request.getAttribute("xml");
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();

        out.println("INSIDE THE WEBSITE" + request.getInputStream().toString());
        logger.info("inside the website");

        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) request.getInputStream(), writer, request.getCharacterEncoding());
        String theString = writer.toString();

        out.println("The string retrieved/......." + theString);

        // out.print(unMarshal(request.getInputStream()));
        unMarshal((String) request.getAttribute("xml"));
    }

    public String unMarshal(String xml) {
        IpayMsg ipay = null;
        try {
            File file = new File("c:\\test.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(IpayMsg.class);
            xml = "<ipayMsg client=\"ipay\" term=\"1\" seqNum=\"0\" time=\"2002-05-16 10:55:35 +0200\">\n" +
"<elecMsg ver=\"2.30\">\n" +
"<trialVendRes>\n" +
"<ref>136105500001</ref>\n" +
"<res code=\"elec000\">OK</res>\n" +
"<customer util=\"Eskom Online\" addr=\"24 Web Crt, Web Road\" agrRef=\"123456\"\n" +
"suppGrpRef=\"\" type=\"STS\">Nevil Best</customer>\n" +
"<summary total=\"10000\" totalTax=\"1400\" totalStd=\"6000\" totalStdTax=\"840\"\n" +
"totalOther=\"4000\" totalOtherTax=\"560\" totalStdUnits=\"124.34\"\n" +
"totalBsstUnits=\"35.5\" />\n" +
"<rtlrMsg>Customer not registered. Log fault at vendor or contact\n" +
"Eskom.</rtlrMsg>\n" +
"<customerMsg>Meter 12345678901 not registered. Log fault at vendor or contact\n" +
"Eskom.</customerMsg>\n" +
"</trialVendRes>\n" +
"</elecMsg>\n" +
"</ipayMsg>";
            
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            //    jaxbUnmarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            //    ipay = (IpayMsg) jaxbUnmarshaller.unmarshal(file);
            ipay = (IpayMsg) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            System.out.println(ipay);
            marshalRequest(ipay);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        if (ipay == null) {
            return "N/A";
        } else {
            return ipay.toString();
        }
    }

    public static String marshalRequest(IpayMsg object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(IpayMsg.class);
        StringWriter result = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);

        m.marshal(object, result);
        System.out.println(result);
        MTNLiberiaCompressor comp = new MTNLiberiaCompressor();
        try {

            System.out.println("The byte arr : " + comp.wrap(result.toString().getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // Set a cookie for the user, so that the counter does not increate
        // everytime the user press refresh
        service(request, response);

    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // Set a cookie for the user, so that the counter does not increate
        // everytime the user press refresh
        service(request, response);

    }

    public void destroy() {
        super.destroy();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
