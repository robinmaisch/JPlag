# Frontend Test

To check the output of your frontend against the input, the `TokenPrinter` can be helpful. The `TokenPrinter` prints the input line by line, and the tokens of each line below it.

```java
10 public class Example {
   |CLASS               |CLASS{
    
11      private int number;
        |FIELD 
        
12      public int getNumber() {
        |METHOD                |METHOD{
            
13          return number;
            |RETURN
       
14      }
        |}METHOD
   
15 }
   |}CLASS
```
To test a frontend, set up a JUnit test class where the `TokenPrinter` prints the output of the `parse` method of the frontend. Read through the output and check whether the `TokenList` satisfies the given requirements.

### Test files

The frontend should be tested with 'authentic' sample code as well as a 'complete' test file that covers all syntactic elements that the frontend should take into account. If you are using an ANTLR parser, such a complete test file may be included in the parser test files in the ANTLRv4 Grammar Repository. 

### Sanity check suggestions

- The TokenList represents the input code correctly.
  - In particular, the nesting tokens are correctly nested and balanced.

- The TokenList represents the input code with an acceptable coverage—how that can be measured and what coverage is acceptable depends on the language. One approach would be line coverage, e.g. 90 percent of code lines should contain a token.

- There are no `TokenConstants` that can never be produced by the frontend for any input.
  - Put another way, the complete test code produces a TokenList that contains every type of token.