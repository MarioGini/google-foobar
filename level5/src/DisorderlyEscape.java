import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class DisorderlyEscape {
    private static BigInteger getFactorial(int number) {
        BigInteger factorial = BigInteger.valueOf(1);
        for (int i = 1; i <= number; ++i)
            factorial = factorial.multiply(BigInteger.valueOf(i));
        return factorial;
    }

    private static int gcd(int first, int second) {
        return BigInteger.valueOf(first).gcd(BigInteger.valueOf(second)).intValue();
    }

    // Describes a single term of a polynomial. E.g. [3, [2,0,5]] represents
    // 3*x_1^2*x_3^5.
    private static class Term {
        BigInteger coefficient;
        BigInteger[] exponent;

        Term(BigInteger coefficient, BigInteger[] exponent) {
            this.coefficient = coefficient;
            this.exponent = exponent;
        }

        public ArrayList<Term> multiply(ArrayList<Term> polynomial) {
            ArrayList<Term> result = new ArrayList<Term>();

            // Handle special case since we use empty list to represent "1".
            if (polynomial.size() == 0) {
                result.add(this);
                return result;
            }

            // Multiply termwise and add all elements.
            for (int i = 0; i < polynomial.size(); ++i) {
                BigInteger coefficient = this.coefficient.multiply(polynomial.get(i).coefficient);
                BigInteger[] exponent = Arrays.copyOf(this.exponent, this.exponent.length);
                for (int j = 0; j < polynomial.get(i).exponent.length; ++j) {
                    exponent[j] = exponent[j].add(polynomial.get(i).exponent[j]);
                }
                result.add(new Term(coefficient, exponent));
            }

            return result;
        }
    }

    // Simplifies the fractions of cycle index polynomials. Only makes sense for
    // "normalized" cycle index polynomials.
    private static void simplifyPolynomial(ArrayList<Term> polynomial) {
        // Handle again special case of "1".
        BigInteger mainDivisor = BigInteger.valueOf(1);
        if (polynomial.size() > 0) {
            mainDivisor = polynomial.get(0).coefficient;
        }

        for (int i = 0; i < polynomial.size(); ++i) {
            // Go through terms and check for terms with same exponent.
            BigInteger[] exponent = polynomial.get(i).exponent;
            TreeSet<Integer> toBeSimplified = new TreeSet<Integer>();
            for (int j = i + 1; j < polynomial.size(); ++j) {
                BigInteger[] otherExponent = polynomial.get(j).exponent;
                if (Arrays.equals(exponent, otherExponent)) {
                    toBeSimplified.add(j);
                }
            }
            // Sum the fractions up and remove the simplified terms.
            BigInteger coefficientSum = mainDivisor.divide(polynomial.get(i).coefficient);
            for (Integer idx : toBeSimplified.descendingSet()) {
                coefficientSum = coefficientSum.add(mainDivisor.divide(polynomial.get(idx).coefficient));
                polynomial.remove(idx.intValue());
            }
            polynomial.get(i).coefficient = mainDivisor.divide(coefficientSum);
        }
    }

    // Sums up the terms with identical exponent of a polynomial.
    private static void sumUpTerms(ArrayList<Term> polynomial) {
        // Go through array list and add coefficients for same numbers
        for (int i = 0; i < polynomial.size(); ++i) {
            BigInteger[] exponent = polynomial.get(i).exponent;
            for (int j = i + 1; j < polynomial.size(); ++j) {
                BigInteger[] otherExponent = polynomial.get(j).exponent;
                if (Arrays.equals(exponent, otherExponent)) {
                    polynomial.get(i).coefficient = polynomial.get(i).coefficient.add(polynomial.get(j).coefficient);
                    polynomial.remove(j);
                }
            }
        }
    }

    // Use the recurrence relation for symmetric group cycle indices to compute them
    // up to the maximum input which is 12.
    private static void precompute() {
        // Base case is "1", represent it using empty list.
        cycleIndices.add(new ArrayList<Term>());

        for (int i = 1; i <= 12; ++i) {
            ArrayList<Term> polynomial = new ArrayList<Term>();
            for (int j = 1; j <= i; ++j) {
                BigInteger[] exponent = new BigInteger[i];
                Arrays.fill(exponent, BigInteger.ZERO);
                exponent[j - 1] = BigInteger.valueOf(1);
                Term term = new Term(BigInteger.valueOf(i), exponent);
                polynomial.addAll(term.multiply(cycleIndices.get(i - j)));
            }

            // The resulting polynomial will have many terms with the same exponent, we
            // simplify the fractions.
            simplifyPolynomial(polynomial);
            cycleIndices.add(polynomial);
        }
    }

    // Denormalizes the cycle index polynomials.
    private static void denormalize() {
        for (int i = 1; i <= 12; ++i) {
            BigInteger mainDivisor = cycleIndices.get(i).get(0).coefficient;
            for (int j = 0; j < cycleIndices.get(i).size(); ++j) {
                cycleIndices.get(i).get(j).coefficient = mainDivisor.divide(cycleIndices.get(i).get(j).coefficient);
            }
        }
    }

    // Multiplies two terms of cycle index polynomials for computing the direct
    // product. Inspired by "Cycle index of direct product of permutation groups and
    // number of equivalence classes of subsets of Zv"
    private static Term multiply(Term first, Term second) {
        // Coefficient is simply multiplied.
        BigInteger coefficient = first.coefficient.multiply(second.coefficient);

        BigInteger[] exponent = new BigInteger[first.exponent.length * second.exponent.length];
        Arrays.fill(exponent, BigInteger.ZERO);
        for (int i = 0; i < first.exponent.length; ++i) {
            for (int j = 0; j < second.exponent.length; ++j) {
                // Contribution of each term is added at respective index due to nontrivial
                // maths.
                BigInteger contribution = first.exponent[i].multiply(second.exponent[j])
                        .multiply(BigInteger.valueOf(gcd(i + 1, j + 1)));
                int idx = (i + 1) * (j + 1) / gcd(i + 1, j + 1) - 1;
                exponent[idx] = exponent[idx].add(contribution);
            }
        }

        return new Term(coefficient, exponent);
    }

    // Computes cycle index of direct product of two symmetric groups in cycle index
    // representation.
    private static ArrayList<Term> multiplyPolynomials(ArrayList<Term> first, ArrayList<Term> second) {
        ArrayList<Term> result = new ArrayList<Term>();

        // The cycle index is computed by multiplying all coefficients with each other.
        for (int i = 0; i < first.size(); ++i) {
            for (int j = 0; j < second.size(); ++j) {
                result.add(multiply(first.get(i), second.get(j)));
            }
        }

        // Sum up terms with same exponent for simplification.
        sumUpTerms(result);
        return result;
    }

    private static BigInteger evaluate(ArrayList<Term> polynomial, int value) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < polynomial.size(); ++i) {
            int summedExponents = 0;
            for (int j = 0; j < polynomial.get(i).exponent.length; ++j) {
                summedExponents += polynomial.get(i).exponent[j].intValue();
            }
            BigInteger term = polynomial.get(i).coefficient.multiply(BigInteger.valueOf(value).pow(summedExponents));
            result = result.add(term);
        }

        return result;
    }

    static ArrayList<ArrayList<Term>> cycleIndices = new ArrayList<ArrayList<Term>>();

    public static String solution(int w, int h, int s) {
        // We only need to compute the symmetric group cycle indices once.
        if (cycleIndices.isEmpty()) {
            precompute();
            denormalize();
        }

        // This is a use of Burnside's lemma.
        ArrayList<Term> cycleIdxProduct = multiplyPolynomials(cycleIndices.get(w), cycleIndices.get(h));
        BigInteger result = evaluate(cycleIdxProduct, s);
        result = result.divide(getFactorial(w).multiply(getFactorial(h)));

        return result.toString();
    }

    public static void main(String[] args) {
        String firstTestCaseResult = solution(2, 2, 2);
        String secondTestCaseResult = solution(2, 3, 4);
    }
}
