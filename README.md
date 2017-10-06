# core-lexer

[![jitpack](https://jitpack.io/v/norswap/core-lexer.svg)][jitpack]

- [Maven Dependency][jitpack] (the badge above indicates the latest commit available)
- [Javadoc][javadoc]

[jitpack]: https://jitpack.io/#norswap/core-lexer
[javadoc]: https://jitpack.io/com/github/norswap/core-lexer/-SNAPSHOT/javadoc/

core-lexer is a robust reusable lexer.

An introduction/motivation is available here: http://norswap.com/reusable-lexer/

## Specification

- Entry point: [`Lexer.lex`]
- Input: a nul-terminated (`\0`) string.
- Output: an array of [`Token`]s

[`Lexer.lex`]: https://jitpack.io/com/github/norswap/norswap-utils/-SNAPSHOT/javadoc/norswap/core/lexer/Lexer.html#lex-java.lang.String-
[`Token`]: https://jitpack.io/com/github/norswap/norswap-utils/-SNAPSHOT/javadoc/norswap/core/lexer/Tokens.Token.html
 
### Token Types

Also see [`Tokens`] for the field list of each token type.

[`Tokens`]: https://jitpack.io/com/github/norswap/norswap-utils/-SNAPSHOT/javadoc/norswap/core/lexer/Tokens.html  

- Identifier

    - For identifiers as well as keywords.
    
    - Must start by an underscore or alphabetic ascii char, followed by similar chars or numbers.

- IntLiteral

    - Decimal integers.
    
    - There are currently no size-specific integers, no floating point numbers,
      no binary, octal or hexadecimal notations. Underscore separators are not allowed.

    - In the future, all of these things will be added, except binary and octal notations.

- StringLiteral

    - Delimited by double quotes.
    
    - May contain escapes (see below), which **are not** processed at this time.
      We only match the string up to the next non-escaped double quote.

- CharLiteral

    - Delimited by single quotes.
    
    - May contain escapes (see below), which **are not** processed at this time.
      We only match the character up to the next non-escaped single quote.
      This means that character literals containing multiple characters are allowed at this stage.

- Bracket

    - The token type for the characters `(`, `)`, `[`, `]`, `{`, `}`.

- Operator

    - A sequence of "operator characters", unbroken by whitespace.
    
    - Operator characters are `!`, `$`, `%`, `&`, `*`, `+`, `,`, `-`, `.`, `/`, `:`, `;`, `<`, `=`,
      `>`, `?`, `@`, ``\ ``, `^`, `` ` ``, `|`, `~`.
    
    - Exception: an operator string may not contain two consecutive `/`
      (as that is the comment syntax).

- Spaces
    
    - Captures space characters. Newlines and comments have their own token type and tabs
      register as garbage instead (see below).
    
- Newline

    - The token type for the newline character.

- Garbage

    - Garbage is a mechanism to continue lexing even when no token can be matched.
    
    - When no token can be matched, the lexer registers a character of garbage, then proceeds
      normally starting at the next character.
      
    - Contiguous garbage characters are compacted into a single garbage string.

- Comment

    - A comment token is either a block or nonblock comment. Both forms share the same token data
      structure.
    
    - A nonblock comment starts with the `//` delimiter and must be preceded by non-space
      content on the same line.
      
    - Otherwise, the delimiter `//` appears on its own line, optionally preceded by space
      characters, and indicates a block comment.

    - A nonblock comment starts with the `//` delimiter and runs until the end of the line,
      including its terminating newline (if any).
    
    - A block comment comprises all consecutive lines where the `//` delimiter appears at the
      same horizontal position as in the initial line (all leading characters must be spaces).
      This includes all leading spaces, and the newline character of its last line (if any).
      
    - Clarification: It's possible for a block comment to consist of a single line.

    - Consequence 1: a block comment token won't be immediately preceded by a `Spaces` token in the
      token stream.
      
    - Consequence 2: A comment token always implies as many newlines as it has lines, except if it
      appears at the end of the input, in which case a final newline character may not be physically
      present in the input.
    
    - In block comments, the amount of leading spaces is known as *the alignment* (and may be zero).
      Nonblock comments have no alignment, even though the `align` field of the token
      will be set to zero.
      
    - The comment token records the content of each line, not including leading whitespace and the
      `//` delimiter. If a space character follows `//`, it is also stripped (at most one space 
      character is stripped in this manner).
      
- EOF

    - An EOF token is emitted at the end of every token stream.
    
### Character Escapes

- The escapes `\n`, `\t`, `\\`, `\"`, `\'`, `\0` have their expected meaning (i.e. respecitvely
  newline, tab, backslash, double quote, quote, nul character).
  
- Unicode escapes are also supported, those must be of the form `\uXXXX`, where each of the
  four `X` must be a decimal digit.
  
### Internationalization / Unicode

- The implementation works on a String, so the decoding part happens when reading the file and is
  not a concern of the lexer.

- All special tokens use ASCII characters. That is not going to change.

- Otherwise, no special processing is done on characters, so they just get aggregated in tokens
  as though they were "dumb bytes". As it stands, the "dumb bytes" are UTF-16 code points (because
  the implementation is in Java) but the logic could be used unchanged with other encodings.
  
- One constraint: hexadecimal escapes are 16 bits wide.
  
- How the dumb bytes and escapes within string/character literals and comments are handled is up to
  the higher levels.
  
### Restrictions

Given the rules outlined in the "Token Types" section, and the fact that the input is processed
from left to right to generate a token stream, the following restrictions on the generated token
stream arise naturally:

- An identifier token cannot be followed by another identifier token or by an integer literal token.

- An integer literal token may not be followed by another integer litteral token.

- A space token may not be followed by another space token.

- An operator token may not be followed by another operator token.

- A garbage token may not be followed by another garbage token.

- The EOF token may (and must) only appear as the last token.

- A space token cannot be followed by a block comment token.

- Two block comment tokens cannot follow one another if their alignment is identical.

## Testing

The lexer is tested using [random generation testing]: a random token stream is generated, unlexed
and then re-lexed, after which we compare the original and reconstructed token for equality.

[random generation testing]: http://norswap.com/gen-testing/