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

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setTodir(File todir) {
        this.todir = todir;
    }

    public void execute() throws BuildException {
        Generator g = new Generator(todir,new Reporter() {
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
