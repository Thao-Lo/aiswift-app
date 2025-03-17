package com.aiswift.dto.Tenant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccessTokenResponse {

	 // The access token issued by PayPal
    @JsonProperty("access_token")
    private String accessToken;

    // The type of token issued (usually "Bearer")
    @JsonProperty("token_type")
    private String tokenType;

    // The lifetime in seconds of the access token
    @JsonProperty("expires_in")
    private int expiresIn;

    // The application ID in PayPal's system (for logging and debugging)
    @JsonProperty("app_id")
    private String appId;

    // A nonce value to prevent replay attacks, provided by the API
    private String nonce;
}

