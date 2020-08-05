import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BigInteger {
    public static final String QUIT_COMMAND = "quit";
    public static final String MSG_INVALID_INPUT = "입력이 잘못되었습니다.";

    // implement this
    //(0개 이상의 공백) (부호 0~1개) (0개 이상의 공백) (숫자) (0개 이상의 공백) (+/-/*) (0개 이상의 공백) (부호 0~1개) (0개 이상의 공백) (숫자) (0개 이상의 공백)
    public static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\s*([+\\-]?\\s*\\d+)\\s*([+\\-*])\\s*([+-]?\\s*\\d+)\\s*");

    boolean negative;
    byte[] numeric;

    public BigInteger(String s) {
        s = s.trim();
        char firstCh = s.charAt(0);
        negative = false;
        if (!Character.isDigit(firstCh)) { // Sign detected
            negative = firstCh == '-';
            s = s.substring(1); // remove sign
        }
        // trim out spaces
        s = s.trim();
        int len = s.length();
        numeric = new byte[len];
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            numeric[i] = (byte) (ch - '0');
        }
    }

    private BigInteger(byte[] values, boolean negative) {
        numeric = values;
        this.negative = negative;
    }

    public BigInteger add(BigInteger big) {
        if (big.negative == negative) { // sign equals
            return addInternal(big);
        } else {
            return subInternal(big);
        }
    }

    public BigInteger subtract(BigInteger big) {
        if (big.negative == negative) {
            return subInternal(big);
        } else {
            return addInternal(big);
        }
    }

    public BigInteger multiply(BigInteger big) {
        boolean newNegative = this.negative != big.negative;
        final byte[] numeric1 = numeric;
        final byte[] numeric2 = big.numeric;
        final int len1 = numeric1.length;
        final int len2 = numeric2.length;
        final int newLen = len1 + len2;
        byte[] newArray = new byte[newLen];
        int persistCarry = 0;
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                int val = numeric1[i] * numeric2[j];
                int realVal = val % 10;
                int carry = val / 10;
                newArray[i + j + 1] += realVal;
                newArray[i + j] += carry;
                newArray[i + j] += newArray[i + j + 1] / 10;
                newArray[i + j + 1] %= 10;
            }
        }
        // Clear carries
        for (int i = newArray.length - 1; i >= 1; i--) {
            int val = newArray[i];
            newArray[i - 1] += val / 10;
            newArray[i] = (byte) (val % 10);
        }
        return new BigInteger(newArray, newNegative).trim();
    }

    //add absolute values, preserve sign of this
    private BigInteger addInternal(BigInteger big) {
        final byte[] numeric1 = numeric;
        final byte[] numeric2 = big.numeric;
        final byte[] smaller, larger;
        final int len1 = numeric1.length;
        final int len2 = numeric2.length;
        final int maxLen;
        if (len1 > len2) {
            smaller = numeric2;
            larger = numeric1;
            maxLen = len1;
        } else {
            smaller = numeric1;
            larger = numeric2;
            maxLen = len2;
        }
        byte[] newArray = new byte[maxLen + 1];
        System.arraycopy(smaller, 0, newArray, newArray.length - smaller.length, smaller.length);
        int carry = 0;
        for (int i = 0; i < maxLen; i++) {
            int val1 = newArray[newArray.length - i - 1];
            int val2 = larger[larger.length - i - 1] + carry;
            int res = val1 + val2;
            if (res >= 10) {
                res -= 10;
                carry = 1;
            } else {
                carry = 0;
            }
            newArray[newArray.length - i - 1] = (byte) res;
        }
        if (carry == 1) {
            newArray[0] = 1;
            return new BigInteger(newArray, negative);
        }
        return new BigInteger(newArray, negative).trim();
    }

    //sub absolute values. preserve sign of this
    private BigInteger subInternal(BigInteger big) {
        boolean shouldInvert = !isAbsLargerThan(big);
        final byte[] numeric1 = numeric;
        final byte[] numeric2 = big.numeric;
        final byte[] smaller, larger;
        final int len1 = numeric1.length;
        final int len2 = numeric2.length;
        final int maxLen;
        final int minLen;
        if (shouldInvert) {
            maxLen = len2;
            minLen = len1;
            smaller = numeric1;
            larger = numeric2;
        } else {
            maxLen = len1;
            minLen = len2;
            smaller = numeric2;
            larger = numeric1;
        }
        byte[] newArray = new byte[maxLen];
        System.arraycopy(larger, 0, newArray, 0, larger.length);
        int carry = 0;
//        System.out.println("Sub from " + Arrays.toString(newArray) + " :" + Arrays.toString(smaller));
        for (int i = 0; i < minLen; i++) {
            int val1 = newArray[newArray.length - 1 - i];
            int val2 = smaller[smaller.length - 1 - i] + carry;
            int res = val1 - val2;
            if (res < 0) {
                carry = 1;
                res += 10;
            } else {
                carry = 0;
            }
            newArray[newArray.length - 1 - i] = (byte) res;
        }
//        System.out.println("Result=" + Arrays.toString(newArray));
//        System.out.println("Carry:" + carry);
//          int carryIndex = newArray.length - 1;
        int carryIndex = newArray.length - smaller.length - 1;
        while (carry == 1) {
//            if (carryIndex < 0) {
//                shouldInvert = !shouldInvert;
//                newArray[0] = 0;
//                break;
//            }
            int res = newArray[carryIndex] - 1;
            if (res < 0) {
                res += 10;
                carry = 1;
            } else {
                carry = 0;
            }
            newArray[carryIndex] = (byte) res;
            carryIndex--;
        }
//        System.out.println(Arrays.toString(newArray));
        boolean newNegative = negative;
        if (shouldInvert) {
            newNegative = !newNegative;
        }
        return new BigInteger(newArray, newNegative).trim();
    }


    private BigInteger trim() {
//        System.out.println("Before trim:" + this);
        int firstNon0 = 0;
        for (; firstNon0 < numeric.length - 1; firstNon0++) {
            if (numeric[firstNon0] != 0) {
                break;
            }
        }
        if (firstNon0 > 0) {
            int newlen = numeric.length - firstNon0;
            byte[] newNumeric = new byte[newlen];
            System.arraycopy(numeric, firstNon0, newNumeric, 0, newlen);
            numeric = newNumeric;
        }
//        System.out.println("After trim:" + this);
        return this;
    }

    public BigInteger(BigInteger big, boolean negative) {
        this.negative = negative;
        numeric = new byte[big.numeric.length];
        System.arraycopy(numeric, 0, big.numeric, 0, numeric.length);
    }

    public boolean isAbsLargerThan(BigInteger big) {
        if (numeric.length > big.numeric.length) {
            return true;
        } else if (numeric.length < big.numeric.length) {
            return false;
        } else {
            int i = 0;
            while (i < numeric.length) {
                if (numeric[i] > big.numeric[i])
                    return true;
                else if (numeric[i] < big.numeric[i])
                    return false;
                i++;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (negative && !(numeric.length == 1 && numeric[0] == 0)) {
            sb.append('-');
        }
        for (byte ch : numeric) {
            sb.append(ch);
//            sb.append(' ');
        }
        return sb.toString();
    }

    static BigInteger evaluate(String input) throws IllegalArgumentException {
        Matcher matcher = EXPRESSION_PATTERN.matcher(input);
        if (!matcher.find())
            throw new IllegalArgumentException("Does not match");
        String first = matcher.group(1);
        String operator = matcher.group(2);
        String second = matcher.group(3);
//        System.out.println(first);
//        System.out.println(operator);
//        System.out.println(second);
        BigInteger num1 = new BigInteger(first);
        BigInteger num2 = new BigInteger(second);
        switch (operator.charAt(0)) {
            case '+':
                return num1.add(num2);
            case '-':
                return num1.subtract(num2);
            case '*':
                return num1.multiply(num2);
            default:
                throw new IllegalArgumentException("Invalid operator " + operator);
        }
    }

    public static void main(String[] args) throws Exception {
        try (InputStreamReader isr = new InputStreamReader(System.in)) {
            try (BufferedReader reader = new BufferedReader(isr)) {
                boolean done = false;
                while (!done) {
                    String input = reader.readLine();

                    try {
                        done = processInput(input);
                    } catch (IllegalArgumentException e) {
                        System.err.println(MSG_INVALID_INPUT);
//                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static boolean processInput(String input) throws IllegalArgumentException {
        boolean quit = isQuitCmd(input);

        if (quit) {
            return true;
        } else {
            BigInteger result = evaluate(input);
            System.out.println(result.toString());

            return false;
        }
    }

    static boolean isQuitCmd(String input) {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }
}
