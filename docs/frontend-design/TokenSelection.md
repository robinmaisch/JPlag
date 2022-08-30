# Token Selection

Apart from extracting the tokens correctly, the task of deciding which syntactical elements should be assigned a token is the essential part when designing a frontend.<br>
This guideline is solely based on experience and intuition – this "worked well" so far. More research might hint towards a more systematic process of token selection.

The goal of the abstraction is to create a token list that is – to some degree –
 - _accurate_: a fair representation of the code as input to the comparison algorithm
 - _consistent per se_: insensitive to small changes in the code that might obfuscate plagiarism; constructs are represented equally throughout the file
 - _readable_ for humans, especially maintainers
 - _consistent_ with the output of other frontends

To create a set of tokens in line with these objectives, we offer these tips:

1) Use a separate token for the beginning and end of every type of _block_ or _body_.


2) More generally, at least `#subtrees + 1` token types are necessary for every type of composite expression or statement. Additional tokens may make the token list more readable.
```
    IF    <condition>  IF{    <statements>  }IF
    WHILE <condition>  WHILE{ <statements>  }WHILE
    DO{   <statements> }DO    WHILE(        <condition> )WHILE     // one extra token to increase readability 
```
Note the `WHILE{` while block token vs. the `WHILE(` do-while condition token.
3) For _list_ subtrees, a single token to mark the beginning of each element may suffice. Whether the beginning end of the list itself should get a token, depends.


4) For _leaf_ subtrees (that do not subdivide further), a single token may suffice.


5) For _optional_ subtrees, a single token may suffice to indicate that it occurred. 
```
    // type parameters are an optional list of leaf subtrees
    CLASS <T> <T> CLASS{ <body> }CLASS
    
    // method arguments are an optional list of arbitrarily complex subtrees
    APPLY ARG NEW() ARG APPLY ARG*
    
    // else statement is an optional block that needs two tokens
    IF <condition> IF{ <statements> }IF ELSE{ <statements> }ELSE 
```
(*This `ARG` token could belong to any of the two `APPLY` tokens before it. Additional `ARGS(` and `)ARGS` tokens could remove this ambiguity, but they would clutter the token list.)

6) Keywords that influence the control flow receive a token, for example
   - `return`, `break`, `continue`
   
   
7) Semantic information, references and concrete values are generally omitted, for example
    - identifiers
    - type information
    - `final` modifier
    - access modifiers
    - instructions to the compiler/VM: `transient`, `volatile`
    - references to classes, objects, fields, array accesses
    - numbers and other literals, as well as built-in operations
   

8) Statements with side effects generally receive a token, for example
   - constructor calls
   - declarations of variables and fields
   - assignments
   - method calls 


10) Regarding sensitivity: Very similar constructs may receive the same token even if they are syntactically distinct, for example
    - variable and constant declarations
    - method and function declarations
    - different variations of `for`-loops
    - (See the documentation about token extraction for more examples)