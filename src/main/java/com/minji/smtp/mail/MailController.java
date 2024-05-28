package com.minji.smtp.mail;

import com.minji.smtp.common.ResultDto;
import com.minji.smtp.mail.model.EmailReq;
import com.minji.smtp.mail.model.VerifyAuthReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class MailController {

    private final MailService service;

    @PostMapping("send")
    public ResultDto<String> sendAuthCode(@RequestBody EmailReq p) {
        HttpStatus code = HttpStatus.OK;
        String msg = "성공적으로 이메일을 전송하였습니다.";
        String data = null;
        try {
            service.checkEmail(p.getEmail());
            data = service.sendAuthCode(p.getEmail()); // 인증 코드를 이메일로 전송합니다.
        } catch (Exception e) {
            code = HttpStatus.NO_CONTENT;
            msg = e.getMessage();
        }
        return ResultDto.<String>builder()
                .statusCode(code)
                .resultMsg(msg)
                .resultData(data)
                .build();
    }

    @PostMapping("verify")
    public ResultDto<Integer> verifyAuthCode(@RequestBody VerifyAuthReq p) {
        HttpStatus code = HttpStatus.OK;
        String msg = "인증에 성공하였습니다.";
        int data = 1;
        try {
            service.checkCode(p.getKey());
            service.verifyAuthCode(p);
        } catch (Exception e) {
            code = HttpStatus.NO_CONTENT;
            msg = e.getMessage();
        }
        return ResultDto.<Integer>builder()
                .statusCode(code)
                .resultMsg(msg)
                .resultData(data)
                .build();

    }
}
