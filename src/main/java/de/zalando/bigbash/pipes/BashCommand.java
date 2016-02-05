package de.zalando.bigbash.pipes;

/**
 * Created by boris on 10.07.14.
 */
public class BashCommand implements BashInput {

    private final String command;

    public BashCommand(final String command) {
        this.command = command;
    }

    public String render() {
        return command;
    }
}
