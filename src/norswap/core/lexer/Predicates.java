package norswap.core.lexer;

/**
 * A few simple character-level predicates used by the lexer.
 */
public final class Predicates
{
    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is a decimal digit (0-9).
     */
    public static boolean is_digit (char c)
    {
        return '0' <= c && c <= '9';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is an hex digit (0-9, a-f and A-F).
     */
    public static boolean is_hex_digit (char c)
    {
        return '0' <= c && c <= '9' || 'A' <= c && c <= 'F' || 'a' <= c && c <= 'f';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is an ascii alphabetic character (a-z and A-Z).
     */
    public static boolean is_alpha (char c)
    {
        return 'a' <= c && c <= 'z'
            || 'A' <= c && c <= 'Z';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is a character allowed at the star of an identifier token (an underscore
     * or ascii alphabetic character).
     */
    public static boolean is_id_start (char c)
    {
        return c == '_' || is_alpha(c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is a character allowed inside an identifier token (an underscore, ascii
     * alphabetic or decimal digit character).
     */
    public static boolean is_id_char (char c)
    {
        return c == '_' || is_alpha(c) || is_digit(c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff {@code c} is an operator character (see {@code README.md}).
     */
    public static boolean is_operator (char c)
    {
        switch (c) {
            case '!':
            case '$':
            case '%':
            case '&':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case '\\':
            case '^':
            case '`':
            case '|':
            case '~':
                return true;
            default:
                return false;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
