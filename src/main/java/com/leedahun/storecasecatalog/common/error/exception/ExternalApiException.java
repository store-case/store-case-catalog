package com.leedahun.storecasecatalog.common.error.exception;

import com.leedahun.storecasecatalog.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class ExternalApiException extends CustomException {

    public ExternalApiException() {
        super(ErrorMessage.EXTERNAL_API_ERROR.getMessage(), HttpStatus.BAD_GATEWAY);
    }

}
