package br.ufpe.cin.tan.commit

import br.ufpe.cin.tan.commit.change.ChangedProdFile
import br.ufpe.cin.tan.commit.change.RenamingChange
import br.ufpe.cin.tan.commit.change.gherkin.ChangedGherkinFile
import br.ufpe.cin.tan.commit.change.stepdef.ChangedStepdefFile
import br.ufpe.cin.tan.commit.change.unit.ChangedUnitTestFile

/***
 * Represents a git commit.
 */
class Commit {

    String hash
    String message
    String author
    long date
    boolean merge

    List<ChangedProdFile> coreChanges //code changes in application code only
    List<ChangedGherkinFile> gherkinChanges //code changes in feature files only
    List<ChangedStepdefFile> stepChanges //code changes in step definition files only
    List<ChangedUnitTestFile> unitChanges //code changes in unit test files only
    List<RenamingChange> renameChanges

    List<String> files //all changed files

    @Override
    String toString() {
        def paths = (coreChanges*.path + unitChanges*.path + gherkinChanges*.path + stepChanges*.path)?.flatten()
        "$hash*${new Date(date * 1000)}*$author*$message*${paths.toListString()}"
    }

}
