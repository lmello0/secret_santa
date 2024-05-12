package br.com.lmello.secret_santa.controller;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.dto.ErrorMessageDTO;
import br.com.lmello.secret_santa.dto.ParticipantDTO;
import br.com.lmello.secret_santa.dto.StartDrawDTO;
import br.com.lmello.secret_santa.model.Draw;
import br.com.lmello.secret_santa.model.Participant;
import br.com.lmello.secret_santa.model.User;
import br.com.lmello.secret_santa.repository.DrawRepository;
import br.com.lmello.secret_santa.repository.UserRepository;
import br.com.lmello.secret_santa.service.DrawService;
import br.com.lmello.secret_santa.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
public class DrawControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private DrawService drawService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private JacksonTester<DrawDTO> drawDTOJacksonTester;

    @Autowired
    private JacksonTester<ErrorMessageDTO> errorMessageDTOJacksonTester;

    @Autowired
    private JacksonTester<StartDrawDTO> startDrawDTOJacksonTester;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String apiKey;

    @BeforeAll
    static void createApiKey(@Autowired UserRepository userRepository) {
        User testUser = new User();

        testUser.setKey("super-test-key");

        userRepository.save(testUser);

        apiKey = testUser.getKey();
    }

    @Test
    @DisplayName("Should return HTTP 201 and a draw response")
    void createDrawSuccess() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        List<ParticipantDTO> participantDTOS = parseParticipants(participants);

        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw")
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(drawDTOJacksonTester.write(
                                        new DrawDTO(10000, participantDTOS)
                                ).getJson())
                ).andReturn()
                .getResponse();

        Draw createdDraw = objectMapper.readValue(response.getContentAsString(), Draw.class);

        String expectedResponse = drawDTOJacksonTester.write(new DrawDTO(createdDraw)).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith(createdDraw.getCode());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return HTTP 400 when data is missing")
    void createDrawInvalidBody() throws Exception {
        MockHttpServletResponse response = mvc
                .perform(post("/draw")
                        .header("X-API-KEY",apiKey))
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Required body not found")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 200 and a draw")
    void getDrawSuccessful() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        MockHttpServletResponse response = mvc
                .perform(
                        get("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                ).andReturn()
                .getResponse();

        String expectedResponse = drawDTOJacksonTester.write(new DrawDTO(draw)).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 200 with a error message")
    void getDrawWithNotFoundMessage() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = produceDraw(participants);

        MockHttpServletResponse response = mvc
                .perform(
                        get("/draw/{code}", draw.getCode())
                            .header("X-API-KEY",apiKey)
                ).andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Draw '" + draw.getCode() + "' not found")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 200 with draw updated")
    void updateDrawSuccessfully() throws Exception {
        final int MULTIPLY_FACTOR = new Random().nextInt(1, 3);

        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw oldDraw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        List<ParticipantDTO> newParticipants = parseParticipants(produceParticipants(new Random().nextInt(10, 20)));

        MockHttpServletResponse response = mvc
                .perform(
                        put("/draw/{code}", oldDraw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        drawDTOJacksonTester
                                                .write(new DrawDTO(oldDraw.getBudget() * MULTIPLY_FACTOR, newParticipants))
                                                .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        Draw newDraw = objectMapper.readValue(response.getContentAsString(), Draw.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(newDraw.getParticipants().size()).isBetween(10, 20);
        assertThat(newDraw.getParticipants().size()).isGreaterThan(oldDraw.getParticipants().size());
        assertThat(newDraw.getBudget()).isEqualTo(oldDraw.getBudget() * MULTIPLY_FACTOR);
    }

    @Test
    @DisplayName("Should return a HTTP 200 with a error message when draw not exists")
    void updateDrawNotFoundException() throws Exception {
        final int MULTIPLY_FACTOR = new Random().nextInt(1, 3);

        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw oldDraw = produceDraw(participants);

        List<ParticipantDTO> newParticipants = parseParticipants(produceParticipants(new Random().nextInt(10, 20)));

        MockHttpServletResponse response = mvc
                .perform(
                        put("/draw/{code}", oldDraw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        drawDTOJacksonTester
                                                .write(new DrawDTO(oldDraw.getBudget() * MULTIPLY_FACTOR, newParticipants))
                                                .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Draw '" + oldDraw.getCode() + "' not found")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 400 with a error message when body is invalid")
    void updateDrawInvalidBodyException() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw oldDraw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        MockHttpServletResponse response = mvc
                .perform(
                        put("/draw/{code}", oldDraw.getCode())
                                .header("X-API-KEY",apiKey)
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Required body not found")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 200 with a message")
    void startDrawSuccessfully() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(3, 10));
        Draw draw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startDrawDTOJacksonTester
                                        .write(new StartDrawDTO(draw.getAdminCode()))
                                        .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        assertThat(response.getContentAsString()).isEqualTo("Draw started");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Should return a HTTP 200 with NotFoundException")
    void startDrawNotFoundException() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = produceDraw(participants);


        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startDrawDTOJacksonTester
                                        .write(new StartDrawDTO(draw.getAdminCode()))
                                        .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Draw '" + draw.getCode() + "' not found")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 400 when admin code is wrong")
    void startDrawInvalidAdminCode() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        String wrongAdminCode = "invalid_code";

        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startDrawDTOJacksonTester
                                        .write(new StartDrawDTO(wrongAdminCode))
                                        .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("The given admin code is incorrect")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 400 when draw already started")
    void startDrawAlreadyStarted() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(3, 10));
        Draw draw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        draw.startDraw();
        drawRepository.save(draw);

        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startDrawDTOJacksonTester
                                        .write(new StartDrawDTO(draw.getAdminCode()))
                                        .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Draw '" + draw.getCode() + "' already started")).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return a HTTP 400 when participant list is lesser than 3")
    void startDrawImpossibleDrawException() throws Exception {
        List<Participant> participants = produceParticipants(new Random().nextInt(2));
        Draw draw = drawService.createDraw(new DrawDTO(produceDraw(participants)));

        draw.startDraw();

        MockHttpServletResponse response = mvc
                .perform(
                        post("/draw/{code}", draw.getCode())
                                .header("X-API-KEY",apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startDrawDTOJacksonTester
                                        .write(new StartDrawDTO(draw.getAdminCode()))
                                        .getJson()
                                )
                )
                .andReturn()
                .getResponse();

        String expectedResponse = errorMessageDTOJacksonTester.write(new ErrorMessageDTO("Secret santa '" + draw.getCode() + "' is impossible due to its size: " + draw.getParticipants().size())).getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    private Draw produceDraw(List<Participant> participants) {
        String id = UUID.randomUUID().toString();
        String code = "ABC-123-DEF";
        String adminCode = "PAPA-MIKE-TANGO";
        int budget = 10000;

        Draw draw = new Draw();

        draw.setId(id);
        draw.setCode(code);
        draw.setAdminCode(adminCode);
        draw.setBudget(budget);
        draw.setParticipants(participants);

        return draw;
    }

    private List<Participant> produceParticipants(int size) {
        List<Participant> participants = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Participant p = new Participant();
            p.setId(UUID.randomUUID().toString());
            p.setName("test" + i);
            p.setEmail("test" + i + "@email.com");

            participants.add(p);
        }

        return participants;
    }

    private List<ParticipantDTO> parseParticipants(List<Participant> participants) {
        return participants.stream()
                .map(
                        p -> new ParticipantDTO(p.getName(), p.getEmail())
                )
                .collect(Collectors.toList());
    }
}
