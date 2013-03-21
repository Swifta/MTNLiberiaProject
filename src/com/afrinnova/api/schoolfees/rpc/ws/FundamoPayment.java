/**
 * 
 */
package com.afrinnova.api.schoolfees.rpc.ws;

import com.afrinnova.api.schoolfees.authentication.ServerAuthentication;
import com.afrinnova.api.schoolfees.face.IFundamoPayment;
import com.afrinnova.api.schoolfees.properties.AccountProperties;
import com.afrinnova.api.schoolfees.response.GeneralResponse;
import com.afrinnova.api.schoolfees.response.ResponseCode;
import com.afrinnova.api.schoolfees.response.TransactionResponse;
import com.afrinnova.api.schoolfees.service.exception.ServiceException;
import com.afrinnova.api.schoolfees.service.model.Account01;
import com.afrinnova.api.schoolfees.service.model.AccountLookup;
import com.afrinnova.api.schoolfees.service.model.TransactionOb;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FundamoPayment implements IFundamoPayment {

    private static final Logger logger = Logger.getLogger("com.afrinnova.api.schoolfees.rpc.ws.FundamoPayment");
    public String Statuscode = "01";
    GeneralResponse response = new GeneralResponse();

    @Override
    public TransactionResponse receivePaymentConfirmation(String payerAccountIdentifier, String customerName, String accountRef, long amount, String paymentRef, String transactionType,
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


            logger.log(Level.INFO, "Fetching school fees amount for student with Identification number: {0}", accountRef);

            AccountLookup look = new AccountLookup();

            Account01 thirdPartyAccount = look.getAccountref(accountRef);

            if (thirdPartyAccount == null) {

                logger.log(Level.INFO, "Student with {0} does not exist", accountRef);

                throw new ServiceException(ResponseCode.CODE_02_INVALID_ACC_DETAILS, "Student with " + accountRef + " does not exist");
            }

            /* Look up account */

            //AccountLookup look1 = new AccountLookup();

            TransactionOb pmt = look.getPayment(accountRef);


            long amt = 0L;

            if (pmt != null) {

                amt = pmt.getAmount() * 100;

                amt += amount;

                logger.log(Level.INFO, "inserting with amount {0}", amt);

                look.updateTransaction(payerAccountIdentifier, accountRef, amt / 100, customerName, paymentRef, fundamoTransactionID);

                look.insertInstallmentPay(payerAccountIdentifier, customerName, accountRef, amount / 100, paymentRef, fundamoTransactionID);

            } else {

                look.insertTransaction(payerAccountIdentifier, customerName, accountRef, amount / 100, paymentRef, fundamoTransactionID, thirdPartyTransactionID, ResponseCode.CODE_01_OK);

                look.insertInstallmentPay(payerAccountIdentifier, customerName, accountRef, amount / 100, paymentRef, fundamoTransactionID);


            }

            /* Look up account */

            logger.info("Mobile Mobile call::::::Payment Added Successfully");


            return new TransactionResponse(thirdPartyTransactionID, fundamoTransactionID, ResponseCode.CODE_01_OK, "Success", AccountProperties.APP_VERSION, null);


        } catch (ServiceException se) {
            logger.log(Level.INFO, "ServiceClass Exception::::Transaction not processed due to {0}", se.getResponseMessage());
            return new TransactionResponse(thirdPartyTransactionID, fundamoTransactionID, se.getResponseCode(), se.getResponseMessage(), AccountProperties.APP_VERSION, null);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception:::::Transaction not processed due to {0}", e.getMessage());
            return new TransactionResponse(null, fundamoTransactionID, ResponseCode.CODE_100_GENERAL_FAILURE, "null", AccountProperties.APP_VERSION, null);
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
