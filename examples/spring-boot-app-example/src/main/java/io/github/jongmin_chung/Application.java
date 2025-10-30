package io.github.jongmin_chung;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

record NullawayExampleRecord(String name, @Nullable Integer value) {}

@SpringBootApplication
public class Application {
    public static void main(String[] args) {

        NullawayExampleRecord record = new NullawayExampleRecord("name", null);

        record.value().toString();

        SpringApplication.run(Application.class, args);
    }
}
