package org.jvnet.localizer;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @goal generate
 * @phase generate-sources
 */
public class GeneratorMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory to place generated property files.
     *
     * @parameter default-value="${project.build.directory}/generated-sources"
     * @required
     * @readonly
     */
    protected File outputDirectory;

    /**
     * Additional file name mask like "Messages.properties" to further
     * restrict the resource processing.
     *
     * @parameter
     */
    protected String fileMask;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Generator g = new Generator(outputDirectory, new Reporter() {
            public void debug(String msg) {
                getLog().debug(msg);
            }
        });

        for(Resource res : (List<Resource>)project.getResources()) {
            File baseDir = new File(res.getDirectory());

            FileSet fs = new FileSet();
            fs.setDir(baseDir);
            for( String name : (List<String>)res.getIncludes() )
                fs.createInclude().setName(name);
            for( String name : (List<String>)res.getExcludes() )
                fs.createExclude().setName(name);

            for( String relPath : fs.getDirectoryScanner(new Project()).getIncludedFiles() ) {
                File f = new File(baseDir,relPath);
                if(!f.getName().endsWith(".properties") || f.getName().contains("_"))
                    continue;
                if(fileMask!=null && !f.getName().equals(fileMask))
                    continue;

                try {
                    g.generate(f,relPath);
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed to generate a class from "+f,e);
                }
            }
        }

        try {
            g.build();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate source files",e);
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }
}
