package br.ufpe.cin.tan.analysis.data.csvExporter

import br.ufpe.cin.tan.analysis.data.ExporterUtil
import br.ufpe.cin.tan.evaluation.TaskInterfaceEvaluator
import br.ufpe.cin.tan.similarity.test.TestSimilarityAnalyser
import br.ufpe.cin.tan.similarity.text.TextualSimilarityAnalyser
import br.ufpe.cin.tan.util.ConstantData
import br.ufpe.cin.tan.util.CsvUtil
import groovy.util.logging.Slf4j

import java.util.regex.Matcher

@Slf4j
class SimilarityExporter {

    public static final int TEXT_SIMILARITY_INDEX = 2
    public static final int REAL_JACCARD_INDEX = 4
    public static final int REAL_COSINE_INDEX = 6
    public static final int INITIAL_TEXT_SIZE = 4
    String analysedTasksFile
    String similarityFile

    SimilarityExporter(String analysedTasksFile, String similarityFile) {
        this.analysedTasksFile = analysedTasksFile
        this.similarityFile = similarityFile
    }

    def save() {
        if (!analysedTasksFile || analysedTasksFile.empty || !(new File(analysedTasksFile).exists())) return
        List<String[]> entries = CsvUtil.read(analysedTasksFile)
        if (entries.size() <= ExporterUtil.INITIAL_TEXT_SIZE_SHORT_HEADER) return

        List<String[]> content = []
        content += entries.get(0)
        String[] resultHeader = ["Task_A", "Task_B", "Text", "Test_Jaccard", "Real_Jaccard", "Test_Cosine", "Real_Cosine"]

        def allTasks = entries.subList(ExporterUtil.INITIAL_TEXT_SIZE_SHORT_HEADER, entries.size())
        if (allTasks.size() <= 1) return
        def taskPairs = ExporterUtil.computeTaskPairs(allTasks)
        List<String[]> data = []
        taskPairs?.each { item ->
            def task = item.task
            def taskText = extractTaskText(task[0])
            def testi1 = task[ExporterUtil.TESTI_INDEX_SHORT_HEADER]?.replace(File.separator, Matcher.quoteReplacement(File.separator))
                    ?.substring(1, task[ExporterUtil.TESTI_INDEX_SHORT_HEADER].size() - 1)?.split(", ") as List
            def taski1 = task[ExporterUtil.TASKI_INDEX_SHORT_HEADER]?.replace(File.separator, Matcher.quoteReplacement(File.separator))
                    ?.substring(1, task[ExporterUtil.TASKI_INDEX_SHORT_HEADER].size() - 1)?.split(", ") as List

            item.pairs?.each { other ->
                def otherText = extractTaskText(other[0])
                def textualSimilarityAnalyser = new TextualSimilarityAnalyser()
                def textSimilarity = textualSimilarityAnalyser.calculateSimilarity(taskText, otherText)

                def testi2 = other[ExporterUtil.TESTI_INDEX_SHORT_HEADER]
                        ?.replace(File.separator, Matcher.quoteReplacement(File.separator))
                        ?.substring(1, other[ExporterUtil.TESTI_INDEX_SHORT_HEADER].size() - 1)?.split(", ") as List
                def taski2 = other[ExporterUtil.TASKI_INDEX_SHORT_HEADER]
                        ?.replace(File.separator, Matcher.quoteReplacement(File.separator))
                        ?.substring(1, other[ExporterUtil.TASKI_INDEX_SHORT_HEADER].size() - 1)?.split(", ") as List

                def similarityAnalyser = new TestSimilarityAnalyser(testi1, testi2)
                def testSimJaccard = similarityAnalyser.calculateSimilarityByJaccard()
                def testSimCosine = similarityAnalyser.calculateSimilarityByCosine()

                similarityAnalyser = new TestSimilarityAnalyser(taski1, taski2)
                def realSimJaccard = similarityAnalyser.calculateSimilarityByJaccard()
                def realSimCosine = similarityAnalyser.calculateSimilarityByCosine()

                data += [task[0], other[0], textSimilarity, testSimJaccard, realSimJaccard, testSimCosine, realSimCosine] as String[]
            }
        }

        def textSimilarity = data.collect { it[TEXT_SIMILARITY_INDEX] as double } as double[]
        def dataRealJaccard = data.collect { it[REAL_JACCARD_INDEX] as double } as double[]
        def dataRealCosine = data.collect { it[REAL_COSINE_INDEX] as double } as double[]
        def correlationJaccard = TaskInterfaceEvaluator.calculateCorrelation(textSimilarity, dataRealJaccard)
        def correlationCosine = TaskInterfaceEvaluator.calculateCorrelation(textSimilarity, dataRealCosine)
        content += ["Correlation Jaccard Text-Real", correlationJaccard.toString()] as String[]
        content += ["Correlation Cosine Text-Real", correlationCosine.toString()] as String[]
        content += resultHeader
        content += data
        CsvUtil.write(similarityFile, content)
    }

    private extractTaskText(taskId) {
        def text = ""
        def filename = analysedTasksFile - ConstantData.RELEVANT_TASKS_FILE_SUFIX
        def index = filename.lastIndexOf(File.separator)
        if (index >= 0) filename = "${filename.substring(0, index)}${File.separator}text${File.separator}${taskId}.txt"
        File file = new File(filename)
        if (file.exists()) {
            file.withReader("utf-8") { reader ->
                text = reader.text
            }
        } else log.warn "Text file '${filename}' not found!"
        text
    }

}
