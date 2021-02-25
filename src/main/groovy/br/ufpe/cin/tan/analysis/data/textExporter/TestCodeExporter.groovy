package br.ufpe.cin.tan.analysis.data.textExporter

import br.ufpe.cin.tan.analysis.AnalysedTask
import br.ufpe.cin.tan.util.ConstantData

class TestCodeExporter extends TextExporter {

    String testcodeFileSufix
    String traceFileSufix
    String detailedTestIFileSufix

    TestCodeExporter(String folderName, List<AnalysedTask> tasks) {
        super(folderName, tasks)
        testcodeFileSufix = "_testcode${ConstantData.TEXT_EXTENSION}"
        traceFileSufix = "_trace${ConstantData.TEXT_EXTENSION}"
        detailedTestIFileSufix = "_detailed_testi${ConstantData.TEXT_EXTENSION}"
    }

    @Override
    void writeFile(AnalysedTask analysedTask) {
        if (!analysedTask) return
        def name = "${filePrefix}${analysedTask.doneTask.id}$testcodeFileSufix"
        File file = new File(name)
        file.withWriter("utf-8") { out ->
            analysedTask.testi.code.each { method ->
                method.lines.each { line ->
                    out.write(line + "\n")
                }
            }
        }
        writeTraceFile(analysedTask)
        writeTestIDetailed(analysedTask)
    }

    private writeTraceFile(AnalysedTask analysedTask) {
        def name = "${filePrefix}${analysedTask.doneTask.id}$traceFileSufix"
        File file = new File(name)
        file.withWriter("utf-8") { out ->
            analysedTask.trace.each { out.write(it.toString() + "\n") }
        }
    }

    private writeTestIDetailed(AnalysedTask analysedTask) {
        def name = "${filePrefix}${analysedTask.doneTask.id}$detailedTestIFileSufix"
        File file = new File(name)
        file.withWriter("utf-8") { out ->
            analysedTask.testi.each { out.write(it.toStringDetailed() + "\n") }
        }
    }

}
