package edu.hse.netchat.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class EndToEndChatTest {

    private static final Pattern LISTEN_PATTERN = Pattern.compile("^Listening on (.+):(\\d+)$");
    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] Bob: hello$");

    @Test
    void twoProcessesCanChatOverLocalhost() throws Exception {
        String classpath = System.getProperty("java.class.path");
        String javaExe = javaExecutable();

        ProcessWithOutput server =
                startProcess(
                        javaExe,
                        classpath,
                        "edu.hse.netchat.App",
                        "--name",
                        "Alice",
                        "--listen",
                        "127.0.0.1:0");

        String listenLine = server.waitForLine(LISTEN_PATTERN, Duration.ofSeconds(10));
        Matcher matcher = LISTEN_PATTERN.matcher(listenLine);
        assertThat(matcher.matches()).isTrue();
        int port = Integer.parseInt(matcher.group(2));

        ProcessWithOutput client =
                startProcess(
                        javaExe,
                        classpath,
                        "edu.hse.netchat.App",
                        "--name",
                        "Bob",
                        "--peer",
                        "127.0.0.1:" + port);

        client.writeLine("hello");
        client.writeLine("/exit");
        client.closeStdin();

        String messageLine = server.waitForLine(MESSAGE_PATTERN, Duration.ofSeconds(10));
        assertThat(messageLine).matches(MESSAGE_PATTERN);

        server.writeLine("/exit");
        server.closeStdin();

        assertThat(client.waitForExit(Duration.ofSeconds(10))).isEqualTo(0);
        assertThat(server.waitForExit(Duration.ofSeconds(10))).isEqualTo(0);

        client.destroy();
        server.destroy();
    }

    private static String javaExecutable() {
        String javaHome = System.getProperty("java.home");
        Path exe = Path.of(javaHome, "bin", isWindows() ? "java.exe" : "java");
        return exe.toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static ProcessWithOutput startProcess(
            String javaExe, String classpath, String mainClass, String... args) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe);
        cmd.add("-cp");
        cmd.add(classpath);
        cmd.add(mainClass);
        for (String arg : args) {
            cmd.add(arg);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        return new ProcessWithOutput(p);
    }

    private static final class ProcessWithOutput {

        private final Process process;
        private final OutputStreamWriter stdin;
        private final LinkedBlockingQueue<String> lines = new LinkedBlockingQueue<>();

        ProcessWithOutput(Process process) {
            this.process = process;
            this.stdin = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);

            Thread gobbler =
                    new Thread(
                            () -> {
                                try (BufferedReader br =
                                        new BufferedReader(
                                                new InputStreamReader(
                                                        process.getInputStream(),
                                                        StandardCharsets.UTF_8))) {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        lines.offer(line);
                                    }
                                } catch (IOException ignored) {
                                    // ignore
                                }
                            },
                            "e2e-output");
            gobbler.setDaemon(true);
            gobbler.start();
        }

        void writeLine(String line) throws IOException {
            stdin.write(line);
            stdin.write("\n");
            stdin.flush();
        }

        void closeStdin() throws IOException {
            stdin.close();
        }

        String waitForLine(Pattern pattern, Duration timeout) throws InterruptedException {
            long deadlineNanos = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadlineNanos) {
                long remainingMillis =
                        Math.max(
                                1,
                                TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime()));
                String line = lines.poll(remainingMillis, TimeUnit.MILLISECONDS);
                if (line == null) {
                    continue;
                }
                if (pattern.matcher(line).matches()) {
                    return line;
                }
            }
            throw new AssertionError("Timed out waiting for output matching: " + pattern);
        }

        int waitForExit(Duration timeout) throws InterruptedException {
            boolean exited = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!exited) {
                process.destroyForcibly();
                throw new AssertionError("Process did not exit in time");
            }
            return process.exitValue();
        }

        void destroy() {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
