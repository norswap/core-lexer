package norswap.core.lexer;

import static norswap.utils.Util.cast;

final class Clone
{
    /**
     * Implementation of {@link Tokens#clone(Tokens.Token)}.
     */
    static <T extends Tokens.Token> T clone (T token)
    {
        if (token == null) return null;

        Tokens.Token out;

        /**/ if (token instanceof Tokens.Identifier) {
            Tokens.Identifier tok = cast(token);
            out = new Tokens.Identifier(tok.str);
        }
        else if (token instanceof Tokens.IntLiteral) {
            Tokens.IntLiteral tok = cast(token);
            out = new Tokens.IntLiteral(tok.value);
        }
        else if (token instanceof Tokens.StringLiteral) {
            Tokens.StringLiteral tok = cast(token);
            out = new Tokens.StringLiteral(tok.str);
        }
        else if (token instanceof Tokens.CharLiteral) {
            Tokens.CharLiteral tok = cast(token);
            out = new Tokens.CharLiteral(tok.str);
        }
        else if (token instanceof Tokens.Bracket) {
            Tokens.Bracket tok = cast(token);
            out = new Tokens.Bracket(tok.c);
        }
        else if (token instanceof Tokens.Operator) {
            Tokens.Operator tok = cast(token);
            out = new Tokens.Operator(tok.str);
        }
        else if (token instanceof Tokens.Spaces) {
            Tokens.Spaces tok = cast(token);
            out = new Tokens.Spaces(tok.count);
        }
        else if (token instanceof Tokens.Newline) {
            out = new Tokens.Newline();
        }
        else if (token instanceof Tokens.Garbage) {
            Tokens.Garbage tok = cast(token);
            out = new Tokens.Garbage(tok.str);
        }
        else if (token instanceof Tokens.Comment) {
            Tokens.Comment tok = cast(token);
            out = new Tokens.Comment(tok.block, tok.align, tok.lines);
        }
        else if (token instanceof Tokens.EOF) {
            out = new Tokens.EOF();
        }
        else {
            throw new Error("unreachable");
        }

        out.pos = token.pos;
        out.len = token.len;
        return cast(out);
    }
}
