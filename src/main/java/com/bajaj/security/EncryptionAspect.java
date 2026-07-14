package com.bajaj.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class EncryptionAspect {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = ")H@McQfTjWnZr4u7x!A%C*F-JaNdRgUk";
    private static final String IV  = "w9z$C&F)J@NcRfUj";

    private EncryptionAspect() {}
    
    public static String decrypt(String encryptedText) throws Exception {

        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decoded = Base64.getDecoder().decode(encryptedText);

        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    public static String encrypt(String plainText) throws Exception {

        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public static void main(String[] args) throws Exception {
	    
		String plaintext = "{\r\n"
				+ "    \"config\": {\r\n"
				+ "        \"orgName\": \"B2C\",\r\n"
				+ "        \"channelName\": \"B2C\",\r\n"
				+ "        \"productName\": \"ZCUC\",\r\n"
				+ "        \"requestId\": \"a1f71000003kMFAAA8\",\r\n"
				+ "        \"caseId\": \"a1f71000003kMFAAA8\",\r\n"
				+ "        \"requestedVersion\": null,\r\n"
				+ "        \"requestTimestamp\": null,\r\n"
				+ "        \"serviceName\": \"Bureau\"\r\n"
				+ "    },\r\n"
				+ "    \"data\": {\r\n"
				+ "        \"demographic\": {\r\n"
				+ "            \"caseId\": \"a1f71000003kMFAAA8\",\r\n"
				+ "            \"product\": \"ZCUC\",\r\n"
				+ "            \"loanapplication\": \"WHLU00000020156\",\r\n"
				+ "            \"mobile_number\": \"8349494274\",\r\n"
				+ "            \"gender\": \"Female\",\r\n"
				+ "            \"date_of_birth\": \"1994-01-30\",\r\n"
				+ "            \"residential_address\": {\r\n"
				+ "                \"address1\": \"PUNE\",\r\n"
				+ "                \"address2\": \"MAHARASHTRA\",\r\n"
				+ "                \"address3\": \"411014\",\r\n"
				+ "                \"pincode\": \"411014\",\r\n"
				+ "                \"state\": \"MAHARASHTRA\",\r\n"
				+ "                \"city\": \"PUNE\"\r\n"
				+ "            },\r\n"
				+ "            \"ids\": [],\r\n"
				+ "            \"pan\": \"DHQPP2671D\",\r\n"
				+ "            \"first_name\": \"Ruchi\",\r\n"
				+ "            \"middle_name\": \"\",\r\n"
				+ "            \"last_name\": \"Mishra\",\r\n"
				+ "            \"name\": null,\r\n"
				+ "            \"driving_licence\": null,\r\n"
				+ "            \"voter_id\": null,\r\n"
				+ "            \"rashaan_card\": null,\r\n"
				+ "            \"sourcing_branch\": null,\r\n"
				+ "            \"sourcing_channel\": null,\r\n"
				+ "            \"loan_type\": null,\r\n"
				+ "            \"requested_loan_amount\": \"150000\",\r\n"
				+ "            \"program_type\": null,\r\n"
				+ "            \"constitution\": null,\r\n"
				+ "            \"customer_type\": null,\r\n"
				+ "            \"employment_type\": null,\r\n"
				+ "            \"nature_of_business\": null,\r\n"
				+ "            \"bureau_source\": \"CIBIL\"\r\n"
				+ "        },\r\n"
				+ "        \"bureauHeader\": null,\r\n"
				+ "        \"transformedResponse\": null,\r\n"
				+ "        \"message\": null\r\n"
				+ "    }\r\n"
				+ "}";
		String encrypted = encrypt(plaintext);
		String decrypted = decrypt("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");

		System.out.println("Plaintext: " + plaintext);
		System.out.println("Encrypted: " + encrypted);
    }
}
