package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traverse;

import com.rabbitmq.jms.parse.Compiler;

public class SqlCompiler implements Compiler {

    private final String compiledCode;
    private final boolean compileOk;
    private final String errorMessage;

    public SqlCompiler(SqlParseTree parseTree) {
        SqlCompilerVisitor compilerVisitor = new SqlCompilerVisitor();
        boolean completed = traverse(parseTree, compilerVisitor);
        if (completed) {
            this.compileOk = true;
            this.compiledCode = compilerVisitor.extractCode();
            this.errorMessage = null;
        } else {
            this.compileOk = false;
            this.compiledCode = null;
            this.errorMessage = "Could not compile parsed tree "+ parseTree.formattedTree();
        }
    }

    @Override
    public String compile() {
        return this.compiledCode;
    }

    @Override
    public boolean compileOk() {
        return this.compileOk;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

}
