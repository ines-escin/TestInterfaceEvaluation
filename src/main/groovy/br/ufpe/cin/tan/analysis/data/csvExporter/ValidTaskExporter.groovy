package br.ufpe.cin.tan.analysis.data.csvExporter

import br.ufpe.cin.tan.analysis.AnalysedTask
import br.ufpe.cin.tan.analysis.data.ExporterUtil
import br.ufpe.cin.tan.analysis.data.textExporter.TextIExporter
import br.ufpe.cin.tan.analysis.data.textExporter.TestCodeExporter
import br.ufpe.cin.tan.util.CsvUtil

/**
 * Filter and export analysed tasks that contain acceptance test.
 */
class ValidTaskExporter {

    String filename
    String folder
    String url
    List<AnalysedTask> tasks

    ValidTaskExporter(String filename, List<AnalysedTask> tasks) {
        this.filename = filename
        def index = filename.lastIndexOf(File.separator)
        folder = filename.substring(0, index)
        initTasks(tasks)
    }

    def save() {
        if (!tasks || tasks.empty) return
        saveTextI(tasks)
        saveTestCode(tasks)
        saveAnalysisData(tasks)
    }

    private saveTextI(List<AnalysedTask> tasks) {
        TextIExporter textiExporter = new TextIExporter(folder, tasks)
        textiExporter.save()
    }

    private saveTestCode(List<AnalysedTask> tasks) {
        TestCodeExporter testCodeExporter = new TestCodeExporter(folder, tasks)
        testCodeExporter.save()
    }

    private saveAnalysisData(List<AnalysedTask> tasksToSave) {
        List<String[]> content = []
        content += ["Repository", url] as String[]
        content += generateNumeralData()
        content += ExporterUtil.SHORT_HEADER_PLUS
        tasksToSave?.each { content += it.parseToArray() }
        CsvUtil.write(filename, content)
    }

    private generateNumeralData() {
        if (!tasks || tasks.empty) return []
        double[] tests = tasks.collect { it.testi.foundAcceptanceTests.size() }
        double[] precisionValues = tasks.collect { it.precision() }
        double[] recallValues = tasks.collect { it.recall() }
        double[] f2Values = tasks.collect { it.f2Measure() }
        def content = ExporterUtil.generateStatistics(precisionValues, recallValues, tests, f2Values)
        content
    }

    private initTasks(List<AnalysedTask> tasks) {
        if (!tasks || tasks.empty) {
            this.tasks = []
            url = ""
        } else {
            this.tasks = tasks.findAll { it.isValid() }?.sort { -it.testi.foundAcceptanceTests.size() }
            url = tasks.first().doneTask.gitRepository.url
        }
    }

}
