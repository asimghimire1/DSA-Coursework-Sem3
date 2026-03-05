import java.util.*;

public class Qn6 {

    // edges with safety probabilities
    static String[][] edges = {
        {"KTM","JA","0.90"},{"KTM","JB","0.80"},
        {"JA","KTM","0.90"},{"JA","PH","0.95"},{"JA","BS","0.70"},
        {"JB","KTM","0.80"},{"JB","JA","0.60"},{"JB","BS","0.90"},
        {"PH","JA","0.95"},{"PH","BS","0.85"},
        {"BS","JA","0.70"},{"BS","JB","0.90"},{"BS","PH","0.85"}
    };

    public static void safestPath() {
        Map<String, List<double[]>> graph = new HashMap<>();
        String[] nodes = {"KTM","JA","JB","PH","BS"};
        for (String n : nodes) graph.put(n, new ArrayList<>());

        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) idx.put(nodes[i], i);

        for (String[] e : edges) {
            double w = -Math.log(Double.parseDouble(e[2]));
            graph.get(e[0]).add(new double[]{idx.get(e[1]), w});
        }

        // Dijkstra from KTM ...
        double[] dist = new double[nodes.length];
        int[] prev = new int[nodes.length];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[0] = 0; 

        // min-heap based on distance - using here 
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0, 0});

        while (!pq.isEmpty()) {
            double[] cur = pq.poll();
            int u = (int) cur[1];
            if (cur[0] > dist[u]) continue;
            for (double[] nb : graph.get(nodes[u])) {
                int v = (int) nb[0];
                double newDist = dist[u] + nb[1];
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    prev[v] = u;
                    pq.offer(new double[]{newDist, v});
                }
            }
        }

        System.out.println("Safest Paths from KTM");
        for (int i = 1; i < nodes.length; i++) {
            double safety = Math.exp(-dist[i]);
            System.out.printf("KTM to %-3s | Safety: %.4f | Path: %s%n",
                nodes[i], safety, getPath(prev, nodes, i));
        }
    }

    static String getPath(int[] prev, String[] nodes, int target) {
        List<String> path = new ArrayList<>();
        for (int cur = target; cur != -1; cur = prev[cur]) path.add(nodes[cur]);
        Collections.reverse(path);
        return String.join(" to ", path);
    }

    public static void main(String[] args) {
        safestPath();
        System.out.println();
        maxFlow();
    }

    //Part 2

    static int N = 5; 
    static int[][] cap = new int[N][N];

    public static void maxFlow() {
        // Capacity table
        int[][] capData = {
            {0,1,10},{0,2,15},{1,0,10},{1,3,8},{1,4,5},
            {2,0,15},{2,1,4},{2,4,12},{3,1,8},{3,4,6},
            {4,1,5},{4,2,12},{4,3,6}
        };
        for (int[] c : capData) cap[c[0]][c[1]] = c[1+1]; 
        int[][] res = new int[N][N];
        for (int i = 0; i < N; i++) res[i] = cap[i].clone();

        int source = 0, sink = 4; // KTM to BS
        int totalFlow = 0;
        int[] parent = new int[N];

        while (bfs(res, source, sink, parent)) {
            // Find bottleneck
            int flow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v])
                flow = Math.min(flow, res[parent[v]][v]);

            for (int v = sink; v != source; v = parent[v]) {
                res[parent[v]][v] -= flow;
                res[v][parent[v]] += flow;
            }
            totalFlow += flow;
        }

        System.out.println("= Max Flow KTM to BS =");
        System.out.println("Maximum trucks/hour: " + totalFlow); 
    }

    static boolean bfs(int[][] res, int src, int sink, int[] parent) {
        boolean[] visited = new boolean[N];
        Queue<Integer> q = new LinkedList<>();
        q.add(src); visited[src] = true; parent[src] = -1;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < N; v++) {
                if (!visited[v] && res[u][v] > 0) {
                    parent[v] = u; visited[v] = true;
                    if (v == sink) return true;
                    q.add(v);
                }
            }
        }
        return false;
    }
}