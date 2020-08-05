import java.io.*;
import java.util.*;

public class CalculatorTest {
    public static void main(String[] args) {
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
            printPostfix(postfix); // 계산 중에 오류가 발생하면 ERROR만 출력되게 postfix 출력을 미룬다.
            System.out.println(result);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("ERROR");
//            e.printStackTrace();
        }
    }

    // Warning: 마지막에 스페이스 조심
    private static void printPostfix(List<Token> postfix) throws IOException {
        StringJoiner sj = new StringJoiner(" ");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        for (Token tok : postfix) {
            sj.add(tok.toString());
        }
        bw.write(sj.toString());
        bw.write("\n"); // bw.newLine하면 안 좋다고 한다.
        bw.flush();
    }

    enum Operation {
        ADD,
        SUB, // binary -
        MUL,
        DIV,
        IDIV, // %
        POW,
        LPAR, // (
        RPAR, // )
        NEG // unary -
    }

    // 숫자가 클수록 우선순위가 높다.
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

    // 동일 우선순위 연산자가 연속될 때 오른쪽부터 계산하는 연산자인가
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
                final Operator opTok = (Operator) tok; // 캐시
                if (/*prevTok == null ||*/
//                        prevTok instanceof Operator ||
                        opTok.operation == Operation.NEG) {
                    // unary operator인함수나 ~는 그냥 푸시. 여기는 ~밖에 없으므로 그냥 푸시
                    operatorStack.push(opTok);
                }
//                else if (operatorStack.isEmpty()) {
//                    operatorStack.push(opTok);
//                }
                else if (opTok.operation == Operation.LPAR) {
                    operatorStack.push(opTok);
                } else if (opTok.operation == Operation.RPAR) {
                    if (operatorStack.isEmpty())
                        throw new IllegalArgumentException("Not matched parenthesis");
                    // LPAR가 나올 때까지 팝
                    while (true) {
                        Operator pp = operatorStack.pop();
                        if (pp.operation != Operation.LPAR) {
                            postfix.add(pp);
                            if (operatorStack.isEmpty()) {
                                throw new IllegalArgumentException("Not matched parenthesis");
                            }
                        } else
                            break;
                    }
                } else { // normal operator
                    Operator topOp;
                    // 우선순위와 associativity 에 따라 연산자를 적당한 위치까지 푸시하며
                    // 그 과정의 연산자를 팝하여 postfix에 넣는다
                    while (!operatorStack.isEmpty() && (topOp = operatorStack.peek()) != null &&
                            topOp.operation != Operation.LPAR &&
                            (
//                                    topOp.operation == Operation.NEG ||
                                    topOp.compareTo(opTok) > 0 ||
                                            (topOp.compareTo(opTok) == 0 && !isRightAssociative(opTok.operation))
                            )) {
                        topOp = operatorStack.pop();
                        postfix.add(topOp);
                    }
                    operatorStack.push(opTok);
                }

            } else/* if ( tok instanceof TokenValue)*/ { // 값은 그냥 넣는다
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
        while (!operatorStack.isEmpty()) { // 남아 있는 연산자를 팝하여 postfix에 넣는다.
            Operator op = operatorStack.pop();
            if (op.operation == Operation.LPAR || op.operation == Operation.RPAR)
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
            if (tok instanceof TokenValue) { // 숫자일 경우
                operands.push(tok); // 데이터를 푸시
            } else /*if (tok instanceof Operator)*/ { // 연산자일 경우
                try {
                    Token t = new TokenValue(((Operator) tok).calc(operands)); // 계산하여 결과를 푸시
                    operands.push(t);
                } catch (EmptyStackException e) {
                    throw new IllegalArgumentException("Bad expression.");
                } catch (Exception e) { // 산술 오류 등이 일어났다.
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return ((TokenValue) (operands.pop())).value;
    }

    // 람다식 맵과 인터페이스를 이용한 실제 연산 구현
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

    // 최적화를 하려 했더니 오버플로우 시 정답지와 답이 달라졌다.
    static long pow(long a, long b) {
        if (a == 0) {
            if (b != 0) {
                if (b < 0) {
                    throw new IllegalArgumentException("Illegal argument");
                }  //return 0;
            }  //return 1;
        }
        //if (b == 0) return 1;
        //if (b == 1) return a;
        return (long) Math.pow(a, b);
//        if ((b & 1) == 0) return pow(a * a, b / 2);
//        else return a * pow(a * a, b / 2);
    }

    // Tokens. 껍데기 인터페이스이다.
    private interface Token {
    }

    // 연산자를 나타내는 토큰
    private static class Operator implements Token, Comparable<Operator> {
        Operation operation; // 실제 연산자의 종류를 추상화

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
                    return "("; // 실제로 나올 일은 없다
                case RPAR:
                    return ")"; // 실제로 나올 일은 없다
                case NEG:
                    return "~";
            }
            return "";
        }
    }

    // 값을 표현하는 토큰
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
        private final char[] chars;
        private Token prevToken;

        public StringParser(String s) {
            // chars = s.replaceAll("\\s", "").toCharArray();
            chars = s.toCharArray();
        }

        // Remove spaces, handle numbers, handle unary minus
        public Token getToken() {
            if (i >= chars.length)
                return null;
            //skip whitespace
            while (i < chars.length && Character.isWhitespace(chars[i]))
                i++;
            // 숫자 추출 먼저
            int s = i;
            while (i < chars.length && Character.isDigit(chars[i])) {
                i++;
            }
            if (s != i) {
                if (prevToken instanceof TokenValue) { // 숫자 스페이스 숫자
                    throw new IllegalArgumentException("Value space Value");
                }
                prevToken = makeLong(chars, s, i);
                return prevToken;
            }
            // 숫자가 아니었다.  연산자여야 함
            Operation op = ch2op.get(chars[i]);
            if (op == null) { // 이상한 연산자
                throw new IllegalArgumentException("Invalid token:" + chars[i]);
            }
            // Unary minus 처리
            if (op == Operation.SUB) {
                if (prevToken == null || !(prevToken instanceof TokenValue ||
                        (prevToken instanceof Operator && ((Operator) prevToken).operation == Operation.RPAR))) {
                    op = Operation.NEG;
                }
//                else if (i == 0 || (i > 0 && !(Character.isDigit(chars[i - 1]) || chars[i - 1] == ')')))
//                    op = Operation.NEG;
            }
            i++;
            prevToken = new Operator(op);
            return prevToken;
        }


        // abc1234bd의 경우
        // s = 3, to= 7일 때 1234가 나옴
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
