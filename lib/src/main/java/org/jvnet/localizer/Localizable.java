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

import java.util.Locale;
import java.text.MessageFormat;
import java.io.Serializable;
import java.util.MissingResourceException;

import static java.util.Arrays.asList;

/**
 * Captures the localizable string. Can be converted to a string just by
 * supplying a {@link Locale}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Localizable implements Serializable {
    private final ResourceBundleHolder holder;
    private final String key;
    private final Serializable[] args;

    public Localizable(ResourceBundleHolder holder, String key, Object... args) {
        this.holder = holder;
        this.key = key;
        this.args = new Serializable[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Serializable) {
                this.args[i] = (Serializable)args[i];
            } else {
                // MessageFormat only supports formats of "number", "date", "time" and "choice"
                // All of which will be formatting objects that must be Serializable
                // Anything else will just have it's toString() method invoked
                // by MessageFormat, so we'll just call toString up front.
                this.args[i] = args[i].toString();
            }
        }
    }

    public String toString(Locale locale) {
        try {
            return MessageFormat.format(holder.get(locale).getString(key),args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Failed to localize key="+key+",args="+ asList(args),e);
        }
    }

    public String toString() {
        return toString(LocaleProvider.getLocale());
    }
}
