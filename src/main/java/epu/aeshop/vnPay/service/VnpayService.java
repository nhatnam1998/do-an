package epu.aeshop.vnPay.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;


import epu.aeshop.util.Utils;

public class VnpayService {

    private final String base_url;
    private final String pay_url;
    private final String website_id;
    private final String hash_secret;
    
    public VnpayService(String base_url, String pay_url, String website_id, String hash_secret) {
        this.base_url = base_url;
        this.pay_url = pay_url;
        this.website_id = website_id;
        this.hash_secret = hash_secret;
    }

    public String getPaymentURL(String txnRef, String ip, Integer amount) throws IOException {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.0.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TmnCode", this.website_id);
        vnp_Params.put("vnp_ReturnUrl", this.base_url + "/buyer/orders/" + txnRef);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + txnRef);
        vnp_Params.put("vnp_IpAddr", ip);
        // vnp_Params.put("vnp_OrderType", orderType);

        Date dt = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(dt);
        String vnp_CreateDate = dateString;
        String vnp_TransDate = vnp_CreateDate;
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        //Build data to hash and querystring
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Utils.sha256(this.hash_secret + hashData.toString());
        queryUrl += "&vnp_SecureHashType=SHA256&vnp_SecureHash=" + vnp_SecureHash;

        return this.pay_url + "?" + queryUrl;
    }

    public boolean verifyRequest(HttpServletRequest request) throws UnsupportedEncodingException {
        Map fields = new HashMap();
		for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = (String) params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				fields.put(fieldName, fieldValue);
			}
		}

		String vnp_SecureHash = request.getParameter("vnp_SecureHash");
		if (fields.containsKey("vnp_SecureHashType")) {
			fields.remove("vnp_SecureHashType");
		}
		if (fields.containsKey("vnp_SecureHash")) {
			fields.remove("vnp_SecureHash");
        }

		return this.hashAllFields(fields).equals(vnp_SecureHash);
    }

    private String hashAllFields(Map fields) throws UnsupportedEncodingException {
        // create a list and sort it
        List fieldNames = new ArrayList(fields.keySet());
        Collections.sort(fieldNames);
        // create a buffer for the md5 input and add the secure secret first
        StringBuilder sb = new StringBuilder();
        sb.append(this.hash_secret);
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLDecoder.decode(fieldValue,"UTF-8"));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return Utils.sha256(sb.toString());
    }
}
