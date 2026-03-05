import java.util.*;

public class Qn1a {
    public static int maxPoints(int[][] points) {
        int n = points.length;
        if (n <= 2) return n;
        int max = 1;

        for (int i = 0; i < n; i++) {
            Map<String, Integer> slopeMap = new HashMap<>();
            int duplicates = 1;

            for (int j = i + 1; j < n; j++) {
                int dx = points[j][0] - points[i][0];
                int dy = points[j][1] - points[i][1];

                if (dx == 0 && dy == 0) { duplicates++; continue; }

                int g = gcd(Math.abs(dx), Math.abs(dy));
                dx /= g; dy /= g;

                if (dx < 0) { dx = -dx; dy = -dy; }
                else if (dx == 0) dy = Math.abs(dy);

                String key = dy + "/" + dx;
                slopeMap.put(key, slopeMap.getOrDefault(key, 0) + 1);
            }

            for (int count : slopeMap.values())
                max = Math.max(max, count + duplicates);
            max = Math.max(max, duplicates);
        }
        return max;
    }

    static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

    public static void main(String[] args) {
        System.out.println(maxPoints(new int[][]{{1,1},{2,2},{3,3}})); // for the 1st example
        System.out.println(maxPoints(new int[][]{{1,1},{3,2},{5,3},{4,1},{2,3},{1,4}})); // for the 2nd example
    }
}