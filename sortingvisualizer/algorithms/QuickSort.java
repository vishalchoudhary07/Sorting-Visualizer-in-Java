package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class QuickSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        quickSortRecursive(array, 0, array.length - 1, controller);
        controller.clearHighlights();
    }

    private void quickSortRecursive(int[] arr, int low, int high, SortController controller)
            throws InterruptedException {
        if (low < high) {
            if (controller.isCancelled())
                return;
            int pi = partition(arr, low, high, controller);
            quickSortRecursive(arr, low, pi - 1, controller);
            quickSortRecursive(arr, pi + 1, high, controller);
        } else if (low == high && low >= 0 && low < arr.length) {
            controller.markSorted(low);
        }
    }

    private int partition(int[] arr, int low, int high, SortController controller) throws InterruptedException {
        int pivot = arr[high];
        controller.pivot(high);

        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (controller.isCancelled())
                return -1;
            controller.compare(j, high);
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                controller.swap(i, j);
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        controller.swap(i + 1, high);
        controller.markSorted(i + 1);
        controller.clearHighlights();
        return i + 1;
    }
}
