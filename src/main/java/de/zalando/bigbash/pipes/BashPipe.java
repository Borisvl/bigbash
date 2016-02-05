package de.zalando.bigbash.pipes;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Created by boris on 10.07.14.
 */
public class BashPipe implements BashInput {
    private static final String BASH_PIPE_SYMBOL = "|";
    private final List<BashInput> input;
    private BashInput output;

    public BashPipe() {
        input = Lists.newArrayList();
    }

    public BashPipe(final BashInput input, final BashInput output) {
        this.input = Lists.newArrayList();
        addInput(input);
        setOutput(output);
    }

    public void setOutput(final BashInput pipe) {
        output = pipe;
    }

    public void addInput(final BashInput inputPipe) {
        input.add(inputPipe);
    }

    public String render() {
        StringBuilder outputString = new StringBuilder();
        if (input.size() == 1) {
            outputString.append(input.get(0).render()).append(BASH_PIPE_SYMBOL).append(output.render());
        } else {
            outputString.append(output.render()).append(" ").append(Joiner.on(" ").join(
                    Iterables.transform(input, new Function<BashInput, String>() {
                            @Override
                            public String apply(final BashInput bashInput) {
                                return "<(" + bashInput.render() + ")";
                            }
                        })));
        }

        return outputString.toString();
    }
}
