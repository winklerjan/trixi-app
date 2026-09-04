package cz.kopidlno;

import cz.kopidlno.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class KopidlnoApplication implements CommandLineRunner {

    private final ImportService importService;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KopidlnoApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting Kopidlno data import...");
        importService.importData();
        log.info("Done.");
    }
}
