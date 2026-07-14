package com.bajaj.dto;

import lombok.Data;

@Data
public class AzureTokenResponse {

	private String token_type;
	private String expires_in;
	private String ext_expires_in;
	private String expires_on;
	private String not_before;
	private String resource;
	private String access_token;

}