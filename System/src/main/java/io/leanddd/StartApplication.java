package io.leanddd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.leanddd.component.data.impl.EntityMetaRegistrar;
import io.leanddd.component.data.impl.MetadataProviderImpl;
import io.leanddd.component.framework.MetadataProvider;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

class Constants {
    static final String rootPackage = "io.leanddd";
}

@EnableTransactionManagement(order = 0)
@EnableScheduling
@SpringBootApplication(scanBasePackages = {Constants.rootPackage})
public class StartApplication {

    public static void main(String[] args) {
        initLogger();

        new EntityMetaRegistrar().initClasses(Constants.rootPackage);

        SpringApplication.run(StartApplication.class, args);
    }

    private static void initLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("org.springframework").setLevel(Level.WARN);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{dd/HH:mm:ss.SSS} %-5level[%15.-15logger{0}-%-10.-10thread] - %msg %n");
        encoder.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
    }

    @Bean
    @DependsOn("_Context")
    Object Init0() {
        return new Object();
    }

    //before init: create tables
    @Bean
    @DependsOn({"Init0", "startUpHandlerImpl", "MetadataProvider"})
    Object Init() {
        return new Object();
    }

    @Bean
    @DependsOn({"Init", "userMapper"})
    Object Init2() {
        return new Object();
    }

    @Bean("MetadataProvider")
    MetadataProvider metadataProvider() {
        return new MetadataProviderImpl();
    }
}
