package jlox;

// Challenge 10.2

import java.util.List;

interface FunctionDeclaration {
    List<Token> getParams();
    List<Stmt> getBody();
    Token getName(); // if anonymous, null
}
