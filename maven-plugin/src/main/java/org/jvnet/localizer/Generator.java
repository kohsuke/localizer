/*
 * The MIT License
 *
 * Copyright (c) 2007-, the localizer project contributors
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

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author Kohsuke Kawaguchi
 */
public class Generator {
    private final JCodeModel cm = new JCodeModel();
    private final File outputDirectory;
    private final String outputEncoding;
    private final Reporter reporter;

    public Generator(File outputDirectory, Reporter reporter) {
        this(outputDirectory, null, reporter);
    }

    public Generator(File outputDirectory, String outputEncoding, Reporter reporter) {
        this.outputDirectory = outputDirectory;
        this.outputEncoding = outputEncoding;
        this.reporter = reporter;
    }

    public void generate(File baseDir, DirectoryScanner ds) throws IOException {
        for( String relPath : ds.getIncludedFiles() ) {
            File f = new File(baseDir,relPath);
            if(!f.getName().endsWith(".properties") || f.getName().contains("_"))
                continue;

            try {
                generate(f,relPath);
            } catch (IOException e) {
                IOException x = new IOException("Failed to generate a class from " + f);
                x.initCause(e);
                throw x;
            }
        }
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

        try {
            JDefinedClass c = cm._class(className);
            c.annotate(SuppressWarnings.class).paramArray("value").param("").param("PMD");

            // [RESULT]
            // private static final ResourceBundleHolder holder = BundleHolder.get(Messages.class);

            JVar holder = c.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, ResourceBundleHolder.class, "holder",
                    cm.ref(ResourceBundleHolder.class).staticInvoke("get").arg(c.dotclass()) );


            for (Entry<Object,Object> e : props.entrySet()) {
                String key = e.getKey().toString();
                String value = e.getValue().toString();

                int n = countArgs(value);

                // generate the default format method
                List<JVar> args = new ArrayList<JVar>();
                JMethod m = c.method(JMod.PUBLIC | JMod.STATIC, cm.ref(String.class), toJavaIdentifier(key));
                for( int i=1; i<=n; i++ )
                    args.add(m.param(Object.class,"arg"+i));

                JInvocation inv = holder.invoke("format").arg(key);
                for (JVar arg : args)
                    inv.arg(arg);
                m.body()._return(inv);

                m.javadoc().add(escape(value));

                // generate localizable factory
                args.clear();
                m = c.method(JMod.PUBLIC | JMod.STATIC, cm.ref(Localizable.class), '_'+toJavaIdentifier(key));
                for( int i=1; i<=n; i++ )
                    args.add(m.param(Object.class,"arg"+i));

                inv = JExpr._new(cm.ref(Localizable.class)).arg(holder).arg(key);
                for (JVar arg : args)
                    inv.arg(arg);
                m.body()._return(inv);

                m.javadoc().add(escape(value));
            }

        } catch (JClassAlreadyExistsException e) {
            throw new AssertionError(e);
        }

    }

    private String escape(String value) {
        return value.replace("&","&amp;").replace("<","&lt;");
    }

    /**
     * Counts the number of arguments.
     */
    protected int countArgs(String formatString) {
        return new MessageFormat(formatString).getFormatsByArgumentIndex().length;
    }

    protected String toJavaIdentifier(String key) {
        // TODO: this is fairly dumb implementation
        return key.replace('.','_').replace('-','_').replace('/','_');
    }

    protected String toClassName(String relPath) {
        relPath = relPath.substring(0,relPath.length()-".properties".length());
        return relPath.replace(File.separatorChar,'.');
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
