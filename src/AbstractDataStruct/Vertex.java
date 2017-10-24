package AbstractDataStruct;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Ethan
 * @desc 节点类
 */
public class Vertex<Item> implements Comparable<Vertex<Item>> {
	//节点id
	final int id;
	//该节点是否为普通节点
	private boolean node_flag = false;
	//该节点是否为服务器
	private boolean client_flag = false;
	//度
	private int degrees = 0;
	//该节点拥有的边
	private HashMap<Integer, Edge> edges;
	//该节点的邻居节点
	private LinkedList<Vertex<Item>> neighbor;
    //性价比
    private double performance = 0.0;
    //每个节点对应一个字符串编码，方便对Vertex类hascode
    private String code;
    //该属性用于影响力最大化
    private double priority = 0;
    //节点的输出值
    private int outputs = 0;
    //如果是服务器，则该服务器的需求带宽为demand
    private int demand = 0;
    //记录被采样概率
    float visited_p = (float) 0.0;
    //子路径
    LinkedList<SubWalks> sub_walk_set = new LinkedList<SubWalks>();

    public Vertex(int id) {
        neighbor = new LinkedList<Vertex<Item>>();
        this.id = id;
        edges = new HashMap<Integer, Edge>();
        this.code = id + "";
    }

    /**
     * @desc 添加邻居
     * @param node 邻居节点
     */
    public void addNeighbors(Vertex<Item> node) {
    	if(node.isNode())
    		neighbor.add(degrees, node);
    	degrees += 1;
    	if (!this.isClient() && !node.isClient())
    		performance += (double) this.getWeight(node) / this.getValue(node);
    	if (!this.isClient())
    		priority += (double) this.getWeight(node) / this.getValue(node);
    }

    /**
     * @desc 对邻居进行排序
     */
    public void sortNeighbors(){
        Collections.sort(neighbor, new Comparator<Vertex<Item>>() {
            public int compare(Vertex<Item> o1, Vertex<Item> o2) {
                if (o1.performance > o2.performance)
                    return -1;
                else if (o1.performance < o2.performance)
                    return 1;
                return 0;
            }
        });
    }

    /**
     * @desc 对子路径进行排序
     */
    public void sortSubWalks(){
        Collections.sort(sub_walk_set, new Comparator<SubWalks>() {
            public int compare(SubWalks o1, SubWalks o2) {
                if(o1.getValue() > o2.getValue())
                    return 1;
                else if(o1.getValue() < o2.getValue())
                    return -1;
                return 0;
            }
        });
    }
    /**
     * @desc 返回子路径
     * @return 子路径
     */
    public LinkedList<SubWalks> getSubWalksSet(){
    	return sub_walk_set;
    }
    /**
     * @desc 设置需求带宽
     * @param demand 需求
     */
    public void setDemand(int demand) {
        this.demand = demand;
    }
    /**
     * @desc 获取需求带宽 
     * @return 返回需求带宽
     */
    public int getDemand() {
        return demand;
    }
    /**
     * @desc 是否是服务器
     */
    public boolean isClient() {
        return client_flag;
    }
    /**
     * @desc 是否是普通节点 
     */
    public boolean isNode() {
        return node_flag;
    }
    /**
     * @desc 设置为服务器 
     */
    public void setClient() {
        client_flag = true;
    }
    /**
     * @desc 设置为普通节点
     */
    public void setNode() {
        node_flag = true;
    }
    /**
     * @desc 获得该节点的所有邻居节点
     */
    public LinkedList<Vertex<Item>> getNeighbors() {
        return neighbor;
    }
    /**
     * @desc 获得度
     */
    public int getDegree() {
        return this.degrees;
    }
    /**
     * @desc 为该节点添加边
     */
    public void addEdge(Edge edge) {
        outputs += edge.weight;
        this.edges.put(edge.getOther(), edge);
    }
    /**
     * @desc 获得指定边上的权重（带宽）
     */
    public int getWeight(int other) {
        return this.edges.get(other).weight;
    }

    public int getWeight(Vertex<Item> other) {
        return getWeight(other.id);
    }
    /**
     * @desc 获得指定边上的单价
     */
    public int getValue(int other) {
        return this.edges.get(other).value;
    }

    public int getValue(Vertex<Item> other) {
        return getValue(other.id);
    }
    /**
     * @desc 获得该节点的性价比
     */
    public double getPerformance() {
        return performance;
    }
    /**
     * @desc 获取输出 
     */
    public int getOutputs() {
        return outputs;
    }
    /**
     * @desc 设置优先级
     */
    public void setPriority(double dv,int id){
    	Edge edge = edges.get(id);
    	double tv = edge.weight / edge.value;
    	//自定义的优先级规则
    	priority = (dv - 2 * tv - (dv - tv) * tv * 0.01);
    }
    /**
     * @desc 获取优先级
     */
    public double getPriority() {
    	return priority;
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
    	return this.id + "";
    }

    @Override
    public int compareTo(Vertex obj) {
        if (!(obj instanceof Vertex))
            new RuntimeException("cuowu");
        Vertex v = (Vertex) obj;

        if (this.getPriority() < v.getPriority())
            return 1;
        else if (this.getPriority() == v.getPriority())
            return this.code.compareTo(v.code);
        return -1;
    }
}
