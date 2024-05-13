package br.com.lmello.secret_santa.service;

import br.com.lmello.secret_santa.dto.DrawEmailDTO;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    public void sendEmail(DrawEmailDTO emailData) {
        Resend resend = new Resend(apiKey);

        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from("Secret Santa <not.reply@lmello-homeserver.com.br>")
                .to(emailData.sender().getEmail())
                .subject(createSubject(emailData.drawCode()))
                .text(createBodyText(
                        emailData.drawCode(),
                        emailData.sender().getName(),
                        emailData.receiver().getName(),
                        emailData.budget()
                ))
                .build();

        try {
            resend.emails().send(sendEmailRequest);
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }

    private String createSubject(String drawCode) {
        return "Secret santa results - " + drawCode;
    }

    private String createBodyText(String drawCode, String sender, String receiver, BigDecimal budget) {
        return "Olá, " + sender + "!\n\n" +
                "Referente ao amigo secreto - " + drawCode + " com limite de R$" + budget + "\n" +
                "Você deverá enviar um presente para " + receiver;
    }
}
