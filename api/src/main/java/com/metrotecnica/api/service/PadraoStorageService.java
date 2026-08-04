package com.metrotecnica.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Um único ponto de leitura/escrita para os PDFs de "Cópia dos Padrões" —
 * resolve o bug antigo do Flask onde a migração salvava em /tmp/padroes
 * e a listagem lia de outro lugar (app/static/padroes).
 */
@Service
public class PadraoStorageService {

    @Value("${app.storage.padroes-path}")
    private String padroesPath;

    public List<String> listarArquivos() throws IOException {
        Path base = Paths.get(padroesPath);
        if (!Files.exists(base)) return List.of();

        try (Stream<Path> stream = Files.walk(base)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .map(base::relativize)
                    .map(Path::toString)
                    .sorted()
                    .toList();
        }
    }

    public List<String> buscar(String termo) throws IOException {
        String termoLower = termo.toLowerCase();
        return listarArquivos().stream()
                .filter(nome -> nome.toLowerCase().contains(termoLower))
                .toList();
    }

    public Path resolverArquivo(String caminhoRelativo) {
        Path base = Paths.get(padroesPath);
        Path alvo = base.resolve(caminhoRelativo).normalize();
        if (!alvo.startsWith(base)) {
            throw new IllegalArgumentException("Caminho inválido.");
        }
        return alvo;
    }

    /** Extrai um ZIP com vários PDFs direto na pasta de padrões. Retorna quantos arquivos foram salvos. */
    public int importarZip(MultipartFile zipFile) throws IOException {
        Path base = Paths.get(padroesPath);
        Files.createDirectories(base);

        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), java.nio.charset.Charset.forName("ISO-8859-1"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                if (!entry.getName().toLowerCase().endsWith(".pdf")) continue;

                Path alvo = base.resolve(Paths.get(entry.getName()).getFileName().toString()).normalize();
                if (!alvo.startsWith(base)) continue; // zip slip

                Files.copy(zis, alvo, StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }
}