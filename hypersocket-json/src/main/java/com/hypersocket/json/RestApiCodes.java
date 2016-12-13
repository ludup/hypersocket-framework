package com.hypersocket.json;

public interface RestApiCodes {


    public class ApiCodes {
        public final String code;
        public final String message;

        public ApiCodes(String code, String message){
            this.code = code;
            this.message = message;
        }
    }
}
