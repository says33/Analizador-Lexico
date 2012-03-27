/*
   This module contains the recursive descent
   parser that uses variables.
*/

// Exception class for parser errors.
class ParserException extends Exception {
  String errStr; // describes the error

  public ParserException(String str) {
    errStr = str;
  }

  public String toString() {
    return errStr;
  }
}

class Parser {
  // These are the token types.
  final int NONE = 0;
  final int DELIMITER = 1;
  final int VARIABLE = 2;
  final int NUMBER = 3;
  final int STRING = 4;

  // These are the types of syntax errors.
  final int SYNTAX = 0;
  final int UNBALPARENS = 1;
  final int NOEXP = 2;
  final int DIVBYZERO = 3;

   // This token indicates end-of-expression.
  final String EOE = "\0";

  private String exp;   // refers to expression string
  private int expIdx;   // current index into the expression
  private String token; // holds current token
  private int tokType;  // holds token's type

  // Array for variables.
  private Object vars[] = new Object[26];

  // Parser entry point.
  public Object evaluate(String expstr) throws ParserException
  {
    Object result;
    exp = expstr;
    expIdx = 0;

    getToken();
    if(token.equals(EOE))
      handleErr(NOEXP); // no expression present

    // Parse and evaluate the expression.
    result = evalExp1();
       // System.out.println("Evalute >>>>> "+result);
    if(!token.equals(EOE)) // last token must be EOE
      handleErr(SYNTAX);

    return result;
  }

  // Process an assignment.
  private Object evalExp1() throws ParserException
  {
    Object result;
    int varIdx;
    int ttokType;
    String temptoken;

    if(tokType == VARIABLE) {
      // save old token
      temptoken = new String(token);
      ttokType = tokType;

      // Compute the index of the variable.
      varIdx = Character.toUpperCase(token.charAt(0)) - 'A';

      getToken();
      if(!token.equals("=")) {
        putBack(); // return current token
        // restore old token -- not an assignment
        token = new String(temptoken);
        tokType = ttokType;
      }
      else {
          getToken(); // get next part of exp
          result = evalExp2();
          vars[varIdx] = result;
          return result;
      }
    }

    return evalExp2();
  }

  // Add or subtract two terms.
  private Object evalExp2() throws ParserException
  {
    char op;
    Object result;
    Object partialResult;

      result = evalExp3();
      //System.out.println("Evaluacion 2   >>>"+result);

      while((op = token.charAt(0)) == '+' || op == '-') {
        getToken();
        partialResult = evalExp3();
          //System.out.println("[ resultado parcial "+partialResult+" ]");
        switch(op) {
            case '-':
                result = (Double) result - (Double) partialResult;
                break;
            case '+':
                if ((result instanceof Double) && (partialResult instanceof Double)) {
                    result = (Double) result + (Double) partialResult;
                }
                else if (result instanceof String){
                    result = (String) result + partialResult;
                }
                else if (result instanceof Double && partialResult instanceof String ){
                    result.toString();
                    String parcial = (String) partialResult;
                    result = result + parcial;
                }
                    break;
            }
        }

    return result;
  }

  // Multiply or divide two factors.
  private Object evalExp3() throws ParserException
  {
    char op;
    Object result;
    Object partialResult;

    result = evalExp4();
      //System.out.println("Evaluacion 3  >>>a"+result);


        while((op = token.charAt(0)) == '*' ||
               op == '/' || op == '%') {
          getToken();
          partialResult = evalExp4();
          switch(op) {
            case '*':
              result = (Double) result * (Double) partialResult;
              break;
            case '/':
             if((Double) partialResult == 0.0)
                handleErr(DIVBYZERO);
                   result = (Double) result / (Double) partialResult;
              break;
            case '%':
              if((Double) partialResult == 0.0)
                handleErr(DIVBYZERO);
               result = (Double) result % (Double) partialResult;;
              break;
          }
        }

    return result;
  }

 // Process an exponent.
  private Object evalExp4() throws ParserException
  {
    Object result;
    Object partialResult;
    Object ex;
    int t;

    result = evalExp5();
        if(token.equals("^")) {
          getToken();
          partialResult = evalExp4();
            ex = result;
          if((Double) partialResult == 0.0) {
            result= 1.0;
          } else
            for(t=((Double) partialResult).intValue()-1; t > 0; t--)
                result = (Double) result * (Double) ex;
        }
    return result;
  }

  //Evaluate a unary + or -.
  private Object evalExp5() throws ParserException
  {
    Object result;
    String  op;

    op = "";

        if((tokType == DELIMITER) &&
            token.equals("+") || token.equals("-")) {
          op = token;
          getToken();
        }
        result = evalExp6();


            if(op.equals("-")) result= -(Double) result;



    return result;
  }

  // Process a parenthesized expression.
  private Object evalExp6() throws ParserException
  {
    Object result;

    if(token.equals("(")) {
      getToken();
      result = evalExp2();
      if(!token.equals(")"))
        handleErr(UNBALPARENS);
      getToken();
    }
    else result = atom();

    return result;
  }

  // Get the value of a number or variable.
  private Object atom() throws ParserException
  {
    Object result = null;

    switch(tokType) {
      case NUMBER:
        try {
          result = Double.parseDouble(token);
        } catch (NumberFormatException exc) {
          handleErr(SYNTAX);
        }
        getToken();
        break;
      case VARIABLE:
        result = findVar(token);
        getToken();
        break;
      case STRING:
          result = token;
          getToken();
        break;
      default:
        handleErr(SYNTAX);
        break;
    }
    return result;
  }

   // Return the value of a variable.
  private Object findVar(String vname) throws ParserException
  {
    if(!Character.isLetter(vname.charAt(0))){
      handleErr(SYNTAX);
      return 0.0;
    }
    return vars[Character.toUpperCase(vname.charAt(0))-'A'];
  }

  // Return a token to the input stream.
  private void putBack()
  {
    if(token == EOE) return;
    for(int i=0; i < token.length(); i++) expIdx--;
  }

  // Handle an error.
  private void handleErr(int error) throws ParserException
  {
    String[] err = {
      "Syntax Error",
      "Unbalanced Parentheses",
      "No Expression Present",
      "Division by Zero"
    };

    throw new ParserException(err[error]);
  }

  // Obtain the next token.
  private void getToken()
  {
    tokType = NONE;
    token = "";

    // Check for end of expression.
    if(expIdx == exp.length()) {
      token = EOE;
      return;
    }

    // Skip over white space.
    while(expIdx < exp.length() &&
      Character.isWhitespace(exp.charAt(expIdx))) ++expIdx;

    // Trailing whitespace ends expression.
    if(expIdx == exp.length()) {
      token = EOE;
      return;
    }

    if(isDelim(exp.charAt(expIdx))) { // is operator
      token += exp.charAt(expIdx);
      expIdx++;
      tokType = DELIMITER;
    }
    else if(Character.isLetter(exp.charAt(expIdx))) { // is variable
      while(!isDelim(exp.charAt(expIdx))) {
        token += exp.charAt(expIdx);
        expIdx++;
        if(expIdx >= exp.length()) break;
      }
      tokType = VARIABLE;
    }
    else if (exp.charAt(expIdx) == '"'){   // is String
       expIdx++;
        while (exp.charAt(expIdx) != '"'){
            token += exp.charAt(expIdx);
            expIdx++;
            if (expIdx >= exp.length()) break;
        }
        expIdx++;
        tokType = STRING;
    }
    else if(Character.isDigit(exp.charAt(expIdx))) { // is number
        while(!isDelim(exp.charAt(expIdx))) {
          token += exp.charAt(expIdx);
          expIdx++;
          if(expIdx >= exp.length()) break;

      }
      tokType = NUMBER;
    }
    else { // unknown character terminates expression
      token = EOE;
      return;
    }
  }

  // Return true if c is a delimiter.
  private boolean isDelim(char c)
  {
    if((" +-/*%^=()".indexOf(c) != -1))
      return true;
    return false;
  }

}

