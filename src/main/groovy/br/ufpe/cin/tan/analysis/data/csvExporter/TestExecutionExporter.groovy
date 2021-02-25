package br.ufpe.cin.tan.analysis.data.csvExporter

import br.ufpe.cin.tan.analysis.AnalysedTask
import br.ufpe.cin.tan.test.AcceptanceTest
import br.ufpe.cin.tan.util.CsvUtil

class TestExecutionExporter {

    String testFile
    List<AnalysedTask> tasks
    public static final int INITIAL_TEXT_SIZE = 2

    TestExecutionExporter(String testFile, List<AnalysedTask> tasks) {
        this.testFile = testFile
        this.tasks = tasks
    }

    def save() {
        List<String[]> content = []

        if (!tasks || tasks.empty) return

        def url = tasks.first().doneTask.gitRepository.url
        content += ["Repository", url] as String[]
        String[] header = ["TASK", "HASH", "RUBY", "RAILS", "TESTS"]
        content += header

        tasks.each { task ->
            def scenarios = extractTests(task)
            String[] line = [task.doneTask.id, task.doneTask.lastCommit.name, task.ruby, task.rails, scenarios]
            content += line
        }
        CsvUtil.write(testFile, content)
    }

    private static extractTests(task) {
        def scenarios = ""
        Set<AcceptanceTest> tests = task.testi.foundAcceptanceTests
        tests.each { test ->
            def lines = test.scenarioDefinition*.location.line
            scenarios += test.gherkinFilePath + "(" + lines.join(",") + ")" + ";"
        }
        if (scenarios.size() > 1) scenarios = scenarios.substring(0, scenarios.size() - 1)
        scenarios
    }

}
