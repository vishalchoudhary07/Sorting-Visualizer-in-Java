package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class InsertionSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        int n = array.length;
        for (int i = 0; i < n; ++i) {
            if (controller.isCancelled())
                return;
            if (i == 0) {
                controller.markSorted(i);
                continue;
            }
            int key = array[i];
            int j = i - 1;
            controller.compare(i, i); // Element to insert

            while (j >= 0 && array[j] > key) {
                if (controller.isCancelled())
                    return;
                controller.compare(j, i); // Compare with key's original pos
                array[j + 1] = array[j];
                controller.setValue(j + 1, array[j]); // Visualize shift
                j = j - 1;
            }
            array[j + 1] = key;
            controller.setValue(j + 1, key); // Place key

            for (int k = 0; k <= i; k++)
                controller.markSorted(k); // Mark sorted portion
        }
        controller.clearHighlights();
    }
}
