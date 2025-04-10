package com.github.solairerove.oopt_murmansk;

import com.github.solairerove.oopt_murmansk.processor.OoptStatisticsProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class OoptMurmanskParserApplication implements CommandLineRunner {

    private final OoptStatisticsProcessor ooptStatisticsProcessor;

    public static void main(String[] args) {
        System.out.println("STARTING THE APPLICATION");
        SpringApplication app = new SpringApplication(OoptMurmanskParserApplication.class);

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", 8085);
        app.setDefaultProperties(props);

        app.run(args);

        System.out.println("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        System.out.println("EXECUTING : command line runner");

        this.ooptStatisticsProcessor.process();

        SpringApplication.exit(SpringApplication.run(OoptMurmanskParserApplication.class, args));
    }
}
