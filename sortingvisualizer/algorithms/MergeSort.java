package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class MergeSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) throws InterruptedException {
        mergeSortRecursive(array, 0, array.length - 1, controller);
        controller.clearHighlights();
    }

    private void mergeSortRecursive(int[] arr, int l, int r, SortController controller) throws InterruptedException {
        if (l < r) {
            if (controller.isCancelled())
                return;
            int m = l + (r - l) / 2;
            controller.subArray(l, r);

            mergeSortRecursive(arr, l, m, controller);
            if (controller.isCancelled())
                return;
            mergeSortRecursive(arr, m + 1, r, controller);
            if (controller.isCancelled())
                return;

            merge(arr, l, m, r, controller);
        } else if (l == r) {
            controller.markSorted(l);
        }
    }

    private void merge(int[] arr, int l, int m, int r, SortController controller) throws InterruptedException {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];

        controller.subArray(l, r);

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (controller.isCancelled())
                return;
            controller.compare(l + i, m + 1 + j);
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                controller.setValue(k, L[i]);
                i++;
            } else {
                arr[k] = R[j];
                controller.setValue(k, R[j]);
                j++;
            }
            controller.markSorted(k);
            k++;
        }
        while (i < n1) {
            if (controller.isCancelled())
                return;
            arr[k] = L[i];
            controller.setValue(k, L[i]);
            controller.markSorted(k);
            i++;
            k++;
        }
        while (j < n2) {
            if (controller.isCancelled())
                return;
            arr[k] = R[j];
            controller.setValue(k, R[j]);
            controller.markSorted(k);
            j++;
            k++;
        }
    }
}
