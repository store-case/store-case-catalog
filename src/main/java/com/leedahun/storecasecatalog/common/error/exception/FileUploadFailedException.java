package com.leedahun.storecasecatalog.common.error.exception;

import com.leedahun.storecasecatalog.common.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class FileUploadFailedException extends CustomException {

    public FileUploadFailedException() {
        super(ErrorMessage.FILE_UPLOAD_FAILED.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
