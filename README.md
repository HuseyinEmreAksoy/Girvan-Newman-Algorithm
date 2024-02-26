Girvan-Newman method for graph partitioning on the giant component of your dataset until the giant component is no longer a single connected component. Notice that at each iteration the Girvan-Newman method is removing all the edges with the same betweenness value so it is possible that the giant component be divided into more than two connected components in your last iteration

run:

cd GirvanNewman

javac *.java

java Main
