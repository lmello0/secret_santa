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
                .path("/draw/{code}")
                .buildAndExpand(draw.getCode())
                .toUri();

        return ResponseEntity.created(uri).body(new DrawDTO(draw));
    }

    @GetMapping("{code}")
    public ResponseEntity<?> getDraw(@PathVariable String code) {
        Draw draw = drawService.getDraw(code);

        return ResponseEntity.ok(new DrawDTO(draw));
    }

    @PutMapping("{code}")
    public ResponseEntity<?> updateDraw(@PathVariable String code, @Valid @RequestBody DrawDTO drawData) {
        Draw draw = drawService.updateDraw(code, drawData);

        return ResponseEntity.ok(new DrawDTO(draw));
    }

    @PostMapping("{code}")
    public ResponseEntity<?> startDraw(@PathVariable String code, @Valid @RequestBody StartDrawDTO startDrawDTO) {
        List<DrawResult> drawResult = drawService.startDraw(code, startDrawDTO.adminCode());

        List<DrawEmailDTO> emailList = drawResult.stream()
                .map(d -> new DrawEmailDTO(
                        d.getFrom(),
                        d.getTo(),
                        d.getDraw().getCode(),
                        d.getDraw().getBudget()
                )).toList();

        emailList.forEach(emailService::sendEmail);

        return ResponseEntity.ok().body(new StartDrawDTO(null, "Draw started"));
    }

    @DeleteMapping("{code}")
    public ResponseEntity<?> deleteDraw(@PathVariable String code) {
        drawService.deleteDraw(code);

        return ResponseEntity.noContent().build();
    }
}
