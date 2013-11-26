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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
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

    public void generate(File propertyFile, String relPath) throws IOException {
        String className = toClassName(relPath);

        // up to date check
        File sourceFile = new File(outputDirectory,className.replace('.','/')+".java");
        if(sourceFile.exists() && sourceFile.lastModified()>propertyFile.lastModified()) {
            reporter.debug(sourceFile+" is up to date");
            return;
        }

        // go generate one
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(propertyFile);
        try {
            props.load(in);
        } catch (IOException e) {
            in.close();
        }

        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String key = e.getKey().toString();
            assertKeyPatternMatched(key);
        }

        generateImpl(className, props);
    }

    abstract protected void generateImpl(String className, Properties props);

    protected String toClassName(String relPath) {
        relPath = relPath.substring(0,relPath.length()-".properties".length());
        return relPath.replace(File.separatorChar,'.');
    }

    protected String toJavaIdentifier(String key) {
        // TODO: this is fairly dumb implementation
        return key.replace('.','_').replace('-','_').replace('/','_');
    }

    protected void assertKeyPatternMatched(String key) {
        if (keyPattern != null && !keyPattern.matcher(key).matches()) {
            String message = String.format(
                    "Key \"%1$s\" does not match specified keyPattern \"%2$s\".", key,
                    keyPattern);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Counts the number of arguments.
     */
    protected int countArgs(String formatString) {
        return new MessageFormat(formatString).getFormatsByArgumentIndex().length;
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
