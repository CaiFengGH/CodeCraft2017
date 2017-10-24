package Algorithms;

import java.util.ArrayList;
import java.util.HashSet;

import AbstractDataStruct.Chromosome;
import AbstractDataStruct.Edge;
import AbstractDataStruct.Graph;
import AbstractDataStruct.ResidualGraph;
import AbstractDataStruct.Vertex;
import AbstractDataStruct.resEdge;

public class Deploy {
	
    public static String[] deployServer(String[] graphContent) {
        /** do your work here **/
        return display(graphContent);
    }
    
    public static String[] display(String[] graphContent) {
        int par;
        Chromosome best_individual = null;
        Chromosome tmp_individual = null;
        int best_fitness_score = Integer.MAX_VALUE;
        long deadline = 85000;
        Long end;
        Long begin = System.currentTimeMillis();
        //读图
        Graph G = Deploy.read(graphContent);
        //最优化网络
        Optimization opt = new Optimization(G, begin, deadline,graphContent);
        //寻找影响力最大点
        ArrayList<Vertex<Integer>> candidates = opt.influenceMax((int) (G.getNumberOfNodes() * 1));
        //最优化设置
        opt.setup(candidates);
        //opt.test();
        //针对不同的测试case
        if(G.getNumberOfNodes()>600){
            //高级
            par = 1;
        }else if(G.getNumberOfNodes()>200){
            //中级
            par = 0;
        }else {
            //低级
            par = -1;
        }
        for (int i = 0; i < 10; i++) {
            //初始化种群
        	opt.init(par);
        	//训练
            tmp_individual = opt.train();
            
            System.out.println("第"+i+"次成本："+tmp_individual.fitness_score);

            if (tmp_individual.fitness_score < best_fitness_score) {
                best_fitness_score = tmp_individual.fitness_score;
                best_individual = Chromosome.clone(tmp_individual);
            }
            end = System.currentTimeMillis();
            if (end - begin > deadline) {
                break;
            }
        }
        //输出最后结果
        String[] result = opt.getFinalResult(best_individual);
        
        System.out.println(best_fitness_score);
        
        System.out.println("耗时:"+(System.currentTimeMillis()-begin));

        return result;
    }
    
    /**
     * @desc 读图(将txt文件中的数据转换为图)
     */
    public static Graph read(String[] graphContent) {
        System.out.println(graphContent[0]);
        String[] data = graphContent[0].split(" ");
        int server_cost = Integer.parseInt(graphContent[2]);
        Graph G = new Graph(Integer.parseInt(data[0]) + Integer.parseInt(data[2]), Integer.parseInt(data[1]),
                Integer.parseInt(data[0]), Integer.parseInt(data[2]), server_cost);
        boolean node_client = true;
        for (int i = 4; i < graphContent.length; i++) {

            if (graphContent[i].equals("")) {
                node_client = false;
                continue;
            }
            if (node_client) {
                String[] parameter = graphContent[i].split(" ");
                G.addNodeEdge(new Edge(Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]),
                                Integer.parseInt(parameter[2]), Integer.parseInt(parameter[3]), Integer.parseInt(parameter[2])),
                        true);
            } else {
                String[] parameter = graphContent[i].split(" ");
                int self = Integer.parseInt(parameter[0]);
                int other = Integer.parseInt(parameter[1]);
                double value = 0;

                G.addNodeEdge(new Edge(self+G.getNumberOfNodes(),
                                Integer.parseInt(parameter[1]),
                                Integer.parseInt(parameter[2]),
                                0, Integer.parseInt(parameter[2])),
                        false);
            }
        }
        return G;
    }

    public static String stringReverse(String s, Graph g) {
        String result = "";
        String[] ss = s.split(" ");

		// 倒着遍历字符串数组，得到每一个元素
        for (int x = 0; x <ss.length-1; x++) {
            // 用新字符串把每一个元素拼接起来
            result += ss[x] + " ";
        }
        // 消费节点的id减去网络节点的数目，得到其真实的id
        int bw = Integer.parseInt(ss[ss.length-1]) - g.getNumberOfNodes();
        result = result + bw;
        return result;
    }
    /**
     * @desc 运用网络流的方法
     */
    public static String[] methodOfFlow(String[] graphContent,HashSet<Integer> client) {

        ResidualGraph g = new ResidualGraph();

        String[] dataNum = graphContent[0].split(" ");
        g.numOfNode = Integer.parseInt(dataNum[0]);
        g.numOfEdge = Integer.parseInt(dataNum[1]);
        g.numOfClient = Integer.parseInt(dataNum[2]);
        g.costOfServer = Integer.parseInt(graphContent[2]);

        for (int i = 0; i < g.numOfClient + g.numOfNode + 2; i++) {
            g.graph.put(i, new ArrayList<resEdge>());
        }
        // 初始化普通节点及边
        for (int i = 4; i < g.numOfEdge + 4; i++) {
            if (graphContent[i].split(" ").length == 4) {
                String[] data = graphContent[i].split(" ");
                //无向图
                resEdge edge = new resEdge(Integer.parseInt(data[0]), Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);
                resEdge edge1 = new resEdge(Integer.parseInt(data[1]), Integer.parseInt(data[0]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);
                g.graph.get(edge.start).add(edge);
                g.graph.get(edge1.start).add(edge1);
            }
        }
        // 初始化超级源点及消费节点
        // g.numOfNode + g.numOfClient + 1作为超级源点
        int demand = 0;
        g.graph.put(g.numOfNode + g.numOfClient + 1, new ArrayList<resEdge>());// 节点为超级源点
        for (int i = 0; i < g.numOfClient; i++) {
            if (graphContent[i + g.numOfEdge + 5].split(" ").length == 3) {
                String[] data = graphContent[i + g.numOfEdge + 5].split(" ");
                // 超级源点
                resEdge edg = new resEdge(g.numOfNode + g.numOfClient + 1, Integer.parseInt(data[0]) + g.numOfNode,
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(g.numOfNode + g.numOfClient + 1).add(edg);
                // 消费节点
                resEdge edge = new resEdge(Integer.parseInt(data[0]) + g.numOfNode, Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(edge.start).add(edge);

                demand += Integer.parseInt(data[2]);
            }
        }

        for (int k : client) {
            resEdge edg = new resEdge(k, g.numOfNode + g.numOfClient, Integer.MAX_VALUE, 0, true);
            g.graph.get(k).add(edg);// 注意这里的i是否就是对应与节点i呢？
        }

        // 获取最大流
        g.findMaxFlow(g.numOfNode + g.numOfClient + 1, g.numOfNode + g.numOfClient);

        //// -------测试输出---------
        System.out.println("候选服务器的个数：" + client.size());
//		Collections.sort(client);
        System.out.println(client);

        System.out.println("使用服务器的个数：" + g.useServer.size());
        Collections.sort(g.useServer);
        System.out.println(g.useServer);
        System.out.println("总需求: " + demand);
        System.out.println("最大流： "+g.maxFlow);
        System.out.println("最小费用流： "+g.minCost);
        System.out.println("总耗费：" + (g.minCost + g.costOfServer * g.useServer.size()));
        System.out.println(g.flowPath);

        //--------数据输出-------
        String[] data = g.flowPath.toString().split(" ;");
        String[] cont = new String[data.length + 2];
        cont[0] = String.valueOf(data.length);
        cont[1] = "";

        for (int i = 0; i < data.length; i++) {
            cont[i + 2] = data[i];
            // System.out.println(cont[i+2]);
        }
        return cont;
    }

    public static int methodOfFlowCost(String[] graphContent,HashSet<Integer> client){
        ResidualGraph g = new ResidualGraph();

        String[] dataNum = graphContent[0].split(" ");
        g.numOfNode = Integer.parseInt(dataNum[0]);
        g.numOfEdge = Integer.parseInt(dataNum[1]);
        g.numOfClient = Integer.parseInt(dataNum[2]);
        g.costOfServer = Integer.parseInt(graphContent[2]);

        for (int i = 0; i < g.numOfClient + g.numOfNode + 2; i++) {
            g.graph.put(i, new ArrayList<resEdge>());
        }

        // 初始化普通节点及边
        for (int i = 4; i < g.numOfEdge + 4; i++) {
            if (graphContent[i].split(" ").length == 4) {
                String[] data = graphContent[i].split(" ");

                //无向图
                resEdge edge = new resEdge(Integer.parseInt(data[0]), Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);
                resEdge edge1 = new resEdge(Integer.parseInt(data[1]), Integer.parseInt(data[0]),
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), true);

                g.graph.get(edge.start).add(edge);
                g.graph.get(edge1.start).add(edge1);

            }
        }

        // 初始化超级源点及消费节点。
        // g.numOfNode + g.numOfClient + 1作为超级源点
        int demand = 0;
        g.graph.put(g.numOfNode + g.numOfClient + 1, new ArrayList<resEdge>());// 节点为超级源点
        for (int i = 0; i < g.numOfClient; i++) {
            if (graphContent[i + g.numOfEdge + 5].split(" ").length == 3) {
                String[] data = graphContent[i + g.numOfEdge + 5].split(" ");
                // 超级源点
                resEdge edg = new resEdge(g.numOfNode + g.numOfClient + 1, Integer.parseInt(data[0]) + g.numOfNode,
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(g.numOfNode + g.numOfClient + 1).add(edg);
                // 消费节点
                resEdge edge = new resEdge(Integer.parseInt(data[0]) + g.numOfNode, Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]), 0, true);
                g.graph.get(edge.start).add(edge);

                demand += Integer.parseInt(data[2]);
            }

        }

        for (int k : client) {
            resEdge edg = new resEdge(k, g.numOfNode + g.numOfClient, Integer.MAX_VALUE, 0, true);
            g.graph.get(k).add(edg);// 注意这里的i是否就是对应与节点i呢？
        }

        // 获取最大流
        g.findMaxFlow(g.numOfNode + g.numOfClient + 1, g.numOfNode + g.numOfClient);

        return (g.minCost + g.costOfServer * g.useServer.size());
    }
}
