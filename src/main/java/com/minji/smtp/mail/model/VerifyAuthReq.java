package com.minji.smtp.mail.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAuthReq {
    private String email;
    private String key;
}
