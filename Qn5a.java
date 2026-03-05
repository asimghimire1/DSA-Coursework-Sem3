import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Qn5a extends JFrame {

    // sabai info about tourist
    static class Spot {
        String name;
        double lat, lon;
        int fee;
        int openMin, closeMin; 
        Set<String> tags;

        Spot(String name, double lat, double lon, int fee, String open, String close, String... tags) {
            this.name     = name;
            this.lat      = lat;
            this.lon      = lon;
            this.fee      = fee;
            this.openMin  = parseTime(open);
            this.closeMin = parseTime(close);
            this.tags     = new HashSet<>();
            for (String t : tags) this.tags.add(t.toLowerCase());
        }
    }

    // for time and budget
    private final JTextField timeHoursField = new JTextField("6");
    private final JTextField budgetField    = new JTextField("500");

    // interest category pick garna
    private final String[]    ALL_TAGS = {"culture","heritage","religious","nature","relaxation","adventure"};
    private final JCheckBox[] tagBoxes = new JCheckBox[ALL_TAGS.length];

    // result print garan ra map draw garna
    private final JTextArea  output    = new JTextArea(12, 40);
    private final PlotPanel  plotPanel = new PlotPanel();

    // tourist spots haru sab dekhauna
    private final List<Spot> spots = sampleSpots();

    public Qn5a() {
        super("Tourist Spot Optimizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Style for the text input fields
        Font  fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        Color fieldBg   = new Color(245, 247, 252);
        for (JTextField f : new JTextField[]{timeHoursField, budgetField}) {
            f.setFont(fieldFont);
            f.setBackground(fieldBg);
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 215), 1),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
            ));
        }

        Font  labelFont  = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(50, 60, 110);
        JPanel top = new JPanel(new GridLayout(0, 2, 8, 8));
        top.setBackground(new Color(238, 242, 255));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // time ra budget halna 
        String[]     labelTexts = {"Total time (hours):", "Max budget (Rs.):"};
        JTextField[] fields     = {timeHoursField, budgetField};
        for (int i = 0; i < labelTexts.length; i++) {
            JLabel lbl = new JLabel(labelTexts[i]);
            lbl.setFont(labelFont);
            lbl.setForeground(labelColor);
            top.add(lbl);
            top.add(fields[i]);
        }

        // pick garna lai checboxes
        JLabel tagLbl = new JLabel("Interest tags:");
        tagLbl.setFont(labelFont);
        tagLbl.setForeground(labelColor);
        top.add(tagLbl);

        JPanel tagPanel = new JPanel(new GridLayout(2, 3, 4, 2));
        tagPanel.setBackground(new Color(238, 242, 255));
        for (int i = 0; i < ALL_TAGS.length; i++) {
            tagBoxes[i] = new JCheckBox(ALL_TAGS[i]);
            tagBoxes[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tagBoxes[i].setBackground(new Color(238, 242, 255));
            tagBoxes[i].setForeground(new Color(30, 40, 80));
            tagBoxes[i].setFocusPainted(false);
            // hertiages ra culture ma interest cha bhane by default tick garne
            if (ALL_TAGS[i].equals("culture") || ALL_TAGS[i].equals("heritage"))
                tagBoxes[i].setSelected(true);
            tagPanel.add(tagBoxes[i]);
        }
        top.add(tagPanel);

        // button style
        JButton run = new JButton("Suggest Itinerary");
        run.setFont(new Font("Segoe UI", Font.BOLD, 13));
        run.setBackground(new Color(65, 120, 210));
        run.setForeground(Color.WHITE);
        run.setFocusPainted(false);
        run.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        run.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        run.addActionListener(e -> solve());

        // output area style
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        output.setBackground(new Color(250, 251, 255));
        output.setForeground(new Color(25, 35, 75));
        output.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));

        // left panel ma top controls, middle button, ra bottom output rakhne
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(new Color(238, 242, 255));
        left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        left.add(top,                     BorderLayout.NORTH);
        left.add(run,                     BorderLayout.CENTER);
        left.add(new JScrollPane(output), BorderLayout.SOUTH);

        // split pane to show controls and output on the left, map on the right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, plotPanel);
        split.setResizeWeight(0.55);

        setContentPane(split);
        pack();
        setLocationRelativeTo(null);
    }

    // Runs when the user clicks the button
    private void solve() {
        int timeHours = Integer.parseInt(timeHoursField.getText().trim());
        int budget    = Integer.parseInt(budgetField.getText().trim());

        Set<String> interests = new HashSet<>();
        for (int i = 0; i < ALL_TAGS.length; i++)
            if (tagBoxes[i].isSelected()) interests.add(ALL_TAGS[i]);
        if (interests.isEmpty()) interests.addAll(Arrays.asList(ALL_TAGS));

        // Greedy: quickly picks the best spot at each step
        List<Spot> heuristic = greedyRoute(spots, timeHours, budget, interests);

        // Brute force: checks every possible order on a small set
        List<Spot> top     = topCandidates(spots, interests, 6);
        List<Spot> optimal = bruteForceBest(top, timeHours, budget, interests);

        output.setText("");
        output.append("Heuristic itinerary:\n");
        printRoute(heuristic, interests);

        output.append("\nBrute force (on <=6 spots):\n");
        printRoute(optimal, interests);

        // Redraw the map with the heuristic route
        plotPanel.setRoute(heuristic);
        plotPanel.repaint();
    }

    // Print each spot with its fee and how many tags matched
    private void printRoute(List<Spot> route, Set<String> interests) {
        int totalFee = 0;
        double dist  = 0;
        for (int i = 0; i < route.size(); i++) {
            Spot s    = route.get(i);
            int match = interestMatch(s, interests);
            totalFee += s.fee;
            if (i > 0) dist += distance(route.get(i - 1), s);
            output.append(String.format("%d) %s | fee=%d | match=%d\n",
                i + 1, s.name, s.fee, match));
        }
        output.append(String.format("Total fee: %d | Approx distance: %.3f\n", totalFee, dist));
    }

    // Greedy: at each step pick the closest high-scoring affordable spot
    private List<Spot> greedyRoute(List<Spot> all, int timeHours, int budget, Set<String> interests) {
        List<Spot> remaining = new ArrayList<>(all);
        List<Spot> route     = new ArrayList<>();
        int    feeLeft     = budget;
        int    minutesLeft = timeHours * 60;
        Spot   current     = null;

        while (!remaining.isEmpty()) {
            Spot   best      = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (Spot s : remaining) {
                if (s.fee > feeLeft) continue; // can't afford
                int visit  = 60; // assume 1 hour per spot
                int travel = (current == null) ? 0 : (int) Math.round(distance(current, s) * 200);
                if (visit + travel > minutesLeft) continue; // not enough time left

                // Higher tag match = better score; far or expensive spots score lower
                int    match       = interestMatch(s, interests);
                double distPenalty = (current == null) ? 0 : distance(current, s);
                double score       = match * 10 - distPenalty - (s.fee / 200.0);
                if (score > bestScore) { bestScore = score; best = s; }
            }

            if (best == null) break; // nothing fits anymore
            route.add(best);
            remaining.remove(best);
            feeLeft -= best.fee;
            int travel = (current == null) ? 0 : (int) Math.round(distance(current, best) * 200);
            minutesLeft -= (60 + travel);
            current = best;
        }
        return route;
    }
    private List<Spot> bruteForceBest(List<Spot> small, int timeHours, int budget, Set<String> interests) {
        List<Spot> bestRoute = new ArrayList<>();
        double     bestScore = Double.NEGATIVE_INFINITY;

        for (List<Spot> perm : permutations(new ArrayList<>(small))) {
            for (int len = 1; len <= perm.size(); len++) {
                List<Spot> cand  = perm.subList(0, len);
                Double     score = evaluateIfValid(cand, timeHours, budget, interests);
                if (score != null && score > bestScore) {
                    bestScore = score;
                    bestRoute = new ArrayList<>(cand);
                }
            }
        }
        return bestRoute;
    }

    // Check if a route is within time and budget; cha bhane return natra null
    private Double evaluateIfValid(List<Spot> route, int timeHours, int budget, Set<String> interests) {
        int    fee      = 0, used = 0;
        int    minutes  = timeHours * 60;
        Spot   prev     = null;
        int    matchSum = 0;
        double dist     = 0;

        for (Spot s : route) {
            fee += s.fee;
            if (fee > budget) return null; // over budget

            int travel = (prev == null) ? 0 : (int) Math.round(distance(prev, s) * 200);
            used += travel + 60;
            if (used > minutes) return null; // over time

            matchSum += interestMatch(s, interests);
            if (prev != null) dist += distance(prev, s);
            prev = s;
        }
        return matchSum * 10 - dist - (fee / 200.0);
    }

    // Count how many of the user's chosen tags this spot has
    private static int interestMatch(Spot s, Set<String> interests) {
        int c = 0;
        for (String t : interests) if (s.tags.contains(t)) c++;
        return c;
    }

    // Straight-line distance between two spots using lat/lon coordinates
    private static double distance(Spot a, Spot b) {
        double dx = a.lat - b.lat, dy = a.lon - b.lon;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Convert "HH:MM" to total minutes since midnight
    private static int parseTime(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    // Return the k spots that best match the user's interests
    private static List<Spot> topCandidates(List<Spot> all, Set<String> interests, int k) {
        List<Spot> copy = new ArrayList<>(all);
        copy.sort((a, b) -> Integer.compare(interestMatch(b, interests), interestMatch(a, interests)));
        return copy.subList(0, Math.min(k, copy.size()));
    }

    // Build every possible ordering of a list of spots
    private static List<List<Spot>> permutations(List<Spot> items) {
        List<List<Spot>> res = new ArrayList<>();
        permute(items, 0, res);
        return res;
    }

    private static void permute(List<Spot> a, int i, List<List<Spot>> res) {
        if (i == a.size()) { res.add(new ArrayList<>(a)); return; }
        for (int j = i; j < a.size(); j++) {
            Collections.swap(a, i, j);
            permute(a, i + 1, res);
            Collections.swap(a, i, j);
        }
    }

    // Hardcoded dataset of tourist spots across Nepal
    private static List<Spot> sampleSpots() {
        return List.of(
            new Spot("Pashupatinath Temple",    27.7104, 85.3488, 100, "06:00","18:00","culture","religious"),
            new Spot("Swayambhunath Stupa",     27.7149, 85.2906, 200, "07:00","17:00","culture","heritage"),
            new Spot("Garden of Dreams",        27.7125, 85.3170, 150, "09:00","21:00","nature","relaxation"),
            new Spot("Chandragiri Hills",       27.6616, 85.2458, 700, "09:00","17:00","nature","adventure"),
            new Spot("Kathmandu Durbar Square", 27.7048, 85.3076, 100, "10:00","17:00","culture","heritage")
        );
    }

    // Panel that draws the route as numbered dots joined by lines
    static class PlotPanel extends JPanel {
        private List<Spot> route = List.of();

        void setRoute(List<Spot> r) { route = r; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (route == null || route.isEmpty()) return;

            // Work out the range of coordinates so we can scale to the panel size
            double minX = route.stream().mapToDouble(s -> s.lat).min().orElse(0);
            double maxX = route.stream().mapToDouble(s -> s.lat).max().orElse(1);
            double minY = route.stream().mapToDouble(s -> s.lon).min().orElse(0);
            double maxY = route.stream().mapToDouble(s -> s.lon).max().orElse(1);

            int w = getWidth(), h = getHeight(), pad = 30;
            int prevX = -1, prevY = -1;

            for (int i = 0; i < route.size(); i++) {
                Spot s = route.get(i);
                // Convert lat/lon to a pixel position inside the panel
                int x = pad + (int)((s.lat - minX) / (maxX - minX + 1e-9) * (w - 2 * pad));
                int y = pad + (int)((s.lon - minY) / (maxY - minY + 1e-9) * (h - 2 * pad));

                // Draw connecting line from the previous spot
                if (i > 0) {
                    g2.setColor(new Color(100, 140, 220));
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.drawLine(prevX, prevY, x, y);
                }

                // Draw a filled circle for the spot
                g2.setColor(new Color(65, 120, 210));
                g2.fillOval(x - 6, y - 6, 12, 12);

                // Write the visit order number inside the circle
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.drawString(String.valueOf(i + 1), x - 3, y + 4);

                // Write the spot name beside the circle
                g2.setColor(new Color(30, 40, 80));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(s.name, x + 10, y + 4);

                prevX = x;
                prevY = y;
            }
        }
    }

    public static void main(String[] args) {
        // Always start Swing on the dedicated UI thread
        SwingUtilities.invokeLater(() -> new Qn5a().setVisible(true));
    }
}