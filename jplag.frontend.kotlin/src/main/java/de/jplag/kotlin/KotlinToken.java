package de.jplag.kotlin;

import de.jplag.Token;
import de.jplag.SharedTokenType;

public class KotlinToken extends Token {

    public KotlinToken(SharedTokenType type, String file, int line, int column, int length) {
        super(type, file, line, column, length);
    }

    public KotlinToken(KotlinTokenType type, String file, int line, int column, int length) {
        super(type, file, line, column, length);
    }

    @Override
    public String type2string() {
        return "";
    }
}
