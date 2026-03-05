import java.util.*;

public class Qn4 {

    static int[][] demand = {
        {6,20,15,25},{7,22,16,28},{8,25,18,30},{9,28,20,32},{10,30,22,35},
        {11,32,24,36},{12,35,25,38},{13,33,24,37},{14,30,22,35},{15,28,20,32},
        {16,26,18,30},{17,24,16,28},{18,22,15,26},{19,20,14,24},{20,18,12,22},
        {21,16,10,20},{22,14,8,18},{23,12,6,15}
    };

    // avoiding float
    static int[][] sources = {
        
        {0, 6, 18, 50, 10},
        {1, 0, 24, 40, 15},
        {2, 17, 23, 60, 30}
    };
    static String[] srcNames = {"Solar","Hydro","Diesel"};

    public static void main(String[] args) {
        System.out.printf("%-4s %-5s %-7s %-7s %-8s %-6s %-6s %-6s%n",
            "Hour","Dist","Solar","Hydro","Diesel","Used","Demand","% Met");

        double totalCost = 0;

        for (int[] row : demand) {
            int hour = row[0];
            int[] dist = {row[1], row[2], row[3]};

            // sources sorted by cost (greedy) yaha use bhako cha
            List<int[]> avail = new ArrayList<>();
            for (int[] src : sources)
                if (hour >= src[1] && hour < src[2]) avail.add(src.clone());
            avail.sort((a, b) -> a[4] - b[4]);

            int[] used = new int[3]; // per-district supplied
            int[] fromSrc = new int[3]; // total used per source

            for (int[] src : avail) {
                int rem = src[3];
                for (int d = 0; d < 3; d++) {
                    int need = dist[d] - used[d];
                    if (need <= 0) continue;
                    int give = Math.min(need, rem);
                    used[d] += give;
                    fromSrc[src[0]] += give;
                    rem -= give;
                    if (rem == 0) break;
                }
            }

            String[] dNames = {"A","B","C"};
            for (int d = 0; d < 3; d++) {
                int pct = used[d] * 100 / dist[d];
                System.out.printf("%-4d %-5s %-7d %-7d %-8d %-6d %-6d %d%%%n",
                    hour, dNames[d], fromSrc[0], fromSrc[1], fromSrc[2],
                    used[d], dist[d], pct);
            }

            for (int[] src : avail)
                totalCost += fromSrc[src[0]] * (src[4] / 10.0);
        }
        System.out.printf("%nTotal Cost: Rs. %.2f%n", totalCost);
    }
}