package org.elasticsearch.analysis;


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.*;

public class JasoTokenFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

    private final JasoDecomposer jasoDecomposer = new JasoDecomposer();


    public JasoTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            CharSequence parsedData = jasoDecomposer.decompose(termAttr.toString());
            termAttr.setEmpty();
            termAttr.append(parsedData);
            return true;
        }
        return false;
    }
}
