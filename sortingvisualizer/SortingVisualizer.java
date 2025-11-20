package sortingvisualizer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sortingvisualizer.algorithms.BubbleSort;
import sortingvisualizer.algorithms.SelectionSort;
import sortingvisualizer.algorithms.InsertionSort;
import sortingvisualizer.algorithms.MergeSort;
import sortingvisualizer.algorithms.QuickSort;
import sortingvisualizer.algorithms.CountingSort;
import sortingvisualizer.algorithms.RadixSort;
import sortingvisualizer.algorithms.HeapSort;

public class SortingVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    private SortPanel sortPanel;
    private JButton startButton, resetButton;
    private JComboBox<Algorithm> algorithmComboBox;
    private JSlider speedSlider, sizeSlider;
    private JLabel statusLabel;
    private JLabel comparisonsLabel;
    private JLabel swapsLabel;
    private JPanel controlPanel;

    private volatile int[] array;
    private SwingWorker<Void, SortStep> sortWorker;
    private int delay = 50;
    private static final int DEFAULT_ARRAY_SIZE = 100;
    private static final int MAX_ARRAY_VALUE = 500;

    // Statistics
    private long comparisons = 0;
    private long swaps = 0;

    public SortingVisualizer() {
        setTitle("Sorting Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sortPanel = new SortPanel();
        add(sortPanel, BorderLayout.CENTER);

        setupControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        generateRandomArray(DEFAULT_ARRAY_SIZE);
        sortPanel.setArray(array);

        pack();
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(30, 30, 30)); // Dark Gray
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        algorithmComboBox = new JComboBox<>(Algorithm.values());
        algorithmComboBox.setBackground(Color.WHITE);
        algorithmComboBox.setForeground(Color.BLACK);
        JLabel algoLabel = new JLabel("Algorithm:");
        algoLabel.setForeground(Color.WHITE);
        controlPanel.add(algoLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(algorithmComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setForeground(Color.WHITE);
        controlPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        sizeSlider = new JSlider(10, 250, DEFAULT_ARRAY_SIZE);
        sizeSlider.setBackground(new Color(30, 30, 30));
        sizeSlider.setForeground(Color.WHITE);
        sizeSlider.setMajorTickSpacing(60);
        sizeSlider.setMinorTickSpacing(10);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        sizeSlider.addChangeListener(e -> {
            if (!sizeSlider.getValueIsAdjusting() && (sortWorker == null || sortWorker.isDone())) {
                generateRandomArray(sizeSlider.getValue());
                sortPanel.setArray(array);
                statusLabel.setText("Array reset. Size: " + array.length);
                comparisonsLabel.setText("Comparisons: 0");
                swapsLabel.setText("Swaps: 0");
            }
        });
        controlPanel.add(sizeSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(Color.WHITE);
        controlPanel.add(speedLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        speedSlider = new JSlider(0, 200, 150);
        speedSlider.setBackground(new Color(30, 30, 30));
        speedSlider.setForeground(Color.WHITE);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.addChangeListener(e -> delay = Math.max(0, 200 - speedSlider.getValue()));
        controlPanel.add(speedSlider, gbc);
        delay = Math.max(0, 200 - speedSlider.getValue());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        startButton = new JButton("Start Sort");
        startButton.setBackground(new Color(46, 204, 113)); // Green
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startSorting());
        controlPanel.add(startButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        resetButton = new JButton("Reset Array");
        resetButton.setBackground(new Color(231, 76, 60)); // Red
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> {
            if (sortWorker != null && !sortWorker.isDone()) {
                sortWorker.cancel(true);
            }
            generateRandomArray(sizeSlider.getValue());
            sortPanel.setArray(array);
            sortPanel.clearAllSortedMarks();
            statusLabel.setText("Array reset. Size: " + array.length);
            comparisonsLabel.setText("Comparisons: 0");
            swapsLabel.setText("Swaps: 0");
            setControlsEnabled(true);
        });
        controlPanel.add(resetButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        statusLabel = new JLabel("Ready. Array Size: " + DEFAULT_ARRAY_SIZE);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(statusLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        comparisonsLabel = new JLabel("Comparisons: 0");
        comparisonsLabel.setForeground(Color.WHITE);
        comparisonsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(comparisonsLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        swapsLabel = new JLabel("Swaps: 0");
        swapsLabel.setForeground(Color.WHITE);
        swapsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(swapsLabel, gbc);

        this.controlPanel = controlPanel;
    }

    private void generateRandomArray(int size) {
        if (size <= 0)
            size = DEFAULT_ARRAY_SIZE;
        int[] newArray = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            newArray[i] = random.nextInt(MAX_ARRAY_VALUE) + 1;
        }
        this.array = newArray;
        statusLabel.setText("Generated new array. Size: " + size);
    }

    private void setControlsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        algorithmComboBox.setEnabled(enabled);
        sizeSlider.setEnabled(enabled);
    }

    private void startSorting() {
        if (sortWorker != null && !sortWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Sorting is already in progress!", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        sortPanel.clearAllSortedMarks();
        statusLabel.setText("Sorting...");
        comparisons = 0;
        swaps = 0;
        comparisonsLabel.setText("Comparisons: 0");
        swapsLabel.setText("Swaps: 0");

        Algorithm selectedAlgorithm = (Algorithm) algorithmComboBox.getSelectedItem();
        final int[] arrayToSort = Arrays.copyOf(this.array, this.array.length);

        sortWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                SortController controller = new SortController() {
                    @Override
                    public void compare(int idx1, int idx2) throws InterruptedException {
                        comparisons++;
                        publish(SortStep.compare(idx1, idx2, comparisons, swaps));
                        Thread.sleep(delay);
                    }

                    @Override
                    public void swap(int idx1, int idx2) throws InterruptedException {
                        swaps++;
                        publish(SortStep.swap(idx1, idx2, comparisons, swaps));
                        Thread.sleep(delay);
                    }

                    @Override
                    public void setValue(int index, int value) throws InterruptedException {
                        publish(SortStep.setValue(index, value, comparisons, swaps));
                        Thread.sleep(delay);
                    }

                    @Override
                    public void markSorted(int index) throws InterruptedException {
                        publish(SortStep.markSorted(index, comparisons, swaps));
                    }

                    @Override
                    public void clearHighlights() throws InterruptedException {
                        publish(SortStep.clearHighlights(comparisons, swaps));
                    }

                    @Override
                    public void pivot(int index) throws InterruptedException {
                        publish(SortStep.pivot(index, comparisons, swaps));
                    }

                    @Override
                    public void subArray(int start, int end) throws InterruptedException {
                        publish(SortStep.subArray(start, end, comparisons, swaps));
                    }

                    @Override
                    public boolean isCancelled() {
                        return sortWorker.isCancelled();
                    }

                    @Override
                    public void addComparison() {
                        comparisons++;
                    }

                    @Override
                    public void addSwap() {
                        swaps++;
                    }
                };

                switch (selectedAlgorithm) {
                    case BUBBLE_SORT:
                        new BubbleSort().runSort(arrayToSort, controller);
                        break;
                    case SELECTION_SORT:
                        new SelectionSort().runSort(arrayToSort, controller);
                        break;
                    case INSERTION_SORT:
                        new InsertionSort().runSort(arrayToSort, controller);
                        break;
                    case MERGE_SORT:
                        new MergeSort().runSort(arrayToSort, controller);
                        break;
                    case QUICK_SORT:
                        new QuickSort().runSort(arrayToSort, controller);
                        break;
                    case COUNTING_SORT:
                        new CountingSort().runSort(arrayToSort, controller);
                        break;
                    case RADIX_SORT:
                        new RadixSort().runSort(arrayToSort, controller);
                        break;
                    case HEAP_SORT:
                        new HeapSort().runSort(arrayToSort, controller);
                        break;
                }
                return null;
            }

            @Override
            protected void process(List<SortStep> chunks) {
                for (SortStep step : chunks) {
                    if (isCancelled())
                        break;
                    sortPanel.processStep(step);
                    comparisonsLabel.setText("Comparisons: " + step.getComparisons());
                    swapsLabel.setText("Swaps: " + step.getSwaps());
                }
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                statusLabel.setText("Sorting Complete!");
            }
        };
        sortWorker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SortingVisualizer::new);
    }
}