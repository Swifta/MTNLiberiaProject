/**
 *
 */
package com.afrinnova.api.schoolfees.rpc.ws;

import afrinnovaelectric.Constants;
import afrinnovaelectric.IpayMsg;
import afrinnovaelectric.StdToken;
import afrinnovaelectric.VendRes;
import afrinnovaelectric.VendRevRes;
import com.afrinnova.api.schoolfees.authentication.ServerAuthentication;
import com.afrinnova.api.schoolfees.face.IFundamoPayment;
import com.afrinnova.api.schoolfees.properties.AccountProperties;
import com.afrinnova.api.schoolfees.response.GeneralResponse;
import com.afrinnova.api.schoolfees.response.ResponseCode;
import com.afrinnova.api.schoolfees.response.TransactionResponse;
import com.afrinnova.api.schoolfees.service.exception.ServiceException;
import com.afrinnova.api.schoolfees.service.model.AccountLookup;
import com.afrinnova.api.schoolfees.service.model.TransactionOb;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FundamoPayment implements IFundamoPayment {

    private static final Logger logger = Logger.getLogger("com.afrinnova.api.schoolfees.rpc.ws.FundamoPayment");
    public String Statuscode = "01";
    GeneralResponse response = new GeneralResponse();
    public int seqCount = 0;
    private Constants constant = new Constants();

    @Override
    public TransactionResponse receivePaymentConfirmation(String payerAccountIdentifier, String customerName, String accountRef, double amount, String paymentRef, String transactionType,
            String fundamoTransactionID, String thirdPartyTransactionID, String fundamoUserId, String fundamoPassword,
            String statusCode, String appVersion) {


        try {

            logger.info("Inside receivePaymentConfirmation ");
            StringBuilder objStringBuffer = new StringBuilder();
            objStringBuffer.append("\n receivePaymentConfirmation Methods Parameters : [ ");
            objStringBuffer.append(" accountIdentifier : ").append(payerAccountIdentifier);
            objStringBuffer.append(" , customerName : ").append(customerName);
            objStringBuffer.append(" , accountRef : ").append(accountRef);
            objStringBuffer.append(" , amount : ").append(amount);
            objStringBuffer.append(" , paymentRef : ").append(paymentRef);
            objStringBuffer.append(" , transactiontype : ").append(transactionType);
            objStringBuffer.append(" , fundamoTransactionID : ").append(fundamoTransactionID);
            objStringBuffer.append(" , thirdPartyTransactionID : ").append(thirdPartyTransactionID);
            objStringBuffer.append(" , fundamoUserId : ").append(fundamoUserId);
            objStringBuffer.append(" , fundamoPassword : ").append(fundamoPassword);
            objStringBuffer.append(" , statusCode : ").append(statusCode);
            objStringBuffer.append(" , appVersion : ").append(appVersion);
            objStringBuffer.append(" ]\n");

            if (statusCode == null || !Statuscode.equals(statusCode)) {

                response.setResponseMessage("Status code returned not 1");
                throw new ServiceException(ResponseCode.CODE_03_TRAN_NOT_ALLOWED, "Request StatusCode not successful");
            }

            logger.info(objStringBuffer.toString());
            /* Look up if the Fundamo credentials are correct */
            if (!ServerAuthentication.isLoginDetailsValid(fundamoUserId, fundamoPassword)) {
                logger.info("Mobile Mobile call::::::Received Userid/Username not correct");
                throw new ServiceException(ResponseCode.CODE_05_INVALID_FUNDAMO_DETAILS, "Invalid authentication");
            }


            logger.log(Level.INFO, "Fetching customer details for Meter number ==" + accountRef);

            AccountLookup look = AccountLookup.getInstance();
            Boolean meterStatus = look.confirmCustomerDetails(accountRef);
            if (meterStatus == null) {
                logger.log(Level.INFO, "customer with meter number has a pending transaction", accountRef);

                throw new ServiceException(ResponseCode.CODE_103_DUPL_TRAN_ID, "Customer with " + accountRef + " has a pending vend request and cannot complete further requests");
            } else if (!meterStatus) {

                logger.log(Level.INFO, "customer with meter number cannot be retrieved", accountRef);

                throw new ServiceException(ResponseCode.CODE_02_INVALID_ACC_DETAILS, "Customer with " + accountRef + " does not exist");
            }
            seqCount++;
            /* Look up account */

            //AccountLookup look1 = new AccountLookup();
            TransactionOb transactionOb = new TransactionOb();
            transactionOb.setAcctref(accountRef);
            transactionOb.setAmount(Double.valueOf(amount).longValue());
            transactionOb.setCustomerName(customerName);
            transactionOb.setFundamoTransactionID(fundamoTransactionID);
            transactionOb.setPayerAccountIdentifier(payerAccountIdentifier);
            transactionOb.setPaymentRef(paymentRef);
            transactionOb.setStatuscode(statusCode);

            IpayMsg ipay = look.makePayments(transactionOb, seqCount);
            String statusMessage = "Successful\nToken(s) ";
            logger.info("returned ipay after payments =====================================\n");
            if (ipay == null) {
                statusMessage = "Unsuccessful and reversed!!!";
            } else {
                if (ipay != null) {
                    if (ipay.getElecMsg() != null) {
                        VendRes vendRes = ipay.getElecMsg().getVendRes();
                        if (vendRes != null) {
                            String responseCode = vendRes.getRes().getCode();
                            if (responseCode.equalsIgnoreCase(constant.STATUS_OK)) {
                                List<StdToken> stdTokens = vendRes.getStdToken();
                                if (stdTokens != null) {
                                    StdToken stdToken = stdTokens.iterator().next();
                                    if (stdToken != null) {
                                        statusMessage += ":" + stdToken.getValue();
                                        look.updateTransactionHistory(vendRes.getRef(), constant.TXN_COMPLETE);
                                    }
                                }

                            } else {
                                statusMessage = retrieveResponseDescription(responseCode);
                            }
                        } else {
                            VendRevRes vendRevRes = ipay.getElecMsg().getVendRevRes();
                            if (vendRevRes != null) {
                                vendRes = vendRevRes.getVendRes();
                                statusMessage = "Unsuccessful and reversed!!!";
                                String responseCode = vendRevRes.getRes().getCode();
                                if (responseCode.equalsIgnoreCase(constant.STATUS_ESKOMO)) {
                                    statusMessage = "Reversal not supported.";
                                    List<StdToken> stdTokens = vendRes.getStdToken();
                                    if (stdTokens != null) {
                                        StdToken stdToken = stdTokens.iterator().next();
                                        if (stdToken != null) {
                                            statusMessage += ":" + stdToken.getValue();
                                        }
                                    }

                                } else {
                                    statusMessage = retrieveResponseDescription(responseCode);
                                }
                            }
                        }
                    }
                }
            }


            logger.info("Mobile Mobile call::::::Payment Added Successfully");


            return new TransactionResponse(thirdPartyTransactionID, fundamoTransactionID, ResponseCode.CODE_01_OK, statusMessage, AccountProperties.APP_VERSION, null);


        } catch (ServiceException se) {
            logger.log(Level.INFO, "ServiceClass Exception::::Transaction not processed due to {0}", se.getResponseMessage());
            return new TransactionResponse(thirdPartyTransactionID, fundamoTransactionID, se.getResponseCode(), se.getResponseMessage(), AccountProperties.APP_VERSION, null);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception:::::Transaction not processed due to {0}", e.getMessage());
            e.printStackTrace();
            return new TransactionResponse(null, fundamoTransactionID, ResponseCode.CODE_100_GENERAL_FAILURE, "null", AccountProperties.APP_VERSION, null);
        }

    }

    public String retrieveResponseDescription(String responseCode) {
        switch (responseCode.trim()) {
            case "elec001":
                return "General Error";

            case "elec002":
                return "Service not available";
            case "elec003":
                return "No record of previous transaction";

            case "elec004":
                return "Warning - reversals are not supported by the suppliers system";
            case "elec010":
                return "Unknown meter number";

            case "elec011":
                return "Meter is block";
            case "elec012":
                return "Too much debt";

            case "elec013":
                return "Invalid Amount";
            case "elec014":
                return "Invalid Number of Tokens";

            case "elec015":
                return "Amount too high";
            case "elec016":
                return "Amount too low";
            case "elec017":
                return "No free BSST token is due";

            case "elec018":
                return "Multiple tokens not supported";
            case "elec019":
                return "Already reversed";

            case "elec020":
                return "Transaction already completed";
            case "elec021":
                return "Duplicate meter number";

            case "elec022":
                return "Meter is blocked";
            case "elec023":
                return "Invalid PaymentType";

            case "elec029":
                return "Invalid replace reference";
            case "elec030":
                return "Invalid XmlVend Format";

            case "elec031":
                return "BSST token must be vended as part of sale";
            case "elec032":
                return "Update meter key";

            case "elec033":
                return "Track 2 meter mismatch";
            case "elec034":
                return "Test vend not supported";
            case "elec900":
                return "General system error";

            case "elec901":
                return "Unsupported message version numb";
            case "elec902":
                return "Invalid Reference";
            default:
                return "Unknown error";
        }
    }

    /* (non-Javadoc)
     * @see com.fundamo.api.payment.ws.IFundamoPayment#retryPaymentConfirmation(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public TransactionResponse retryPaymentConfirmation(String string, String string1, String string2, String string3, String string4, String string5, String string6, String string7) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
