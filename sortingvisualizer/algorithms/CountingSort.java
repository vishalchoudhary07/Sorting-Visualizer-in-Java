package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;
import java.util.Arrays;

public class CountingSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Counting Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        if (array.length == 0)
            return;

        int max = Arrays.stream(array).max().orElse(0);
        int[] count = new int[max + 1];

        // Counting frequencies
        for (int i = 0; i < array.length; i++) {
            if (controller.isCancelled())
                return;
            controller.compare(i, i); // Highlight element being counted
            count[array[i]]++;
        }
        controller.clearHighlights();

        // Calculating cumulative counts
        for (int i = 1; i <= max; i++) {
            if (controller.isCancelled())
                return;
            count[i] += count[i - 1];
        }

        int[] output = new int[array.length];
        // Placing elements
        for (int i = array.length - 1; i >= 0; i--) {
            if (controller.isCancelled())
                return;
            controller.compare(i, i); // Element being placed
            output[count[array[i]] - 1] = array[i];
            count[array[i]]--;
        }
        controller.clearHighlights();

        // Copying back
        for (int i = 0; i < array.length; i++) {
            if (controller.isCancelled())
                return;
            array[i] = output[i];
            controller.setValue(i, array[i]);
            controller.markSorted(i);
        }
        controller.clearHighlights();
    }
}
