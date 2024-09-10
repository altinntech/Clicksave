package com.altinntech.clicksave;

import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.examples.repository.JpaPersonRepository;
import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Long test designed to detect possible memory leaks and thread deaths.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ClickSaveConfiguration.class)
public class LongClicksaveTest {

    private static final Logger log = LoggerFactory.getLogger(LongClicksaveTest.class);

    @Autowired
    private JpaPersonRepository jpaPersonRepository;

    @Test
    @Disabled
    @SneakyThrows
    void longSaveTest() {
        registerShutdownHooks();
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 60 * 60 * 24; i++) {
                log.error("Iteration {}", i);
                jpaPersonRepository.save(Person.buildMockPerson());
                jpaPersonRepository.findAll();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return 0;
                }
                if (i % 3600 == 0) {
                    log.error("Saving heap dump");
                    dumpHeap();
                }
            }
            return 1;
        });
        Integer result = completableFuture.get();
        Assertions.assertEquals(1, result);
    }

    // Shutdown hooks

    private static void registerShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(LongClicksaveTest::dumpHeap));
        Runtime.getRuntime().addShutdownHook(new Thread(LongClicksaveTest::printAllStackTraces));
        Runtime.getRuntime().addShutdownHook(new Thread(LongClicksaveTest::printMemoryUsage));
        Runtime.getRuntime().addShutdownHook(new Thread(LongClicksaveTest::printExitMessage));
    }

    private static void printExitMessage() {
        System.err.println("Exiting the Clicksave application");
    }

    private static void printMemoryUsage() {
        System.err.println(
                String.format(
                        "Exiting the Clicksave application, memory stats: Total memory: %d | Free memory: %d | Max memory: %d",
                        Runtime.getRuntime().totalMemory(),
                        Runtime.getRuntime().freeMemory(),
                        Runtime.getRuntime().maxMemory())
        );
    }

    public static void dumpHeap() {
        String outputFile = "./clicksave_" + Instant.now().toEpochMilli() + ".hprof";
        try {
            System.err.println("Exiting the Clicksave application, attempting to save heap dump " + outputFile);
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                    server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class
            );
            mxBean.dumpHeap(outputFile, false);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exiting the Clicksave application, unable to save heap dump " + outputFile);
        }
    }

    private static void printAllStackTraces() {
        System.err.println("Exiting the Clicksave application, printing all stacktraces:");
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            System.err.println(entry.getKey() + " " + entry.getKey().getState());
            for (StackTraceElement ste : entry.getValue()) {
                System.err.println("\tat " + ste);
            }
            System.err.println();
        }
    }
}
