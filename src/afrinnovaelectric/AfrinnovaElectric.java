/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package afrinnovaelectric;

import com.afrinnova.api.schoolfees.service.exception.AccountDAOException;
import com.afrinnova.api.schoolfees.service.model.AccountLookup;
import com.afrinnova.api.schoolfees.service.model.TransactionOb;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import scheduler.DateUtilities;

/**
 *
 * @author princeyekaso
 */
public class AfrinnovaElectric {

    /**
     * @param args the command line arguments
     */
    private Constants constant = new Constants();
    public boolean revRequestStatus = false;
    public boolean advResponseStatus = false;
    public int repCount = 0, seqCount = 0;
    private String origTime = null;
    private DateUtilities du = new DateUtilities();
    private static final Logger logger = Logger.getLogger(AfrinnovaElectric.class);
    DecimalFormat decimalFormat = new DecimalFormat("00000");
    public AfrinnovaElectric(){
        seqCount = 0;
    }

    public byte[] wrap(byte[] msg) throws Exception {
        int len = msg.length;
        if (len > 65535) {
            throw new IllegalArgumentException("Exceeds 65535 bytes.");
        }
        byte firstByte = (byte) (len >>> 8);
        byte secondByte = (byte) len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len + 2);
        baos.write(firstByte);
        baos.write(secondByte);
        baos.write(msg);
        return baos.toByteArray();
    }

    public byte[] unWrap(InputStream inputStream) throws Exception {
        int firstByte = inputStream.read();
        if (firstByte == -1) {
            throw new IOException("End of Stream while trying to read vli byte 1");
        }
        int firstByteValue = firstByte << 8;
        int secondByteValue = inputStream.read();
        if (secondByteValue == -1) {
            throw new IOException("End of Stream reading vli byte 2.");
        }
        int len = firstByteValue + secondByteValue;
        byte[] message = new byte[len];
        int requestLen;
        int readLen;
        int currentIndex = 0;
        while (true) {
            requestLen = len - currentIndex;
            readLen = inputStream.read(message, currentIndex, requestLen);
            if (readLen == requestLen) {
                break;  // Message is complete.
            }

            // Either data was not yet available, or End of Stream.
            currentIndex += readLen;
            int nextByte = inputStream.read();
            if (nextByte == -1) {
                throw new IOException("End of Stream at " + currentIndex);
            }
            message[currentIndex++] = (byte) nextByte;
        }
        return message;
    }

    public String sendData(String data,int timeout) throws Exception {
        logger.info("Connecting ... ");
        Socket socket = new Socket("41.204.194.188", 8932);
        socket.setKeepAlive(true);
        socket.setSoTimeout(timeout);
        logger.info("Opening port ... ");
        OutputStream out = socket.getOutputStream();
        logger.info("Writing to port ... " + out.toString());
        out.write(wrap(data.getBytes()));
        out.flush();
        logger.info("Information sent ... ");

        InputStream in = socket.getInputStream();
        logger.info("Available : " + in.available());
        logger.info("InputStream : " + in.toString());
        logger.info("Reading ... ");
        String response = new String(unWrap(in));
        logger.info("Response ... " + response);

        in.close();
        out.close();
        socket.close();
        return response;

    }

    public IpayMsg generateCustomerRequestInfo(String meterNo, String ref, AccountLookup lookup) {
        IpayMsg ipay = initializeDefaultIpayMsg();
        try {
            lookup.insertTransactionHistory(ref, meterNo, constant.PAYTYPE_OTHER, constant.CUSTINFOREQ, constant.TXN_COMPLETE, 0, ipay.getTime());
        } catch (AccountDAOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        }
        Meter meter = new Meter();
        meter.setValue(meterNo);

        CustInfoReq customerInfoReq = new CustInfoReq();
        customerInfoReq.setMeter(meter);
        customerInfoReq.setRef(ref);

        ipay.getElecMsg().setCustInfoReq(customerInfoReq);
        try {
            return unMarshal(sendData(marshalRequest(ipay),constant.TIMEOUT));
        } catch (JAXBException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AfrinnovaElectric.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public IpayMsg generateSimpleVendRequest(String ref, TransactionOb transactionOb, AccountLookup lookup) {
        IpayMsg ipay = initializeDefaultIpayMsg();
        try {
            //send request
            logger.info("inserting transactions....");

            lookup.insertTransaction(transactionOb.getPayerAccountIdentifier(), transactionOb.getCustomerName(), transactionOb.getAcctref(), transactionOb.getAmount(), transactionOb.getPaymentRef(), transactionOb.getFundamoTransactionID(), ref, transactionOb.getStatuscode());
            lookup.insertTransactionHistory(ref, transactionOb.getAcctref(), constant.PAYTYPE_OTHER, constant.VENDREQ, constant.TXN_PENDING, repCount, ipay.getTime());
        } catch (AccountDAOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        }

        Amt amt = new Amt();
        PayType payType = new PayType();
        Meter meter = new Meter();

        meter.setValue(transactionOb.getAcctref());

        payType.setValue(constant.IPAY_PAYTYPE);
        amt.setCur(constant.IPAY_DEFAULT_CURRENCY);
        amt.setValue(transactionOb.getAmount());
        VendReq vendReq = new VendReq();
        vendReq.setAmt(amt);
        vendReq.setNumTokens(1);
        vendReq.setPayType(payType);
        vendReq.setRef(ref);
        vendReq.setMeter(meter);
        //  vendReq.setUseAdv(true);

        ipay.getElecMsg().setVendReq(vendReq);
        try {
            return unMarshal(sendData(marshalRequest(ipay),constant.TIMEOUT));
        } catch (JAXBException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AfrinnovaElectric.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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

    public IpayMsg unMarshal(String xml) {
        IpayMsg ipay = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(IpayMsg.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            ipay = (IpayMsg) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            System.out.println(ipay);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return ipay;

    }

    public IpayMsg generateSimpleVendAdvRequest(String origRef, String ref, String meterNo, AccountLookup lookup) {
        IpayMsg ipay = initializeDefaultIpayMsg();
        try {
            lookup.insertTransactionHistory(ref, meterNo, constant.PAYTYPE_OTHER, constant.VENDADVREQ, constant.TXN_PENDING, repCount, ipay.getTime());
        } catch (AccountDAOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        }
        VendAdvReq vendAdvReq = new VendAdvReq();
        String currentDate = du.formatDateToString(new Date(), "yyyy-mm-dd hh:mm:ss Z");

        if (this.repCount == 0) {
            this.origTime = currentDate;
        }
        //this is a test code before endpoint url
        if (this.repCount > 3) {
            advResponseStatus = false;
        }
        //ends here
        Integer repCount = 0;
        repCount = this.repCount == 0 ? null : this.repCount++;

        vendAdvReq.setOrigRef(origRef);
        vendAdvReq.setOrigTime(origTime);
        vendAdvReq.setRef(ref);
        vendAdvReq.setRepCount(repCount);

        ipay.getElecMsg().setVendAdvReq(vendAdvReq);
        try {
            return unMarshal(sendData(marshalRequest(ipay),constant.TIMEOUT));
        } catch (JAXBException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AfrinnovaElectric.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public IpayMsg generateSimpleVendRevReq(String origRef, String ref, String meterNo, AccountLookup lookup) {
        IpayMsg ipay = initializeDefaultIpayMsg();
        try {
            lookup.insertTransactionHistory(ref, meterNo, constant.PAYTYPE_OTHER, constant.VENDREVREQ, constant.TXN_PENDING, repCount, ipay.getTime());
        } catch (AccountDAOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AccountLookup.class.getName()).log(Level.SEVERE, null, ex);
        }
        VendRevReq vendRevReq = new VendRevReq();
        String currentDate = du.formatDateToString(new Date(), "yyyy-mm-dd hh:mm:ss Z");;

        if (this.repCount == 0) {
            this.origTime = currentDate;
        }
        //this is a test code before endpoint url
        if (this.repCount > 3) {
            revRequestStatus = false;
        }
        //ends here
        Integer repCount = 0;
        repCount = this.repCount == 0 ? null : this.repCount++;

        vendRevReq.setOrigRef(origRef);
        vendRevReq.setOrigTime(origTime);
        vendRevReq.setRef(ref);
        vendRevReq.setRepCount(repCount);

        ipay.getElecMsg().setVendRevReq(vendRevReq);
        try {
            return unMarshal(sendData(marshalRequest(ipay),constant.REV_TIMEOUT));
        } catch (JAXBException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AfrinnovaElectric.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public IpayMsg initializeDefaultIpayMsg() {
        IpayMsg ipay = new IpayMsg();
        ElecMsg elecMsg = new ElecMsg();

        elecMsg.setVer(constant.IPAY_VERSION);
        ipay.setElecMsg(elecMsg);
        ipay.setClient(constant.IPAY_CLIENT);
        ipay.setSeqNum(decimalFormat.format(seqCount));
        seqCount++;
        ipay.setTerm(constant.IPAY_TERM_ESKOMO);
        ipay.setTime(du.formatDateToString(new Date(), "yyyy-mm-dd hh:mm:ss Z"));
        return ipay;
    }
}
