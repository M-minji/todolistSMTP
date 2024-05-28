package com.minji.smtp.mail;

import com.minji.smtp.mail.model.EmailReq;
import com.minji.smtp.mail.model.VerifyAuthReq;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class MailController {

    private final MailService service;

    @PostMapping("send")
    public String sendAuthCode(@RequestBody @Valid EmailReq p) {
        try {
            System.out.println("컨트롤러 1");
            service.sendAuthCode(p.getEmail()); // 인증 코드를 이메일로 전송합니다.
            System.out.println("컨트롤러 2");
            return "Authentication code sent."; // 성공 메시지를 반환합니다.
        } catch (MessagingException e) {
            System.out.println("컨트롤러 3");
            return "Failed to send authentication code."; // 실패 메시지를 반환합니다.
        }
    }

    @PostMapping("verify")
    public String verifyAuthCode(@RequestBody VerifyAuthReq p) {
        boolean isValid = service.verifyAuthCode(p);
        return isValid ? "Authentication successful." : "Authentication failed."; // 결과에 따라 메시지를 반환합니다.
    }
}
