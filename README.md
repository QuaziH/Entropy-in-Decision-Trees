# Entropy-in-Decision-Trees
A dataset classifier for several continuous and categorical datasets

The data files are all inside the Data folder. For convenience, a dataset either has all continuous features (inside the folder “Continuous”), or has all categorical features (inside the folder “Categorical”, and start with ‘CAT_’).

To run the program first compile the java files and then run the DTMain file with training and testing feature file and the training and testing label file alongside the max height and the maximum number of leaves.

For example

```
java DTMain Data/Continuous/iris_train_fea.txt Data/Continuous/iris_train_label.txt Data/Continuous/iris_test_fea.txt Data/Continuous/iris_test_label.txt 3 5
```

will run the program on the iris dataset, using max height=3 and maximum number of leaves=5.
