package br.com.lmello.secret_santa.service;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.infra.exception.ImpossibleDrawException;
import br.com.lmello.secret_santa.infra.exception.InvalidAdminCodeException;
import br.com.lmello.secret_santa.infra.exception.NotFoundException;
import br.com.lmello.secret_santa.infra.exception.SecretSantaAlreadyStartedException;
import br.com.lmello.secret_santa.model.Draw;
import br.com.lmello.secret_santa.model.DrawResult;
import br.com.lmello.secret_santa.model.Participant;
import br.com.lmello.secret_santa.repository.DrawRepository;
import br.com.lmello.secret_santa.repository.DrawResultRepository;
import br.com.lmello.secret_santa.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrawService {
    DrawRepository drawRepository;
    DrawResultRepository drawResultRepository;
    ParticipantRepository participantRepository;

    public DrawService(DrawRepository drawRepository, DrawResultRepository drawResultRepository, ParticipantRepository participantRepository) {
        this.drawRepository = drawRepository;
        this.drawResultRepository = drawResultRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public Draw createDraw(DrawDTO data) {
        List<Participant> participants = data.participants()
                .stream()
                .map(p -> participantRepository
                        .getParticipantByNameAndEmailC(p.name(), p.email())
                        .orElseGet(() -> new Participant(p)))
                .toList();

        participantRepository.saveAll(participants);

        Draw draw = new Draw(data, participants);

        drawRepository.save(draw);

        return draw;
    }

    public Draw getDraw(String code) {
        Optional<Draw> draw = drawRepository.findByCode(code);

        if (draw.isEmpty()) {
            throw new NotFoundException("Draw '" + code + "' not found");
        }

        return draw.get();
    }

    @Transactional
    public Draw updateDraw(String code, DrawDTO data) {
        Optional<Draw> optionalDraw = drawRepository.findByCode(code);

        if (optionalDraw.isEmpty()) {
            throw new NotFoundException("Draw '" + code + "' not found");
        }

        Draw draw = optionalDraw.get();

        List<Participant> participants = data.participants()
                .stream()
                .map(p -> participantRepository
                        .getParticipantByNameAndEmailC(p.name(), p.email())
                        .orElseGet(() -> new Participant(p)))
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);

        draw.setParticipants(participants);
        draw.setBudget(data.budget());

        drawRepository.save(draw);

        return draw;
    }

    @Transactional
    public List<DrawResult> startDraw(String code, String adminCode) {
        Optional<Draw> optionalDraw = drawRepository.findByCode(code);

        if (optionalDraw.isEmpty()) {
            throw new NotFoundException("Draw '" + code + "' not found");
        }

        Draw draw = optionalDraw.get();

        if (!draw.getAdminCode().equals(adminCode)) {
            throw new InvalidAdminCodeException();
        }

        if (draw.isStarted()) {
            throw new SecretSantaAlreadyStartedException(code);
        }

        if (draw.getParticipants().size() < 3) {
            throw new ImpossibleDrawException(code, draw.getParticipants().size());
        }

        List<DrawResult> drawResult = draft(draw);

        drawResultRepository.saveAll(drawResult);
        draw.startDraw();

        return drawResult;
    }

    public List<DrawResult> draft(Draw draw) {
        Random rand = new Random();

        List<Participant> participants = draw.getParticipants();
        List<DrawResult> results = new ArrayList<>(participants.size());

        Collections.shuffle(participants);

        boolean redraw;
        do {
            redraw = false;
            results.clear();
            participants.forEach(p -> p.setSelected(false));

            for (int i = 0; i < participants.size(); i++) {
                Participant sender = participants.get(i);

                int recipientIdx = rand.nextInt(participants.size());
                Participant recipient = participants.get(recipientIdx);

                while (sender.getId().equals(recipient.getId()) || recipient.isSelected()) {
                    recipientIdx = rand.nextInt(participants.size());
                    recipient = participants.get(recipientIdx);

                    long totalAvailable = participants.stream()
                            .filter(p -> !p.isSelected())
                            .count();

                    if (totalAvailable <= 1) {
                        redraw = true;
                        break;
                    }
                }

                if (redraw) {
                    break;
                }

                sender.setTo(recipient);
                sender.setFrom(sender);
                recipient.setSelected(true);

                results.add(new DrawResult(null, draw, sender, recipient));
            }
        } while (redraw);

        return results;
    }
}
