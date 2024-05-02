package br.com.lmello.secret_santa.service;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.dto.ParticipantDTO;
import br.com.lmello.secret_santa.exception.ImpossibleDrawException;
import br.com.lmello.secret_santa.exception.InvalidAdminCodeException;
import br.com.lmello.secret_santa.exception.NotFoundException;
import br.com.lmello.secret_santa.exception.SecretSantaAlreadyStartedException;
import br.com.lmello.secret_santa.model.Draw;
import br.com.lmello.secret_santa.model.DrawResult;
import br.com.lmello.secret_santa.model.Participant;
import br.com.lmello.secret_santa.repository.DrawRepository;
import br.com.lmello.secret_santa.repository.DrawResultRepository;
import br.com.lmello.secret_santa.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private DrawResultRepository drawResultRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private DrawService drawService;

    @Test
    @DisplayName("Should create a new Draw successfully")
    public void testCreateDrawSuccessful() {
        int budget = 10000;
        List<ParticipantDTO> participantsDTOs = parseParticipants(produceParticipants(2));

        DrawDTO drawDTO = new DrawDTO(budget, participantsDTOs);

        Draw createdDraw = drawService.createDraw(drawDTO);

        assertNotNull(createdDraw);
        assertThat(createdDraw.getCode()).matches("^[A-Z0-9]{3}-[A-Z0-9]{3}-[A-Z0-9]{3}$");
        assertThat(createdDraw.getAdminCode()).matches("^[A-Z]{4,8}-[A-Z]{4,8}-[A-Z]{4,8}$");
        assertEquals(createdDraw.getBudget(), budget);
        assertEquals(createdDraw.getParticipants().size(), participantsDTOs.size());

        for (int i = 0; i < participantsDTOs.size(); i++) {
            assertEquals(participantsDTOs.get(i).email(), createdDraw.getParticipants().get(i).getEmail());
            assertEquals(participantsDTOs.get(i).name(), createdDraw.getParticipants().get(i).getName());
        }
    }

    @Test
    @DisplayName("Should successfully return a existent Draw")
    public void testGetDrawSuccessful() {
        Draw draw = produceDraw(produceParticipants(2));
        String code = draw.getCode();

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));

        Draw foundDraw = drawService.getDraw(code);

        assertNotNull(foundDraw);
        assertThat(foundDraw).isEqualTo(draw);
    }

    @Test
    @DisplayName("Should throw a NotFoundException when draw with given code not exists")
    public void testGetDrawNotFound() {
        Draw draw = produceDraw(produceParticipants(2));
        String code = draw.getCode();

        draw.setCode(code);

        when(drawRepository.findByCode(code)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> drawService.getDraw(code)
        );

        String expectedMessage = "Draw '" + code + "' not found";
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Should update participants and budget fields")
    public void testUpdateDrawSuccessful() {
        List<Participant> participants = produceParticipants(2);
        Draw draw = produceDraw(participants);

        String code = draw.getCode();
        int budget = draw.getBudget();
        List<ParticipantDTO> participantsDTOs = parseParticipants(participants);

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));

        Draw updatedDraw = drawService.updateDraw(code, new DrawDTO(budget * 2, participantsDTOs));

        int expectedBudget = budget * 2;

        assertNotNull(updatedDraw);

        assertEquals(updatedDraw.getCode(), draw.getCode());
        assertThat(updatedDraw.getParticipants().size()).isEqualTo(participantsDTOs.size());
        assertThat(updatedDraw.getBudget()).isEqualTo(expectedBudget);

        for (int i = 0; i < participantsDTOs.size(); i++) {
            assertThat(updatedDraw.getParticipants().get(i).getName()).isEqualTo(participantsDTOs.get(i).name());
            assertThat(updatedDraw.getParticipants().get(i).getEmail()).isEqualTo(participantsDTOs.get(i).email());
        }
    }

    @Test
    @DisplayName("Should throw a NotFoundException when draw with given code not exists")
    public void testUpdateDrawNotFound() {
        List<Participant> participants = produceParticipants(2);
        Draw draw = produceDraw(participants);

        String code = draw.getCode();
        int budget = draw.getBudget();
        List<ParticipantDTO> participantsDTOs = parseParticipants(participants);

        when(drawRepository.findByCode(code)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> drawService.updateDraw(code, new DrawDTO(budget * 2, participantsDTOs))
        );

        String expectedMessage = "Draw '" + code + "' not found";
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @DisplayName("Should start a draw successfully")
    @RepeatedTest(1000)
    public void testStartDrawSuccessful() {
        int totalParticipants = new Random().nextInt(3,100);

        List<Participant> participants = produceParticipants(totalParticipants);
        Draw draw = produceDraw(participants);

        String code = draw.getCode();
        String adminCode = draw.getAdminCode();

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));

        List<DrawResult> drawResults = drawService.startDraw(code, adminCode);

        assertNotNull(drawResults);
        assertThat(drawResults.size()).isEqualTo(draw.getParticipants().size());
        assertThat(draw.isStarted()).isTrue();

        List<Participant> senders = drawResults.stream().map(DrawResult::getFrom).toList();
        List<Participant> receivers = drawResults.stream().map(DrawResult::getTo).toList();

        for (int i = 0; i < drawResults.size(); i++) {
            assertThat(senders.get(i)).isNotEqualTo(receivers.get(i));
        }
    }

    @Test
    @DisplayName("Should throw a NotFoundException")
    public void testStartDrawNotFoundException() {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = produceDraw(participants);

        String code = draw.getCode();

        when(drawRepository.findByCode(code)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> drawService.startDraw(code, "")
        );

        String expectedMessage = "Draw '" + code + "' not found";
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Should throw a InvalidAdminCodeException")
    public void testStartDrawInvalidAdminCodeException() {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = produceDraw(participants);

        String code = draw.getCode();

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));

        InvalidAdminCodeException ex = assertThrows(
                InvalidAdminCodeException.class,
                () -> drawService.startDraw(code, "")
        );

        String expectedMessage = "The given admin code is incorrect";
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Should throw a SecretSantaAlreadyStartedException")
    public void testStartDrawSecretSantaAlreadyStartedException() {
        List<Participant> participants = produceParticipants(new Random().nextInt(10));
        Draw draw = produceDraw(participants);

        String code = draw.getCode();
        String adminCode = draw.getAdminCode();

        draw.setStarted(true);

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));


        SecretSantaAlreadyStartedException ex = assertThrows(
                SecretSantaAlreadyStartedException.class,
                () -> drawService.startDraw(code, adminCode)
        );

        String expectedMessage = "Draw '" + code + "' already started";
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Should throw a ImpossibleDrawException")
    public void testStartDrawImpossibleDrawException() {
        List<Participant> participants = produceParticipants(2);
        Draw draw = produceDraw(participants);

        String code = draw.getCode();
        String adminCode = draw.getAdminCode();
        int totalParticipants = draw.getParticipants().size();

        when(drawRepository.findByCode(code)).thenReturn(Optional.of(draw));


        ImpossibleDrawException ex = assertThrows(
                ImpossibleDrawException.class,
                () -> drawService.startDraw(code, adminCode)
        );

        String expectedMessage = "Secret santa '" + code + "' is impossible due to its size: " + totalParticipants;
        String actualMessage = ex.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
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
