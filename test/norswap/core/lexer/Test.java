package norswap.core.lexer;

import norswap.core.lexer.Tokens.*;
import norswap.utils.ArrayStack;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static norswap.core.lexer.Generation.*;
import static norswap.core.lexer.Generation.generate_input;

/**
 * Implements random generation testing of the lexer: a random token stream is generated,
 * unlexed and then re-lexed, after which we compare the original and reconstructed token for
 * equality.
 */
public final class Test
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Compares two tokens for equality.
     */
    public static boolean equals (Token a, Token b)
    {
        try {
            if (a == b) return true;
            if (a == null || b == null) return false;
            Class<?> cl = a.getClass();
            if (cl != b.getClass()) return false;
            for (Field field: cl.getFields())
                if (!Objects.deepEquals(field.get(a), field.get(b)))
                    return false;
            return true;
        }
        catch (Exception e) { return false; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a token stream of approximately {@code n} tokens (modulo stream modifications
     * enacted by {@link Generation#generate_token(ArrayStack)} then dump this stream to text form
     * (using {@link Generation#dump_tokens(Token[])}`) and lex it, finally checking if the new
     * token stream is equal to the one it was generated from.
     *
     * Exits on failure.
     */
    public static void test (int n)
    {
        Input input = generate_input(n);
        Token[] tokens0 = input.tokens;
        Token[] tokens1 = Lexer.lex(input.str);
        int len         = Math.min(tokens0.length, tokens1.length);

        System.out.println(Arrays.toString(tokens0));
        System.out.println(Arrays.toString(tokens1));

        for (int i = 0; i < len; ++ i) {
            // System.out.println(i + ": " + tokens0[i]);
            // System.out.println(i + ": " + tokens1[i]);
            boolean equal = equals(tokens0[i], tokens1[i]);
            if (!equal) {
                System.err.println(tokens0[i]);
                System.err.println(tokens1[i]);
                System.exit(1);
            }
            if (i == len - 1 && tokens0.length != tokens1.length) {
                System.err.println("different sizes");
                System.exit(1);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Repeatedly calls `test()` to perform random testing of the lexer. Runs forever until
     * stopped by the user.
     */
    public static void main (String[] args)
    {
        while (true) test(20);
    }

    // ---------------------------------------------------------------------------------------------
}
