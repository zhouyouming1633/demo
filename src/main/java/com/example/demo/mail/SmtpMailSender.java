package com.example.demo.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpMailSender {

    public static void main(String[] args) {
        // 1. 配置 SMTP 属性
        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.jinkosolar.com");  // 你的邮件服务器
        props.put("mail.smtp.port", "25");                 // 常用端口：587 或 25
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");     // 启用 TLS
        props.put("mail.smtp.auth.mechanisms", "LOGIN");

        // 2. 创建认证器
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 注意：用户名可能需要包含域名
                return new PasswordAuthentication("jinkosolar.cn\\hyyyjsz", "JKm2e2025");
            }
        };

        // 3. 创建会话
        Session session = Session.getInstance(props, auth);

        try {
            // 4. 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("hyyyjsz@jinkopower.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("zhouyouming_1633@163.com"));
            message.setSubject("测试邮件主题");
            message.setText("这是一封通过 SMTP 发送的测试邮件。");

            // 5. 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功！");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("邮件发送失败：" + e.getMessage());
        }
    }
}
