import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SortingVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    private SortPanel sortPanel;
    private JButton startButton, resetButton;
    private JComboBox<Algorithm> algorithmComboBox;
    private JSlider speedSlider, sizeSlider;
    private JLabel statusLabel;

    private volatile int[] array; // Made volatile as it's read by worker and written by main/EDT
    private SwingWorker<Void, SortStep> sortWorker;
    private volatile int delay = 50;
    private final int DEFAULT_ARRAY_SIZE = 50;
    private final int MAX_ARRAY_VALUE = 100; // For random generation

    // Helper class to publish sort steps
    private static class SortStep {
        enum StepType {
            COMPARE, SWAP, SET_VALUE, MARK_SORTED, CLEAR_HIGHLIGHTS,
            PIVOT_HIGHLIGHT, SUB_ARRAY_HIGHLIGHT
        }

        final StepType type;
        final int index1;
        final int index2; // For COMPARE, SWAP, SUB_ARRAY_HIGHLIGHT (end index)
        final int value;  // For SET_VALUE

        private SortStep(StepType type, int index1, int index2, int value) {
            this.type = type;
            this.index1 = index1;
            this.index2 = index2;
            this.value = value;
        }

        public static SortStep compare(int idx1, int idx2) {
            return new SortStep(StepType.COMPARE, idx1, idx2, 0);
        }
        public static SortStep swap(int idx1, int idx2) {
            return new SortStep(StepType.SWAP, idx1, idx2, 0);
        }
        public static SortStep setValue(int targetIndex, int newValue) {
            return new SortStep(StepType.SET_VALUE, targetIndex, -1, newValue);
        }
        public static SortStep markSorted(int targetIndex) {
            return new SortStep(StepType.MARK_SORTED, targetIndex, -1, 0);
        }
        public static SortStep clearHighlights() {
            return new SortStep(StepType.CLEAR_HIGHLIGHTS, -1, -1, 0);
        }
        public static SortStep pivot(int pivotIdx) {
            return new SortStep(StepType.PIVOT_HIGHLIGHT, pivotIdx, -1, 0);
        }
        public static SortStep subArray(int startIdx, int endIdx) {
            return new SortStep(StepType.SUB_ARRAY_HIGHLIGHT, startIdx, endIdx, 0);
        }
    }

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
        setMinimumSize(new Dimension(600, 400)); // Ensure reasonable minimum size
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel controlPanel;

    private void setupControlPanel() {
        controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        algorithmComboBox = new JComboBox<>(Algorithm.values());
        controlPanel.add(new JLabel("Algorithm:"), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2;
        controlPanel.add(algorithmComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Size:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3;
        sizeSlider = new JSlider(10, 250, DEFAULT_ARRAY_SIZE); // Increased max size
        sizeSlider.setMajorTickSpacing(60);
        sizeSlider.setMinorTickSpacing(10);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        sizeSlider.addChangeListener(e -> {
            if (!sizeSlider.getValueIsAdjusting() && (sortWorker == null || sortWorker.isDone())) {
                generateRandomArray(sizeSlider.getValue());
                sortPanel.setArray(array); // Pass the new array instance
                statusLabel.setText("Array reset. Size: " + array.length);
            }
        });
        controlPanel.add(sizeSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Speed:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        speedSlider = new JSlider(0, 200, 150);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.addChangeListener(e -> delay = Math.max(0, 200 - speedSlider.getValue()));
        controlPanel.add(speedSlider, gbc);
        delay = Math.max(0, 200 - speedSlider.getValue());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        startButton = new JButton("Start Sort");
        startButton.addActionListener(e -> startSorting());
        controlPanel.add(startButton, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 2;
        resetButton = new JButton("Reset Array");
        resetButton.addActionListener(e -> {
            if (sortWorker != null && !sortWorker.isDone()) {
                sortWorker.cancel(true);
            }
            generateRandomArray(sizeSlider.getValue());
            sortPanel.setArray(array); // Pass the new array instance
            sortPanel.clearAllSortedMarks();
            statusLabel.setText("Array reset. Size: " + array.length);
            setControlsEnabled(true);
        });
        controlPanel.add(resetButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        statusLabel = new JLabel("Ready. Array Size: " + DEFAULT_ARRAY_SIZE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(statusLabel, gbc);
    }

    private void generateRandomArray(int size) {
        if (size <= 0) size = DEFAULT_ARRAY_SIZE; // Basic validation
        int[] newArray = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            newArray[i] = random.nextInt(MAX_ARRAY_VALUE) + 1;
        }
        this.array = newArray; // Assign the new array instance
        statusLabel.setText("Generated new array. Size: " + size);
    }

    private void setControlsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        resetButton.setEnabled(enabled); // Always enable reset? Or only when not sorting?
        algorithmComboBox.setEnabled(enabled);
        sizeSlider.setEnabled(enabled);
    }

    private void startSorting() {
        if (sortWorker != null && !sortWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Sorting is already in progress!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        sortPanel.clearAllSortedMarks();
        statusLabel.setText("Sorting...");

        Algorithm selectedAlgorithm = (Algorithm) algorithmComboBox.getSelectedItem();
        
        // Critical: Make a copy for the worker to sort.
        // The SortPanel's array 'this.array' will be updated by the 'process' method on SWAP or SET_VALUE steps.
        final int[] arrayToSort = Arrays.copyOf(this.array, this.array.length);

        sortWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Update status label (can be done via publish if preferred for more complex messages)
                // SwingUtilities.invokeLater(() -> statusLabel.setText("Sorting with " + selectedAlgorithm + "..."));

                switch (selectedAlgorithm) {
                    case BUBBLE_SORT:       bubbleSort(arrayToSort);        break;
                    case SELECTION_SORT:    selectionSort(arrayToSort);     break;
                    case INSERTION_SORT:    insertionSort(arrayToSort);     break;
                    case MERGE_SORT:        mergeSortRecursive(arrayToSort, 0, arrayToSort.length - 1); break;
                    case QUICK_SORT:        quickSortRecursive(arrayToSort, 0, arrayToSort.length - 1); break;
                    case COUNTING_SORT:     countingSort(arrayToSort);      break;
                    case RADIX_SORT:        radixSort(arrayToSort);         break;
                }
                return null;
            }

            @Override
            protected void process(List<SortStep> chunks) {
                for (SortStep step : chunks) {
                    if (isCancelled()) break;
                    // Apply changes to 'SortingVisualizer.this.array' which SortPanel uses for drawing
                    switch (step.type) {
                        case COMPARE:
                            sortPanel.highlightCompare(step.index1, step.index2);
                            break;
                        case SWAP:
                            int temp = SortingVisualizer.this.array[step.index1];
                            SortingVisualizer.this.array[step.index1] = SortingVisualizer.this.array[step.index2];
                            SortingVisualizer.this.array[step.index2] = temp;
                            // sortPanel.setArray(SortingVisualizer.this.array); // Not needed if we just highlight
                            sortPanel.highlightSwap(step.index1, step.index2);
                            break;
                        case SET_VALUE:
                            if (step.index1 >= 0 && step.index1 < SortingVisualizer.this.array.length) {
                                SortingVisualizer.this.array[step.index1] = step.value;
                            }
                            // sortPanel.setArray(SortingVisualizer.this.array); // Not needed if we just highlight
                            sortPanel.highlightCompare(step.index1, -1); // Highlight the changed element
                            break;
                        case MARK_SORTED:
                            sortPanel.markAsSorted(step.index1);
                            break;
                        case PIVOT_HIGHLIGHT:
                            sortPanel.highlightPivot(step.index1);
                            break;
                        case SUB_ARRAY_HIGHLIGHT:
                            sortPanel.highlightSubArray(step.index1, step.index2);
                            break;
                        case CLEAR_HIGHLIGHTS:
                            sortPanel.resetHighlights();
                            break;
                    }
                    sortPanel.repaint(); // Repaint after each processed step
                }
            }

            @Override
            protected void done() {
                try {
                    get(); 
                    if (!isCancelled()) {
                        statusLabel.setText(selectedAlgorithm + " complete!");
                        sortPanel.markRangeAsSorted(0, array.length -1);
                        sortPanel.resetHighlights();
                    } else {
                        statusLabel.setText(selectedAlgorithm + " cancelled.");
                    }
                } catch (InterruptedException e) {
                    statusLabel.setText(selectedAlgorithm + " interrupted.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setControlsEnabled(true);
                    sortPanel.repaint(); // Final repaint
                }
            }

            // --- Sorting Algorithms ---
            // (These operate on 'arr' which is a copy, and publish steps)
            // (The 'process' method will update 'SortingVisualizer.this.array' based on SWAP/SET_VALUE steps)

            private void bubbleSort(int[] arr) throws InterruptedException {
                int n = arr.length;
                boolean swapped;
                for (int i = 0; i < n - 1; i++) {
                    swapped = false;
                    for (int j = 0; j < n - i - 1; j++) {
                        if (isCancelled()) return;
                        publish(SortStep.compare(j, j + 1));
                        Thread.sleep(delay);

                        if (arr[j] > arr[j + 1]) {
                            int temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                            swapped = true;
                            publish(SortStep.swap(j, j + 1));
                            Thread.sleep(delay);
                        }
                    }
                    publish(SortStep.markSorted(n - 1 - i));
                    if (!swapped) break;
                }
                if (n > 0 && !isCancelled()) publish(SortStep.markSorted(0));
                 publish(SortStep.clearHighlights());
            }

            private void selectionSort(int[] arr) throws InterruptedException {
                int n = arr.length;
                for (int i = 0; i < n - 1; i++) {
                    if (isCancelled()) return;
                    int minIdx = i;
                    publish(SortStep.compare(i, i)); // Current selection anchor
                    Thread.sleep(delay);

                    for (int j = i + 1; j < n; j++) {
                        if (isCancelled()) return;
                        publish(SortStep.compare(minIdx, j));
                        Thread.sleep(delay);
                        if (arr[j] < arr[minIdx]) {
                            minIdx = j;
                            publish(SortStep.compare(i, minIdx)); // Show new min relative to anchor
                            Thread.sleep(delay);
                        }
                    }
                    if (minIdx != i) {
                        int temp = arr[minIdx]; arr[minIdx] = arr[i]; arr[i] = temp;
                        publish(SortStep.swap(i, minIdx));
                        Thread.sleep(delay);
                    }
                    publish(SortStep.markSorted(i));
                }
                if (n > 0 && !isCancelled()) publish(SortStep.markSorted(n - 1));
                publish(SortStep.clearHighlights());
            }

            private void insertionSort(int[] arr) throws InterruptedException {
                int n = arr.length;
                for (int i = 0; i < n; ++i) { // Mark first as sorted, then insert others
                    if (isCancelled()) return;
                    if (i == 0) {
                        publish(SortStep.markSorted(i)); Thread.sleep(delay); continue;
                    }
                    int key = arr[i];
                    int j = i - 1;
                    publish(SortStep.compare(i, i)); // Element to insert
                    Thread.sleep(delay);

                    while (j >= 0 && arr[j] > key) {
                        if (isCancelled()) return;
                        publish(SortStep.compare(j, i)); // Compare with key's original pos
                        Thread.sleep(delay);
                        arr[j + 1] = arr[j];
                        publish(SortStep.setValue(j + 1, arr[j])); // Visualize shift
                        Thread.sleep(delay);
                        j = j - 1;
                    }
                    arr[j + 1] = key;
                    publish(SortStep.setValue(j + 1, key)); // Place key
                    Thread.sleep(delay);
                    
                    for(int k=0; k<=i; k++) publish(SortStep.markSorted(k)); // Mark sorted portion
                    Thread.sleep(delay);
                }
                publish(SortStep.clearHighlights());
            }

            private void mergeSortRecursive(int[] arr, int l, int r) throws InterruptedException {
                if (l < r) {
                    if (isCancelled()) return;
                    int m = l + (r - l) / 2;
                    publish(SortStep.subArray(l, r)); Thread.sleep(delay);

                    mergeSortRecursive(arr, l, m);
                    if (isCancelled()) return;
                    mergeSortRecursive(arr, m + 1, r);
                    if (isCancelled()) return;

                    merge(arr, l, m, r);
                } else if (l == r) { // Single element is sorted
                     publish(SortStep.markSorted(l)); Thread.sleep(delay);
                }
            }

            private void merge(int[] arr, int l, int m, int r) throws InterruptedException {
                int n1 = m - l + 1;
                int n2 = r - m;
                int[] L = new int[n1];
                int[] R = new int[n2];

                for (int i = 0; i < n1; ++i) L[i] = arr[l + i];
                for (int j = 0; j < n2; ++j) R[j] = arr[m + 1 + j];

                publish(SortStep.subArray(l,r)); // Highlight merging area
                Thread.sleep(delay);

                int i = 0, j = 0, k = l;
                while (i < n1 && j < n2) {
                    if (isCancelled()) return;
                    publish(SortStep.compare(l + i, m + 1 + j)); // Compare elements from conceptual L and R
                    Thread.sleep(delay);
                    if (L[i] <= R[j]) {
                        arr[k] = L[i];
                        publish(SortStep.setValue(k, L[i]));
                        i++;
                    } else {
                        arr[k] = R[j];
                        publish(SortStep.setValue(k, R[j]));
                        j++;
                    }
                    publish(SortStep.markSorted(k)); // Mark element as placed correctly in this merge
                    Thread.sleep(delay);
                    k++;
                }
                while (i < n1) {
                    if (isCancelled()) return;
                    arr[k] = L[i];
                    publish(SortStep.setValue(k, L[i]));
                    publish(SortStep.markSorted(k)); Thread.sleep(delay);
                    i++; k++;
                }
                while (j < n2) {
                    if (isCancelled()) return;
                    arr[k] = R[j];
                    publish(SortStep.setValue(k, R[j]));
                    publish(SortStep.markSorted(k)); Thread.sleep(delay);
                    j++; k++;
                }
                 publish(SortStep.clearHighlights());
            }

            private void quickSortRecursive(int[] arr, int low, int high) throws InterruptedException {
                if (low < high) {
                    if (isCancelled()) return;
                    int pi = partition(arr, low, high);
                    quickSortRecursive(arr, low, pi - 1);
                    quickSortRecursive(arr, pi + 1, high);
                } else if (low == high && low >=0 && low < arr.length) { // Single element is sorted
                     publish(SortStep.markSorted(low)); Thread.sleep(delay);
                }
            }

            private int partition(int[] arr, int low, int high) throws InterruptedException {
                int pivot = arr[high];
                publish(SortStep.pivot(high)); // Highlight pivot
                Thread.sleep(delay*2); // Longer pause for pivot

                int i = (low - 1);
                for (int j = low; j < high; j++) {
                    if (isCancelled()) return -1; // Or throw exception
                    publish(SortStep.compare(j, high)); // Compare with pivot
                    Thread.sleep(delay);
                    if (arr[j] < pivot) {
                        i++;
                        int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
                        publish(SortStep.swap(i, j));
                        Thread.sleep(delay);
                    }
                }
                int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
                publish(SortStep.swap(i + 1, high)); // Place pivot
                Thread.sleep(delay);
                publish(SortStep.markSorted(i + 1)); // Pivot is now sorted
                publish(SortStep.clearHighlights()); Thread.sleep(delay);
                return i + 1;
            }

            private void countingSort(int[] arr) throws InterruptedException {
                 if (arr.length == 0) return;
                SwingUtilities.invokeLater(() -> statusLabel.setText("Counting Sort: Finding max value..."));
                int max = Arrays.stream(arr).max().orElse(0);
                // For visualization, ensure MAX_ARRAY_VALUE from generation is respected,
                // or adjust if actual max is much larger (though this algo is best for small range)
                
                SwingUtilities.invokeLater(() -> statusLabel.setText("Counting Sort: Initializing count array..."));
                int[] count = new int[max + 1];
                Thread.sleep(delay*5); // Pause for status

                SwingUtilities.invokeLater(() -> statusLabel.setText("Counting Sort: Counting frequencies..."));
                for (int i=0; i < arr.length; i++) {
                    if (isCancelled()) return;
                    publish(SortStep.compare(i,i)); // Highlight element being counted
                    Thread.sleep(delay/2); // Faster for counting
                    count[arr[i]]++;
                }
                publish(SortStep.clearHighlights());

                SwingUtilities.invokeLater(() -> statusLabel.setText("Counting Sort: Calculating cumulative counts..."));
                for (int i = 1; i <= max; i++) {
                    if (isCancelled()) return;
                    count[i] += count[i - 1];
                     Thread.sleep(delay/4); // Very fast, internal step
                }
                 Thread.sleep(delay*5); // Pause for status

                int[] output = new int[arr.length];
                SwingUtilities.invokeLater(() -> statusLabel.setText("Counting Sort: Placing elements..."));
                for (int i = arr.length - 1; i >= 0; i--) {
                    if (isCancelled()) return;
                    publish(SortStep.compare(i,i)); // Element being placed
                    Thread.sleep(delay);
                    output[count[arr[i]] - 1] = arr[i];
                    count[arr[i]]--;
                }
                publish(SortStep.clearHighlights());

                for (int i = 0; i < arr.length; i++) {
                    if (isCancelled()) return;
                    arr[i] = output[i];
                    publish(SortStep.setValue(i, arr[i]));
                    publish(SortStep.markSorted(i));
                    Thread.sleep(delay);
                }
                 publish(SortStep.clearHighlights());
            }

            private int getMax(int arr[]) {
                int max = arr[0];
                for (int i = 1; i < arr.length; i++)
                    if (arr[i] > max) max = arr[i];
                return max;
            }

            // Radix sort uses a stable sort, like counting sort
            private void countingSortForRadix(int arr[], int exp) throws InterruptedException {
                int n = arr.length;
                int output[] = new int[n];
                int count[] = new int[10]; // Digits 0-9
                Arrays.fill(count, 0);

                // Store count of occurrences in count[]
                for (int i = 0; i < n; i++) {
                    if (isCancelled()) return;
                    publish(SortStep.compare(i,i)); Thread.sleep(delay/2);
                    count[(arr[i] / exp) % 10]++;
                }
                publish(SortStep.clearHighlights());

                // Change count[i] so that count[i] now contains actual
                // position of this digit in output[]
                for (int i = 1; i < 10; i++) {
                     if (isCancelled()) return;
                    count[i] += count[i - 1];
                }

                // Build the output array
                for (int i = n - 1; i >= 0; i--) {
                    if (isCancelled()) return;
                    publish(SortStep.compare(i,i)); Thread.sleep(delay/2);
                    int digitValue = (arr[i] / exp) % 10;
                    output[count[digitValue] - 1] = arr[i];
                    count[digitValue]--;
                }
                 publish(SortStep.clearHighlights());

                // Copy the output array to arr[], so that arr[] now
                // contains sorted numbers according to current digit
                for (int i = 0; i < n; i++) {
                    if (isCancelled()) return;
                    arr[i] = output[i];
                    publish(SortStep.setValue(i, arr[i]));
                    // Don't mark as sorted until final pass of Radix
                    Thread.sleep(delay);
                }
                publish(SortStep.clearHighlights());
            }

            private void radixSort(int arr[]) throws InterruptedException {
                if (arr.length == 0) return;
                SwingUtilities.invokeLater(() -> statusLabel.setText("Radix Sort: Finding max value..."));
                int m = getMax(arr);
                Thread.sleep(delay*2);

                for (int exp = 1; m / exp > 0; exp *= 10) {
                    if (isCancelled()) return;
                    final int currentExp = exp;
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Radix Sort: Sorting by digit place " + currentExp));
                    Thread.sleep(delay*5); // Pause for status update to show
                    countingSortForRadix(arr, exp);
                    // After each pass, the array is more sorted according to that digit
                    // Optionally, highlight all elements to show they've been processed for this pass
                    for(int i=0; i<arr.length; i++) {
                        publish(SortStep.compare(i,i)); // Just a quick visual flicker
                    }
                    Thread.sleep(delay);
                    publish(SortStep.clearHighlights());
                }
                // Final marking as sorted
                for(int i=0; i<arr.length; i++) {
                    publish(SortStep.markSorted(i));
                }
                Thread.sleep(delay);
                publish(SortStep.clearHighlights());
            }
        };
        sortWorker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SortingVisualizer::new);
    }
}