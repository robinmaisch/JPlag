# JPlag Frontend Design

To add support for a new language to JPlag, a JPlag frontend needs to be created for that specific language. The core purpose of a frontend is to transform each submission to a TokenList, an abstraction of the content of the submission files independent of the language of the submissions.<br>
The TokenLists of the different submissions are then passed on to a comparison algorithm that checks the TokenLists for matching sequences.

## How are submissions represented? — Notion of _Token_

In the context of JPlag, a Token does not represent a lexical unit, as identifiers, keywords or operators. Instead, Tokens represent syntactic entities, like statements, or control structures. More than one token might be needed to represent the nested structure of a statement or expression in a linear token list.

```java
    class MyClass extends SuperClass {  private String name;  }
    //CLASS_DECLARATION              //CLASS_BODY_BEGIN
                                        //FIELD_DECLARATION
                                                              //CLASS_BODY_END
```
Each comment is intended to represent one token.

From this example in Java, you may be able to see the following things:
 - a class declaration is represented by three tokens of different _types_: `CLASS_DECLARATION`, `CLASS_BODY_BEGIN` and `CLASS_BODY_END`
 - a token is associated with a _position_ in a code file.
 - the abstraction is incomplete, many details of the code are omitted. The original code cannot be reconstructed from the TokenList, but its structure can.

A few more points about Tokens in JPlag:
 - a TokenList contains the Tokens from _all files of one submission_. For that reason, Tokens save the _filename_ of their origin in addition to their position.
 - Token types are represented as nonnegative integers. Different frontends may associate different meanings to the same integer. 
   - `0` and `1` are reserved for special token types universal to all frontends, so custom token types start at `2`.
   - A `TokenConstants` interface saves all token types as integer constants with a speaking name. This way, the token types can be addressed by their human-understandable identifier throughout the frontend.
   - For brevity, each token type is also associated with a String representation, usually shorter than their name. Looking at the String representations used in existing frontends, you may recognize a kind of convention about how they are formed. The example above uses the full names of token types.

## How does the transformation work?

Here is an outline of the transformation process.
 - each submitted file is _parsed_. The result is a set of ASTs for each submission.
 - each AST is now _traversed_ depth-first. The nodes of the AST represent the grammatical units of the language.
    - upon entering and exiting a node, Tokens can be created that match the type of the node. They are added to the current TokenList.
    - for block-type nodes like bodies of classes or if expressions, the point of entry and exit correspond to the respective `BEGIN` and `END` token types. If done correctly, the TokenList should contain balanced pairs of matching `BEGIN` and `END` tokens.

```java
@Override
public void enterClassDeclaration(ClassDeclarationContext context) {
    Token token = new Token(CLASS_DECLARATION, /* more parameters ... */);
    addToken(token);
}

@Override
public void exitClassDeclaration(ClassDeclarationContext context) {
     // class declarations get no end token -> do nothing
}

@Override
public void enterClassBody(ClassBodyContext context) {
    Token token = new Token(CLASS_BODY_START, /* more parameters ... */);
    addToken(token);
}

@Override
public void enterClassDeclaration(ClassBodyContext context) {
    Token token = new Token(CLASS_BODY_END, /* more parameters ... */);
    addToken(token);
}
```
The way the traversal works and how you can interact with the process depends on the parser technology used. In the example above, **ANTLR-generated parsers** were used, as was in most of the current JPlag frontends. We recommend to use ANTLR for any new frontend.

If a hard-coded (as opposed to dynamically generated) parser library is available for your language, it may make sense to use it. An implementation of the visitor pattern for the resulting AST should be included.

## How to get started

The other articles cover the different areas of a frontend.
 - [Frontend Structure](FrontendStructure.md) explains the purpose of every component of a JPlag frontend, which files to just copy-and-paste, and how to integrate the new frontend into the system.
 - [Token Selection](TokenSelection.md) provides a guideline on which elements of a language should be marked with a token and which should be omitted.
 - [Token Extraction](TokenExtraction.md) reiterates how Tokens are created during the traversal of the ASTs, and provides a collection of strategies to use when the parser does not cover all the elements that need tokens.