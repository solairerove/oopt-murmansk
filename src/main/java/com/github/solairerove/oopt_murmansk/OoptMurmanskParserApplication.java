package com.github.solairerove.oopt_murmansk;

import com.github.solairerove.oopt_murmansk.processor.OoptStatisticsProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class OoptMurmanskParserApplication implements CommandLineRunner {

    private final OoptStatisticsProcessor ooptStatisticsProcessor;

    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(OoptMurmanskParserApplication.class, args);
        log.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner");

        this.ooptStatisticsProcessor.process();
    }
}
