package com.metrotecnica.api.service;

import com.metrotecnica.api.model.*;
import com.metrotecnica.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Persiste UM registro do DBF por vez, em sua PRÓPRIA transação (REQUIRES_NEW).
 * Isso evita que a sessão do Hibernate cresça indefinidamente numa migração
 * grande (causa da lentidão progressiva) e evita que um registro com erro
 * derrube a migração inteira (cada linha commita ou falha isoladamente).
 */
@Component
@RequiredArgsConstructor
public class InstrumentoRecordPersister {

    private final TenantRepository tenantRepository;
    private final SetorRepository setorRepository;
    private final LocalUsoRepository localUsoRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final HistoricoCalibracaoRepository historicoCalibracaoRepository;
    private final InstrumentoService instrumentoService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvarRegistro(Long tenantId, Map<String, Object> reg, CacheAuxiliares cache) {
        Tenant tenant = tenantRepository.getReferenceById(tenantId);

        String ident = limparStr(reg.get("IDENTIFICA"));
        if (ident.isBlank()) return;

        String setorNome = limparStr(reg.get("SETOR"));
        String localNome = limparStr(getSafeField(reg, "RESPONSÁVEL", "RESPONSAVE"));

        Long setorId = null, localId = null;
        if (!setorNome.isBlank()) {
            setorId = cache.setores.computeIfAbsent(setorNome, nome ->
                    setorRepository.findByNomeAndTenantId(nome, tenantId)
                            .map(Setor::getId)
                            .orElseGet(() -> {
                                Setor s = new Setor();
                                s.setNome(nome);
                                s.setTenant(tenant);
                                return setorRepository.save(s).getId();
                            }));
        }
        if (!localNome.isBlank()) {
            localId = cache.locais.computeIfAbsent(localNome, nome ->
                    localUsoRepository.findByNomeAndTenantId(nome, tenantId)
                            .map(LocalUso::getId)
                            .orElseGet(() -> {
                                LocalUso l = new LocalUso();
                                l.setNome(nome);
                                l.setTenant(tenant);
                                return localUsoRepository.save(l).getId();
                            }));
        }

        Instrumento inst = instrumentoRepository.findByIdentificacaoAndTenantId(ident, tenantId)
                .orElseGet(Instrumento::new);
        inst.setTenant(tenant);
        inst.setIdentificacao(ident);

        inst.setNumeroSequencial(limparStr(getSafeField(reg, "SEQUÊNCIA", "SEQUENCIA")));
        inst.setDescricao(limparStr(getSafeField(reg, "DESCRIÇÃO", "DESCRICAO")));
        inst.setMarca(limparStr(reg.get("MARCA")));
        inst.setModelo(limparStr(reg.get("MODELO")));
        inst.setCapacidade(limparStr(reg.get("CAPACIDADE")));
        inst.setPrecisao(limparStr(getSafeField(reg, "PRECISÃO", "PRECISAO")));

        if (setorId != null) inst.setSetor(setorRepository.getReferenceById(setorId));
        if (localId != null) inst.setLocalUso(localUsoRepository.getReferenceById(localId));

        inst.setDataCertificacao(converterData(reg.get("DATA")));
        inst.setFrequenciaMeses(diasParaMeses(getFloat(reg.get("FREQUENCIA"))));
        inst.setDataProximaCalibracao(converterData(reg.get("DTPROXIMA")));
        inst.setCertificado(limparStr(getSafeField(reg, "CERTIFICAD", "CERTIFICADO")));

        inst.setLpMin(getFloat(reg.get("LPMIN")));
        inst.setLpMax(getFloat(reg.get("LPMAX")));
        inst.setAferidoLp(getFloat(reg.get("AFERIDOLP")));
        inst.setLnpMin(getFloat(reg.get("LNPMIN")));
        inst.setLnpMax(getFloat(reg.get("LNPMAX")));
        inst.setAferidoLnp(getFloat(reg.get("AFERIDOLNP")));
        inst.setErro(getFloat(reg.get("ERRO")));
        inst.setIncertezaMedicao(getFloat(getSafeField(reg, "INCERTEZAM", "INCERTEZAM")));
        inst.setCriterioAceitacao(getFloat(getSafeField(reg, "CRITERIOAC", "CRITERIOAC")));

        String situacao = limparStr(getSafeField(reg, "SITUAÇÃO", "SITUACAO"));
        inst.setStatusGeral(situacao.isBlank() ? "APROVADO" : situacao);
        inst.setObservacoes(limparStr(getSafeField(reg, "HISTÓRICO", "HISTORICO")));

        instrumentoService.calcularMetrologia(inst, false);
        Instrumento salvo = instrumentoRepository.save(inst);

        for (String campoHist : List.of("CERTIFICA2", "CERTIFICA3")) {
            String certAntigo = limparStr(reg.get(campoHist));
            if (certAntigo.isBlank()) continue;

            boolean jaExiste = historicoCalibracaoRepository
                    .findByInstrumentoIdAndCertificado(salvo.getId(), certAntigo)
                    .isPresent();
            if (jaExiste) continue;

            HistoricoCalibracao h = new HistoricoCalibracao();
            h.setInstrumento(salvo);
            h.setCertificado(certAntigo);
            h.setStatusNaEpoca("APROVADO");
            if (salvo.getDataCertificacao() != null) {
                h.setDataCertificacao(salvo.getDataCertificacao().minusYears(1));
            }
            historicoCalibracaoRepository.save(h);
        }
    }

    /** Guarda os IDs de setor/local já resolvidos, evitando repetir a busca a cada linha. */
    public static class CacheAuxiliares {
        public final Map<String, Long> setores = new java.util.HashMap<>();
        public final Map<String, Long> locais = new java.util.HashMap<>();
    }

    private String limparStr(Object val) {
        if (val == null) return "";
        String s = val.toString().trim();
        if (s.isEmpty() || s.equalsIgnoreCase("none") || s.equalsIgnoreCase("null")) return "";
        return s;
    }

    private Double getFloat(Object val) {
        if (val == null) return 0.0;
        try {
            String s = val.toString().trim().replace(",", ".");
            if (s.isEmpty()) return 0.0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Object getSafeField(Map<String, Object> reg, String principal, String fallback) {
        Object val = reg.get(principal.toUpperCase());
        return val != null ? val : reg.get(fallback.toUpperCase());
    }

    private LocalDate converterData(Object val) {
        if (val == null) return null;
        if (val instanceof Date d) {
            LocalDate ld = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            return ld.getYear() > 1900 ? ld : null;
        }
        if (val instanceof String s) {
            s = s.trim();
            if (s.isEmpty() || s.equalsIgnoreCase("none")) return null;
            for (String pattern : List.of("yyyy-MM-dd", "dd/MM/yyyy", "yyyyMMdd")) {
                try {
                    return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Integer diasParaMeses(Double dias) {
        if (dias == null || dias == 0) return 12;
        int d = dias.intValue();
        if (d == 360 || d == 365 || d == 366) return 12;
        if (d == 720 || d == 730) return 24;
        if (d == 1090 || d == 1095) return 36;
        if (d == 180 || d == 184) return 6;
        return Math.max(1, Math.round(d / 30.5f));
    }
}