/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.parse.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class SqlTokenStreamTest {

    @Test
    public void testSeries() {
        assertTokenise("1 2.0 2.1e-1 name is null  'hello world''s'  2e2 2.e2 2D 2.0f",
                       "integer: 1"
                     , "float: 2.0"
                     , "float: 0.21"
                     , "ident: name"
                     , "is_null"
                     , "string: 'hello world''s'"
                     , "float: 200.0"
                     , "float: 200.0"
                     , "float: 2.0"
                     , "float: 2.0");
    }

    @Test
    public void testKeywordIdentifier() {
        assertTokenise("nothing not"
                       , "ident: nothing"
                       , "not");
    }

    @Test
    public void testExpression() {
        assertTokenise("nothing IS NULL"
                       , "ident: nothing"
                       , "is_null");
    }

    @Test
    public void testKeywordSequenceSpacing() {
        assertTokenise("IS  NOT NULL", "not_null");
        assertTokenise("IS\nNOT NULL", "not_null");
        assertTokenise("IS\tNOT NULL", "not_null");
        assertTokenise("IS\fNOT NULL", "not_null");
        assertTokenise("IS\rNOT NULL", "not_null");
        assertTokenise("IS\u000BNOT NULL", "not_null");
    }

    @Test
    public void testIdentifierError() {
        assertTokeniseFailure(" ~ABC='abc'");
    }

    @Test
    public void testSingleQuoteError() {
        assertTokeniseFailure("abc'");
    }

    @Test
    public void testGeneralSpacing() {
        assertTokenise("\n\t\f\r\u000BIS NULLify \n"
                       , "ident: IS"
                       , "ident: NULLify");
    }

    private void assertTokenise(String inStr, String ...strs) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("", stream.getResidue(), "Residue not empty");
        List<String> listOut = new ArrayList<String>();
        while (stream.moreTokens()) {
            SqlToken t = stream.getNext();
            listOut.add(t.toString());
        }
        assertEquals(resultList(strs), listOut, "Parse failure");
    }

    private void assertTokeniseFailure(String inStr, String ...strs) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertNotEquals("", stream.getResidue(), "Residue empty");
    }

    private static List<String> resultList(String ... strs) {
        ArrayList<String> res = new ArrayList<String>();
        for (String str : strs) {
            res.add(str);
        }
        return res;
    }
}
