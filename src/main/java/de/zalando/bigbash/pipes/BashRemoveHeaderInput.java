package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;
import org.stringtemplate.v4.ST;

/**
 * Created by bvonloesch on 7/28/15.
 */
public class BashRemoveHeaderInput implements BashInput {

    private final String files;
    private final BashInput input;
    private final int nrOflines2Skip;


    /**
     * @param fileNamePattern The globing pattern to access the files
     * @param input The bash input but with {} instead of the fileNamePattern
     */
    public BashRemoveHeaderInput(String fileNamePattern, BashInput input) {
        this(fileNamePattern, input, 1);
    }
    
    /**
     * @param fileNamePattern The globing pattern to access the files
     * @param input The bash input but with {} instead of the fileNamePattern
     */
    public BashRemoveHeaderInput(String fileNamePattern, BashInput input, int nrOflines2Skip) {
        this.files = fileNamePattern;
        this.input = input;
        this.nrOflines2Skip = nrOflines2Skip;
    }
    
    @Override
    public String render() {
        ST st = new ST("<find> <files> -print0 | <xargs> -0 -i <shell> \"<input> | <tail> -n +<skiplines>\"");
        st.add("find", ConfigCache.getOrCreate(ProgramConfig.class).find());
        st.add("files", files);
        st.add("xargs", ConfigCache.getOrCreate(ProgramConfig.class).xargs());
        st.add("shell", ConfigCache.getOrCreate(ProgramConfig.class).getExecuteShellCommand());
        st.add("input", input.render());
        st.add("tail", ConfigCache.getOrCreate(ProgramConfig.class).tail());
        st.add("skiplines", nrOflines2Skip+1);
        return st.render();
    }
}
