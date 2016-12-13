package com.hypersocket.json;

public interface RestApi {
    /*API Tokens*/
    String API_REST = "API_REST";
    String API_USER = "API_USER";

    /*HTTP Headers*/
    String HTTP_HEADER_MASTER_PASSWORD = "X-Hypersocket-Master-Password";
    String HTTP_HEADER_AUTH = "Authorization";
    String HTTP_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    String HTTP_BASIC_AUTH_SCHEME = "basic";

    /*Model tokens*/
    String MODEL_TOKEN_MASTER_PASSWORD = "masterPassword";
}
