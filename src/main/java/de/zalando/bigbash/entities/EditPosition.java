package de.zalando.bigbash.entities;

import com.google.common.base.Objects;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/**
 * Created by bvonloesch on 08/06/16.
 */
public class EditPosition {
    final private int line;
    final private int charPositionInLine;
    final private int absoluteCharPosition;
    final private int length;

    public static EditPosition fromTokens(Token start, Token stop) {
        return new EditPosition(start.getLine(), start.getCharPositionInLine(),
                start.getStartIndex(), stop.getStopIndex() - start.getStartIndex() + 1);
    }

    public static EditPosition fromContext(ParserRuleContext ctx) {
        return fromTokens(ctx.getStart(), ctx.getStop());
    }

    public EditPosition(int line, int charPositionInLine, int absoluteCharPosition, int length) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.absoluteCharPosition = absoluteCharPosition;
        this.length = length;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public int getAbsoluteCharPosition() {
        return absoluteCharPosition;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("line", line)
                .add("charPositionInLine", charPositionInLine)
                .add("absoluteCharPosition", absoluteCharPosition)
                .add("length", length)
                .toString();
    }
}
