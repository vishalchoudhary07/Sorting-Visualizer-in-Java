package sortingvisualizer;

public class SortStep {
    public enum StepType {
        COMPARE, SWAP, SET_VALUE, MARK_SORTED, CLEAR_HIGHLIGHTS,
        PIVOT, SUB_ARRAY
    }

    final StepType type;
    final int index1;
    final int index2; // For COMPARE, SWAP, SUB_ARRAY (end index)
    final int value; // For SET_VALUE
    final long comparisons;
    final long swaps;

    private SortStep(StepType type, int index1, int index2, int value, long comparisons, long swaps) {
        this.type = type;
        this.index1 = index1;
        this.index2 = index2;
        this.value = value;
        this.comparisons = comparisons;
        this.swaps = swaps;
    }

    public StepType getType() {
        return type;
    }

    public int getIndex1() {
        return index1;
    }

    public int getIndex2() {
        return index2;
    }

    public int getValue() {
        return value;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSwaps() {
        return swaps;
    }

    public static SortStep compare(int idx1, int idx2, long comparisons, long swaps) {
        return new SortStep(StepType.COMPARE, idx1, idx2, 0, comparisons, swaps);
    }

    public static SortStep swap(int idx1, int idx2, long comparisons, long swaps) {
        return new SortStep(StepType.SWAP, idx1, idx2, 0, comparisons, swaps);
    }

    public static SortStep setValue(int targetIndex, int newValue, long comparisons, long swaps) {
        return new SortStep(StepType.SET_VALUE, targetIndex, -1, newValue, comparisons, swaps);
    }

    public static SortStep markSorted(int targetIndex, long comparisons, long swaps) {
        return new SortStep(StepType.MARK_SORTED, targetIndex, -1, 0, comparisons, swaps);
    }

    public static SortStep clearHighlights(long comparisons, long swaps) {
        return new SortStep(StepType.CLEAR_HIGHLIGHTS, -1, -1, 0, comparisons, swaps);
    }

    public static SortStep pivot(int pivotIdx, long comparisons, long swaps) {
        return new SortStep(StepType.PIVOT, pivotIdx, -1, 0, comparisons, swaps);
    }

    public static SortStep subArray(int startIdx, int endIdx, long comparisons, long swaps) {
        return new SortStep(StepType.SUB_ARRAY, startIdx, endIdx, 0, comparisons, swaps);
    }
}
