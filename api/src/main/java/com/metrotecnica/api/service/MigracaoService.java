package com.metrotecnica.api.service;

import com.linuxense.javadbf.DBFReader;
import com.metrotecnica.api.dto.MigracaoResponseDTO;
import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.model.User;
import com.metrotecnica.api.repository.TenantRepository;
import com.metrotecnica.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class MigracaoService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final InstrumentoRecordPersister persister;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final Charset DBF_CHARSET = Charset.forName("ISO-8859-1");

    // SEM @Transactional aqui de propósito — cada etapa (criar tenant,
    // salvar cada instrumento) cuida da própria transação curta. Uma
    // transação única cobrindo os 1000+ registros é o que causava a
    // lentidão progressiva e a falha total quando um registro dava erro.
    public MigracaoResponseDTO migrar(MultipartFile zipFile, String nomeEmpresaBruto) {
        String nomeEmpresa = nomeEmpresaBruto.trim().toUpperCase();
        if (nomeEmpresa.isBlank()) {
            throw new IllegalArgumentException("Digite o nome da empresa antes de subir o arquivo.");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("migracao_");
            extrairZip(zipFile, tempDir);

            String slug = gerarSlug(nomeEmpresa);
            Tenant tenant = obterOuCriarTenant(nomeEmpresa, slug);

            Path dbfPath = localizarInstrumentosDbf(tempDir)
                    .orElseThrow(() -> new IllegalArgumentException("Arquivo instrumentos.dbf não encontrado dentro do ZIP"));

            List<String> warnings = new ArrayList<>();
            int count = processarDbf(dbfPath, tenant.getId(), warnings);

            return new MigracaoResponseDTO("Dados importados com sucesso", count, tenant.getId(), tenant.getName(), tenant.getSlug(), warnings);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo de migração: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } finally {
            if (tempDir != null) {
                try { apagarRecursivo(tempDir); } catch (Exception ignored) {}
            }
        }
    }

    @Transactional
    protected Tenant obterOuCriarTenant(String nomeEmpresa, String slug) {
        return tenantRepository.findBySlug(slug)
                .or(() -> tenantRepository.findAll().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(nomeEmpresa))
                        .findFirst())
                .orElseGet(() -> criarTenant(nomeEmpresa, slug));
    }

    private Tenant criarTenant(String nomeEmpresa, String slug) {
        Tenant t = new Tenant();
        t.setName(nomeEmpresa);
        t.setSlug(slug);
        t.setUrl("http://" + slug + ".metrotecnica.com.br");
        Tenant salvo = tenantRepository.save(t);

        String emailPadrao = "qualidade@" + slug + ".com.br";
        if (userRepository.findByEmail(emailPadrao).isEmpty()) {
            User u = new User();
            u.setEmail(emailPadrao);
            u.setPassword(passwordEncoder.encode("123456"));
            u.setRole("user");
            u.setTenant(salvo);
            userRepository.save(u);
        }
        return salvo;
    }

    private String gerarSlug(String nome) {
        String normalizado = java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalizado.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private void extrairZip(MultipartFile zipFile, Path destino) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), DBF_CHARSET)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path alvo = destino.resolve(entry.getName()).normalize();
                if (!alvo.startsWith(destino)) continue;
                if (entry.isDirectory()) {
                    Files.createDirectories(alvo);
                } else {
                    Files.createDirectories(alvo.getParent());
                    Files.copy(zis, alvo, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private Optional<Path> localizarInstrumentosDbf(Path raiz) throws IOException {
        try (Stream<Path> stream = Files.walk(raiz)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("instrumentos.dbf"))
                    .findFirst();
        }
    }

    /** Cada registro é persistido em sua própria transação curta via InstrumentoRecordPersister. */
    private int processarDbf(Path dbfPath, Long tenantId, List<String> warnings) throws IOException {
        int count = 0;
        InstrumentoRecordPersister.CacheAuxiliares cache = new InstrumentoRecordPersister.CacheAuxiliares();

        try (InputStream is = Files.newInputStream(dbfPath)) {
            DBFReader reader = new DBFReader(is, DBF_CHARSET);
            reader.setCharactersetName("ISO-8859-1");

            List<String> nomesCampos = new ArrayList<>();
            for (int i = 0; i < reader.getFieldCount(); i++) {
                nomesCampos.add(reader.getField(i).getName().toUpperCase());
            }

            Object[] rowObjects;
            int linha = 0;
            while ((rowObjects = reader.nextRecord()) != null) {
                linha++;
                Map<String, Object> reg = new HashMap<>();
                for (int i = 0; i < nomesCampos.size(); i++) {
                    reg.put(nomesCampos.get(i), rowObjects[i]);
                }

                try {
                    persister.salvarRegistro(tenantId, reg, cache);
                    count++;
                } catch (Exception e) {
                    // Um registro com problema não derruba os demais —
                    // cada um tem sua própria transação (REQUIRES_NEW).
                    warnings.add("Linha " + linha + " ignorada: " + e.getMessage());
                }
            }
            reader.close();
        }

        return count;
    }

    private void apagarRecursivo(Path dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        }
    }
}