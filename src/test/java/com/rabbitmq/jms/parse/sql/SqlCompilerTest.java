/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.parse.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

public class SqlCompilerTest {

    @Test
    public void simpleCompiles() {
        assertCompile( "nothing is null"
                     , "{'is_null',{'ident',<<\"nothing\">>}}." );

        assertCompile( "nothing IS NULL And JMSPrefix > 12-4"
                     , "{'and'"+
                       ",{'is_null',{'ident',<<\"nothing\">>}}"+
                       ",{'>'"+
                         ",{'ident',<<\"JMSPrefix\">>}"+
                         ",{'-',12,4}}}." );
    }

    private void assertCompile(String inStr, String outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("", stream.getResidue(), "Residue not empty");

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();

        if (!sp.parseOk()) {
            fail(sp.getErrorMessage());
        }

        SqlCompiler compiled = new SqlCompiler(new SqlEvaluator(sp, Collections.<String, SqlExpressionType> emptyMap()));
        if (!compiled.compileOk()) {
            fail("Did not compile, tree=" + Arrays.toString(pt.formattedTree()));
        }
        assertNull(compiled.getErrorMessage(), "Error message generated!");
        assertEquals(outStr, compiled.compile(), "Compiled code doesn't match");
    }

}
