package org.elasticsearch.analysis;

import junit.framework.TestCase;


public class JasoDecomposerTest extends TestCase {

    public void testJasoDecomposer() {

        JasoDecomposer jasoDecomposer = new JasoDecomposer();

        String expected = "ㅎㅏㄴㄱㅡㄹABC123!";
        String actual = jasoDecomposer.decompose("한글ABC123!");
        assertEquals(expected, actual);
    }
}