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

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.IOException;

/**
 * Ant task for resource generation.
 *
 * @author Kohsuke Kawaguchi
 */
public class GeneratorTask extends MatchingTask {
    /**
     * Source and destination.
     */
    private File dir,todir;

    /**
     * Encoding of generated sources.
     */
    private String encoding;

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setTodir(File todir) {
        this.todir = todir;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void execute() throws BuildException {
        Generator g = new Generator(todir, encoding, new Reporter() {
            public void debug(String msg) {
                log(msg, Project.MSG_DEBUG);
            }
        });

        try {
            g.generate(dir,getDirectoryScanner(dir));
            g.build();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
