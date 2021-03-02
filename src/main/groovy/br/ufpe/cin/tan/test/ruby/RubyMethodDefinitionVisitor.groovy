package br.ufpe.cin.tan.test.ruby

import org.jrubyparser.ast.DefnNode
import org.jrubyparser.ast.DefsNode
import org.jrubyparser.util.NoopVisitor

/***
 * Finds all method definition in a file.
 */
class RubyMethodDefinitionVisitor extends NoopVisitor {

    Set methods = [] //keys: name, args, optionalArgs, path
    String path

    @Override
    Object visitDefnNode(DefnNode iVisited) {
        super.visitDefnNode(iVisited)
        methods += [name        : iVisited.name, args: iVisited.args.getMaxArgumentsCount(),
                    optionalArgs: iVisited.args.getOptionalCount(), path: path]
        iVisited
    }

    @Override
    Object visitDefsNode(DefsNode iVisited) {
        super.visitDefsNode(iVisited)
        methods += [name        : iVisited.name, args: iVisited.args.getMaxArgumentsCount(),
                    optionalArgs: iVisited.args.getOptionalCount(), path: path]
        iVisited
    }
}
