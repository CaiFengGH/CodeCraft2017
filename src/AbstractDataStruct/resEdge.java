package AbstractDataStruct;

/**
 * @author Ethan
 * @desc 残留边类
 */
public class resEdge {
	//边的起始节点
    public final int start;
    //目标节点
    public final int end;
    //是否为残留边   false表示该边是原始边
    public final boolean isResidual;
    //反向边
    public resEdge reverse;
    //容量，即带宽
    public int capacity;
    //单位带宽费用
    public int unitcost;
    //流经该边的流
    public int flow;

    public resEdge(int start, int end, int capacity, int unitcost, boolean isOriginal) {
        this.start = start;
        this.end = end;
        this.capacity = capacity;
        this.unitcost = unitcost;
        this.isResidual = !isOriginal;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUnitcost() {
        return unitcost;
    }
    /**
     * @desc 更新残留网络中边的流
     */
    public void addFlow(int amount) {
        if (amount < 0) {
            reverse.addFlow(-amount);
            return;
        }
        capacity -= amount;
        flow += amount;
        reverse.capacity += amount;
    }

    public resEdge getReverse() {
        return reverse;
    }

    public void setReverse(resEdge reverse) {
        this.reverse = reverse;
    }

    public boolean isOriginal() {
        return !isResidual;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setUnitcost(int unitcost) {
        this.unitcost = unitcost;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }
}