package sortingvisualizer.algorithms;

import sortingvisualizer.ISortingAlgorithm;
import sortingvisualizer.SortController;

public class HeapSort implements ISortingAlgorithm {

    @Override
    public String getName() {
        return "Heap Sort";
    }

    @Override
    public void runSort(int[] array, SortController controller) {
        int n = array.length;

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--) {
            if (controller.isCancelled()) return;
            heapify(array, n, i, controller);
        }

        // One by one extract an element from heap
        for (int i = n - 1; i > 0; i--) {
            if (controller.isCancelled()) return;
            
            // Move current root to end
            try {
                controller.swap(0, i);
                int temp = array[0];
                array[0] = array[i];
                array[i] = temp;
                
                controller.markSorted(i);
            } catch (InterruptedException e) {
                return;
            }

            // call max heapify on the reduced heap
            heapify(array, i, 0, controller);
        }
        try {
            controller.markSorted(0);
        } catch (InterruptedException e) {
            return;
        }
    }

    void heapify(int[] array, int n, int i, SortController controller) {
        if (controller.isCancelled()) return;

        int largest = i; // Initialize largest as root
        int l = 2 * i + 1; // left = 2*i + 1
        int r = 2 * i + 2; // right = 2*i + 2

        try {
            // If left child is larger than root
            if (l < n) {
                controller.compare(l, largest);
                if (array[l] > array[largest])
                    largest = l;
            }

            // If right child is larger than largest so far
            if (r < n) {
                controller.compare(r, largest);
                if (array[r] > array[largest])
                    largest = r;
            }

            // If largest is not root
            if (largest != i) {
                controller.swap(i, largest);
                int swap = array[i];
                array[i] = array[largest];
                array[largest] = swap;

                // Recursively heapify the affected sub-tree
                heapify(array, n, largest, controller);
            }
        } catch (InterruptedException e) {
            // Thread interrupted, just return
        }
    }
}
