import java.lang.Math;
import java.util.HashSet;
import java.util.Set;

public class Gunfight {
    // Returns [yourX,yourY,guardX,guardY] for the specified roomId, relative to
    // yourPos.
    private static int[] getMirroredPos(int[] roomId, int[] yourPos, int[] guardPos, int[] dimensions) {
        int[] result = new int[4];

        // Precompute coordinates of mirrored room origin.
        int xTranslation = roomId[0] * dimensions[0];
        int yTranslation = roomId[1] * dimensions[1];

        // Compute orientation along x direction.
        if (roomId[0] % 2 == 0) {
            result[0] = xTranslation;
            result[2] = xTranslation + guardPos[0] - yourPos[0];
        } else {
            result[0] = xTranslation + dimensions[0] - 2 * yourPos[0];
            result[2] = xTranslation + dimensions[0] - guardPos[0] - yourPos[0];
        }

        // Compute orientation along y direction.
        if (roomId[1] % 2 == 0) {
            result[1] = yTranslation;
            result[3] = yTranslation + guardPos[1] - yourPos[1];
        } else {
            result[1] = yTranslation + dimensions[1] - 2 * yourPos[1];
            result[3] = yTranslation + dimensions[1] - guardPos[1] - yourPos[1];
        }

        return result;
    }

    // Returns a room id which spirals around the initial [0,0] room in
    // counterclockwise direction.
    private static void getNextRoomId(int[] roomId) {
        // Find in which "ring" we are.
        int ring = Math.max(Math.abs(roomId[0]), Math.abs(roomId[1]));

        if (roomId[0] == ring) {
            if (roomId[1] == 0) {
                // This is the room from which we go to next outer ring.
                ++roomId[0];
                roomId[1] = 1;
            } else if (roomId[1] == ring) {
                --roomId[0];
            } else {
                ++roomId[1];
            }
        } else if (roomId[0] == -ring) {
            if (roomId[1] == -ring) {
                ++roomId[0];
            } else {
                --roomId[1];
            }
        } else if (roomId[1] == ring) {
            if (roomId[0] == -ring) {
                --roomId[1];
            } else {
                --roomId[0];
            }
        } else {
            if (roomId[0] == ring) {
                ++roomId[1];
            } else {
                ++roomId[0];
            }
        }
    }

    public static int solution(int[] dimensions, int[] ownPos, int[] guardPos, int distance) {
        // Handle special cases where distance allows no or only one kill.
        int[] vectorToGuard = { guardPos[0] - ownPos[0], guardPos[1] - ownPos[1] };
        Double distanceToGuard = Math.sqrt(Math.pow(vectorToGuard[0], 2) + Math.pow(vectorToGuard[1], 2));
        if (distanceToGuard > distance) {
            return 0;
        }
        if (distanceToGuard == distance) {
            return 1;
        }

        // Maintain a set of all angles at which we have already shot.
        Set<Double> shotAngles = new HashSet<Double>();
        shotAngles.add(Math.atan2(vectorToGuard[1], vectorToGuard[0]));

        // Define a room id that identifies each room. The initial room has id [0,0].
        // Compute maximum room id which is limited by shooting distance.
        int[] roomId = { 1, 1 };
        int[] maxRoomId = { distance / dimensions[0] + 1, distance / dimensions[1] + 1 };

        // Count number of shot guards.
        int guardCount = 1;

        // The loop visits all mirrored rooms in a spiraling pattern that is defined
        // through getNextRoomId. The room id is sufficient to compute the mirrored own
        // and guard position of the respective room.
        while (roomId[0] <= maxRoomId[0] || roomId[1] <= maxRoomId[1]) {
            // Get vectors to positions in current room.
            int[] mirroredPos = getMirroredPos(roomId, ownPos, guardPos, dimensions);

            // Add the angle at which we would shoot ourselves to set of shot angles.
            Double shootingDistance = Math.sqrt(Math.pow(mirroredPos[0], 2) + Math.pow(mirroredPos[1], 2));
            if (shootingDistance <= distance) {
                shotAngles.add(Math.atan2(mirroredPos[1], mirroredPos[0]));
            }

            // Get distance and angle at which we can shoot the guard.
            shootingDistance = Math.sqrt(Math.pow(mirroredPos[2], 2) + Math.pow(mirroredPos[3], 2));
            Double shotAngle = Math.atan2(mirroredPos[3], mirroredPos[2]);

            // Guard must be close enough and shot angle must be still unused to shoot the
            // guard.
            if (shootingDistance <= distance && !shotAngles.contains(shotAngle)) {
                ++guardCount;
                shotAngles.add(shotAngle);
            }

            // This will modify the room id accordingly.
            getNextRoomId(roomId);
        }

        return guardCount;
    }

    public static void main(String[] args) {
        int firstTestCaseResult = solution(new int[] { 3, 2 }, new int[] { 1, 1 }, new int[] { 2, 1 }, 4);
        int secondTestCaseResult = solution(new int[] { 300, 275 }, new int[] { 150, 150 }, new int[] { 185, 100 },
                500);
    }
}