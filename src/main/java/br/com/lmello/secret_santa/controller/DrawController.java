package br.com.lmello.secret_santa.controller;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.dto.DrawEmailDTO;
import br.com.lmello.secret_santa.dto.StartDrawDTO;
import br.com.lmello.secret_santa.model.Draw;
import br.com.lmello.secret_santa.model.DrawResult;
import br.com.lmello.secret_santa.service.DrawService;
import br.com.lmello.secret_santa.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/draw")
public class DrawController {
    DrawService drawService;
    EmailService emailService;

    public DrawController(DrawService drawService, EmailService emailService) {
        this.drawService = drawService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> createDraw(@Valid @RequestBody DrawDTO drawData, UriComponentsBuilder uriComponentsBuilder) {
        Draw draw = drawService.createDraw(drawData);

        URI uri = uriComponentsBuilder
                .path("/draw/{id}")
                .buildAndExpand(draw.getId())
                .toUri();

        return ResponseEntity.created(uri).body(new DrawDTO(draw));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getDraw(@PathVariable String id) {
        Draw draw = drawService.getDraw(id);

        return ResponseEntity.ok(new DrawDTO(draw));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateDraw(@PathVariable String id, @Valid @RequestBody DrawDTO drawData) {
        Draw draw = drawService.updateDraw(id, drawData);

        return ResponseEntity.ok(new DrawDTO(draw));
    }

    @PostMapping("{id}")
    public ResponseEntity<?> startDraw(@PathVariable String id, @Valid @RequestBody StartDrawDTO startDrawDTO) {
        List<DrawResult> drawResult = drawService.startDraw(id, startDrawDTO.adminCode());

        List<DrawEmailDTO> emailList = drawResult.stream()
                .map(d -> new DrawEmailDTO(
                        d.getFrom(),
                        d.getTo(),
                        d.getDraw().getCode(),
                        d.getDraw().getBudget()
                )).toList();

//        emailList.forEach(emailService::sendEmail);

        return ResponseEntity.ok().body("Draw started");
    }
}
