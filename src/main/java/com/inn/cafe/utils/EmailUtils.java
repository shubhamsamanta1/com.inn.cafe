package com.inn.cafe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    public JavaMailSender mailer;

    public void remaider(String to, String subject, String text, List<String> list){
        SimpleMailMessage simple = new SimpleMailMessage();
        simple.setFrom("****gmail.com");
        simple.setTo(to);
        simple.setText(text);
        simple.setSubject(subject);
        if(list!=null && list.size()>0) {
            simple.setCc(convert(list));
        }
        mailer.send(simple);

    }

    private String[] convert(List<String> list){

        String [] cc = new String[list.size()];
        for(int i =0 ; i< list.size(); i++){

            cc[i] = list.get(i);

        }
        return cc;
    }



}
