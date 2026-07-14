package com.bajaj.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Encrypts plain text using AES-256 CBC and returns Base64 encoded cipher text.
     *
     * @param plainText Text to encrypt
     * @param key       32-byte (256-bit) AES key
     * @param iv        16-byte IV
     * @return Base64 encoded encrypted text
     */
    public static String encrypt(String plainText, String key, String iv) throws Exception {

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Decrypts Base64 encoded cipher text.
     *
     * @param encryptedText Base64 encoded encrypted string
     * @param key           32-byte AES key
     * @param iv            16-byte IV
     * @return Original plain text
     */
    public static String decrypt(String encryptedText, String key, String iv) throws Exception {

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decoded = Base64.getDecoder().decode(encryptedText);

        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    public static void main(String[] args) {

        try {

            // AES-256 Key (32 characters = 32 bytes)
            //String key = "12345678901234567890123456789012";

            // IV (16 characters = 16 bytes)
            //String iv = "abcdefghijklmnop";
            
            String key = ")H@McQfTjWnZr4u7x!A%C*F-JaNdRgUk";
            
            String iv = "w9z$C&F)J@NcRfUj";

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

            System.out.println("Original:");
            System.out.println(plaintext);

            String encrypted = AESUtil.encrypt(plaintext, key, iv);

            System.out.println("\nEncrypted(Base64):");
            System.out.println(encrypted);

            String decrypted = AESUtil.decrypt("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", key, iv);

            System.out.println("\nDecrypted:");
            System.out.println(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}