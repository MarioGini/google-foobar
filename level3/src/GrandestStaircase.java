public class GrandestStaircase {

    // Memoization array large enough for largest possible input.
    static int[][] memo = new int[202][202];

    public static int count(int height, int left) {

        // Check memoization.
        if (memo[height][left] != 0) {
            return memo[height][left];
        }

        // Base case: We exactly used all bricks.
        if (left == 0) {
            return 1;
        }

        // Second base case: Not enough bricks left for another step.
        if (left < height) {
            return 0;
        }

        // Either add a step of current height, or try adding a step that is larger than
        // current step.
        int value = count(height + 1, left - height) + count(height + 1, left);

        // Update memoization.
        memo[height][left] = value;
        return value;
    }

    public static int solution(int n) {
        return count(1, n) - 1;
    }

    public static void main(final String[] args) {
        int firstTestCaseResult = solution(3);
        int secondTestCaseResult = solution(200);
    }
}