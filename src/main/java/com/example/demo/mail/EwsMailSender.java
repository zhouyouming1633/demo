package com.example.demo.mail;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

import java.net.URI;

public class EwsMailSender {

    public static void main(String[] args) {
        try {
            // 1. 创建 ExchangeService 实例
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

            // 2. 设置认证凭据（域\用户名 和 密码）
            //    注意：用户名需要包含域名
            service.setCredentials(new WebCredentials("jinkosolar.cn\\hyyyjsz", "JKm2e2025"));

            // 3. 设置 EWS 服务器 URL
            //    根据你图片中的服务器地址 mail.jinkosolar.com 构建
            service.setUrl(new URI("https://mail.jinkosolar.com/EWS/Exchange.asmx"));

            // 4. 创建邮件消息
            EmailMessage email = new EmailMessage(service);
            email.setSubject("测试邮件主题");
            email.setBody(MessageBody.getMessageBodyFromText("这是一封通过 EWS 发送的测试邮件。"));

            // 5. 设置收件人（将  替换为实际收件人邮箱）
            email.getToRecipients().add("zhouyouming_1633@163.com");

            // 6. 发送邮件
            email.send();

            System.out.println("邮件发送成功！");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("邮件发送失败：" + e.getMessage());
        }
    }
}
