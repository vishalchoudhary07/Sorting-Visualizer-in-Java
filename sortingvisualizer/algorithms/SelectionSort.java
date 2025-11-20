package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class SelectionSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Selection Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            if (controller.isCancelled())
                return;
            int minIdx = i;
            controller.compare(i, i); // Current selection anchor

            for (int j = i + 1; j < n; j++) {
                if (controller.isCancelled())
                    return;
                controller.compare(minIdx, j);
                if (array[j] < array[minIdx]) {
                    minIdx = j;
                    controller.compare(i, minIdx); // Show new min relative to anchor
                }
            }
            if (minIdx != i) {
                int temp = array[minIdx];
                array[minIdx] = array[i];
                array[i] = temp;
                controller.swap(i, minIdx);
            }
            controller.markSorted(i);
        }
        if (n > 0 && !controller.isCancelled()) {
            controller.markSorted(n - 1);
        }
        controller.clearHighlights();
    }
}
