package com.bajaj.constants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mahesh Shelke
 *
 * 
 */

public class Constants {

	final public static Integer CODE_200 = 200;
	final public static Integer CODE_601 = 601;
	final public static Integer CODE_602 = 602;
	final public static Integer CODE_603 = 603;
	final public static Integer CODE_604 = 604;
	final public static Integer CODE_500 = 500;

	final public static String FIELD_LEVEL_VALIDATION = "FIELD_LEVEL_VALIDATION : ";
	final public static String UNEXPECTED_ERROR = "UNEXPECTED_ERROR - ";
	final public static String INVALID_REQUEST_JSON = "Invalid JSON Request : ";
	final public static String BAD_REQUEST = "Bad Request, Please check Input Parameters - ";
	final public static String FAILED_API_CALL = "FAILED - API CALL ";
	final public static String SUCCESS_API_CALL = "SUCCESS - API CALL ";
	final public static String API_EXCEPTION = " API Exception = ";

	final public static String SUCCESS = "Success";
	final public static String FAILED = "Failed";
	final public static String COLON = " : ";
	final public static String EMPTY_STRING = "";
	final public static String DASH = " - ";
	final public static String OCP_KEY_HEADER = "Ocp-Apim-Subscription-Key";
	final public static String OCP_SUB_KEY = "ocp_sub_key";
	final public static String CLIENT_ERROR = "Client error: ";
	final public static String API_URL = "api_url";
	final public static String API_READ_TIME_OUT = "api_read_time_out";
	final public static String API_CONNECTION_TIME_OUT = "api_connection_time_out";
	final public static String PROCESSED = "PROCESSED";

	final public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	final public static String TRUE_STRING = "true";
	final public static String APP_NAME = "spring.application.name";
	final public static String PROCESS_NAME = "PROCESS_NAME";
	final public static String PROCESS_SUCCESS = "PROCESS_SUCCESS";
	final public static String PROCESS_FAILED = "PROCESS_FAILED";
	final public static String STATUS_CODE = "STATUS_CODE";
	final public static String REQUEST_RECIEVED = "REQUEST_RECIEVED";
	final public static String REQUEST_PROCESSED = "REQUEST_PROCESSED";
	final public static String CURRENT_DATE = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

	final public static String TOTAL = "TOTAL";

	final public static String IS_KPI_ON = "IsKPIon";
	final public static String KPI_FOLDER_PATH = "KPIFolderPath";

	final public static String Success = "Success";
	final public static String Failed = "Failed";

	final public static String AUTHORIZATION_HEADER = "Authorization";
	final public static String BEARER = "Bearer ";

	final public static String STATUC_CODE = "STATUS_CODE";

	final public static String CLIENT_ID = "client_id";
	final public static String CLIENT_SECRET = "client_secret";
	final public static String GRANT_TYPE = "grant_type";
	final public static String RESOURCE = "resource";
	final public static String RESOURCE_URL = "https://management.azure.com/";
	final public static String CLIENT_CREDENTIALS = "client_credentials";
	final public static String AUTH_CLIENT_ID = "auth_client_id";
	final public static String AUTH_CLIENT_SECRET = "auth_client_secret";
	final public static String AUTH_TOKEN_URL = "auth_token_url";

	public static final String ALGORITHM = "AES";
	public static final String MODE = "CBC";
	public static final String PADDING = "PKCS5Padding";
	public static final String TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING;
	public static final String CHARSET = "UTF-8";

	public static final String KEY = ")H@McQfTjWnZr4u7x!A%C*F-JaNdRgUk";

	public static final String IV = "w9z$C&F)J@NcRfUj";

	public static final String HEADER_IS_ENCRYPTED = "is_encrypted";

}
