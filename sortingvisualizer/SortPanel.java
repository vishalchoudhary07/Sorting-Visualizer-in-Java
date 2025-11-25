package sortingvisualizer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SortPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private int[] array;
    private int barWidth;

    // Indices for highlighting
    private int currentIndex = -1;
    private int comparingIndex = -1;
    private int swapIndex1 = -1;
    private int swapIndex2 = -1;
    private int pivotIndex = -1; // For QuickSort
    private int subArrayStart = -1, subArrayEnd = -1; // For MergeSort focus

    private boolean[] isSorted;

    private static final Color BAR_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color COMPARE_COLOR = new Color(255, 215, 0); // Gold
    private static final Color SWAP_COLOR = new Color(255, 69, 0); // Orange Red
    private static final Color SORTED_COLOR = new Color(50, 205, 50); // Lime Green
    private static final Color PIVOT_COLOR = new Color(186, 85, 211); // Medium Orchid
    private static final Color SUB_ARRAY_COLOR = new Color(0, 255, 255); // Cyan

    public SortPanel() {
        setBackground(new Color(30, 30, 30)); // Dark Gray
        setPreferredSize(new Dimension(800, 400));
    }

    public void setArray(int[] array) {
        this.array = array;
        if (array != null) {
            this.isSorted = new boolean[array.length];
            Arrays.fill(this.isSorted, false);
        }
        resetHighlights();
        repaint();
    }

    public void resetHighlights() {
        currentIndex = -1;
        comparingIndex = -1;
        swapIndex1 = -1;
        swapIndex2 = -1;
        pivotIndex = -1;
        subArrayStart = -1;
        subArrayEnd = -1;
    }

    public void highlightCompare(int idx1, int idx2) {
        resetHighlights();
        this.currentIndex = idx1;
        this.comparingIndex = idx2;
        repaint();
    }

    public void highlightSwap(int idx1, int idx2) {
        resetHighlights();
        this.swapIndex1 = idx1;
        this.swapIndex2 = idx2;
        repaint();
    }

    public void highlightPivot(int idx) {
        resetHighlights();
        this.pivotIndex = idx;
        repaint();
    }

    public void highlightSubArray(int start, int end) {
        resetHighlights();
        this.subArrayStart = start;
        this.subArrayEnd = end;
        repaint();
    }

    public void markAsSorted(int index) {
        if (isSorted != null && index >= 0 && index < isSorted.length) {
            isSorted[index] = true;
        }
        // No repaint here, often done in batch or after other highlights
    }

    public void markRangeAsSorted(int start, int end) {
        if (isSorted != null) {
            for (int i = start; i <= end && i < isSorted.length; i++) {
                if (i >= 0)
                    isSorted[i] = true;
            }
        }
        // repaint(); // Let the caller decide when to repaint
    }

    public void clearAllSortedMarks() {
        if (isSorted != null) {
            Arrays.fill(isSorted, false);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (array == null || array.length == 0) {
            g.setColor(Color.WHITE);
            g.drawString("Array is empty or not initialized.", getWidth() / 2 - 100, getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        barWidth = Math.max(1, panelWidth / array.length);
        int maxVal = 0;
        for (int val : array) {
            if (val > maxVal)
                maxVal = val;
        }
        if (maxVal == 0)
            maxVal = 1; // Avoid division by zero for all zero array

        for (int i = 0; i < array.length; i++) {
            int barHeight = (int) (((double) array[i] / maxVal) * (panelHeight * 0.9));
            int x = i * barWidth;
            int y = panelHeight - barHeight;

            if (isSorted != null && isSorted[i]) {
                g2d.setColor(SORTED_COLOR);
            } else if (i == pivotIndex) {
                g2d.setColor(PIVOT_COLOR);
            } else if (i == swapIndex1 || i == swapIndex2) {
                g2d.setColor(SWAP_COLOR);
            } else if (i == currentIndex || i == comparingIndex) {
                g2d.setColor(COMPARE_COLOR);
            } else if (subArrayStart != -1 && i >= subArrayStart && i <= subArrayEnd) {
                g2d.setColor(SUB_ARRAY_COLOR);
            } else {
                g2d.setColor(BAR_COLOR);
            }
            g2d.fillRect(x, y, Math.max(1, barWidth - 1), barHeight);

            if (barWidth > 25 && barHeight > 15 && array[i] > 0) { // Only draw if it fits and positive
                g2d.setColor(Color.BLACK);
                String valStr = String.valueOf(array[i]);
                FontMetrics fm = g2d.getFontMetrics();
                int stringX = x + (barWidth - 1 - fm.stringWidth(valStr)) / 2;
                int stringY = y + fm.getAscent() + 2; // +2 for slight top margin
                if (barHeight > fm.getHeight()) {
                    g2d.drawString(valStr, stringX, stringY);
                }
            }
        }
    }

    public void processStep(SortStep step) {
        switch (step.getType()) {
            case COMPARE:
                highlightCompare(step.getIndex1(), step.getIndex2());
                break;
            case SWAP:
                highlightSwap(step.getIndex1(), step.getIndex2());
                break;
            case SET_VALUE:
                highlightCompare(step.getIndex1(), step.getIndex1());
                break;
            case MARK_SORTED:
                markAsSorted(step.getIndex1());
                repaint();
                break;
            case CLEAR_HIGHLIGHTS:
                resetHighlights();
                repaint();
                break;
            case PIVOT:
                highlightPivot(step.getIndex1());
                break;
            case SUB_ARRAY:
                highlightSubArray(step.getIndex1(), step.getIndex2());
                break;
        }
    }
}