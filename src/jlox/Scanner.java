package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static jlox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static { // keywords
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("break",  BREAK); // Challenge 9.3
        keywords.put("continue", CONTINUE); // Challenge 9.3
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // WE are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '+' -> addToken(PLUS);
            case '-' -> addToken(MINUS);
            case '%' -> addToken(PERCENT);
            case ':' -> addToken(COLON);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '?' -> addToken(QUESTION);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    singleLineComment();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
                // ignore whitespace
                }
            case '\n' -> line++;
            case '"' -> string();

            default -> {
                if (isDigit(c)){
                    number();
                } else if (isAlpha(c)){
                    identifier();
                } else {
                    jlox.error(line, "Unexpected character: '" + c + "'.");
                }
            }
        }
    }

    private void singleLineComment(){
        // a comment goes until the end of the line
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    /* Challenge 4.4:
     * Beyond the finicky logics of peek vs advance, it wasn't too difficult to implement.
     * Drawing from CSCI101, for non-regular languages, we build them using PDA which used a stack to push and pop.
     * Using that same logic, keeping track of the comment level we're in seemed to work best.
     */
    private void blockComment(){
        // block comment spans across multiple lines
        int level = 1;

        while (level > 0) {
            if (isAtEnd()) {
                jlox.error(line, "Unterminated comment.");
                return;
            }

            if (peek() == '/' && peekNext() == '*') {
                advance(); // consume the *
                advance(); // consume the /
                level++;
            } else if (peek() == '*' && peekNext() == '/') {
                advance(); // consume the *
                advance(); // consume the /
                level--;
            } else if (advance() == '\n') {
                line++;
            }
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            jlox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1); // +1, -1 to ingore the ""
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        // addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
        String text = source.substring(start, current);
        double value = Double.parseDouble(text);
        addToken(NUMBER, value);
        
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } 

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        return source.charAt(current++);
        // returns source.charAt(current), then current += current
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    public static boolean isKeyword(String lexeme) {
        return keywords.containsKey(lexeme);
    }
    
}
