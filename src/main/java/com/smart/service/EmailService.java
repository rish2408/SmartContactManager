package com.smart.service;

import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String to){

        boolean f = false;

        String from = "rishika.vitalassets@gmail.com";
        // Rest of the Code

        // Set Gmail host Responsible to send Email
        String host = "smtp.gmail.com";

// Get the System properties
        Properties properties = System.getProperties();
        System.out.println(properties);

// Setting important information to properties object

        // Setting host
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // To get the Session Object
        Session session = Session.getInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // TODO Auto-generated method stub
                return new PasswordAuthentication("rishika.vitalassets@gmail.com", "yaadaagaya");
            }

        });

        session.setDebug(true);

        // Compose the message[text,multimedia]
        MimeMessage msg = new MimeMessage(session);

        try {

            // from Email
            msg.setFrom(from);

            // Adding Recipient to Message
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Adding Subject to Message
            msg.setSubject(subject);

            // Adding Text to Message
//            msg.setText(message);
            msg.setContent(message,"text/html");
            
            // Send the email using Transport class
            Transport.send(msg);

            System.out.println("Mail Sent Successfully");
            f = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
            return f;
    }
}
