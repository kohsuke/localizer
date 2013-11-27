/*
 * The MIT License
 *
 * Copyright (c) 2013-, the localizer project contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.localizer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;

public abstract class CodeModelGenerator extends GeneratorBase {

    protected final JCodeModel cm = new JCodeModel();

    public CodeModelGenerator(GeneratorConfig config) {
        super(config);
    }

    public JCodeModel getCodeModel() {
        return cm;
    }

    public void build() throws IOException {
        outputDirectory.mkdirs();
        cm.build(new CodeWriter() {
            private final CodeWriter delegate = new FileCodeWriter(outputDirectory, outputEncoding);
    
            public Writer openSource(JPackage pkg, String fileName) throws IOException {
                super.encoding = outputEncoding;
                Writer w = super.openSource(pkg, fileName);
                new PrintWriter(w).println("// CHECKSTYLE:OFF");
                return w;
            }
    
            public void close() throws IOException {
                delegate.close();
            }
    
            public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
                return delegate.openBinary(pkg, fileName);
            }
        });
    }

}
