package norswap.core.lexer;

import norswap.core.lexer.Tokens.*;
import norswap.utils.ArrayStack;

import static norswap.core.lexer.Tokens.unlex;
import static norswap.utils.Chance.*;

/**
 * Logic to help generate random token streams.
 */
public final class Generation
{
    // ---------------------------------------------------------------------------------------------

    private static String[] ESCAPES
        = new String[] { "\\\"", "\\'", "\\\\", "\\n", "\\t", "\\0" };

    // ---------------------------------------------------------------------------------------------

    private static char[] BRACKETS
        = new char[] { '(', ')', '[', ']', '{', '}' };

    // ---------------------------------------------------------------------------------------------

    private static char[] OPERATORS = new char[] {
        '!', '$', '%', '&', '*', '+', ',', '-', '.', '/', ':', ';',
        '<', '=', '>', '?', '@', '\\', '^', '`', '|', '~' };

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random identifier of 1-4 characters, starting with '_' or 'a' and containing
     * only '_', 'a' and '1'.
     */
    private static String generate_id()
    {
        StringBuilder b = new StringBuilder(4);
        b.append(selectp('_', 'a'));
        int len = random(4);
        for (int i = 0; i < len; ++ i) b.append(selectp('_', 'a', '1'));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates the character 'a' 9 times out of 10, a standard escape 1 time out of 20, and a
     * unicode escape 1 time out of 20.
     */
    private static String generate_char()
    {
        if (probability(9, 10)) return "a";
        int r = random(ESCAPES.length * 2);
        if (r < ESCAPES.length)
            return ESCAPES[r];
        else
            return "\\u" + random(65536);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random string of 0-9 characters. Each of these characters are generated
     * through `generate_char`.
     */
    private static String generate_string()
    {
        StringBuilder b = new StringBuilder();
        int len = random(10);
        for (int i = 0; i < len; ++i)
            b.append(generate_char());
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random valid operator of length 1-4.
     */
    private static String generate_operator()
    {
        StringBuilder b = new StringBuilder();
        int len = 1 + random(4);
        for (int i = 0; i < len; ++ i) {
            char c = selectp(OPERATORS);
            if (c == '/' && i != 0 && b.charAt(i - 1) == '/') {
                -- i;
                continue;
            }
            b.append(c);
        }
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random string of garbage of length 1-4.
     */
    private static String generate_garbage()
    {
        StringBuilder b = new StringBuilder();
        int len = 1 + random(4);
        for (int i = 0; i < len; ++ i)
            b.append(selectp('\t', 'é', '\u0001'));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates the content of a comment, which always contains an operator, a bracket character,
     * an escape, spaces, some garbage characters and a string. This is so that we can confirm
     * that these are correctly gobbled up by the comment.
     */
    private static String generate_comment_line()
    {
        return
            select("foo", "bar", "baz") +
            selectp(OPERATORS) +
            selectp(BRACKETS) +
            select(ESCAPES) +
            ' ' +
            generate_garbage() +
            ' ' +
            generate_string();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the token implies the start of a new line.
     */
    private static boolean is_line_demarcator (Token token)
    {
        return token == null || token instanceof Newline || token instanceof Comment;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random comment. If the comment naturally occurs in a block position in the
     * stream, it will be a block comment. We may also have to force the comment to be block
     * if inserting a nonblock comment in the stream would make it invalid. In other cases,
     * we generate a block comment one time out of two.
     *
     * When we generate a block comment, we insert a newline token in the stream if necessary.
     * When a block token is generated, it will be 1-4 lines in length and have an alignment
     * of 0-7.
     */
    private static Comment generate_comment (ArrayStack<Token> tokens)
    {
        Token last = tokens.poll();
        Token penultimate = tokens.peek();
        if (last != null) tokens.add(last);

        boolean block
               // Block token by virtue of its position in the token stream.
            =  is_line_demarcator(last)
               // Insert a newline, otherwise the space comment should have been omitted.
            || last instanceof Spaces && is_line_demarcator(penultimate)
               // Insert a newline, otherwise comment should have started one characte earlier.
            || last instanceof Operator && ((Operator) last).str.endsWith("/")
               // 50% chance to insert a newline
            || flip();

        int len = 1;
        int align = 0;

        if (block) {
            len += random(4);
            while (true) {
                align = random(8);
                // Can't have same alignment as previous block comment.
                if (last instanceof Comment) {
                    Comment old = (Comment) last;
                    if (old.block && align == old.align) continue;
                }
                break;
            }
            // Force comment to start on own line if needed.
            if (!is_line_demarcator(last))
                tokens.add(new Newline());
        }

        String[] lines = new String[len];
        for (int i = 0; i < len; ++ i)
            lines[i] = generate_comment_line();

        return new Comment(block, align, lines);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a random token: every token type is mostly equiprobable. If the chosen token type
     * would make the stream invalid, we pick another, which skew the distribution slightly.
     *
     * The generated token is appended to token stream (`tokens`), which may also be modified in
     * other ways to ensure the validity of the stream.
     */
    private static void generate_token (ArrayStack<Token> tokens)
    {
        // Assign to token to generate to this.
        Token token = null;

        Token last = tokens.peek();

        while(true) {
            int i = random(10);

            switch (i) {
                // Identifier
                case 0:
                    if (last instanceof Identifier) continue;
                    token = new Identifier(generate_id());
                    break;

                // IntLiteral
                case 1:
                    if (last instanceof IntLiteral || last instanceof Identifier) continue;
                    token = new IntLiteral(random(100000));
                    break;

                // StringLiteral
                case 2:
                    token = new StringLiteral(generate_string());
                    break;

                // CharLiteral
                case 3:
                    token = new CharLiteral(generate_char());
                    break;

                // Bracket
                case 4:
                    token = new Bracket(selectp(BRACKETS));
                    break;

                // Operator
                case 5:
                    if (last instanceof Operator) continue;
                    token = new Operator(generate_operator());
                    break;

                // Spaces
                case 6:
                    if (last instanceof Spaces) continue;
                    // Generates 1-10 spaces.
                    int count = 1 + random(10);
                    if (last instanceof Comment && ((Comment) last).align == count) continue;
                    token = new Spaces(count);
                    break;

                // Garbage
                case 7:
                    if (last instanceof Garbage) continue;
                    token = new Garbage(generate_garbage());
                    break;

                // Comment
                case 8:
                    token = generate_comment(tokens);
                    break;

                // Newline
                case 9:
                    token = new Newline();
                    break;
            }
            break;
        }

        tokens.add(token);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Converts the token stream to a string that should lex to an identical token stream.
     * Also assigns the position and length of each token in the stream.
     */
    private static String dump_tokens (Token[] tokens)
    {
        StringBuilder b = new StringBuilder();
        for (Token token: tokens) {
            token.pos = b.length();
            b.append(unlex(token));
            token.len = b.length() - token.pos;
        }
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a valid stream of approximately `n` tokens. These tokens do *not* have
     * their input positions set properly.
     */
    private static Token[] generate_tokens (int n)
    {
        ArrayStack<Token> tokens = new ArrayStack<>(n);
        for (int i = 0; i < n; ++ i) generate_token(tokens);
        tokens.add(new EOF());
        return tokens.toArray(new Token[0]);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a valid token stream of approximately `n` tokens. These tokens do have
     * their input positions set properly.
     */
    public static Token[] generate_token_stream (int n)
    {
        Token[] tokens = generate_tokens(n);
        dump_tokens(tokens);
        return tokens;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates an input string that will lex to approximately `n` tokens.
     */
    public static String generate_input_string (int n)
    {
        Token[] tokens = generate_tokens(n);
        return dump_tokens(tokens);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates an input string that will lex to approximately `n` tokens, along with its token
     * stream. The tokens' input position are consistent with the input stream.
     */
    public static Input generate_input (int n)
    {
        Token[] tokens = generate_tokens(n);
        String str = dump_tokens(tokens);
        Input input = new Input();
        input.tokens = tokens;
        input.str = str;
        return input;
    }

    // ---------------------------------------------------------------------------------------------

    public static class Input
    {
        public Token[] tokens;
        public String str;
    }

    // ---------------------------------------------------------------------------------------------
}
