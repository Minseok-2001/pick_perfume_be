package ym_cosmetic.pick_perfume_be.auth.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val emailSender: JavaMailSender
) {
    fun sendPasswordResetEmail(email: String, resetLink: String) {
        val message = emailSender.createMimeMessage()

        try {
            val helper = MimeMessageHelper(message, true)
            helper.setFrom("contact@scentist.link")  // 발신자 이메일 설정
            helper.setTo(email)
            helper.setSubject("[향수 서비스] 비밀번호 재설정 링크")

            // HTML 형식의 이메일 내용
            val htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #5d4037;">향수 서비스 비밀번호 재설정</h2>
                    <p>안녕하세요, 향수 서비스입니다.</p>
                    <p>비밀번호 재설정을 위한 링크를 보내드립니다.</p>
                    <p>아래 버튼을 클릭하여 비밀번호를 재설정해주세요:</p>
                    <p style="text-align: center; margin: 25px 0;">
                        <a href="$resetLink" style="background-color: #795548; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">비밀번호 재설정</a>
                    </p>
                    <p>이 링크는 1시간 동안만 유효합니다.</p>
                    <p>요청하지 않으셨다면 이 이메일을 무시해주세요.</p>
                </div>
            """.trimIndent()

            helper.setText(htmlContent, true)  // true는 HTML 사용 가능하게 함

            emailSender.send(message)
        } catch (e: Exception) {
            // 예외 처리
            throw RuntimeException("이메일 전송 중 오류가 발생했습니다", e)
        }
    }
}