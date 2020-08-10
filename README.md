# MapReduce Distributed Algorithm

A MapReduce program is composed of a map procedure, which performs filtering and sorting (such as sorting students by first name into queues, one queue for each name), and a reduce method, which performs a summary operation (such as counting the number of students in each queue, yielding name frequencies).

In this project, I implement the MapReduce algorithm using `Kompics` library in Java.

The program first read `mst.txt` file and running leader election algorithm to find root node and leaves, Then running MapReduce algorithm for counting the numbers of the words in a text file.