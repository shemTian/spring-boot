package com.qik.demo.util;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.lang.StringUtils;


/**
 * Created by swift.mao on 2017/6/6.
 */
public class EmailUtil {

    private static Session getMailSession(String host, final String userName, final String password) {

        final Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.class", "com.sun.mail.smtp.SMTPTransport");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", true);

        return Session.getDefaultInstance(props, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(userName, password);
            }
        });
    }

    public static void sendEmail(String mailTo, String mailCC, String mailTitle, String mailContent, File att) {

        String host = "smtp.263.net";
        String userName = "service@upwild.cn";
        String password = "uw123456";

        Session sendMailSession = getMailSession(host, userName, password);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(userName);
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(mailTo);
            mailMessage.setRecipient(Message.RecipientType.TO, to);

            // 获取抄送者信息
            if (StringUtils.isNotBlank(mailCC)) {
                String[] mailCCs = mailCC.split(";");
                Address[] ccAddresses = new InternetAddress[mailCCs.length];
                for (int i = 0; i < mailCCs.length; i++) {
                    ccAddresses[i] = new InternetAddress(mailCCs[i]);
                }
                mailMessage.setRecipients(Message.RecipientType.CC, ccAddresses);
            }

            mailMessage.setSubject(mailTitle);
            mailMessage.setSentDate(new Date());

            Multipart mainPart = new MimeMultipart();

            if (StringUtils.isNotBlank(mailContent)) {
                BodyPart html = new MimeBodyPart();
                html.setContent(mailContent, "text/html; charset=GBK");
                mainPart.addBodyPart(html);
            }

            if (att != null) {
                BodyPart attch = new MimeBodyPart();
                DataSource ds1 = new FileDataSource(att);
                DataHandler dh1 = new DataHandler(ds1);
                attch.setDataHandler(dh1);
                attch.setFileName(MimeUtility.encodeWord(att.getName()));
                mainPart.addBodyPart(attch);
            }

            mailMessage.setContent(mainPart);
            Transport.send(mailMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
