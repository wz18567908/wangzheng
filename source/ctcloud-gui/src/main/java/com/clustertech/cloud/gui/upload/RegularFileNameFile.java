package com.clustertech.cloud.gui.upload;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class RegularFileNameFile implements FilenameFilter {

    private Pattern pattern;

    public RegularFileNameFile(String pattern) {

        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
    }
}
