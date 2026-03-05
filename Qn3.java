
  public class Qn3 {
    public static int maxProfit(int k, int[] prices) {
        int n = prices.length;
        if (n == 0 || k == 0) return 0;

        // If k >= n/2 gareu bhane unlimited transactions 
        if (k >= n / 2) {
            int profit = 0;
            for (int i = 1; i < n; i++)
                if (prices[i] > prices[i-1]) profit += prices[i] - prices[i-1];
            return profit;
        }

        int[][] dp = new int[k + 1][n];

        for (int t = 1; t <= k; t++) {
            int prevMax = -prices[0]; 
            for (int i = 1; i < n; i++) {
                dp[t][i] = Math.max(dp[t][i-1], prices[i] + prevMax);
                prevMax   = Math.max(prevMax, dp[t-1][i] - prices[i]);
            }
        }
        return dp[k][n - 1];
    }

    public static void main(String[] args) {
        System.out.println(maxProfit(2, new int[]{2000, 4000, 1000})); 
    }
}

