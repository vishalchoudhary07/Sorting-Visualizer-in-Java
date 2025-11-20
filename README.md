# Sorting Algorithm Visualizer

A modern Java Swing application that visualizes various sorting algorithms with real-time statistics and a beautiful dark theme interface.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)

## ✨ Features

### 🎨 Modern Dark Theme UI
- Sleek dark interface with vibrant color-coded visualizations
- Intuitive control panel with styled buttons
- Responsive design with smooth animations

### 📊 Real-time Statistics
- **Comparisons Counter**: Tracks comparison operations
- **Swaps Counter**: Tracks swap operations
- Live updates during sorting process

### 🔄 8 Sorting Algorithms
1. **Bubble Sort** - Simple comparison-based algorithm
2. **Selection Sort** - In-place comparison sort
3. **Insertion Sort** - Builds sorted array incrementally
4. **Merge Sort** - Divide and conquer algorithm
5. **Quick Sort** - Efficient partition-based sort
6. **Counting Sort** - Non-comparison integer sorting
7. **Radix Sort** - Digit-by-digit sorting
8. **Heap Sort** ⭐ - Binary heap-based sorting

### 🎮 Interactive Controls
- **Algorithm Selection**: Choose from 8 different algorithms
- **Array Size Slider**: Adjust array size (10-250 elements)
- **Speed Control**: Control visualization speed
- **Reset Function**: Generate new random arrays

## 🏗️ Architecture

The project follows a modular, object-oriented design:

```
sortingvisualizer/
├── SortingVisualizer.java    # Main application
├── SortPanel.java             # Visualization panel
├── Algorithm.java             # Algorithm enum
├── ISortingAlgorithm.java     # Algorithm interface
├── SortController.java        # Controller interface
├── SortStep.java              # Visualization event class
└── algorithms/
    ├── BubbleSort.java
    ├── SelectionSort.java
    ├── InsertionSort.java
    ├── MergeSort.java
    ├── QuickSort.java
    ├── CountingSort.java
    ├── RadixSort.java
    └── HeapSort.java
```

## 🚀 Getting Started

### Prerequisites
- Java JDK 8 or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code) or command line

### Compilation
```bash
javac -d bin -sourcepath . sortingvisualizer/SortingVisualizer.java
```

### Running
```bash
java -cp bin sortingvisualizer.SortingVisualizer
```

## 🎯 How to Use

1. **Select Algorithm**: Choose a sorting algorithm from the dropdown menu
2. **Adjust Array Size**: Use the size slider to set the number of elements
3. **Control Speed**: Adjust the speed slider for faster/slower visualization
4. **Start Sorting**: Click "Start Sort" to begin visualization
5. **Reset**: Click "Reset Array" to generate a new random array

## 🎨 Color Scheme

| Element | Color | Meaning |
|---------|-------|---------|
| Steel Blue | `#4682B4` | Default bars |
| Gold | `#FFD700` | Comparing elements |
| Orange Red | `#FF4500` | Swapping elements |
| Lime Green | `#32CD32` | Sorted elements |
| Medium Orchid | `#BA55D3` | Pivot element (Quick Sort) |
| Cyan | `#00FFFF` | Sub-array focus (Merge Sort) |

## 📚 Learning Resources

Each algorithm has its own implementation demonstrating:
- Time complexity characteristics
- Step-by-step sorting process
- Comparison and swap operations
- Visual feedback for understanding

## 🤝 Contributing

Contributions are welcome! To add a new sorting algorithm:

1. Create a new class in `sortingvisualizer/algorithms/`
2. Implement the `ISortingAlgorithm` interface
3. Add the algorithm to the `Algorithm` enum
4. Update the switch statement in `SortingVisualizer.java`

## 📝 License

This project is open source and available for educational purposes.

## 🙏 Acknowledgments

- Original concept for educational visualization of sorting algorithms
- Refactored and enhanced with modern UI and architecture

---

**Made with ☕ and Java Swing**
