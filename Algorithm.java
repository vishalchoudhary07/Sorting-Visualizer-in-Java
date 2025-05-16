public enum Algorithm {
    BUBBLE_SORT("Bubble Sort"),
    SELECTION_SORT("Selection Sort"),
    INSERTION_SORT("Insertion Sort"),
    MERGE_SORT("Merge Sort"),
    QUICK_SORT("Quick Sort"),
    COUNTING_SORT("Counting Sort"),
    RADIX_SORT("Radix Sort");

    private final String displayName;

    Algorithm(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}