package AbstractDataStruct;

/**
 * @author Ethan
 * @desc 边类
 */
public class Edge {
	//边起点
    final int v;
    //边终点
    final int e;
    //边权重
    final int weight;
    //边单价
    final int value;
    //边余量
    int remain;

    public Edge(int v, int e, int w, int val, int remain) {
        this.weight = w;
        this.value = val;
        this.v = v;
        this.e = e;
        this.remain = remain;
    }
    /**
     * @desc 返回节点本身id 
     */
    public int getSelf() {
        return this.v;
    }
    /**
     * @desc 返回另一端节点
     */
    public int getOther() {
        return this.e;
    }
    /**
     * @desc 返回边权重
     */
    public int getWeight() {
        return weight;
    }
    /**
     * @desc 返回边单价
     */
    public int getValue() {
        return value;
    }
    /**
     * @desc 返回边余量
     */
    public int getRemain() {
        return remain;
    }
}
