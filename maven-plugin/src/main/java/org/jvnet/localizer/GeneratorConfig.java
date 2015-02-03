package org.jvnet.localizer;

import java.io.File;
import java.util.regex.Pattern;

public class GeneratorConfig {

    private File outputDirectory;
    private String outputEncoding;
    private Reporter reporter;
    private Pattern keyPattern;
    private boolean strictTypes;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public Pattern getKeyPattern() {
        return keyPattern;
    }

    public void setKeyPattern(Pattern keyPattern) {
        this.keyPattern = keyPattern;
    }

    public void setKeyPattern(String keyPattern) {
        if (keyPattern != null && !"".equals(keyPattern)) {
            this.keyPattern = Pattern.compile(keyPattern);
        } else {
            this.keyPattern = null;
        }
    }

    public boolean isStrictTypes() {
        return strictTypes;
    }

    public void setStrictTypes(boolean strictTypes) {
        this.strictTypes = strictTypes;
    }

    public static GeneratorConfig of(File outputDirectory, String outputEncoding,
            Reporter reporter, String keyPattern) {
        return of(outputDirectory, outputEncoding, reporter, keyPattern, false);
    }
    public static GeneratorConfig of(File outputDirectory, String outputEncoding,
            Reporter reporter, String keyPattern, boolean strictTypes) {
        GeneratorConfig config = new GeneratorConfig();
        config.setOutputDirectory(outputDirectory);
        config.setOutputEncoding(outputEncoding);
        config.setReporter(reporter);
        config.setKeyPattern(keyPattern);
        config.setStrictTypes(strictTypes);
        return config;
    }
}
