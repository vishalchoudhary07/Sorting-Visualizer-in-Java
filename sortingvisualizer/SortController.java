package sortingvisualizer;

public interface SortController {
    void compare(int idx1, int idx2) throws InterruptedException;

    void swap(int idx1, int idx2) throws InterruptedException;

    void setValue(int index, int value) throws InterruptedException;

    void markSorted(int index) throws InterruptedException;

    void clearHighlights() throws InterruptedException;

    void pivot(int index) throws InterruptedException;

    void subArray(int start, int end) throws InterruptedException;

    // Check if sorting is cancelled
    boolean isCancelled();

    // Statistics
    void addComparison();

    void addSwap();
}
