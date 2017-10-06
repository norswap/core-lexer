package norswap.core.lexer;

import static norswap.utils.Strings.append;
import static norswap.utils.Strings.repeat;
import static norswap.utils.Util.cast;

final class Unlexer
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation of {@link Tokens#unlex(Tokens.Token)}.
     */
    static String unlex (Tokens.Token token)
    {
        /**/ if (token instanceof Tokens.Identifier) {
            Tokens.Identifier tok = cast(token);
            return tok.str;
        }
        else if (token instanceof Tokens.IntLiteral) {
            Tokens.IntLiteral tok = cast(token);
            return "" + tok.value;
        }
        else if (token instanceof Tokens.StringLiteral) {
            Tokens.StringLiteral tok = cast(token);
            return "\"" + tok.str + "\"";
        }
        else if (token instanceof Tokens.CharLiteral) {
            Tokens.CharLiteral tok = cast(token);
            return "\'" + tok.str + "\'";
        }
        else if (token instanceof Tokens.Bracket) {
            Tokens.Bracket tok = cast(token);
            return "" + tok.c;
        }
        else if (token instanceof Tokens.Operator) {
            Tokens.Operator tok = cast(token);
            return tok.str;
        }
        else if (token instanceof Tokens.Spaces) {
            Tokens.Spaces tok = cast(token);
            return repeat(' ', tok.count);
        }
        else if (token instanceof Tokens.Newline) {
            Tokens.Newline tok = cast(token);
            return "\n";
        }
        else if (token instanceof Tokens.Garbage) {
            Tokens.Garbage tok = cast(token);
            return tok.str;
        }
        else if (token instanceof Tokens.Comment) {
            Tokens.Comment tok = cast(token);
            String spacing = repeat(' ', tok.align);
            StringBuilder b = new StringBuilder();
            for (String line: tok.lines)
                append(b, spacing, "// ", line, "\n");
            return b.toString();
        }
        else if (token instanceof Tokens.EOF) {
            return "\0";
        }
        throw new Error("unreachable");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation of {@link Tokens#unlex(Tokens.Token[])}.
     */
    static String unlex (Tokens.Token[] tokens)
    {
        StringBuilder b = new StringBuilder();
        for (Tokens.Token token: tokens)
            append(b, unlex(token));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation of {@link Tokens#synchronize(Tokens.Token[])}.
     */
    static String synchronize (Tokens.Token[] tokens)
    {
        int pos = 0;
        StringBuilder b = new StringBuilder();

        for (Tokens.Token token: tokens) {
            String unlexed = unlex(token);
            append(b, unlexed);
            token.pos = pos;
            token.len = unlexed.length();
            pos += token.len;
        }

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
