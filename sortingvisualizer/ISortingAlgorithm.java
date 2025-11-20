package sortingvisualizer;

public interface ISortingAlgorithm {
    String getName();

    void runSort(int[] array, SortController controller) throws InterruptedException;
}
