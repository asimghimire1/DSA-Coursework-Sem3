import java.util.*;

public class Qn1b {
    static Map<Integer, List<String>> memo = new HashMap<>();

    public static List<String> wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        return backtrack(s, dict, 0);
    }

    static List<String> backtrack(String s, Set<String> dict, int start) {
        if (memo.containsKey(start)) return memo.get(start);
        List<String> result = new ArrayList<>();
        if (start == s.length()) { result.add(""); return result; }

        for (int end = start + 1; end <= s.length(); end++) {
            String word = s.substring(start, end);
            if (dict.contains(word)) {
                for (String rest : backtrack(s, dict, end)) {
                    result.add(rest.isEmpty() ? word : word + " " + rest);
                }
            }
        }
        memo.put(start, result);
        return result;
    }

    public static void main(String[] args) {
        memo.clear();
        System.out.println(wordBreak("nepaltrekkingguide",
            Arrays.asList("nepal","trekking","guide","nepaltrekking")));
        // output expected with word breaking

        memo.clear();
        System.out.println(wordBreak("visitkathmandunepal",
            Arrays.asList("visit","kathmandu","nepal","visitkathmandu","kathmandunepal")));
    

        memo.clear();
        System.out.println(wordBreak("everesthikingtrail",
            Arrays.asList("everest","hiking","trek")));
        
    }
}