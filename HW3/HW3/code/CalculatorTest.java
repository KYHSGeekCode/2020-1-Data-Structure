import java.io.*;
import java.util.*;

public class CalculatorTest {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("q") == 0)
                    break;

                command(input);
            } catch (Exception e) {
                System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
//                e.printStackTrace();
            }
        }
    }

    private static void command(String input) {
        try {
            List<Token> postfix = toPostfix(input);
            long result = calcPostfix(postfix);
            printPostfix(postfix);
            System.out.println(result);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("ERROR");
//            e.printStackTrace();
        }
    }

    //Warning: 마지막에 스페이스 조심
    private static void printPostfix(List<Token> postfix) throws IOException {
        StringJoiner sj = new StringJoiner(" ");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        for (Token tok : postfix) {
            sj.add(tok.toString());
        }
        bw.write(sj.toString());
        bw.write("\n");
        bw.flush();
    }

    enum Operation {
        ADD,
        SUB, // binary -
        MUL,
        DIV,
        IDIV,
        POW,
        LPAR,
        RPAR,
        NEG // unary -
    }

    private static int getOperationPriority(Operation op) {
        switch (op) {
            case LPAR:
            case RPAR:
                return -0;
            case POW:
                return -1;
            case NEG:
                return -2;
            case MUL:
            case DIV:
            case IDIV:
                return -3;
            case ADD:
            case SUB:
                return -4;
        }
        return 0;
    }

    private static boolean isRightAssociative(Operation op) {
        switch (op) {
            case NEG:
            case POW:
                return true;
            default:
                return false;
        }
    }

    public static List<Token> toPostfix(String infix) {
        StringParser parser = new StringParser(infix);
        Stack<Operator> operatorStack = new Stack<>();
        List<Token> postfix = new ArrayList<>();
        Token tok;
//        Token prevTok = null;
        while ((tok = parser.getToken()) != null) {
//            System.out.println("Token:" + tok.toString());
            if (tok instanceof Operator) {
                final Operator opTok = (Operator) tok;
                if (/*prevTok == null ||*/
//                        prevTok instanceof Operator ||
                        opTok.operation == Operation.NEG) {
                    // unary
                    operatorStack.push(opTok);
                }
//                else if (operatorStack.isEmpty()) {
//                    operatorStack.push(opTok);
//                }
                else if (opTok.operation == Operation.LPAR) {
                    operatorStack.push(opTok);
                } else if (opTok.operation == Operation.RPAR) {
                    if(operatorStack.isEmpty())
                        throw new IllegalArgumentException("Not matched parenthesis");
                    while (!operatorStack.isEmpty()) {
                        Operator pp = operatorStack.pop();
                        if (pp.operation != Operation.LPAR) {
                            postfix.add(pp);
                            if(operatorStack.isEmpty()) {
                                throw new IllegalArgumentException("Not matched parenthesis");
                            }
                        } else
                            break;
                    }
                } else { // normal operator
                    Operator topOp;
                    while (!operatorStack.isEmpty() && (topOp = operatorStack.peek()) != null &&
                            topOp.operation != Operation.LPAR &&
                            (
//                                    topOp.operation == Operation.NEG ||
                                            topOp.compareTo(opTok) > 0 ||
                                            (topOp.compareTo(opTok) == 0 &&  !isRightAssociative(opTok.operation))
                            ) && !operatorStack.isEmpty()) {
                        topOp = operatorStack.pop();
                        postfix.add(topOp);
                    }
                    operatorStack.push(opTok);
                }

            } else/* if ( tok instanceof TokenValue)*/ {
                postfix.add(tok);
            }
//            prevTok = tok;
//            try {
//                printPostfix(postfix);
//                System.out.println(Arrays.toString(operatorStack.toArray()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        while (!operatorStack.isEmpty()) {
            Operator op = operatorStack.pop();
            if(op.operation == Operation.LPAR || op.operation == Operation.RPAR)
                throw new IllegalArgumentException("Not matched parenthesis");
            postfix.add(op);
        }
        return postfix;
    }

    public static long calcPostfix(List<Token> postfix) {
        if (postfix.size() == 0)
            throw new IllegalArgumentException("Please enter a expression");
        Stack<Token> operands = new Stack<>();
        for (Token tok : postfix) {
            if (tok instanceof TokenValue) {
                operands.push(tok);
            } else /*if (tok instanceof Operator)*/ {
                try {
                    Token t = new TokenValue(((Operator) tok).calc(operands));
                    operands.push(t);
                } catch (EmptyStackException e) {
                    throw new IllegalArgumentException("Bad expression.");
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return ((TokenValue) (operands.pop())).value;
    }


    private interface OperatorImpl {
        long apply(Stack<Token> operands) throws EmptyStackException;
    }

    static final Map<Operation, OperatorImpl> operators = new HashMap<>();

    static {
        operators.put(Operation.ADD, (Stack<Token> stack) -> (((TokenValue) stack.pop()).value + ((TokenValue) stack.pop()).value));
        operators.put(Operation.SUB, (Stack<Token> stack) -> (-((TokenValue) stack.pop()).value + ((TokenValue) stack.pop()).value));
        operators.put(Operation.MUL, (Stack<Token> stack) -> (((TokenValue) stack.pop()).value * ((TokenValue) stack.pop()).value));
        operators.put(Operation.DIV, (Stack<Token> stack) -> {
                    final long val1 = ((TokenValue) stack.pop()).value;
                    final long val2 = ((TokenValue) stack.pop()).value;
//                    System.out.println(val2 + "/" + val1);
                    return val2 / val1;
                }
        );
        operators.put(Operation.IDIV, (Stack<Token> stack) -> {
                    final long val1 = ((TokenValue) stack.pop()).value;
                    final long val2 = ((TokenValue) stack.pop()).value;
//                    System.out.println(val2 + "%" + val1 + "=" + val2 % val1);
                    return val2 % val1;
                }
        );
        operators.put(Operation.POW, (Stack<Token> stack) -> {
                    final long val1 = ((TokenValue) stack.pop()).value;
                    final long val2 = ((TokenValue) stack.pop()).value;
//                    System.out.println(val2 + "^" + val1 + "=" + pow(val2, val1));
                    return pow(val2, val1);
                }
        );
        operators.put(Operation.NEG, (Stack<Token> stack) -> (-(((TokenValue) stack.pop()).value)));
        operators.put(Operation.LPAR, (Stack<Token> stack) -> ((((TokenValue) stack.pop()).value)));
    }

    static long pow(long a, long b) {
        if (a == 0) {
            if (b == 0) {
                //return 1;
            } else if (b < 0) {
                throw new IllegalArgumentException("Illegal argument");
            } else {
                //return 0;
            }
        }
        //if (b == 0) return 1;
        //if (b == 1) return a;
        return (long)Math.pow(a,b);
//        if ((b & 1) == 0) return pow(a * a, b / 2);
//        else return a * pow(a * a, b / 2);
    }

    // Tokens
    private interface Token {
    }

    private static class Operator implements Token, Comparable<Operator> {
        Operation operation;

        public Operator(Operation operation) {
            this.operation = operation;
        }

        long calc(Stack<Token> operands) throws EmptyStackException {
            return operators.get(operation).apply(operands);
        }

        @Override
        public int compareTo(Operator operator) {
            return getOperationPriority(operation) - getOperationPriority(operator.operation);
        }

        @Override
        public String toString() {
            switch (operation) {
                case ADD:
                    return "+";
                case SUB:
                    return "-";
                case MUL:
                    return "*";
                case DIV:
                    return "/";
                case IDIV:
                    return "%";
                case POW:
                    return "^";
                case LPAR:
                    return "(";
                case RPAR:
                    return ")";
                case NEG:
                    return "~";
            }
            return "";
        }
    }

    private static class TokenValue implements Token {
        long value;

        public TokenValue(long v) {
            this.value = v;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }
    }

    // Parser
    private static class StringParser {
        // character to operation
        private static final Map<Character, Operation> ch2op = new HashMap<>();

        static {
            ch2op.put('+', Operation.ADD);
            ch2op.put('-', Operation.SUB);
            ch2op.put('*', Operation.MUL);
            ch2op.put('/', Operation.DIV);
            ch2op.put('%', Operation.IDIV);
            ch2op.put('^', Operation.POW);
            ch2op.put('(', Operation.LPAR);
            ch2op.put(')', Operation.RPAR);
        }

        int i = 0;
        private char[] chars;
        private Token prevToken;

        public StringParser(String s) {
            // chars = s.replaceAll("\\s", "").toCharArray();
            chars = s.toCharArray();
        }

        // Removed spaces, handles numbers, Handle andle unary minus
        public Token getToken() {
            //delimiters: space, nl, ops,
            if (i >= chars.length)
                return null;
            //skip whitespace
            while(i<chars.length && Character.isWhitespace(chars[i]))
                i++;
            // 숫자 추출 먼저
            int s = i;
            while (i < chars.length && Character.isDigit(chars[i])) {
                i++;
            }
            if (s != i) {
                if(prevToken instanceof TokenValue) {
                    throw new IllegalArgumentException("Value space Value");
                }
                prevToken = makeLong(chars, s, i);
                return prevToken;
            }
            // 숫자가 아니었다.  연산자여야 함
            Operation op = ch2op.get(chars[i]);
            if (op == null) {
                throw new IllegalArgumentException("Invalid token:" + chars[i]);
            }
            if (op == Operation.SUB) {
                if(prevToken == null || !(prevToken instanceof TokenValue ||
                        (prevToken instanceof Operator && ((Operator)prevToken).operation == Operation.RPAR))) {
                    op = Operation.NEG;
                }
//                else if (i == 0 || (i > 0 && !(Character.isDigit(chars[i - 1]) || chars[i - 1] == ')')))
//                    op = Operation.NEG;
            }
            i++;
            prevToken = new Operator(op);
            return prevToken;
        }


        // abc1234bd
        // s = 3, to= 7
        private TokenValue makeLong(char[] chars, int s, int to) {
            long total = 0;
            int multiplier = 1;
            to--;
            while (to >= s) {
                int digit = Character.getNumericValue(chars[to]);
                total += multiplier * digit;
                multiplier *= 10;
                to--;
            }
            return new TokenValue(total);
        }
    }
}
