# Token Selection

Apart from extracting the tokens correctly, the task of deciding which syntactical elements should be assigned a token is the essential part when designing a frontend.<br>
This guideline is solely based on experience and intuition – this "worked well" so far. More research might hint towards a more systematic process of token selection.

The goal of the abstraction is to create a token list that is
 - _accurate_: a fair representation of the code as input to the comparison algorithm
 - _consistent per se_: insensitive to small changes in the code that might obfuscate plagiarism; constructs are represented equally throughout the file
 - _consistent_ with the output of other trusted frontends—only to the extent that their respective languages are comparable, naturally. 

To create a set of tokens in line with these objectives, we offer the tips below.

### Quick Word on Notation

Elements with `BIG_AND_FAT` text represent tokens, while elements in lowercase surrounded by `<angle-brackets>` represent subexpressions that may produce any number of tokens themselves.<br>
? marks optional parts which may occur zero or one times, * marks elements that may occur any number of times.
<hr>

1) Use a separate token for both ends of every type of _block_ or _body_.


2) More generally, for any type of composite expression or statement, the number of designated token types needed to separate them in the token list is the number of subexpressions + 1. 
Additional tokens may make be needed in certain locations, like optional parts.

| Expression type    | #expressions | #tokens | Example code and tokenization pattern                                                                                                                               |
|--------------------|--------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| _loop_ (Rust)      | 1            | 2       | `loop { println!("{}", 1) }`<p></p>`LOOP{` `<statements>` `}LOOP`                                                                                                           |
| _if_ (C)           | 2            | 3       | `if (true) { printf("1"); } `<p></p>`IF` `<condition>` `IF{` `<statements>` `}IF`                                                                                     |
| _do-while_ (C)     | 2            | 3       | `do { printf("1") } while (true);`<p></p>`DO{` `<statements>` `}DO-WHILE(` `<condition>` `)WHILE`<br>alt.: `DO{` `<statements>` `}DO` `WHILE(` `<condition>` `)WHILE` |
| Ternary – _?:_ (C) | 3            | 4       | `true ? 1 : 0`<p></p>`COND(` `<condition>` `IF_TRUE:` `<expression>` `IF_FALSE:` `<expression>` `)COND`                                                             |

In the do-while example above, the `}DO-WHILE(` marks the end of the loop block and the beginning of the condition expression at once. For the sake of having a designated token for the ending of the loop block, a possible alternative tokenization with an extra token is given.     

3) For _list_ subtrees, a single token to mark the beginning of each element may suffice.<br>
Note: If lists of the same type are nested, the end of the inner list may become unclear. Additional tokens for both ends of the list may be appropriate in that case. 


4) For _leaf_ subtrees (that do not subdivide further), a single token may suffice.


5) For _optional_ subtrees, a single token may suffice to indicate that it occurred. 

| Optional expression type                   | #expressions | #tokens | Example code and tokenization pattern                                                                                                    |
|--------------------------------------------|--------------|---------|------------------------------------------------------------------------------------------------------------------------------------------|
| Class declaration: generic type parameters | _n_ + 1      | _n_ + 2 | `class Map<K,V> { ... }`<p></p>`CLASS` (`TYPE_PARAM`)* `CLASS{` `<body>` `}CLASS`                                                        |
| Method invocation: arguments               | _n_          | _n_ + 1 | `printf("%d: %s", 1, "one");`<p></p>`APPLY` (`ARG` `<expression>`)*                                                                      |
| _if_ statement: _else_ block               | 2 (+ 1)      | 3 (+ 2) | `if (true) { printf("1"); } else { printf("0"); }`<p></p>`IF` `<condition>` `IF{` `<statements>` `}IF` (`ELSE{` `<statements>` `}ELSE`)? |


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


9) Regarding sensitivity: Very similar constructs may receive the same token even if they are syntactically distinct, for example
    - variable and constant declarations
    - method and function declarations
    - different variations of `for`-loops
    - (See the documentation about token extraction for more examples)