package norswap.core.lexer;

import norswap.utils.ArrayStack;

/**
 * Contains the {@link #lex} function which performs tokenization.
 */
public final class Lexer
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Turns {@code code} (a nul-terminated string) into a sequence of tokens.
     * See {@code README.md} for more details.
     */
    public static Tokens.Token[] lex (String code)
    {
        if (code.charAt(code.length() - 1) != '\0')
            throw new RuntimeException("Supplied code is not nul-terminated.");

        char c;
        int pos = 0;
        ArrayStack<Tokens.Token> stack = new ArrayStack<>(1024);

        while ((c = code.charAt(pos)) != '\0')
        {
            int start = pos;
            switch (c)
            {
                case ' ':

                    pos = match_spaces(code, pos, stack);
                    break;

                case '\n':

                    stack.push(new Tokens.Newline());
                    ++ pos;
                    break;

                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
                case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
                case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
                case 'v': case 'w': case 'x': case 'y': case 'z':
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G':
                case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
                case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U':
                case 'V': case 'W': case 'X': case 'Y': case 'Z':
                case '_':

                    pos = match_id(code, pos, stack);
                    break;

                case '/':

                    if (code.charAt(pos+1) == '/')
                        pos = match_comment(code, pos, stack);
                    else
                        pos = match_operator(code, pos, stack);
                    break;

                case '(': case ')':
                case '{': case '}':
                case '[': case ']':

                    stack.push(new Tokens.Bracket(c));
                    ++ pos;
                    break;

                case '!': case '$': case '%': case '&': case '*': case '+': case ',':
                case '-': case '.': case ':': case ';': case '<': case '=': case '>':
                case '?': case '@': case '^': case '`': case '|': case '~': case '\\':

                    pos = match_operator(code, pos, stack);
                    break;

                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':

                    pos = match_int_literal(code, pos, stack);
                    break;

                case '"':

                    pos = match_string_literal(code, pos, stack);
                    break;

                case '\'':

                    pos = match_char_literal(code, pos, stack);
                    break;

                default:
                    append_garbage_char(code, pos++, stack);
            }

            Tokens.Token top = stack.peek();
            if (top.pos < 0) top.pos = start;
            top.len = pos - top.pos;
        }

        Tokens.EOF eof = new Tokens.EOF();
        eof.pos = pos;
        eof.len = 1;
        stack.push(eof);

        return stack.toArray(new Tokens.Token[0]);
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_spaces (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        int start = pos;
        char c = code.charAt(pos);
        while (c == ' ') c = code.charAt(++ pos);
        stack.push(new Tokens.Spaces(pos - start));
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_id (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        StringBuilder b = new StringBuilder();

        while (true) {
            char c = code.charAt(pos);
            if (!Predicates.is_id_char(c)) break;
            ++ pos;
            b.append(c);
        }

        stack.push(new Tokens.Identifier(b.toString()));
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the token implies the start of a new line.
     */
    private static boolean is_line_demarcator (Tokens.Token token)
    {
        return token == null || token instanceof Tokens.Newline || token instanceof Tokens.Comment;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_comment (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        // 1. set alignment, potentially pop leading space

        Tokens.Token last = stack.poll();
        Tokens.Token penultimate = stack.peek();
        ArrayStack<String> lines;
        boolean block = false;

        int align = last instanceof Tokens.Spaces && is_line_demarcator(penultimate)
            ? ((Tokens.Spaces) last).count
            : 0;

        pos -= align;
        if (align == 0 && last != null) stack.push(last);

        if (align > 0 || is_line_demarcator(last)) {
            block = true;
            lines = new ArrayStack<>(4);
        }
        else {
            // minimize reallocations
            lines = new ArrayStack<>(1);
        }

        int start = pos;

        // 2. match lines

        while (true) {
            int line_start = pos;

            // skip leading space
            char c = code.charAt(pos);
            while (c == ' ') c = code.charAt(++ pos);

            // check alignment & comment syntax
            if (pos - line_start != align || c != '/' || code.charAt(++ pos) != '/') {
                pos = line_start;
                break;
            }
            c = code.charAt(++ pos);

            // skip a single leading space, if present
            if (c == ' ') c = code.charAt(++ pos);
            // match line content
            StringBuilder b = new StringBuilder();
            while (c != '\n' && c != 0) {
                b.append(c);
                c = code.charAt(++ pos);
            }
            if (c == '\n') ++ pos;

            lines.push(b.toString());
            if (!block) break;
        }

        Tokens.Comment comment = new Tokens.Comment(block, align, lines.toArray(new String[0]));
        comment.pos = start;
        stack.push(comment);
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_operator (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        StringBuilder b = new StringBuilder();

        char c = code.charAt(pos);
        char d;
        while (Predicates.is_operator(c)) {
            d = code.charAt(++ pos);
            if (c == '/' && d == '/') { -- pos; break; }
            b.append(c);
            c = d;
        }

        stack.push(new Tokens.Operator(b.toString()));
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_int_literal (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        int num = 0;
        boolean overflow = false;
        char c = code.charAt(pos);

        while (Predicates.is_digit(c)) {
            int tmp = num * 10 + c - '0';
            if (tmp < num) {
                overflow = true;
                break;
            }
            num = tmp;
            c = code.charAt(++ pos);
        }

        stack.push(new Tokens.IntLiteral(num));
        if (!overflow) return pos;

        // overflow: add extraneous characters to garbage string
        while (Predicates.is_digit(c)) {
            append_garbage_char(code, pos, stack);
            c = code.charAt(++ pos);
        }
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_string_literal (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        int start = pos;
        StringBuilder b = new StringBuilder();
        char c = code.charAt(++ pos);

        while (c != '"' && c != 0) {
            b.append(c);
            if (c == '\\') {
                c = code.charAt(++ pos);
                if (c == 0) break;
                b.append(c);
            }
            c = code.charAt(++ pos);
        }
        if (c == 0) return start;

        stack.push(new Tokens.StringLiteral(b.toString()));
        return pos + 1;
    }

    // ---------------------------------------------------------------------------------------------

    private static int match_char_literal (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        int start = pos;
        StringBuilder b = new StringBuilder();
        char c = code.charAt(++ pos);

        while (c != '\'' && c != 0) {
            b.append(c);
            if (c == '\\') {
                c = code.charAt(++ pos);
                if (c == 0) break;
                b.append(c);
            }
            c = code.charAt(++ pos);
        }
        if (c == 0) return start;

        stack.push(new Tokens.CharLiteral(b.toString()));
        return pos + 1;
    }

    // ---------------------------------------------------------------------------------------------

    private static void append_garbage_char (String code, int pos, ArrayStack<Tokens.Token> stack)
    {
        char c = code.charAt(pos);
        Object top = stack.peek();

        if (top instanceof Tokens.Garbage) {
            Tokens.Garbage garbage = (Tokens.Garbage) top;
            garbage.str += c;
        }
        else {
            stack.push(new Tokens.Garbage("" + c));
        }
    }

    // ---------------------------------------------------------------------------------------------
}
