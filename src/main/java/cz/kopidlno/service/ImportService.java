package cz.kopidlno.service;

import cz.kopidlno.parser.ParseResult;
import cz.kopidlno.parser.RuianXmlParser;
import cz.kopidlno.repository.CastObceRepository;
import cz.kopidlno.repository.ObecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private final ObecRepository obecRepository;
    private final CastObceRepository castObceRepository;
    private final RuianXmlParser parser;

    @Value("${app.source-url}")
    private String sourceUrl;

    public void importData() {
        log.info("Downloading ZIP from: {}", sourceUrl);

        try (InputStream httpStream = URI.create(sourceUrl).toURL().openStream();
             ZipInputStream zipStream = new ZipInputStream(httpStream)) {

            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    log.info("Parsing entry: {}", entry.getName());
                    ParseResult result = parser.parse(zipStream);
                    save(result);
                    return;
                }
            }

            log.warn("No XML file found in the ZIP archive.");

        } catch (IOException e) {
            throw new RuntimeException("Failed to download or read ZIP file", e);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }

    @Transactional
    protected void save(ParseResult result) {
        obecRepository.saveAll(result.obce());
        castObceRepository.saveAll(result.castiObci());

        log.info("Import complete: {} obec(í), {} část(í) obce",
                result.obce().size(), result.castiObci().size());
    }
}
