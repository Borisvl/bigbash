package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BashPipeTest {

    @Test
    public void renderSimpleTest() {
        BashPipe p = new BashPipe();
        p.addInput(new BashFileInput("files"));
        p.setOutput(new BashCommand("A"));

        BashPipe p2 = new BashPipe(p, new BashCommand("B"));
        assertEquals(ConfigFactory.create(ProgramConfig.class).cat() + " files|A|B", p2.render());
    }

    @Test
    public void renderDoubleJoinTest() {
        BashPipe p = new BashPipe();
        p.addInput(new BashFileInput("file1"));
        p.addInput(new BashFileInput("file2"));
        p.setOutput(new BashCommand("join"));

        BashPipe p2 = new BashPipe(p, new BashCommand("join -"));
        p2.addInput(new BashFileInput("file3"));

        String cat = ConfigFactory.create(ProgramConfig.class).cat();
        assertEquals("join - <(join <(" + cat + " file1) <(" + cat + " file2)) <(" + cat + " file3)", p2.render());
    }

}
