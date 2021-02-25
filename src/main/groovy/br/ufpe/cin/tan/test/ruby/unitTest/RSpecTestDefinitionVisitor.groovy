package br.ufpe.cin.tan.test.ruby.unitTest

import br.ufpe.cin.tan.util.ConstantData
import br.ufpe.cin.tan.util.Util
import org.jrubyparser.ast.CallNode
import org.jrubyparser.ast.ConstNode
import org.jrubyparser.ast.FCallNode
import org.jrubyparser.ast.StrNode
import org.jrubyparser.util.NoopVisitor

class RSpecTestDefinitionVisitor extends NoopVisitor {

    List<String> KEYWORDS = ["describe"]
    List<String> projectFiles
    def applicationClass //keywords: name, path
    Set tests //keywords: name, path, lines
    String path
    String content

    RSpecTestDefinitionVisitor(String path, String content, String repositoryPath) {
        this.tests = []
        this.path = path
        this.content = content
        this.projectFiles = Util.findFilesFromDirectoryByLanguage(repositoryPath)
    }

    private configureApplicationClass(def value) {
        if (!applicationClass) {
            def index1 = this.path.lastIndexOf(File.separator)
            def index2 = this.path.indexOf("_spec")

            if (index1 != -1 && index2 != -1) {
                def name = this.path.substring(index1 + 1, index2) + ConstantData.RUBY_EXTENSION
                def path = Util.findAllApplicationFiles(projectFiles).find { it.endsWith(name) }
                applicationClass = [name: value.name, path: path]
            } else {
                applicationClass = [name: value.name, path: null]
            }
        }
    }

    Set getTests() {
        def result = tests.sort { it.lines.size() }
        if (!result.empty) {
            def outterDescribe = tests.last()
            configureApplicationClass(outterDescribe)
            result.remove(outterDescribe) //the one that englobes all others
        }
        result
    }

    @Override
    Object visitFCallNode(FCallNode iVisited) {
        super.visitFCallNode(iVisited)

        if (iVisited.name in KEYWORDS) {
            def name = ""
            if (iVisited.args.last instanceof StrNode) {
                name = iVisited.args.last.value
            } else if (iVisited.args.last instanceof ConstNode) {
                name = iVisited.args.last.name
            }
            tests += [name: name, path: path, lines: iVisited.position.startLine..iVisited.position.endLine]
            //delimits method block
        }

        return iVisited
    }

    @Override
    Object visitCallNode(CallNode iVisited) {
        super.visitCallNode(iVisited)

        if (iVisited.name == "describe" && iVisited.receiver?.name == "RSpec") {
            println "Chamada do tipo 'RSpec.describe'!!!"
        }
        return iVisited
    }

}
