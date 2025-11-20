package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class BubbleSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        int n = array.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (controller.isCancelled())
                    return;

                controller.compare(j, j + 1);

                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;

                    swapped = true;
                    controller.swap(j, j + 1);
                }
            }
            controller.markSorted(n - 1 - i);
            if (!swapped)
                break;
        }
        if (n > 0 && !controller.isCancelled())
            controller.markSorted(0);
        controller.clearHighlights();
    }
}
