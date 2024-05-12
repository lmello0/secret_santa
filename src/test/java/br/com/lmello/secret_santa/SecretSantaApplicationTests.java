package br.com.lmello.secret_santa;

import br.com.lmello.secret_santa.controller.DrawController;
import br.com.lmello.secret_santa.service.DrawService;
import br.com.lmello.secret_santa.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SecretSantaApplicationTests {
    @Autowired
    private DrawController drawController;

    @MockBean
    private DrawService drawService;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("DrawController should not be null")
    public void drawControllerShouldNotBeNull() {
        assertThat(drawController).isNotNull();
    }
}
