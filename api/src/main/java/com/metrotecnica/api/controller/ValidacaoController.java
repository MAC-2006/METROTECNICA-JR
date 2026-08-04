package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.ValidacaoResponseDTO;
import com.metrotecnica.api.model.Instrumento;
import com.metrotecnica.api.repository.InstrumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público (sem autenticação) usado pelo QR code impresso nos
 * certificados. Recebe o hash SHA-256 gerado na assinatura e devolve
 * um resumo do certificado — sem expor dados sensíveis do tenant.
 */
@RestController
@RequiredArgsConstructor
public class ValidacaoController {

    private final InstrumentoRepository instrumentoRepository;

    @GetMapping("/api/validar/{hash}")
    public ResponseEntity<ValidacaoResponseDTO> validar(@PathVariable String hash) {
        return instrumentoRepository.findByDocumentHash(hash)
                .map(this::montarResposta)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(respostaInvalida()));
    }

    private ValidacaoResponseDTO montarResposta(Instrumento inst) {
        String tenantNome = inst.getTenant() != null
                ? (inst.getTenant().getRazaoSocial() != null ? inst.getTenant().getRazaoSocial() : inst.getTenant().getName())
                : "---";

        return new ValidacaoResponseDTO(
                true,
                inst.getIdentificacao(),
                inst.getDescricao(),
                inst.getMarca(),
                inst.getModelo(),
                tenantNome,
                inst.getDataCertificacao(),
                inst.getProxCalibFormatada(),
                inst.getStatusGeral(),
                inst.getMetrologistaNome(),
                inst.getResponsavelNome(),
                inst.getDocumentHash() != null ? inst.getDocumentHash().substring(0, 16) : null
        );
    }

    private ValidacaoResponseDTO respostaInvalida() {
        return new ValidacaoResponseDTO(false, null, null, null, null, null, null, null, null, null, null, null);
    }
}