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
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class Generator extends CodeModelGenerator implements ClassGenerator {
    public Generator(GeneratorConfig config) {
        super(config);
    }

    @Deprecated
    public Generator(File outputDirectory, Reporter reporter) {
        this(outputDirectory, null, reporter);
    }

    @Deprecated
    public Generator(File outputDirectory, String outputEncoding, Reporter reporter) {
        this(outputDirectory, null, reporter, null);
    }

    @Deprecated
    public Generator(File outputDirectory, String outputEncoding, Reporter reporter,
            String keyPattern) {
        this(GeneratorConfig.of(outputDirectory, outputEncoding, reporter, keyPattern));
    }

    protected void generateImpl(String className, Properties props) throws AssertionError {
        try {
            JDefinedClass c = cm._class(className);
            c.annotate(SuppressWarnings.class).paramArray("value").param("").param("PMD").param("all");
            c.javadoc().add("Generated localization support class.");

            // [RESULT]
            // private static final ResourceBundleHolder holder = BundleHolder.get(Messages.class);

            JFieldVar holder = c.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, ResourceBundleHolder.class, "holder",
                    cm.ref(ResourceBundleHolder.class).staticInvoke("get").arg(c.dotclass()) );
            holder.javadoc().add("The resource bundle reference");


            for (Entry<Object,Object> e : props.entrySet()) {
                String key = e.getKey().toString();
                String value = e.getValue().toString();

                final Format[] formats = new MessageFormat(value).getFormatsByArgumentIndex();
                Map<String,String> params = new HashMap<String, String>();

                // generate the default format method
                List<JVar> args = new ArrayList<JVar>();
                JMethod m = c.method(JMod.PUBLIC | JMod.STATIC, cm.ref(String.class), toJavaIdentifier(key));
                for( int i=0; i<formats.length; i++ ) {
                    String argName = String.format("arg%d", i);
                    args.add(m.param(inferType(formats[i]), argName));
                    if (formats[i] instanceof NumberFormat) {
                        params.put(argName, String.format("%s format parameter, {@code {%d}}, a number.",
                                positionalName(i), i));
                    } else if (formats[i] instanceof DateFormat) {
                        params.put(argName,
                                String.format("%s format parameter, {@code {%d}}, a {@link Date}.",
                                        positionalName(i), i));
                    } else {
                        params.put(argName, String.format(
                                "%s format parameter, {@code {%d}}, as {@link Object#toString()}.",
                                positionalName(i), i));
                    }
                }

                JInvocation inv = holder.invoke("format").arg(key);
                for (JVar arg : args)
                    inv.arg(arg);
                m.body()._return(inv);

                m.javadoc().add(WordUtils.wrap(String.format(
                        "Returns the formatted message string for key %s from the bundle using the {@link org.jvnet"
                                + ".localizer.LocaleProvider#getLocale()}, in the default {@link java.util.Locale} the"
                                + " message string is %s.",
                        code(key), code(value)), 70));
                for (Map.Entry<String,String> p: params.entrySet()) {
                    m.javadoc().addParam(p.getKey()).add(p.getValue());
                }
                m.javadoc().addReturn().add(WordUtils.wrap(code(value), 70));

                // generate localizable factory
                args.clear();
                params.clear();
                m = c.method(JMod.PUBLIC | JMod.STATIC, cm.ref(Localizable.class), '_'+toJavaIdentifier(key));
                for( int i=0; i<formats.length; i++ ) {
                    String argName = String.format("arg%d", i);
                    args.add(m.param(inferType(formats[i]), argName));
                    if (formats[i] instanceof NumberFormat) {
                        params.put(argName, String.format("%s format parameter, {@code {%d}}, a number.",
                                positionalName(i), i));
                    } else if (formats[i] instanceof DateFormat) {
                        params.put(argName,
                                String.format("%s format parameter, {@code {%d}}, a {@link Date}.",
                                        positionalName(i), i));
                    } else {
                        params.put(argName, String.format(
                                "%s format parameter, {@code {%d}}, as {@link Object#toString()}.",
                                positionalName(i), i));
                    }
                }

                inv = JExpr._new(cm.ref(Localizable.class)).arg(holder).arg(key);
                for (JVar arg : args)
                    inv.arg(arg);
                m.body()._return(inv);

                m.javadoc().add(WordUtils.wrap(String.format(
                        "Returns a {@link Localizable} of the formatted message string for key %s from the bundle,"
                                + " in the default {@link java.util.Locale} the message string is %s.",
                        code(key), code(value)), 70));
                for (Map.Entry<String,String> p: params.entrySet()) {
                    m.javadoc().addParam(p.getKey()).add(p.getValue());
                }
                m.javadoc().addReturn().add(WordUtils.wrap(code(value), 70));
            }

        } catch (JClassAlreadyExistsException e) {
            throw new AssertionError(e);
        }
    }

    private String code(String value) {
        return String.format("{@code %s}", escape(value));
    }

    private String escape(String value) {
        return value.replace("&","&amp;").replace("<","&lt;").replace(">", "&gt;");
    }

    private String positionalName(int i) {
        if (i >= 9 && i < 20) {
            return String.format("%dth", i+1);
        }
        switch (i) {
            case 0:
                return String.format("%dst", i+1);
            case 1:
                return String.format("%dnd", i+1);
            case 2:
                return String.format("%drd", i+1);
            default:
                return String.format("%dth", i+1);
        }
    }

}
