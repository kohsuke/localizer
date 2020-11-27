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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @goal generate
 * @phase generate-sources
 * @threadSafe true
 */
public class GeneratorMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory to place generated Java sources.
     *
     * @parameter default-value="${project.build.directory}/generated-sources"
     * @required
     */
    protected File outputDirectory;

    /**
     * Additional file name mask like "Messages.properties" to further
     * restrict the resource processing.
     *
     * @parameter
     */
    protected String fileMask;

    /**
     * The charset encoding of generated Java sources.
     * Default to the platform specific encoding.
     *
     * @parameter default-value="${project.build.sourceEncoding}"
     * @required
     */
    protected String outputEncoding;

    /**
     * Regular expression pattern for properties keys.
     *
     * @parameter default-value=".*"
     */
    protected String keyPattern;

    /**
     * Class file generator. Implementation of org.jvnet.localizer.ClassGenerator.
     *
     * @parameter default-value="org.jvnet.localizer.Generator"
     */
    protected String generatorClass;

    /**
     * Generates strict types for messages that format dates or numbers
     *
     * @parameter
     */
    protected boolean strictTypes;

    /**
     * Whether to annotate Localizer-generated classes with @Restricted(NoExternalUse.class) from access-modifier
     *
     * @parameter
     */
    protected boolean accessModifierAnnotations;

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        String pkg = project.getPackaging();
        if(pkg!=null && pkg.equals("pom"))
            return; // skip POM modules

        GeneratorConfig config = GeneratorConfig.of(outputDirectory, outputEncoding,
                new Reporter() {
                    public void debug(String msg) {
                        getLog().debug(msg);
                    }
                }, keyPattern, strictTypes, accessModifierAnnotations);
        ClassGenerator g = createGenerator(config);

        for(Resource res : (List<Resource>)project.getResources()) {
            File baseDir = new File(res.getDirectory());
            if(!baseDir.exists())
                continue;   // this happens for example when POM inherits the default resource folder but no such folder exists.

            FileSet fs = new FileSet();
            fs.setDir(baseDir);
            for( String name : (List<String>)res.getIncludes() )
                fs.createInclude().setName(name);
            for( String name : (List<String>)res.getExcludes() )
                fs.createExclude().setName(name);

            try {
                DirectoryScanner ds = fs.getDirectoryScanner(new Project());
                g.generate(baseDir, ds, new FileFilter() {

                    public boolean accept(File f) {
                        if (!f.getName().endsWith(".properties") || f.getName().contains("_"))
                            return false;

                        if (fileMask != null && !f.getName().equals(fileMask))
                            return false;

                        return true;
                    }
                });
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to generate class", e);
            }
        }

        try {
            g.build();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate source file(s)",e);
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }

    private ClassGenerator createGenerator(GeneratorConfig config) throws MojoExecutionException {
        Class<?> clazz;
        try {
            clazz = Class.forName(generatorClass);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Cannot load ClassGenerator class \"" + generatorClass
                    + "\".");
        }

        Constructor<? extends ClassGenerator> cstr;
        try {
            cstr = clazz.asSubclass(ClassGenerator.class).getDeclaredConstructor(
                    GeneratorConfig.class);
        } catch (ClassCastException e) {
            throw new MojoExecutionException("generatorClass \"" + generatorClass
                    + "\" is not an implementation of ClassGenerator.");
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException(
                    "GeneratorClass must have visible constructor accepting GeneratorConfig as parameter.",
                    e);
        } catch (SecurityException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        try {
            return cstr.newInstance(config);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create instance of ClassGenerator \""
                    + generatorClass + "\".", e);
        }
    }
}
