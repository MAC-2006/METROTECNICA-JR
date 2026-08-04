package com.metrotecnica.api.service;

import com.metrotecnica.api.model.Instrumento;
import com.metrotecnica.api.repository.InstrumentoRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final InstrumentoRepository instrumentoRepository;
    private final TemplateEngine templateEngine;

    public byte[] gerarPdf(String tipo, String start, String end, String valor, Long tenantId) {
        List<Instrumento> insts = buscarInstrumentos(tipo, start, end, valor, tenantId);

        Context context = new Context();
        String htmlRenderizado;

        if ("setor".equals(tipo)) {
            Map<String, List<Instrumento>> agrupados = insts.stream()
                    .collect(Collectors.groupingBy(
                            i -> i.getSetor() != null ? i.getSetor().getNome() : "OUTROS",
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            context.setVariable("instrumentosAgrupados", agrupados);
            htmlRenderizado = templateEngine.process("relatorios/setor", context);
        } else {
            context.setVariable("insts", insts);
            context.setVariable("titulo", tipo.toUpperCase());
            context.setVariable("dataHoje", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            htmlRenderizado = templateEngine.process("relatorios/listagem", context);
        }

        return converterParaPdf(htmlRenderizado);
    }

    private List<Instrumento> buscarInstrumentos(String tipo, String start, String end, String valor, Long tenantId) {
        LocalDate hoje = LocalDate.now();

        return switch (tipo) {
            case "vencidos" -> instrumentoRepository.findByTenantIdAndDataProximaCalibracaoLessThan(tenantId, hoje);
            case "proximas" -> (start == null || end == null)
                    ? instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId)
                    : instrumentoRepository.findByTenantIdAndDataProximaCalibracaoBetween(tenantId, LocalDate.parse(start), LocalDate.parse(end));
            case "setor" -> (valor == null)
                    ? instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId)
                    : instrumentoRepository.findByTenantIdAndSetorIdOrderByDescricaoAsc(tenantId, Long.valueOf(valor));
            case "local" -> (valor == null)
                    ? instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId)
                    : instrumentoRepository.findByTenantIdAndLocalUsoId(tenantId, Long.valueOf(valor));
            case "situacao" -> (valor == null)
                    ? instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId)
                    : instrumentoRepository.findByTenantIdAndStatusGeral(tenantId, valor);
            case "cadastro" -> (start == null || end == null)
                    ? instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId)
                    : instrumentoRepository.findByTenantIdAndDataCadastroBetween(tenantId, LocalDate.parse(start), LocalDate.parse(end));
            default -> instrumentoRepository.findByTenantIdOrderBySetorIdAscDescricaoAsc(tenantId);
        };
    }

    private byte[] converterParaPdf(String html) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    public byte[] gerarExcel(String tipo, String start, String end, String valor, Long tenantId) {
        List<Instrumento> insts = buscarInstrumentos(tipo, start, end, valor, tenantId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatório");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] colunas = {"Nº Sequencial", "TAG/ID", "Descrição", "Setor", "Local", "Certificado", "Próx. Calibração", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Instrumento i : insts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i.getNumeroSequencial() != null ? i.getNumeroSequencial() : "");
                row.createCell(1).setCellValue(i.getIdentificacao());
                row.createCell(2).setCellValue(i.getDescricao());
                row.createCell(3).setCellValue(i.getSetor() != null ? i.getSetor().getNome() : "");
                row.createCell(4).setCellValue(i.getLocalUso() != null ? i.getLocalUso().getNome() : "");
                row.createCell(5).setCellValue(i.getCertificado() != null ? i.getCertificado() : "");
                row.createCell(6).setCellValue(i.getDataProximaCalibracao() != null ? i.getDataProximaCalibracao().format(fmt) : "");
                row.createCell(7).setCellValue(i.getStatusGeral() != null ? i.getStatusGeral() : "");
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(os);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel: " + e.getMessage(), e);
        }
    }

}