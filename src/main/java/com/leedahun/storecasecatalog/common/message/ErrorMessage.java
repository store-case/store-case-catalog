package com.leedahun.storecasecatalog.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다."),
    ENTITY_NOT_FOUND("데이터가 존재하지 않습니다. "),
    ENTITY_ALREADY_EXISTS("데이터가 이미 존재합니다. ");

    private final String message;
}
