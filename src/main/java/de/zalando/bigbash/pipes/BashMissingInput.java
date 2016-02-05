package de.zalando.bigbash.pipes;

/**
 * Created by bvonloesch on 7/12/15.
 */
public class BashMissingInput implements BashInput {

    private final String errorMsg;

    public BashMissingInput(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    @Override
    public String render() {
        throw new RuntimeException(errorMsg);
    }
}
