import java.math.BigInteger;

public class FuelInjectionPerfection {
    public static int solution(String x) {
        // Use BigInteger since numbers are huge.
        BigInteger input = new BigInteger(x);

        // Declare two constants used in algorithm below.
        BigInteger one = BigInteger.valueOf(1);
        BigInteger three = BigInteger.valueOf(3);

        int iterations = 0;

        // Keep on going until we have reduced the number to one.
        while (!input.equals(one)) {

            // Convenient function to determine how often we can divide a number by two.
            int divisionsByTwo = input.getLowestSetBit();
            if (divisionsByTwo > 0) {
                // Shift number to represent multiple divisions by two.
                input = input.shiftRight(divisionsByTwo);
                iterations += divisionsByTwo;
            } else {
                // When no division is possible, we check which of the two next options can be
                // divided more often by two.
                BigInteger nextLarger = input.add(one);
                BigInteger nextSmaller = input.subtract(one);

                // Guard against the special case three, for which it is better to subtract.
                if (nextLarger.getLowestSetBit() > nextSmaller.getLowestSetBit() && !input.equals(three)) {
                    input = nextLarger;
                } else {
                    input = nextSmaller;
                }
                ++iterations;
            }
        }

        return iterations;
    }

    public static void main(String[] args) {
        int firstTestCaseResult = solution("4");
        int secondTestCaseResult = solution("15");
    }
}