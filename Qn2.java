public class Qn2 {
    static int maxSum;

    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
        TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
    }

    public static int maxPathSum(TreeNode root) {
        maxSum = Integer.MIN_VALUE;
        dfs(root);
        return maxSum;
    }

    static int dfs(TreeNode node) {
        if (node == null) return 0;
        int left  = Math.max(0, dfs(node.left));   // negatives is removed here
        int right = Math.max(0, dfs(node.right));
        maxSum = Math.max(maxSum, node.val + left + right); // here path bending is done
        return node.val + Math.max(left, right);            // single arm up
    }

    public static void main(String[] args) {
        // with example 1 here
        TreeNode root1 = new TreeNode(1, new TreeNode(2), new TreeNode(3));
        System.out.println(maxPathSum(root1)); 

        // with example 2 here
        TreeNode root2 = new TreeNode(-10,
            new TreeNode(9),
            new TreeNode(20, new TreeNode(15), new TreeNode(7)));
        System.out.println(maxPathSum(root2)); 
    }
}