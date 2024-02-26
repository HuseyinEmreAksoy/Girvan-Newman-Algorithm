import java.io.*;
import java.util.*;

public class Main {

    static class Edge {
        int source;
        int target;

        public Edge(int source, int target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return source == edge.source && target == edge.target;
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }

    private Map<Integer, Set<Integer>> adjList;
    private Map<Edge, Integer> edgeBetweenness;

    public Main() {
        this.adjList = new HashMap<>();
        this.edgeBetweenness = new HashMap<>();
    }

    public void addEdge(int source, int target) {
        this.adjList.computeIfAbsent(source, k -> new HashSet<>()).add(target);
        this.adjList.computeIfAbsent(target, k -> new HashSet<>()).add(source);
        this.edgeBetweenness.put(new Edge(source, target), 0);
        this.edgeBetweenness.put(new Edge(target, source), 0);
    }

    public void computeBetweenness() {
        Main gra = new Main();

        for (Integer node : adjList.keySet()) {
            bfs(node);
            removeEdges();
            List<Set<Integer>> components = getConnectedComponents();
            if(getConnectedComponents().size() > 1){
                for (Set<Integer> component : components) {
                    List<Integer> sortedNodeIds = new ArrayList<>(component);
                    Collections.sort(sortedNodeIds);
                }
                break;
            }
        }
    }

    private void bfs(int source) {
        Map<Integer, List<Integer>> pred = new HashMap<>();
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> sigma = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Stack<Integer> stack = new Stack<>();

        adjList.keySet().forEach(node -> {
            pred.put(node, new ArrayList<>());
            dist.put(node, -1);
            sigma.put(node, 0);
        });

        dist.put(source, 0);
        sigma.put(source, 1);
        queue.add(source);

        while (!queue.isEmpty()) {
            int i = queue.remove();
            stack.push(i);
            for (int neig : adjList.get(i)) {
                if (dist.get(neig) < 0) {
                    dist.put(neig, dist.get(i) + 1);
                    queue.add(neig);
                }
                if (dist.get(neig) == dist.get(i) + 1) {
                    sigma.put(neig, sigma.get(neig) + sigma.get(i));
                    pred.get(neig).add(i);
                }
            }
        }

        Map<Integer, Double> delta = new HashMap<>();
        adjList.keySet().forEach(node ->
                delta.put(node, 0.0)
        );
        while (!stack.isEmpty()) {
            int w = stack.pop();
            List<Integer> predsW = pred.get(w);
            if (predsW == null) continue;
            for (int v : predsW) {
                Integer sigmaW = sigma.get(w);
                Integer sigmaV = sigma.get(v);
                Double deltaW = delta.get(w);
                if (sigmaW == null || sigmaV == null || deltaW == null) continue;

                double bet = ((double) sigmaV / sigmaW) * (1.0 + deltaW);
                if (bet > ((double) adjList.size() / 2)) {
                    continue;
                }
                Edge e = new Edge(v, w);
                edgeBetweenness.compute(e, (k, val) -> (val == null ? 0 : val) + (int) bet);
                delta.put(v, delta.getOrDefault(v, 0.0) + bet);
            }
        }
    }

    public void removeEdges() {
        int maxBetweenness = Collections.max(edgeBetweenness.values());
        Set<Edge> edgesToRemove = new HashSet<>();
        for (Edge e : edgeBetweenness.keySet()) {
            if (edgeBetweenness.get(e) == maxBetweenness) {

                edgesToRemove.add(e);
            }
        }
        for (Edge e : edgesToRemove) {


            adjList.get(e.source).remove(e.target);
            adjList.get(e.target).remove(e.source);
        }

    }

    public List<Set<Integer>> getConnectedComponents() {
        List<Set<Integer>> components = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        for (Integer node : adjList.keySet()) {
            if (!visited.contains(node)) {
                Set<Integer> component = new HashSet<>();
                explore(node, visited, component);
                components.add(component);
            }
        }
        return components;
    }

    private void explore(int node, Set<Integer> visited, Set<Integer> component) {
        visited.add(node);
        component.add(node);
        for (Integer neighbor : adjList.get(node)) {
            if (!visited.contains(neighbor)) {
                explore(neighbor, visited, component);
            }
        }
    }
    public void readGraphFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                int source = Integer.parseInt(parts[0]);
                int target = Integer.parseInt(parts[1]);
                addEdge(source, target);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeNodesOutsideLargestComponent() {
        Set<Integer> largestComponent = findLargestConnectedComponent();

        Iterator<Map.Entry<Integer, Set<Integer>>> it = adjList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Set<Integer>> entry = it.next();
            if (!largestComponent.contains(entry.getKey())) {
                it.remove();
            } else {
                entry.getValue().removeIf(neighbor -> !largestComponent.contains(neighbor));
            }
        }

        edgeBetweenness.keySet().removeIf(edge -> !largestComponent.contains(edge.source) || !largestComponent.contains(edge.target));
    }


    public Set<Integer> findLargestConnectedComponent() {
        List<Set<Integer>> components = getConnectedComponents();
        Set<Integer> largestComponent = null;
        int maxSize = 0;
        for (Set<Integer> component : components) {
            if (component.size() > maxSize) {
                maxSize = component.size();
                largestComponent = component;
            }
        }
        return largestComponent;
    }

    public static void main(String[] args) throws IOException {
        Main mainGra = new Main();

        mainGra.readGraphFromFile("../dataset/CA-HepPh.txt");
        Set<Integer> largestComponent = mainGra.findLargestConnectedComponent();

        mainGra.removeNodesOutsideLargestComponent();

        mainGra.computeBetweenness();
        while (mainGra.getConnectedComponents().size() == 1) {
            mainGra.removeEdges();
            mainGra.computeBetweenness();
        }

        List<Set<Integer>> components = mainGra.getConnectedComponents();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"))) {
            bw.write(String.valueOf(components.size()));
            bw.newLine();

            for (Set<Integer> component : components) {
                List<Integer> sortedNodeIds = new ArrayList<>(component);
                Collections.sort(sortedNodeIds);
                bw.write("--");
                bw.newLine();

                bw.write(String.valueOf(sortedNodeIds.size()));
                bw.newLine();

                for (Integer nodeId : sortedNodeIds) {
                    bw.write(nodeId.toString());
                    bw.newLine();
                }


            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
