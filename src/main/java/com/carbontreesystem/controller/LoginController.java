package com.carbontreesystem.controller;

import com.carbontreesystem.dto.LoginDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @PostMapping("/login")
    public ResponseEntity<?> autenticar(@RequestBody LoginDTO dados) {

        // Simulação de login (enquanto o colega não liga ao banco)
        if ("admin".equals(dados.getUsuario()) && "123".equals(dados.getSenha())) {
            return ResponseEntity.ok(Map.of("sucesso", true));
        }

        return ResponseEntity.status(401).body(Map.of("sucesso", false, "mensagem", "Incorreto"));
    }
}