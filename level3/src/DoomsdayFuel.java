import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DoomsdayFuel {
    private static class Fraction {
        int numerator;
        int denominator;

        // Stores a fraction in simplified form.
        Fraction(int nom, int denom) {
            this.numerator = nom;
            this.denominator = denom;

            simplify();
        }

        // Finds greatest common denominator and simplifies fraction.
        void simplify() {
            if (numerator == 0) {
                denominator = 1;
                return;
            }
            int gcd = BigInteger.valueOf(numerator).gcd(BigInteger.valueOf(denominator)).intValue();
            numerator /= gcd;
            denominator /= gcd;
        }

        public Fraction multiply(Fraction other) {
            return new Fraction(this.numerator * other.numerator, this.denominator * other.denominator);
        }

        public Fraction divide(Fraction other) {
            return new Fraction(this.numerator * other.denominator, this.denominator * other.numerator);
        }

        public Fraction add(Fraction other) {
            return new Fraction(this.numerator * other.denominator + other.numerator * this.denominator,
                    this.denominator * other.denominator);
        }
    }

    private static class Matrix {
        Fraction[][] data;

        Matrix(Fraction[][] input) {
            this.data = input;
        }

        // Getter function for a specific row of the matrix.
        public Fraction[] getRow(int idx) {
            return data[idx];
        }

        // Matrix is divided by dividing each element with the fraction.
        public Matrix divideByFactor(Fraction factor) {
            Fraction[][] newData = new Fraction[this.data.length][this.data[0].length];

            for (int i = 0; i < this.data.length; ++i) {
                for (int j = 0; j < this.data[0].length; ++j) {
                    newData[i][j] = this.data[i][j].divide(factor);
                }
            }

            return new Matrix(newData);
        }

        // Implements standard matrix multiplication.
        public Matrix multiply(Matrix other) {
            if (this.data[0].length != other.data.length) {
                return null;
            }

            Fraction[][] result = new Fraction[this.data.length][other.data[0].length];

            for (int i = 0; i < this.data.length; ++i) {
                for (int j = 0; j < other.data[0].length; ++j) {
                    Fraction multiplied = new Fraction(0, 1);
                    for (int k = 0; k < this.data[0].length; ++k) {
                        multiplied = multiplied.add(this.data[i][k].multiply(other.data[k][j]));
                    }
                    result[i][j] = multiplied;
                }
            }

            return new Matrix(result);
        }

        // Inverts matrix by dividing adjoint matrix by the determinant.
        public Matrix invert() {
            if (this.data.length != this.data[0].length) {
                return null;
            }

            Fraction determinant = this.getDeterminant();
            Matrix adjoint = this.getAdjoint();

            return adjoint.divideByFactor(determinant);
        }

        public Matrix getAdjoint() {
            Fraction[][] cofactors = new Fraction[this.data.length][this.data.length];

            // Get cofactors
            for (int i = 0; i < this.data.length; ++i) {
                for (int j = 0; j < this.data.length; ++j) {
                    cofactors[i][j] = this.getSubMatrix(i, j).getDeterminant();
                    if ((i + j) % 2 != 0) {
                        cofactors[i][j] = cofactors[i][j].multiply(new Fraction(-1, 1));
                    }
                }
            }

            // Transpose cofactors
            Fraction[][] adjointData = new Fraction[this.data.length][this.data.length];
            for (int i = 0; i < this.data.length; ++i) {
                for (int j = 0; j < this.data.length; ++j) {
                    adjointData[i][j] = cofactors[j][i];
                }
            }

            return new Matrix(adjointData);
        }

        // Excludes indices as required and returns submatrix
        public Matrix getSubMatrix(int rowExclude, int columnExclude) {
            Fraction[][] subData = new Fraction[this.data.length - 1][this.data.length - 1];
            int row = 0;
            int column = 0;
            for (int i = 0; i < this.data.length; ++i) {
                for (int j = 0; j < this.data.length; ++j) {
                    if (i != rowExclude && j != columnExclude) {
                        subData[row][column++] = this.data[i][j];
                        if (column == this.data.length - 1) {
                            column = 0;
                            row++;
                        }
                    }
                }
            }

            return new Matrix(subData);
        }

        public Fraction getDeterminant() {
            if (this.data.length == 1) {
                return this.data[0][0];
            }
            Fraction determinant = new Fraction(0, 1);
            Fraction sign = new Fraction(1, 1);
            for (int i = 0; i < this.data.length; ++i) {
                determinant = determinant
                        .add(sign.multiply(data[0][i]).multiply(this.getSubMatrix(0, i).getDeterminant()));
                sign = sign.multiply(new Fraction(-1, 1));
            }

            return determinant;
        }
    }

    // Finds the non terminal dynamics of the absorbing markov chain system.
    static HashMap<String, Matrix> getNonTerminalDynamics(int[][] input) {
        // Get list that maps new ordering
        List<Integer> nonTerminalIdx = new ArrayList<Integer>();
        for (int i = 0; i < input.length; ++i) {
            for (int j = 0; j < input.length; ++j) {
                if (input[i][j] != 0) {
                    nonTerminalIdx.add(i);
                    break;
                }
            }
        }
        List<Integer> reorderedStates = new ArrayList<Integer>();
        int nonTermSize = nonTerminalIdx.size();
        int termSize = input.length - nonTermSize;
        for (int i = 0; i < input.length; ++i) {
            if (!nonTerminalIdx.contains(i)) {
                reorderedStates.add(i);
            }
        }
        reorderedStates.addAll(nonTerminalIdx);

        // Reorder states, first the terminating ones.
        int[][] reordered = new int[nonTermSize][input.length];
        int[] sums = new int[nonTermSize];
        for (int i = termSize; i < input.length; ++i) {
            for (int j = 0; j < input.length; ++j) {
                reordered[i - termSize][j] = input[reorderedStates.get(i)][reorderedStates.get(j)];
                sums[i - termSize] += reordered[i - termSize][j];
            }
        }

        // Extract (I-Q) and R matrix by switching to fractions.
        Fraction[][] IQ = new Fraction[nonTermSize][nonTermSize];
        Fraction[][] R = new Fraction[nonTermSize][termSize];
        for (int i = 0; i < nonTermSize; ++i) {
            for (int j = termSize; j < input.length; ++j) {
                if (i == j - termSize) {
                    IQ[i][j - termSize] = new Fraction(-reordered[i][j] + sums[i], sums[i]);
                } else {
                    IQ[i][j - termSize] = new Fraction(-reordered[i][j], sums[i]);
                }
            }
            for (int j = 0; j < termSize; ++j) {
                R[i][j] = new Fraction(reordered[i][j], sums[i]);
            }
        }

        HashMap<String, Matrix> result = new HashMap<String, Matrix>();
        result.put("IQ", new Matrix(IQ));
        result.put("R", new Matrix(R));

        return result;
    }

    // Computes least common multiple of all denominators in the input sequence.
    public static int getLCM(Fraction[] sequence) {
        int lcm = sequence[0].denominator;
        for (int i = 1; i < sequence.length; ++i) {
            lcm = (lcm * sequence[i].denominator)
                    / (BigInteger.valueOf(lcm).gcd(BigInteger.valueOf(sequence[i].denominator)).intValue());
        }
        return lcm;
    }

    // Checks whether already the first row is a terminating state.
    public static Boolean firstStateTerminating(int[] firstRow) {
        int sum = 0;
        for (int i = 0; i < firstRow.length; ++i) {
            sum += firstRow[i];
        }
        return firstRow[0] == sum;
    }

    public static int[] solution(int[][] input) {
        // Handle special case: First state is already a terminating state.
        if (firstStateTerminating(input[0])) {
            int[] result = new int[input.length + 1];
            result[0] = 1;
            result[input.length] = 1;

            return result;
        }

        HashMap<String, Matrix> dynamics = getNonTerminalDynamics(input);
        Matrix R = dynamics.get("R");
        Matrix IQ = dynamics.get("IQ");
        Matrix F = IQ.invert();

        // Get first row of FR matrix and adapt result to required output format
        Fraction[] firstRow = F.multiply(R).getRow(0);
        int newDenom = getLCM(firstRow);
        int[] result = new int[firstRow.length + 1];
        result[firstRow.length] = newDenom;
        for (int i = 0; i < firstRow.length; ++i) {
            result[i] = firstRow[i].numerator * (newDenom / firstRow[i].denominator);
        }

        return result;
    }

    public static void main(final String[] args) {
        int[] firstTestCaseResult = solution(new int[][] { { 0, 2, 1, 0, 0 }, { 0, 0, 0, 3, 4 }, { 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 } });
        int[] secondTestCaseResult = solution(new int[][] { { 0, 1, 0, 0, 0, 1 }, { 4, 0, 0, 3, 2, 0 },
                { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } });
    }
}
