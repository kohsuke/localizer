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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;

public abstract class GeneratorBase implements ClassGenerator {

    protected final File outputDirectory;
    protected final String outputEncoding;
    protected final Reporter reporter;
    protected final Pattern keyPattern;

    public GeneratorBase(GeneratorConfig config) {
        outputDirectory = config.getOutputDirectory();
        outputEncoding = config.getOutputEncoding();
        reporter = config.getReporter();
        keyPattern = config.getKeyPattern();
    }

    public void generate(File baseDir, DirectoryScanner ds, FileFilter filter) throws IOException {
        if (filter == null) {
            generate(baseDir, ds);
            return;
        }

        for (String relPath : ds.getIncludedFiles()) {
            File f = new File(baseDir, relPath);
            if (!filter.accept(f)) {
                continue;
            }

            try {
                generate(f, relPath);
            } catch (IOException e) {
                IOException x = new IOException("Failed to generate a class from " + f);
                x.initCause(e);
                throw x;
            }
        }
    }

    public void generate(File baseDir, DirectoryScanner ds) throws IOException {
        generate(baseDir, ds, new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".properties") && !f.getName().contains("_");
            }
        });
    }

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    protected String getOutputEncoding() {
        return outputEncoding;
    }

    protected Reporter getReporter() {
        return reporter;
    }

    protected Pattern getKeyPattern() {
        return keyPattern;
    }
}
