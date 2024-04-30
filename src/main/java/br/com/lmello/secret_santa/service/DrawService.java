package br.com.lmello.secret_santa.service;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.exception.InvalidAdminCodeException;
import br.com.lmello.secret_santa.exception.NotFoundException;
import br.com.lmello.secret_santa.exception.SecretSantaAlreadyStartedException;
import br.com.lmello.secret_santa.model.Draw;
import br.com.lmello.secret_santa.model.DrawResult;
import br.com.lmello.secret_santa.model.Participant;
import br.com.lmello.secret_santa.repository.DrawRepository;
import br.com.lmello.secret_santa.repository.DrawResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrawService {
    DrawRepository drawRepository;

    DrawResultRepository drawResultRepository;

    public DrawService(DrawRepository drawRepository, DrawResultRepository drawResultRepository) {
        this.drawRepository = drawRepository;
        this.drawResultRepository = drawResultRepository;
    }

    @Transactional
    public Draw createDraw(DrawDTO data) {
        Draw draw = new Draw(data);

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

        List<Participant> participants = data.
                participants().
                stream().
                map(Participant::new)
                .collect(Collectors.toList());

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

        List<DrawResult> drawResult = draft2(draw);

        drawResultRepository.saveAll(drawResult);
        draw.startDraw();

        return drawResult;
    }

    private List<DrawResult> draft(Draw draw) {
        Random rand = new Random();
        List<DrawResult> results = new ArrayList<>();;

        boolean retry = true;
        while (retry) {
            List<Participant> participants = draw.getParticipants();
            List<Participant> recipients = draw.getParticipants().stream().map(Participant::new).collect(Collectors.toList());
            results.clear();

            for (int i = 0; i < 10; i++) {
                Collections.shuffle(recipients);
            }

            for (int i = 0; i < participants.size(); i++) {
                int recipientIdx = rand.nextInt(recipients.size());

                Participant sender = participants.get(i);
                Participant recipient = recipients.get(recipientIdx);

                boolean isGivingItself = sender.getId().equals(recipient.getId());

                if (isGivingItself && recipients.size() == 1) {
                    break;
                }

                while (isGivingItself) {
                    recipientIdx = rand.nextInt(recipients.size());
                    recipient = recipients.get(recipientIdx);
                    isGivingItself = sender.getId().equals(recipient.getId());
                }

                sender.setTo(recipient);
                sender.setFrom(sender);

                results.add(new DrawResult(null, draw, sender, recipient));

                recipients.remove(recipient);

                if (i == participants.size() - 1) {
                    retry = false;
                }
            }
        }

        return results;
    }

    private List<DrawResult> draft2(Draw draw) {
        Random rand = new Random();
        List<Participant> participants = draw.getParticipants();
        List<DrawResult> results = new ArrayList<>(participants.size());

        Collections.shuffle(participants);

        for (int i = 0; i < participants.size(); i++) {
            Participant sender = participants.get(i);

            int recipientIdx = rand.nextInt(participants.size());
            Participant recipient = participants.get(recipientIdx);

            while(sender.getId().equals(recipient.getId()) || recipient.isSelected()) {
                recipientIdx = rand.nextInt(participants.size());
                recipient = participants.get(recipientIdx);
            }

            sender.setTo(recipient);
            sender.setFrom(sender);
            recipient.setSelected(true);

            results.add(new DrawResult(null, draw, sender, recipient));
        }

        return results;
    }
}
