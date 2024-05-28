package com.minji.smtp.mail;

import com.minji.smtp.mail.model.VerifyAuthReq;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.mail.username}")
    private String senderMAil;
    private ConcurrentHashMap<String, String> authCodes; // 이메일과 인증 코드를 저장하는 맵
    private ScheduledExecutorService scheduler; // 주어진 시간에 작업을 실행하게 해줌

    @PostConstruct  // 서비스 객체가 생성되면 초기화를 실행함
    public void init() {
        authCodes = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void checkEmail(String email) {
        if(email == null || email.isEmpty()) {  // 입력되지 않았다면
            throw new RuntimeException("이메일을 입력해주세요.");
        }
        String[] isAt = email.split("@");
        if(isAt.length == 1) {  // @ 이 포함되어있지 않다면
            throw new RuntimeException("다시 입력해주세요.");
        }
        String[] isDot = isAt[1].split("\\.");
        if(isDot.length == 1) {  // 도메인 부분에 . 이 없다면
            throw new RuntimeException("다시 입력해주세요.");
        }
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

    public String sendAuthCode(String userEmail) {
        String key = createKey();
        authCodes.put(userEmail, key); // 코드 맵에 저장
        scheduler.schedule(() -> authCodes.remove(userEmail), EXPIRATION_TIME, TimeUnit.MINUTES); // 3분 후에 맵에서 인증 코드 삭제

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(senderMAil);
            helper.setTo(userEmail);
            helper.setSubject("POSTIME 인증 코드 안내");  // 제목
            helper.setText(String.format("안녕하십니까.\nPOSTIME 인증 코드 안내입니다.\n아래의 코드를 정확하게 입력해주세요.\n인증코드는 %d분 동안 유효합니다.\n\n%s\n",EXPIRATION_TIME ,key));
        } catch (MessagingException e) {
            throw new RuntimeException("전송 실패");
        }
        mailSender.send(message);
        return key;
    }

    public void checkCode(String key) {
        if(key == null || key.isEmpty()) {  // 입력되지 않았다면
            throw new RuntimeException("인증코드를 입력해주세요.");
        }
        if(key.length() != CODE_LENGTH) {
            throw new RuntimeException("다시 입력해주세요.");
        }
    }
    public void verifyAuthCode(VerifyAuthReq p) {
        // 인증 코드를 비교하여 유효성 검증

        if (!authCodes.containsKey(p.getEmail())) {  // 유효시간이 지나면 맵에서 사라짐
            throw new RuntimeException("인증 코드를 다시 받아주세요.");
        }
        String storedAuthCode = authCodes.get(p.getEmail());  // 이메일에 저장된 코드 가져오기
        if(!p.getKey().equals(storedAuthCode)) {
            throw new RuntimeException("코드가 올바르지 않습니다.");
        }
    }
}
