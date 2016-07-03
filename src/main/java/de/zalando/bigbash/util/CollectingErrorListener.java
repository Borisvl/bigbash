package de.zalando.bigbash.util;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import de.zalando.bigbash.entities.EditPosition;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;

import java.util.List;

/**
 * Created by bvonloesch on 08/06/16.
 */
public class CollectingErrorListener extends BaseErrorListener {

    public static class SyntaxError {
        EditPosition position;
        String msg;

        public SyntaxError(EditPosition position, String msg) {
            this.position = position;
            this.msg = msg;
        }

        public EditPosition getPosition() {
            return position;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("position", position)
                    .add("msg", msg)
                    .toString();
        }
    }

    List<SyntaxError> errors = Lists.newArrayList();

    @Override
    public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
                            int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
        if (offendingSymbol instanceof Token) {
            Token startToken = (Token) offendingSymbol;
            SyntaxError error = new SyntaxError(new EditPosition(line, charPositionInLine, startToken.getStartIndex(),
                    startToken.getStopIndex() - startToken.getStartIndex() + 1), msg);
            errors.add(error);
        } else {
            errors.add(new SyntaxError(new EditPosition(line, charPositionInLine, -1, 0), msg));
        }
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }
}
