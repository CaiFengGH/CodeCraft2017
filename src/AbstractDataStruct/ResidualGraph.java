package AbstractDataStruct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author Ethan
 * @desc 残留网络
 */
public class ResidualGraph {
	// 普通网络节点的数量，不包括消费节点
	int numOfNode;
	// 边的数量
    int numOfEdge;
    // 服务器的费用
    int costOfServer;
    //消费节点的费用
    int numOfClient;
    //最大流
    int maxFlow;
    //最小费用
    int minCost;
    //记录所有增广路径上节点及该路径的带宽
    StringBuilder flowPath = new StringBuilder();
    //记录使用的服务器
    ArrayList<Integer> useServer = new ArrayList<Integer>();
    //残留图
    Map<Integer, ArrayList<resEdge>> graph = new HashMap<Integer, ArrayList<resEdge>>();

    /**
     * @desc 获取源s到汇点t的最大流
     */
    public void findMaxFlow(int s, int t) {
        if (s == t)
            return;
        ////始终循环去找增广路径，直到路径为null退出循环
        while (true) {
            ArrayList<resEdge> pathEdge = new ArrayList<resEdge>();
            Deque<Integer> path = shortestPaths(s, t);
            if (path.isEmpty())
                break;

            path.addLast(numOfNode + numOfClient);// 超级汇点
            path.addFirst(numOfNode + numOfClient + 1);// 超级源点

            while (!path.isEmpty()) {
                int i = path.poll();
                Integer k = path.poll();
                if (k != null) {
                    path.addFirst(k);
                    for (resEdge edg : graph.get(i)) {
                        if (edg.end == k && edg.capacity > 0)
                            pathEdge.add(edg);
                    }
                }
            }
            augmentPath(pathEdge);
        }
    }
    /**
     * @desc 根据增广路径path来更新残留网络的属性值
     */
    public void augmentPath(ArrayList<resEdge> path) {
        int capacity = Integer.MAX_VALUE;
        for (resEdge edge : path)
            capacity = Math.min(capacity, edge.getCapacity());
        maxFlow += capacity;
        for (resEdge edge : path)
            minCost += (capacity * edge.unitcost);

        flowPath.append(capacity + " ;");
        for (resEdge edge : path) {

            // edge.reverse.reverse = edge;
            if (edge.reverse == null) {
                resEdge reverse = new resEdge(edge.end, edge.start, 0, -edge.unitcost, false);

                edge.setReverse(reverse);
                reverse.setReverse(edge);
                graph.get(edge.end).add(reverse);
            }
            edge.addFlow(capacity);
        }

    }

    /**
     * @desc 寻找最小费用路径 
     */
    public Deque<Integer> shortestPaths(int source, int t) {
        Queue<Integer> queue = new ArrayDeque<Integer>();

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (int node : graph.keySet())
            result.put(node, Integer.MAX_VALUE);
        result.put(source, 0);
        
        //前向节点，用于记录节点的最小费用路径的前向节点，默认初始化为超级源点的的位置
        int[] pre = new int[graph.size()];
        for (int i = 0; i < pre.length; i++) {
            pre[i] = (numOfClient + numOfNode + 1);
        }
        queue.add(source);

        while (!queue.isEmpty()) {
            int i = queue.poll();
            // int[] copyPre = Arrays.copyOf(pre, pre.length);
            for (resEdge edge : graph.get(i)) {
                if (edge.isOriginal() && edge.capacity > 0) {
                    if (result.get(edge.end) > edge.getUnitcost() + result.get(i)) {
                        queue.add(edge.end);
                        result.put(edge.end, edge.getUnitcost() + result.get(i));
                        pre[edge.end] = i;
                    } else {
                        result.put(edge.end, result.get(edge.end));
                    }
                }
            }
        }
        Deque<Integer> path = new ArrayDeque<Integer>();
        // path.addFirst(4);
        int tt = t;
        //循环前向数组来记录增广路径
        while (pre[t] != (numOfClient + numOfNode + 1)) {
            if (!useServer.contains(pre[tt])) {
                useServer.add(pre[tt]);//// 记录使用的服务器
            }
            path.addFirst(pre[t]);

            if (pre[pre[t]] == (numOfClient + numOfNode + 1)) {
                flowPath.append((pre[t] - numOfNode) + " ");
            } else {
                flowPath.append(pre[t] + " ");
            }
            t = pre[t];
        }
        return path;
    }

}