package AbstractDataStruct;

/**
 * @author Ethan
 * @desc 存储路径中的每一步
 */
public class Step {
	//	选择的节点
	//	demand：该步骤提供了需求
	//	value:该步骤单价
	//	cost：该步骤成本
    Vertex<Integer> node;
    int demand = 0;
    double value = 0;
    int cost = 0;

    public Step(Vertex<Integer> node, int demand, double value) {
        this.node = node;
        this.demand = demand;
        this.value = value;
    }

    public String toString() {
        return "该节点为" + node + " 承担需求： " + demand + " 价格为:" + value;
    }
}
