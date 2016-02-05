package de.zalando.bigbash.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.ProcessBuilder.Redirect;

import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class BashStarter {

    private File scriptFile;
    private Process process;
    private final String path;
    private final String scriptContent;

    public BashStarter(final String path, final String scriptContent) {
        this.path = path;
        this.scriptContent = scriptContent;
    }

    private File write2LocalFileSystem(final String path, final String scriptContent) throws IOException {
        File script = File.createTempFile("script", "");
        script.deleteOnExit();
        FileUtils.writeStringToFile(script, scriptContent);
        return script;
    }

    public String startScript(final String... args) throws IOException {
        scriptFile = write2LocalFileSystem(path, scriptContent);

        List<String> arguments = Lists.newArrayList(args);
        arguments.add(0, "bash");
        arguments.add(1, scriptFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectError(Redirect.INHERIT);
        process = processBuilder.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }
}
