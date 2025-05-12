package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Test Runner Commands")
@RequiredArgsConstructor
public class TestRunnerShellCommands {
    private final LocalizedIOService ioService;

    private final TestRunnerService service;

    @ShellMethod(value = "Start the testing process", key = {"s", "start", "r", "run"})
    public String runTests() {
        service.run();
        return ioService.getMessage("Shell.method.run.success");
    }
}
