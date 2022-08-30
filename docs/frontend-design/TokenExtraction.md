# Token Extraction

The token extraction is the most time-consuming part of the frontend design.
How difficult it is is largely dependent on the underlying **grammar** of the parser.

This article deals with the implementation of the listener which is called at every stage of traversal of the AST. The examples center around tokenizing the Java language, using a grammar written in ANTLR4.

```mermaid
flowchart LR
    AstVisitor -->|"enterNode(Node)"| Listener
    AstVisitor -->|"exitNode(Node)"| Listener
    
    Listener --> |"addToken(Token)"| TokenList
```
In the actual listener, there will be concrete `enter` and `exit` implementations for each syntactic category, e.g. `enterClassDeclaration`, `enterIfStatement` and so on.

## Basic case
The basic case is that a syntactic category of the grammar corresponds to a token directly.
```java
@Override
public void enterIfStatement(IfStatementContext context) {
    addToken(IF_STATEMENT, context);
}

@Override
public void enterIfBody(IfBodyContext context) {
    addToken(IF_BODY_START, context);
}
        
private void addToken(int tokenType, ParserRuleContext context) {
    tokens.add(new Token(tokenType, context.getLine(), context.getColumn(), context.getLength()));    
}
```
## Complex case: Ambiguity
The complexity comes from the degree to which syntactic categories are _reused_ in different contexts in the grammar. Instead of a distinct `ifBody` category, for example, there may be only one category for 'control structure bodies' or 'block expressions' of any kind.

```java
@Override
public void enterBlockExpression(BlockExpressionContext context) {
        // Now, is this the body of a method, a for/while/if/try/catch expression?
}
```
 
```antlrv4
    // unedited grammar
    ifStatement: 
        'if' '(' expression ')' statement
        ('else' statement))? ;
        
    statement:
        blockStatement | ifStatement | forStatement | ... ;    
```

### Approach 1: Edit the grammar
If you can alter the grammar definition, it may be well worth considering. Introduce a separate rule for each use of rules that you want to differentiate. Example in antlr4:

```antlrv4
    // after editing
    ifStatement: 
        'if' '(' expression ')' ifBody ('else' elseBody)? ;
    
    ifBody: 
        statement ;
    elseBody: 
        statement ;
        
    statement:
        blockStatement | ifStatement | forStatement | ... ;    
```
This does introduce some kind of redundancy to the grammar, but it makes the implementation of the listener methods much easier. The caveat that comes with this approach is that updated grammars will have to be edited again. The licence of the grammar should also be considered. 

### Approach 2: Manage a context stack
If you are stuck with the grammar and parser as they are, you can mimic what the parser does and introduce a _stack machine_ to the listener. Then, anytime a relevant syntactical structure is entered, you add a context to the stack, and when you enter an ambiguous subtree, the current context will help distinguish the different cases.

```java
@Override
public void enterIfStatement(IfStatementContext context) {
    addToken(IF_STATEMENT, context);
    contexts.enterContext(IF_CONTEXT);
}

@Override
public void enterBlockExpression(BlockExpressionContext context) {
    switch (contexts.peek()) {
        case IF_BODY -> addToken(IF_BODY_START, context);
        case FOR_BODY -> addToken(FOR_BODY_START, context);
        ...
    }
}

@Override
public void exitIfStatement(IfStatementContext context) {
    contexts.popContext();
}
```

The management of the context makes the listener much more complicated to read and maintain.

### Approach 3: Add tokens out of order and sort afterwards

(untested)

It is not required that the token list is sorted by position in the code. Adding tokens in the wrong order can even be of good use, see "Preventing simple attacks" below. Generally, however, the token list should be ordered to ensure consistency.

You may solve the problem of ambiguity by assigning tokens in the next higher context. For this, you search the list of children of the current subtree for the delimiters of the part in question, and assign the token from here. After that, the subtrees are parsed and more tokens may be added. Sort the token list in the end to restore the correct order of tokens.

```java
@Override
public void enterIfStatement(IfStatementContext context) {
    addToken(IF_STATEMENT, context);
    
    // fourth terminal in this rule: 'if' '(' <condition> ')' '{' <statements> '}' 
    ParserRuleContext bodyBegin = context.getChild(TerminalNode.class, 3);
    addToken(IF_BODY_BEGIN, bodyBegin);

    // fifth terminal in this rule: 'if' '(' <condition> ')' '{' <statements> '}'
    ParserRuleContext bodyEnd = context.getChild(TerminalNode.class, 4);
    addToken(IF_BODY_BEGIN, bodyBegin);
    
    // after this method, the condition and statements are traversed. 
}
```

Notes: 
- If there are optional parts in the grammar rule, the index of terminals may not be static. A more sophisticated search method may be necessary, possibly using the text content of the child nodes (`ParserRuleContext::getText`).
- In this example, the curly braces themselves are optional. The case where they are omitted needs to be covered as well.

## Additional notes

### Using the `exit` methods

The `exit` methods can be used to add `END` tokens for bodies and blocks. If you put the ´enter` and ´exit´ methods of a kind directly next to each other in the code as a pair, there should be little room for confusion about which token types should be added there. 

### Using terminals

Depending on the implementation of the grammar, some keywords or symbols may not have a rule for themselves. Using Antlr, you can always catch their occurrences in the `visitTerminal(TerminalNode)` method.

```java
@Override
public void visitTerminal(TerminalNode node) {
    switch (node.getText()) {
        case "catch" -> addToken(CATCH, node.getToken());
        //...
    }    
}
```

### Preventing simple attacks

The token extraction process can support the prevention of simple refactoring attacks by treating equivalent constructs the same. For example, a language may support multi-definitions:

```java
var a, b, c = 1;
```
This statement could translate to the token list `VAR_DEF VAR_DEF VAR_DEF ASSIGN`. An easy refactoring would produce a different token list:
```java
var a = 1;
var b = 1;
var c = 1;
```
Now, this looks more like `VAR_DEF ASSIGN VAR_DEF ASSIGN VAR_DEF ASSIGN`. It might be a good idea to duplicate the `ASSIGN` token in the first case as well, so that this simple attack is overcome easily. (The resulting token list may be unsorted, but that is acceptable.)

This almost goes in the direction of partly compiling the code in your mind. Another example would be the treatment of control structures, which can be refactored into each other fairly easily as well:
```java
for (<initialization>; <condition>; <incrementation>) {
    <body>;
}

// is the same as:
<initialization>
while (<condition>) {
    <body>;
    <incrementation>;
}
```
```java
<ref> = <condition> ? <valueIfTrue> : <valueIfFalse>;

// is the same as
if (<condition>) {
    <ref> = <valueIfTrue>;
} else {
    <ref> = <valueIfFalse>;    
}
```

The degree to which the Token extraction process should try to preemptively generalize over similar grammatical elements is unclear and heavily dependent on the language.   