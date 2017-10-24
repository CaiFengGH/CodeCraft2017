package AbstractDataStruct;

import java.util.LinkedList;

/**
 * @author Ethan
 * @desc
 */
public class Graph {
	//	V：节点数量（普通节点+消费节点）
	//	num_nodes：普通节点的数量
	//	server_cost：服务器成本
	private final int V, num_nodes, server_cost;
	//	E:边的数量
	int E;
	//	num_client:服务器数量
	private int num_client = 0;
	//	vertexs：图中所有的节点
	private Vertex<Integer>[] vertexs;
	//	Edges:图中所有的边
	private Edge[] edges;
	//	clients:图中服务器的集合
	private LinkedList<Vertex<Integer>> clients = new LinkedList<Vertex<Integer>>();

    int edge_num = 0;

    /**
     * 给定图中，节点数量，边数量，普通节点数量，服务器数量，服务器成本
     */
    public Graph(int V, int E, int num_nodes, int num_client, int server_cost) {
        this.V = V;
        this.E = E * 2;
        this.num_nodes = num_nodes;
        this.server_cost = server_cost;
        vertexs = (Vertex<Integer>[]) new Vertex[this.V];
        for (int v = 0; v < this.V; v++) {
            vertexs[v] = new Vertex<Integer>(v);
        }
        edges = (Edge[]) new Edge[this.E];
    }

    /**
     * @desc 返回服务器成本
     */
    public int getServerCost() {
        return server_cost;
    }
    /**
     * @desc 为给定节点添加边 
     * 注：node_client:用来判断给节点是否为消费节点 True:普通节点  False:消费节点
     */
    public void addNodeEdge(Edge edge, boolean node_client) {
        int v = edge.getSelf();
        int e = edge.getOther();
        if (node_client) {
            vertexs[v].setNode();
            vertexs[e].setNode();
        } else {
            vertexs[v].setClient();
            vertexs[v].setDemand(edge.getWeight());
            clients.add(num_client, vertexs[v]);
            num_client += 1;
        }
        vertexs[v].addEdge(edge);
        vertexs[v].addNeighbors(vertexs[e]);

        Edge other_edge = new Edge(edge.getOther(), edge.getSelf(), edge.getWeight(), edge.getValue(),
                edge.getRemain());
        vertexs[e].addEdge(other_edge);
        vertexs[e].addNeighbors(vertexs[v]);
        if (node_client) {
            edges[edge_num] = edge;
            edge_num += 1;
            edges[edge_num] = other_edge;
            edge_num += 1;
        }
    }
    /**
     * @desc 返回图中所有节点
     */
    public Vertex<Integer>[] getVertexs() {
        return vertexs;
    }

    /**
     * @desc 返回图中所有消费节点
     */
    public LinkedList<Vertex<Integer>> getClients() {
        return clients;
    }
    /**
     * @desc 返回给定节点的度
     */
    public int getNodeDegree(int node) {
        return vertexs[node].getDegree();
    }
    /**
     * @desc 返回给定边上的权重（带宽）
     */
    public int getEdgeWeight(int self, int other) {
        return vertexs[self].getWeight(other);
    }
    /**
     * @desc 获得给定边上的单价
     */
    public double getEdgeValue(int self, int other) {
        return vertexs[self].getValue(other);
    }
    /**
     * @desc 获得给定节点的，邻居节点
     */
    public LinkedList<Vertex<Integer>> getNodeNeighbors(int node) {
        return vertexs[node].getNeighbors();
    }
    public LinkedList<Vertex<Integer>> getNodeNeighbors(Vertex<Integer> node) {
        return getNodeNeighbors(node.id);
    }
    /**
     * @desc 获得所有节点的数量
     */
    public int getNumberOfVertexes() {
        return V;
    }
    /**
     * @desc 获得边的数量
     */
    public int getNumberOfEdges() {
        return E;
    }
    /**
     * @desc 给定节点的id，获得该节点类
     */
    public Vertex<Integer> getNode(int id) {
        return vertexs[id];
    }
    /**
     * @desc 获得普通节点的数量
     */
    public int getNumberOfNodes() {
        return this.num_nodes;
    }
    /**
     * @desc 获得消费节点的数量
     */
    public int getNumberOfClient() {
        return this.num_client;
    }
}