import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScreenTimeTracker {
    private static final long DAILY_LIMIT = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    private long startTime = 0;
    private boolean tracking = false;
    private List<UsageEntry> usageLog = new ArrayList<>();
    
    private JLabel timerLabel;
    private JLabel logLabel;
    private JLabel totalUsageLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScreenTimeTracker().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("ScreenTime Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Background image (if needed, can be set on a JPanel)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.add(panel, BorderLayout.CENTER);

        // Timer label
        timerLabel = new JLabel("Elapsed Time: 00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        timerLabel.setBackground(Color.YELLOW);
        timerLabel.setOpaque(true);
        panel.add(timerLabel);

        // Buttons
        JButton startButton = new JButton("Start Tracking");
        startButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        startButton.setPreferredSize(new Dimension(200, 40));
        startButton.addActionListener(this::startTracking);
        panel.add(startButton);

        JButton stopButton = new JButton("Stop Tracking");
        stopButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        stopButton.setPreferredSize(new Dimension(200, 40));
        stopButton.addActionListener(this::stopTracking);
        panel.add(stopButton);

        JButton exportButton = new JButton("Export Log");
        exportButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        exportButton.setPreferredSize(new Dimension(200, 40));
        exportButton.addActionListener(this::exportUsageLog);
        panel.add(exportButton);

        JButton viewLogButton = new JButton("View Usage Log");
        viewLogButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        viewLogButton.setPreferredSize(new Dimension(200, 40));
        viewLogButton.addActionListener(this::viewUsageLog);
        panel.add(viewLogButton);

        JButton resetButton = new JButton("Reset Log");
        resetButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        resetButton.setPreferredSize(new Dimension(200, 40));
        resetButton.addActionListener(this::resetLog);
        panel.add(resetButton);

        // Logs and total usage
        logLabel = new JLabel("", SwingConstants.CENTER);
        logLabel.setFont(new Font("Helvetica", Font.ITALIC, 14));
        panel.add(logLabel);

        totalUsageLabel = new JLabel("Total Usage: 0 hours, 0 minutes", SwingConstants.CENTER);
        totalUsageLabel.setFont(new Font("Helvetica", Font.ITALIC, 14));
        panel.add(totalUsageLabel);

        // Setting up the window
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void startTracking(ActionEvent e) {
        if (tracking) {
            JOptionPane.showMessageDialog(null, "Tracking is already in progress!");
            return;
        }

        startTime = System.currentTimeMillis();
        tracking = true;
        logLabel.setText("Tracking started...");
        updateTimer();
    }

    private void stopTracking(ActionEvent e) {
        if (!tracking) {
            JOptionPane.showMessageDialog(null, "No active tracking session.");
            return;
        }

        long stopTime = System.currentTimeMillis();
        long duration = stopTime - startTime;
        usageLog.add(new UsageEntry(new Date(startTime), new Date(stopTime), duration));
        tracking = false;
        logLabel.setText("Last session duration: " + formatDuration(duration));
        totalUsageLabel.setText("Total Usage: " + calculateTotalUsage());
        checkDailyLimit();
    }

    private String formatDuration(long duration) {
        long hours = duration / (1000 * 60 * 60);
        long minutes = (duration % (1000 * 60 * 60)) / (1000 * 60);
        return String.format("%d hours, %d minutes", hours, minutes);
    }

    private String calculateTotalUsage() {
        long totalDuration = 0;
        for (UsageEntry entry : usageLog) {
            totalDuration += entry.duration;
        }
        return formatDuration(totalDuration);
    }

    private void checkDailyLimit() {
        long totalDuration = 0;
        long currentDate = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        for (UsageEntry entry : usageLog) {
            long entryDate = entry.start.getTime() / (1000 * 60 * 60 * 24);
            if (entryDate == currentDate) {
                totalDuration += entry.duration;
            }
        }
        if (totalDuration >= DAILY_LIMIT) {
            JOptionPane.showMessageDialog(null, "You have exceeded your daily screen time limit!");
        }
    }

    private void updateTimer() {
        if (tracking) {
            long elapsed = System.currentTimeMillis() - startTime;
            timerLabel.setText("Elapsed Time: " + formatDuration(elapsed));
            Timer timer = new Timer(1000, e -> updateTimer());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void exportUsageLog(ActionEvent e) {
        if (usageLog.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No usage data to export.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("usage_log.csv"))) {
            writer.println("Start Time, End Time, Duration");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (UsageEntry entry : usageLog) {
                writer.printf("%s, %s, %s%n", dateFormat.format(entry.start), dateFormat.format(entry.end), formatDuration(entry.duration));
            }
            JOptionPane.showMessageDialog(null, "Usage log exported to usage_log.csv successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error exporting the usage log.");
        }
    }

    private void viewUsageLog(ActionEvent e) {
        if (usageLog.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No usage data to view.");
            return;
        }

        StringBuilder logContent = new StringBuilder("Usage Log:\n");
        for (UsageEntry entry : usageLog) {
            logContent.append(String.format("Start: %s, End: %s, Duration: %s%n", entry.start, entry.end, formatDuration(entry.duration)));
        }

        JOptionPane.showMessageDialog(null, logContent.toString());
    }

    private void resetLog(ActionEvent e) {
        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset the usage log?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            usageLog.clear();
            logLabel.setText("Usage log reset.");
            totalUsageLabel.setText("Total Usage: 0 hours, 0 minutes");
        }
    }

    private static class UsageEntry {
        Date start;
        Date end;
        long duration;

        public UsageEntry(Date start, Date end, long duration) {
            this.start = start;
            this.end = end;
            this.duration = duration;
        }
    }
}
