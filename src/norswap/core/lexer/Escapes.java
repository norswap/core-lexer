package norswap.core.lexer;

import java.awt.event.KeyEvent;

/**
 * Utility functions dealing with character and string escapes as defined by the lexer.
 */
public final class Escapes
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether {@code c} is a printable Unicode character
     * (even if it is, there is no guarantee that a GUI program will be able to render it).
     */
    public static boolean is_printable (char c)
    {
        // Trusting the internet man: http://stackoverflow.com/questions/220547
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return !Character.isISOControl(c)
            && c != KeyEvent.CHAR_UNDEFINED
            && block != null
            && block != Character.UnicodeBlock.SPECIALS;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether {@code c} is an ASCII character and is printable.
     */
    public static boolean is_ascii_printable (char c)
    {
        return 32 <= c && c <= 126 || c == '\n' || c == '\t';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether {@code c} is a character that needs to be escaped inside string and
     * character literals in Core0, or if it isn't printable according to {@link
     * #is_printable(char)}.
     */
    public static boolean is_escapable (char c)
    {
        return c == '"' || c == '\'' || c == '\\' || c == '\n' || c == '\t' || c == '\0'
            || !is_printable(c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string where all characters matched by {@link #is_escapable(char)} are replaced by
     * their escapes.
     */
    public static String escape (String str)
    {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < str.length(); ++i)
        {
            char c = str.charAt(i);
            if (!is_escapable(c))   b.append(c);
            else                    b.append(escape(c));
        }
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * If {@link #is_escapable(char)}, returns the escape sequence, otherwise returns a string
     * containing only {@code c}.
     */
    public static String escape (char c)
    {
        switch (c) {
            case '"':
                return "\\\"";
            case '\'':
                return "\\'";
            case '\\':
                return "\\\\";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\0':
                return "\\0";
            default:
                if (is_printable(c))
                    return "" + c;
                else
                    return String.format("\\u%05d", (int) c);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Performs the reverse operation to {@link #escape}: replaces all Core0 escape sequences by the
     * escaped character.
     */
    public static int unescape_char (String str)
    {
        char c;
        switch (str.length()) {
            case 0:
                return -1;
            case 1:
                c = str.charAt(0);
                if (c == '\\')
                    return -1;
                else
                    return c;
            case 2:
                c = str.charAt(0);
                if (c != '\\') return -1;
                c = str.charAt(1);
                switch (c) {
                    case '"':
                    case '\'':
                    case '\\':
                    case '\n':
                    case '\t':
                    case '\0':
                        return c;
                    default:
                        return -1;
                }
            case 6:
                if (str.charAt(0) != '\\' || str.charAt(1) != 'u') return -1;
                int num = 0;
                for (int i = 2; i < 6; ++ i) {
                    c = str.charAt(i);
                    if (!Predicates.is_hex_digit(c))return -1;
                    num = num * 16 + c - '0';
                }
                return (char) num;
            default:
                return -1;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
