import java.util.*;

class Solution {
    private int[] start, end, leftZero, rightZero, value, segTree;
    private int m;

    public List<Integer> maxActiveSectionsAfterTrade(String s, int[][] queries) {
        int n = s.length();

        // Prefix sum: prefix[i] = number of 1s in s[0...i-1]
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + (s.charAt(i) == '1' ? 1 : 0);
        }

        // Store every 1-run that has zeros on both sides.
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        List<Integer> lefts = new ArrayList<>();
        List<Integer> rights = new ArrayList<>();

        int i = 0;

        while (i < n) {
            if (s.charAt(i) == '0') {
                i++;
                continue;
            }

            int st = i;

            while (i < n && s.charAt(i) == '1') {
                i++;
            }

            int en = i - 1;

            // Count consecutive zeros on the left
            int lz = 0;
            int p = st - 1;

            while (p >= 0 && s.charAt(p) == '0') {
                lz++;
                p--;
            }

            // Count consecutive zeros on the right
            int rz = 0;
            p = en + 1;

            while (p < n && s.charAt(p) == '0') {
                rz++;
                p++;
            }

            if (lz > 0 && rz > 0) {
                starts.add(st);
                ends.add(en);
                lefts.add(lz);
                rights.add(rz);
            }
        }

        m = starts.size();

        start = new int[m];
        end = new int[m];
        leftZero = new int[m];
        rightZero = new int[m];
        value = new int[m];

        for (i = 0; i < m; i++) {
            start[i] = starts.get(i);
            end[i] = ends.get(i);
            leftZero[i] = lefts.get(i);
            rightZero[i] = rights.get(i);

            value[i] = leftZero[i] + rightZero[i];
        }

        // Segment tree stores maximum full gain
        segTree = new int[Math.max(1, 4 * m)];

        if (m > 0) {
            build(1, 0, m - 1);
        }

        List<Integer> answer = new ArrayList<>();

        for (int[] query : queries) {
            int l = query[0];
            int r = query[1];

            // Number of 1s in the whole original string
            int totalOnes = prefix[n];

            int bestGain = 0;

            /*
             Find first candidate 1-run whose start > l.
             It must have at least one zero before it inside query.
            */
            int first = upperBound(start, l);

            /*
             Find last candidate whose end < r.
             It must have at least one zero after it inside query.
            */
            int last = lowerBound(end, r) - 1;

            if (first <= last) {

                // Handle first candidate because its left zero block
                // may be cut by query boundary.
                int leftAvailable =
                    start[first] - Math.max(l, start[first] - leftZero[first]);

                int rightAvailable =
                    Math.min(r, end[first] + rightZero[first]) - end[first];

                bestGain = Math.max(
                    bestGain,
                    leftAvailable + rightAvailable
                );

                // Handle last candidate
                if (last != first) {
                    leftAvailable =
                        start[last] - Math.max(l, start[last] - leftZero[last]);

                    rightAvailable =
                        Math.min(r, end[last] + rightZero[last]) - end[last];

                    bestGain = Math.max(
                        bestGain,
                        leftAvailable + rightAvailable
                    );
                }

                // Middle candidates have complete zero blocks inside query
                if (first + 1 <= last - 1) {
                    bestGain = Math.max(
                        bestGain,
                        queryTree(
                            1,
                            0,
                            m - 1,
                            first + 1,
                            last - 1
                        )
                    );
                }
            }

            answer.add(totalOnes + bestGain);
        }

        return answer;
    }


    // ---------------- SEGMENT TREE ----------------

    private void build(int node, int l, int r) {

        if (l == r) {
            segTree[node] = value[l];
            return;
        }

        int mid = l + (r - l) / 2;

        build(node * 2, l, mid);
        build(node * 2 + 1, mid + 1, r);

        segTree[node] =
            Math.max(segTree[node * 2], segTree[node * 2 + 1]);
    }


    private int queryTree(
        int node,
        int l,
        int r,
        int ql,
        int qr
    ) {

        if (ql > r || qr < l) {
            return 0;
        }

        if (ql <= l && r <= qr) {
            return segTree[node];
        }

        int mid = l + (r - l) / 2;

        return Math.max(
            queryTree(node * 2, l, mid, ql, qr),
            queryTree(node * 2 + 1, mid + 1, r, ql, qr)
        );
    }


    // First index where arr[index] > target
    private int upperBound(int[] arr, int target) {

        int l = 0;
        int r = arr.length;

        while (l < r) {

            int mid = l + (r - l) / 2;

            if (arr[mid] <= target) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }

        return l;
    }


    // First index where arr[index] >= target
    private int lowerBound(int[] arr, int target) {

        int l = 0;
        int r = arr.length;

        while (l < r) {

            int mid = l + (r - l) / 2;

            if (arr[mid] < target) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }

        return l;
    }
}