package com.minji.smtp.mail;

import com.minji.smtp.mail.model.VerifyAuthReq;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.minji.smtp.common.GlobalConst.*;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    private ConcurrentHashMap<String, String> authCodes; // 이메일과 인증 코드를 저장하는 맵
    private ScheduledExecutorService scheduler; // 주어진 시간에 작업을 실행하게 해줌

    @PostConstruct  // 서비스 객체가 생성되면 초기화를 실행함
    public void init() {
        authCodes = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public String createKey() {  // 인증코드 생성
        Random random = new Random();
        StringBuilder key = new StringBuilder(CODE_LENGTH);
        int arrayLength = NUM_DIGITS + NUM_LETTERS;
        int[] asciiArray = new int[arrayLength];
        for (int i = 0; i < NUM_DIGITS; i++) {  // 0~9 번 인덱스에 숫자 저장
            asciiArray[i] = 48 + i;  // 0~9 숫자의 아스키코드는 48~57
        }
        for (int i = 0; i < NUM_LETTERS; i++) {
            int idx = i + NUM_DIGITS;           // 10~35번 인덱스에 대문자 저장
            asciiArray[idx] = 65 + i;  // A~Z 문자의 아스키코드는 65~90
        }
        for (int i = 0; i < CODE_LENGTH; i++) {  // 랜덤한 인덱스 뽑아서 key 에 인증코드 저장
            int idx = random.nextInt(arrayLength);
            key.append((char) asciiArray[idx]);
        }
        return key.toString();
    }

    public void sendAuthCode(String userEmail) throws MessagingException {
        System.out.println("서비스 1");
        String key = createKey();
        System.out.println("서비스 2");
        authCodes.put(userEmail, key); // 코드 맵에 저장
        scheduler.schedule(() -> authCodes.remove(userEmail), EXPIRATION_TIME, TimeUnit.MINUTES); // 3분 후에 맵에서 인증 코드 삭제

        System.out.println("서비스 3");
        MimeMessage message = mailSender.createMimeMessage();
        System.out.println("서비스 4");
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom("mj17428@naver.com");
        helper.setTo(userEmail); // 수신자 이메일 주소를 설정합니다.
        helper.setSubject("Your Authentication Code"); // 이메일 제목을 설정합니다.
        helper.setText("Your authentication code is: " + key); // 이메일 내용을 설정합니다.

        System.out.println("서비스 5");
        mailSender.send(message); // 이메일을 전송합니다.
        System.out.println("서비스 6");
    }

    public boolean verifyAuthCode(VerifyAuthReq p) {
        // 이메일로 저장된 인증 코드와 입력받은 인증 코드를 비교하여 유효성을 검증합니다.
        String storedAuthCode = authCodes.get(p.getUserEmail());
        return p.getKey().equals(storedAuthCode);
    }
}
