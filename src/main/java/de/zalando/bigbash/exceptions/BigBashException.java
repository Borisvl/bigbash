package de.zalando.bigbash.exceptions;

import de.zalando.bigbash.entities.EditPosition;

/**
 * Created by bvonloesch on 08/06/16.
 */
public class BigBashException extends RuntimeException {

    EditPosition position;

    public BigBashException(String message, EditPosition position) {
        super(message);
        this.position = position;
    }

    public EditPosition getPosition() {
        return position;
    }
}
