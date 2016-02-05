package de.zalando.bigbash.pipes;

import de.zalando.bigbash.entities.ProgramConfig;
import org.aeonbits.owner.ConfigCache;
import org.stringtemplate.v4.ST;

/**
 * Created by bvonloesch on 7/29/15.
 */
public class DelimiterAndQuoteConverter implements BashInput {

    private final String delimiter;
    private final String replacement;
    private final char quoteChar;

    public DelimiterAndQuoteConverter(String delimiter, String replacement, char quote) {
        this.delimiter = delimiter;
        this.replacement = replacement;
        this.quoteChar = quote;
    }


    @Override
    public String render() {
        ST st = new ST("<awk> -F'<del>' '{q=0; o=\"\"; for (i=1; i\\<=NF; i+=1) {if (q) o=o\"<del>\"$i; " +
                "else o=o\"<ndel>\"$i; if ($i == \"<quote>\") q=1-q; " +
                "else{ if (substr($i,length($i),1)==\"<quote>\") q=0; else if (substr($i,1,1)==\"<quote>\") q=1; }}" +
                "gsub(/<quote>/,\"\",o); print substr(o,2)}'");
        st.add("awk", ConfigCache.getOrCreate(ProgramConfig.class).awk());
        st.add("del", delimiter);
        st.add("ndel", replacement);
        String quote = new String(quoteChar+"");
        if (quoteChar == '"') {
            quote = "\\\"";
        } else if (quoteChar == '\'') {
            quote = "'\\''";
        }
        st.add("quote", quote);
        return st.render();
    }
}
