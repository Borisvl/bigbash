package de.zalando.bigbash.integration;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import de.zalando.bigbash.commandline.BashCompiler;
import de.zalando.bigbash.entities.CompressionType;
import de.zalando.bigbash.entities.FileMappingProperties;
import de.zalando.bigbash.util.BashStarter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by bvonloesch on 8/8/15.
 */
public class DelimiterIT {

    @Test
    public void checkQuoteDelimiterConverter() throws Exception {
        String content = "'This is one entry',432,'Another, another!',No quotes,',,','aa',',',,'aa'\n";
        String sql = "Create table test (s1 TEXT, i1 INT, s2 TEXT, s3 TEXT, s4 TEXT); Select * from test;";
        Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();
        File tableFile = File.createTempFile("test", "");
        tableFile.deleteOnExit();
        FileUtils.writeStringToFile(tableFile, content);
        fileMappingPropertiesMap.put("test",
                new FileMappingProperties(tableFile.getAbsolutePath(), CompressionType.NONE, ",",
                        false, Optional.of('\'')));
        FileMappingProperties.outputDelimiter = ";";
        BashCompiler compiler = new BashCompiler();
        String bashScript = compiler.compile(sql, fileMappingPropertiesMap, true);
        BashStarter starter = new BashStarter(".", bashScript);
        String scriptOutput = starter.startScript(bashScript);
        System.out.println(sql);
        System.out.println(bashScript);
        System.out.flush();
        assertEquals("This is one entry;432;Another, another!;No quotes;,,;aa;,;;aa\n", scriptOutput);
    }

    @Test
    public void checkSingleDelimiterConverter() throws Exception {
        String content = "Entry 1,Entry 2,3434,,Entry 3\n";
        String sql = "Create table test (s1 TEXT, s2 TEXT, i1 INT, s3 TEXT); Select * from test;";
        Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();
        File tableFile = File.createTempFile("test", "");
        tableFile.deleteOnExit();
        FileUtils.writeStringToFile(tableFile, content);
        fileMappingPropertiesMap.put("test",
                new FileMappingProperties(tableFile.getAbsolutePath(), CompressionType.NONE, ",",
                        false, Optional.<Character>absent()));
        FileMappingProperties.outputDelimiter = ";";
        BashCompiler compiler = new BashCompiler();
        String bashScript = compiler.compile(sql, fileMappingPropertiesMap, true);
        BashStarter starter = new BashStarter(".", bashScript);
        String scriptOutput = starter.startScript(bashScript);
        System.out.println(sql);
        System.out.println(bashScript);
        System.out.flush();
        assertEquals("Entry 1;Entry 2;3434;;Entry 3\n", scriptOutput);
    }

    @Test
    public void checkLongDelimiterConverter() throws Exception {
        String content = "Entry:1::Entry 2::3434::::Entry 3:::\n";
        String sql = "Create table test (s1 TEXT, s2 TEXT, i1 INT, s3 TEXT, s4 TEXT); Select * from test;";
        Map<String, FileMappingProperties> fileMappingPropertiesMap = Maps.newHashMap();
        File tableFile = File.createTempFile("test", "");
        tableFile.deleteOnExit();
        FileUtils.writeStringToFile(tableFile, content);
        fileMappingPropertiesMap.put("test",
                new FileMappingProperties(tableFile.getAbsolutePath(), CompressionType.NONE, "::",
                        false, Optional.<Character>absent()));
        FileMappingProperties.outputDelimiter = ";";
        BashCompiler compiler = new BashCompiler();
        String bashScript = compiler.compile(sql, fileMappingPropertiesMap, true);
        BashStarter starter = new BashStarter(".", bashScript);
        String scriptOutput = starter.startScript(bashScript);
        System.out.println(sql);
        System.out.println(bashScript);
        System.out.flush();
        assertEquals("Entry:1;Entry 2;3434;;Entry 3;:\n", scriptOutput);
    }

}
