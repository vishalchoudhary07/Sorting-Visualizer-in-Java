package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;
import java.util.Arrays;

public class RadixSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Radix Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        if (array.length == 0)
            return;
        int m = getMax(array);

        for (int exp = 1; m / exp > 0; exp *= 10) {
            if (controller.isCancelled())
                return;
            countingSortForRadix(array, exp, controller);

            // Visual flicker to show pass completion
            for (int i = 0; i < array.length; i++) {
                controller.compare(i, i);
            }
            controller.clearHighlights();
        }
        // Final marking as sorted
        for (int i = 0; i < array.length; i++) {
            controller.markSorted(i);
        }
        controller.clearHighlights();
    }

    private int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++)
            if (arr[i] > max)
                max = arr[i];
        return max;
    }

    private void countingSortForRadix(int[] arr, int exp, SortController controller) throws InterruptedException {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10];
        Arrays.fill(count, 0);

        // Store count of occurrences
        for (int i = 0; i < n; i++) {
            if (controller.isCancelled())
                return;
            controller.compare(i, i);
            count[(arr[i] / exp) % 10]++;
        }
        controller.clearHighlights();

        // Change count[i] so that count[i] now contains actual position
        for (int i = 1; i < 10; i++) {
            if (controller.isCancelled())
                return;
            count[i] += count[i - 1];
        }

        // Build the output array
        for (int i = n - 1; i >= 0; i--) {
            if (controller.isCancelled())
                return;
            controller.compare(i, i);
            int digitValue = (arr[i] / exp) % 10;
            output[count[digitValue] - 1] = arr[i];
            count[digitValue]--;
        }
        controller.clearHighlights();

        // Copy the output array to arr[]
        for (int i = 0; i < n; i++) {
            if (controller.isCancelled())
                return;
            arr[i] = output[i];
            controller.setValue(i, arr[i]);
        }
        controller.clearHighlights();
    }
}
