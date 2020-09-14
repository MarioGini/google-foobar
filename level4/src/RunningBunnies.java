import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class RunningBunnies {
    // Separate class to describe the state of the saved bunnies. Separated from
    // RescueState because we need a separate hash table with a key BunnyState.
    private static class BunnyState {
        int location;
        Set<Integer> bunnies;

        BunnyState(int location, Set<Integer> bunnies) {
            this.location = location;
            this.bunnies = new TreeSet<Integer>(bunnies);
        }

        // Need to override equals and hashCode methods since we use a hash table with
        // BunnyState as a key.
        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof BunnyState)) {
                return false;
            }
            BunnyState state = (BunnyState) o;
            return location == state.location && bunnies.equals(state.bunnies);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, bunnies);
        }

    }

    private static class RescueState {
        BunnyState bunnyState;
        int timeLeft;

        RescueState(BunnyState bunnyState, int timeLeft) {
            this.bunnyState = bunnyState;
            this.timeLeft = timeLeft;
        }
    }

    // Convenience function to compare two sets which represent the bunnies.
    // According to the vague description, the set with lower ids should be chosen.
    private static Set<Integer> getSmallerIdSet(Set<Integer> first, Set<Integer> second) {
        int firstSum = first.stream().mapToInt(Integer::intValue).sum();
        int secondSum = second.stream().mapToInt(Integer::intValue).sum();

        return firstSum < secondSum ? first : second;
    }

    // Using breadth-first search (bfs) instead of depth-first search has the
    // advantage that we do not need to explicitly check for negative cycles, since
    // the bfs will not get "stuck" in the cycle, instead the cycle will lead to a
    // state where all bunnies are rescued, which is an exit condition.
    private static int[] bfs(RescueState initialState, int[][] matrix) {
        Set<Integer> result = new TreeSet<Integer>();

        Hashtable<BunnyState, Integer> bunnyMap = new Hashtable<BunnyState, Integer>();

        Queue<RescueState> queue = new LinkedList<RescueState>();
        queue.add(initialState);

        while (!queue.isEmpty()) {
            int timeLeft = queue.peek().timeLeft;
            BunnyState currentState = queue.poll().bunnyState;

            // When we are at the bulkhead with enough time left, update set of rescued
            // bunnies.
            if (currentState.location == matrix.length - 1 && timeLeft >= 0
                    && currentState.bunnies.size() >= result.size()) {
                // Check whether we found a set of bunnies with lower ids.
                if (currentState.bunnies.size() == result.size()) {
                    result = getSmallerIdSet(currentState.bunnies, result);
                } else {
                    result = currentState.bunnies;

                    // Terminate search when we have rescued all bunnies. This will avoid never
                    // terminating in the case of negative cycles.
                    if (result.size() == matrix.length - 2) {
                        return result.stream().mapToInt(Integer::intValue).toArray();
                    }
                }
            }

            // This means that we have reached a location where a bunny can be saved.
            if (currentState.location != 0 && currentState.location != matrix.length - 1) {
                currentState.bunnies.add(Integer.valueOf(currentState.location - 1));
            }

            // Actual search condition: We only continue the search from the current state
            // when we have not yet seen this BunnyState or when we have more time left than
            // previously.
            if (!bunnyMap.containsKey(currentState) || bunnyMap.get(currentState).intValue() < timeLeft) {
                bunnyMap.put(currentState, timeLeft);

                for (int i = 0; i < matrix.length; ++i) {
                    // Avoid not moving to a new location.
                    if (i != currentState.location) {
                        // Add new states to queue which have the location set accordingly, the time
                        // left is reduced as given by the transition matrix.
                        queue.add(new RescueState(new BunnyState(i, currentState.bunnies),
                                timeLeft - matrix[currentState.location][i]));
                    }
                }
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int[] solution(int[][] times, int times_limit) {
        RescueState initialState = new RescueState(new BunnyState(0, new TreeSet<Integer>()), times_limit);

        // Run breath-first search.
        return bfs(initialState, times);
    }

    public static void main(String[] args) {
        int[] firstTestCaseResult = solution(new int[][] { { 0, 1, 1, 1, 1 }, { 1, 0, 1, 1, 1 }, { 1, 1, 0, 1, 1 },
                { 1, 1, 1, 0, 1 }, { 1, 1, 1, 1, 0 } }, 3);
        int[] secondTestCaseResult = solution(new int[][] { { 0, 2, 2, 2, -1 }, { 9, 0, 2, 2, -1 }, { 9, 3, 0, 2, -1 },
                { 9, 3, 2, 0, -1 }, { 9, 3, 2, 2, 0 } }, 1);
    }
}