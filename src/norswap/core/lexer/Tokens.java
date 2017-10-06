package norswap.core.lexer;

import static norswap.core.lexer.Escapes.escape;

/**
 * Wrapper class for all {@link Token} types; also contains a few functions on tokens.
 */
public final class Tokens
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The ancestor of all token types. Records the input positions corresponding to the token.
     */
    public abstract static class Token
    {
        /**
         * (immutable) The input position at which the token was matched.
         * <p>
         * This is set by the lexer, and has to be defined manually for manually instancied
         * tokens. Alternatively, the default value of -1 indicates the position isn't set.
         */
        public int pos = -1;

        /**
         * (immutable) The length of the token in the input.
         * <p>
         * This is set by the lexer, and has to be defined manually for manually instancied
         * tokens. Alternatively, the default value of 0 indicates the length isn't set.
         */
        public int len = 0;

        /**
         * Textual representation of the token for debugging purposes.
         *
         * Includes the token type, the escaped result of {@link #unlex(Token)} and the input
         * positions, if defined.
         */
        public String toString() {
            return getClass().getSimpleName()
                    + " (" + escape(unlex(this)) + ")"
                    + (pos >= 0 ? " [" + pos + "-" + (pos + len) + "]" : "");
        }
    }

    // =============================================================================================

    /**
     * Makes a copy of the given token.
     */
    public static <T extends Token> T clone (T token) {
        return Clone.clone(token);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the token, generated independently from the input
     * the token was generated from.
     * <p>
     * This is not guarantee to yield the same string as the one the token was generated from,
     * but it is guarantee that lexing that string will yield the same token.
     * <p>
     * Currently, there are two possible differences between the lexed and unlexed string:
     * <ul>
     * <li>the unlexed string always has a space after the comment delimiter (`//`)</li>
     * <li>if the last comment before EOF is a comment, the unlexed string will include a
     *     terminating newline</li>
     * </ul>
     */
    public static String unlex (Tokens.Token token) {
        return Unlexer.unlex(token);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the given token stream (as per {@link #unlex(Token)}.
     */
    public static String unlex (Tokens.Token[] tokens) {
        return Unlexer.unlex(tokens);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the given token stream (as per {@link #unlex(Token)}, and
     * sets the tokens' positions to match the generated text.
     */
    public static String synchronize (Tokens.Token[] tokens) {
        return Unlexer.synchronize(tokens);
    }

    // =============================================================================================

    /**
     * A token representing an identifier (or keyword).
     */
    public static final class Identifier extends Token
    {
        public String str;
        public Identifier (String str) { this.str = str; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing an integer litteral.
     */
    public static final class IntLiteral extends Token
    {
        public int value;
        public IntLiteral (int value) { this.value = value; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a string litteral.
     */
    public static final class StringLiteral extends Token
    {
        public String str;
        public StringLiteral (String str) { this.str = str; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a character litteral.
     */
    public static final class CharLiteral extends Token
    {
        public String str;
        public CharLiteral (String str) { this.str = str; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a bracket character (see {@code README.md}).
     */
    public static final class Bracket extends Token
    {
        public char c;
        public Bracket (char c) { this.c = c; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a sequence of operator characters (see {@code README.md}).
     */
    public static final class Operator extends Token
    {
        public String str;
        public Operator (String str) { this.str = str; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing one or more space characters.
     */
    public static final class Spaces extends Token
    {
        public int count;
        public Spaces (int count) { this.count = count; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a single newline.
     */
    public static final class Newline extends Token {}

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing a string of characters that cannot be used to form another valid token.
     */
    public static final class Garbage extends Token
    {
        public String str;
        public Garbage (String str) { this.str = str; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A token representing an end-of-line comment, or a contiugous block of commented lines.
     */
    public static final class Comment extends Token
    {
        /** Whether this is a block comment. Block comments start on their own line. */
        public boolean block;

        /** All lines (one or more) in the comment. */
        public String[] lines;

        /** Number of leading whitespace before each line. 0 if `lines.size == 1`. */
        public int align;

        public Comment (boolean block, int align, String[] lines) {
            this.block = block;
            this.align = align;
            this.lines = lines;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The end of file token.
     */
    public static final class EOF extends Token {}

    // ---------------------------------------------------------------------------------------------
}
