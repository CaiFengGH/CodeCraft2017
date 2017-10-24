package Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import AbstractDataStruct.Chromosome;
import AbstractDataStruct.Graph;
import AbstractDataStruct.SubWalks;
import AbstractDataStruct.Vertex;

/**
 * @author Ethan
 * @desc
 */
public class Optimization {
    /*
    种群基本信息配置
    best_individual:最优个体
    global_best_fitness：最有适应度值
    gen_size：染色体大小
    pop_size:种群大小
    max_gen:最大迭代次数
    mutation_rate:变异概率
    candidates：候选种子集
    population:种群
     */
    String[] graphContent;
    private Chromosome sample_individual;
    private Chromosome best_individual;
    private int global_best_fitness = Integer.MAX_VALUE;
    private int gen_size;
    private int pop_size = 200;
    private final int max_gen = 1000;
    private final int mate_size = (int) (pop_size * 0.5);
    private final double mutation_rate = 0.5;
    static private ArrayList<Vertex<Integer>> candidates;
    private ArrayList<Chromosome> population;
    private int end_condition;

    // 寻找路径所需变量
    /*
    performance_init：性价比
    outputs_init:每个节点的输出带宽
    weights_init：两个边上的带宽
    clients_init:消费节点
     */
    static private double max_performance = 0;
    static private double min_performance = Integer.MAX_VALUE;
    static private double[] normalization_performance;

    static private int total_visited_num = 0;
    static private int[] visited_num;
    static double[] visited_p;

    static private int[][] values_init;
    static private double[] performance_init;
    static private int[] outputs_init;
    static private int[][] weights_init;
    static private LinkedList<Vertex<Integer>> clients_init;
    static Graph G;

    // 计时器
    long start_time;
    long deadline;

    //
    public Optimization(Graph G, long start_time, long deadline,String[] graphContent) {
        this.deadline = deadline;
        this.start_time = start_time;
        this.G = G;
        this.graphContent=graphContent;
    }

    /**
     * @desc 测试
     */
    public void test(){
        SubWalks.weights = weights_init;
        Vertex<Integer> node;
        LinkedList<Vertex<Integer>> one_hop;
        LinkedList<Vertex<Integer>> two_hop;
        int value;
        SubWalks sub_walk;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            node = G.getNode(i);
            one_hop = node.getNeighbors();
            for(Vertex<Integer> node1:one_hop){
                value = node.getValue(node1);
                sub_walk = new SubWalks(node ,value,true);
                sub_walk.add(node1);
                node.sub_walk_set.add(sub_walk);
                two_hop = node1.getNeighbors();
                for(Vertex<Integer> node2:two_hop){
                    if(node2.id != node.id){
                        sub_walk = new SubWalks(node ,value + node1.getValue(node2),false);
                        sub_walk.add(node1);
                        sub_walk.add(node2);
                        node.sub_walk_set.add(sub_walk);
                    }
                }
            }
            LinkedList<SubWalks> sub_walks = node.getSubWalksSet();
            node.sortSubWalks();
            for(SubWalks walk : sub_walks){
                System.out.println(walk);
            }
        }
    }

    /**
     * @desc 初始化设置
     */
    public void setup(ArrayList<Vertex<Integer>> candidates){
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        this.candidates = candidates;
        this.gen_size = G.getNumberOfNodes();
        visited_num = new int[G.getNumberOfNodes()];
        visited_p = new double[G.getNumberOfNodes()];
        best_individual = null;
        normalization_performance = new double[G.getNumberOfNodes()];
        performance_init = new double[G.getNumberOfVertexes()];
        outputs_init = new int[G.getNumberOfVertexes()];
        weights_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        values_init = new int[G.getNumberOfVertexes() ][G.getNumberOfVertexes()];
        clients_init = G.getClients();

        //对消费者排序
        Collections.sort(clients_init, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if (o1.getDemand() > o2.getDemand())
                    return -1;
                else if (o1.getDemand() < o2.getDemand())
                    return 1;
                else
                    return 0;
            }
        });
        //
        LinkedList<Vertex<Integer>> two_hop;
        int value;
        SubWalks sub_walk;
        Vertex<Integer> node;
        for (int i = 0; i < G.getNumberOfVertexes(); i++) {
            node = G.getNode(i);
            performance_init[i] = node.getPerformance() ;
            outputs_init[i] = node.getOutputs();
            LinkedList<Vertex<Integer>> neighbors = node.getNeighbors();
            for (Vertex<Integer> neighbor : neighbors) {
                if (neighbor.isNode()){
                    weights_init[i][neighbor.id] = node.getWeight(neighbor);
                    values_init[i][neighbor.id] = node.getValue(neighbor);
                }
                if (node.isClient())
                    weights_init[node.id][neighbor.id] = node.getDemand();
                value = node.getValue(neighbor);
                sub_walk = new SubWalks(node ,value,true);
                sub_walk.add(neighbor);
                node.sub_walk_set.add(sub_walk);
                two_hop = neighbor.getNeighbors();
                for(Vertex<Integer> node2:two_hop){
                    if(node2.id != node.id){
                        sub_walk = new SubWalks(node ,value + neighbor.getValue(node2),false);
                        sub_walk.add(neighbor);
                        sub_walk.add(node2);
                        node.sub_walk_set.add(sub_walk);
                    }
                }
                node.sortSubWalks();

            }
            if(node.isNode()){
                node.sortNeighbors();
                if(performance_init[i] > max_performance){
                    max_performance = performance_init[i];
                }else if(performance_init[i] < min_performance){
                    min_performance = performance_init[i];
                }
            }
        }
        //归一化
        for(int i = 0; i < normalization_performance.length; i++){
            normalization_performance[i] = (performance_init[i]-min_performance)/(max_performance-min_performance);
        }

        //生成样本解
        sample_individual = new Chromosome();
        sample_individual.initGeneSize(gen_size);

        int idx;
        LinkedList<Vertex<Integer>> set = new LinkedList<Vertex<Integer>>();
        for(Vertex<Integer> client : clients_init) {
            idx = client.getNeighbors().getFirst().id;
            sample_individual.gene[idx] = true;
            set.add(client.getNeighbors().getFirst());
        }
        Collections.sort(set, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if(o1.getPerformance() > o2.getPerformance())
                    return 1;
                else if(o1.getPerformance() < o2.getPerformance())
                    return -1;
                return 0;
            }
        });
        for(int i=0; i<set.size();i++){
            sample_individual.gene[set.get(i).id] = false;
            tmp_cost = anotherGetCostNew(sample_individual);
            if(tmp_cost < min_cost){
                min_cost = tmp_cost;
            }else {
                sample_individual.gene[set.get(i).id] = true;
            }
        }
    }
    /**
     * @desc 种群初始化
     */
    public void init(int par) {
        long end_time;
        int min_cost = Integer.MAX_VALUE;
        int tmp_cost = 0;
        best_individual = null;

        //初始化种群
        population = new ArrayList<Chromosome>();
        int idx = 0;
        min_cost = Integer.MAX_VALUE;
        tmp_cost = 0;
        System.out.println("初始化");
        if(par==1){
            pop_size = 50;
            end_condition = 200;
        }else if(par==0){
            pop_size = 100;
            end_condition = 100;
        }else {
            pop_size = 200;
            end_condition = 50;
        }
        for (int i = 0; i < pop_size; i++) {
            Chromosome individual;
            if(i < (int)(pop_size*0.1)){
                individual = new Chromosome();
                individual.initGeneSize(gen_size);
                for(int j = 0; j < sample_individual.gene.length; j++){
                    individual.gene[j] = sample_individual.gene[j];
                }
            }else if(i < (int)(pop_size*0.5)){
                individual = new Chromosome(gen_size,
                        normalization_performance,
                        clients_init,
                        candidates,
                        (float) 0.6,
                        sample_individual);
            }else {
                individual = new Chromosome(gen_size,
                        normalization_performance,
                        clients_init,
                        candidates,
                        (float) 0.6);
            }
            tmp_cost = getCostNew(individual);
            individual.fitness_score = tmp_cost;
            population.add(individual);
            if (min_cost > tmp_cost) {
                min_cost = tmp_cost;
                best_individual = Chromosome.clone(individual);
                global_best_fitness = min_cost;
            }
        }
    }

    /**
     * @desc 训练
     */
    public Chromosome train() {
        System.out.println("训练");
        int count = 0;
        int last_min_cost = 0;
        int min_cost, tmp_cost;
        Long end_time;
        for (int t = 0; t < max_gen; t++) {

            min_cost = global_best_fitness;
            tmp_cost = 0;
            
            population = genNextPopulation();

            for (Chromosome individual : population) {
                tmp_cost = getCostNew(individual);
                individual.fitness_score = tmp_cost;
                if (min_cost > tmp_cost) {
                    min_cost = tmp_cost;
                    best_individual = Chromosome.clone(individual);
                    global_best_fitness = min_cost;
                    //局部搜索
                    localSearch();
                }
                end_time = System.currentTimeMillis();
                //判断是否超时
                if (end_time - start_time > deadline) {
                    return best_individual;
                }
            }

            //判断是否收敛
            if (last_min_cost == global_best_fitness) {
                count += 1;
                if (count == end_condition) {
                    return best_individual;
                }
            } else {
                count = 0;
            }
            last_min_cost = min_cost;
            end_time = System.currentTimeMillis();
            //判断是否超时
            if (end_time - start_time > deadline) {
                return best_individual;
            }
            System.out.println(t+"   "+global_best_fitness);
        }
        return best_individual;
    }

    /**
     * @desc 选择
     */
    public ArrayList<Chromosome> selection() {
        ArrayList<Chromosome> next_population = new ArrayList<Chromosome>();
        next_population.add(best_individual);
        while (next_population.size() < mate_size) {
            Random rand = new Random();
            int i = rand.nextInt(pop_size);
            int j = rand.nextInt(pop_size);
            if (population.get(i).fitness_score < population.get(j).fitness_score)
                next_population.add(Chromosome.clone(population.get(i)));
            else
                next_population.add(Chromosome.clone(population.get(j)));
        }
        return next_population;
    }

    /**
     * @desc 交叉 
     */
    public void crossover(ArrayList<Chromosome> next_population) {
        Chromosome a;
        Chromosome b;
        int start_index;
        int end_index;
        int tmp;
        Chromosome a_next;
        Chromosome b_next;
        while (next_population.size() < pop_size) {
            Random random = new Random();
            a = next_population.get(random.nextInt(mate_size));
            b = next_population.get(random.nextInt(mate_size));
            start_index = random.nextInt(gen_size);
            end_index = random.nextInt(gen_size);
            if(start_index > end_index){
                tmp = start_index;
                start_index = end_index;
                end_index = start_index;
            }

            a_next = new Chromosome();
            b_next = new Chromosome();
            a_next.initGeneSize(gen_size);
            b_next.initGeneSize(gen_size);

            for (int i = 0; i < start_index; i++) {
                a_next.gene[i] = a.gene[i];
                b_next.gene[i] = b.gene[i];
            }
            for (int i = start_index; i < end_index; i++) {
                a_next.gene[i] = b.gene[i];
                b_next.gene[i] = a.gene[i];
            }
            for (int i = end_index; i < gen_size; i++) {
                a_next.gene[i] = a.gene[i];
                b_next.gene[i] = b.gene[i];
            }
            next_population.add(a_next);
            next_population.add(b_next);
        }
    }
    /**
     * @desc 变异
     */
    public void mutate(ArrayList<Chromosome> next_population) {
        Chromosome individual;
        Random random = new Random();
        Vertex<Integer> node;
        int idx;
        for (int i = 1; i < next_population.size(); i++) {
            individual = next_population.get(i);
            if (Math.random() < mutation_rate) {
                for(int t = 0; t < 9; t++){
                    node = candidates.get(random.nextInt(candidates.size()));
                    idx = node.id;
                    individual.gene[idx] = 0.5 > Math.random();
                }
            }
        }
    }
    //局部搜索
    public void localSearch() {
        Chromosome tmp = Chromosome.clone(best_individual);
        int min_cost = global_best_fitness;
        int tmp_cost = 0;
        System.out.println("-----");
        for(int i = 0; i < tmp.gene.length; i++){
            if(tmp.gene[i]){
                tmp.gene[i] = !tmp.gene[i];
                tmp_cost = anotherGetCostNew(tmp);
                if (tmp_cost < min_cost) {
                    min_cost = tmp_cost;
                    System.out.println("+++++");
                    tmp.fitness_score = tmp_cost;
                    best_individual = Chromosome.clone(tmp);
                    best_individual.fitness_score = min_cost;
                    global_best_fitness = min_cost;
                } else {
                    tmp.gene[i] = !tmp.gene[i];
                }
            }
        }
    }

    public void anotherlocalSearch() {
        Chromosome tmp = Chromosome.clone(best_individual);
        boolean flag = true;
        Random random;
        int idx;
        int min_cost = global_best_fitness;
        int tmp_cost = 0;
        while (flag) {
            random = new Random();
            for(int i=0; i < 7; i++){
                idx = random.nextInt(gen_size);
                if(normalization_performance[idx]>Math.random())
                    tmp.gene[idx] = true;
                else
                    tmp.gene[idx] = false;
            }
            tmp_cost = getCostNew(tmp);
            if (tmp_cost < min_cost) {
                System.out.println("******");
                min_cost = tmp_cost;
                tmp.fitness_score = tmp_cost;
                best_individual = Chromosome.clone(tmp);
                best_individual.fitness_score = min_cost;
                global_best_fitness = min_cost;
            } else {
                flag = false;
            }
        }
    }
    /**
     * @desc 获得下一代
     */
    public ArrayList<Chromosome> genNextPopulation() {
        ArrayList<Chromosome> next_population = selection();
        crossover(next_population);
        mutate(next_population);
        anotherlocalSearch();
        return next_population;
    }

    //获得结果
    public String[] getResult(Chromosome individual) {
        int idx = 0;
        String[] result;
        String tmp;
        boolean[] gen = individual.getGene();
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        for (int i = 0; i < gen.length; i++) {
            if (gen[i])
                server_set.add(G.getNode(i));
        }
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());
        result = new String[walks.size()];

        for (LinkedList<Step> walk : walks) {
            tmp = "";
            for (Step step : walk) {
                tmp += (step.node + " ");
                if (server_set.contains(step.node)) {
                    tmp += (";" + walk.getLast().demand);
                    break;
                }
            }
            result[idx] = tmp;
            idx += 1;
        }


        return result;
    }
    
    /**
     * @desc 获取最终结果 
     */
    public String[] getFinalResult(Chromosome individual) {
        String tmp;
        int idx = 0;
        String[] result;
        int support;
        Vertex<Integer> server;
        HashSet<Integer> server_set = new HashSet<Integer>();
        
        HashSet<Vertex<Integer>> final_server_set = new HashSet<Vertex<Integer>>();
        
        for(int i = 0; i < individual.gene.length; i++){
            if(individual.gene[i])
                server_set.add(G.getNode(i).id);
        }
        
        result=Deploy.methodOfFlow(graphContent, server_set);

        return result;
    }
    
    
    /**
     * @desc 按照一定规则寻找网络中影响力最大化点集合
     */
    public ArrayList<Vertex<Integer>> influenceMax(int keyNum){
        ArrayList<Vertex<Integer>> maxVertexs = new ArrayList<Vertex<Integer>>();

        TreeSet<Vertex> sortVertexsByPriority = new TreeSet<Vertex>();
        //此处是原方法的基于度的节点的存储方式
        ArrayList<Integer> vertexsOfNotClient = new ArrayList<Integer>();

        Vertex<Integer>[] vertexs = this.G.getVertexs();
        LinkedList<Vertex<Integer>> clients = this.G.getClients();

        for(Vertex v : vertexs){
            if(clients.contains(v)){
                continue;
            }
            sortVertexsByPriority.add(v);
            vertexsOfNotClient.add(v.id);
        }

        for(int j=0;j < keyNum;j++){
            Vertex v = sortVertexsByPriority.pollFirst();
            maxVertexs.add(v);
            LinkedList<Vertex> neighbor = v.getNeighbors();
            int value = 0;
            for(Vertex vn : neighbor){
                if(!maxVertexs.contains(vn) && !clients.contains(vn)){
                    sortVertexsByPriority.remove(vn);
                    vn.setPriority(vn.getPriority(), v.id);
                    sortVertexsByPriority.add(vn);
                }
            }
        }
        return maxVertexs;
    }
    
    public ArrayList<Vertex<Integer>> influenceMax(){
        ArrayList<Vertex<Integer>> candidates = new ArrayList<Vertex<Integer>>();
        int[][] value_init = new int[G.getNumberOfVertexes()][G.getNumberOfVertexes()];
        int[] visited_num_tmp = new int[G.getNumberOfVertexes()];
        int[] values = new int[G.getNumberOfVertexes()];
        LinkedList<Vertex<Integer>> neighbors;
        LinkedList<Vertex<Integer>> current_neighbors;
        LinkedList<Vertex<Integer>> next_neighbors;

        Vertex<Integer> node;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            node = G.getNode(i);
            neighbors = node.getNeighbors();
            for(Vertex<Integer> neighbor : neighbors){
                value_init[node.id][neighbor.id] = (int)node.getValue(neighbor);
            }
        }
        for(Vertex<Integer> client : G.getClients()){
            for(int idx = 0; idx < values.length; idx++){
                values[idx] = Integer.MAX_VALUE;
            }
            current_neighbors = new LinkedList<Vertex<Integer>>();
            values[client.id] = 0;
            current_neighbors.add(client);
            for(int i = 0; i < 3; i++){
                next_neighbors = new LinkedList<Vertex<Integer>>();
                while (!current_neighbors.isEmpty()){
                    node = current_neighbors.removeFirst();
                    neighbors = node.getNeighbors();
                    for(Vertex<Integer> neighbor : neighbors){
                        visited_num_tmp[neighbor.id] +=1;
                        next_neighbors.add(neighbor);
                    }
                }
                current_neighbors = next_neighbors;
            }
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            if(visited_num_tmp[i]!=0){
                if(visited_num_tmp[i] > max){
                    max = visited_num_tmp[i];
                }
                if(visited_num_tmp[i] < min){
                    min = visited_num_tmp[i];
                }
            }
        }
        for(int i = 0; i < G.getNumberOfNodes(); i++){
            G.getNode(i).visited_p = ((visited_num_tmp[i] - min)/(float)(max-min));
            candidates.add(G.getNode(i));

        }
        Collections.sort(candidates, new Comparator<Vertex<Integer>>() {
            public int compare(Vertex<Integer> o1, Vertex<Integer> o2) {
                if(o1.visited_p > o2.visited_p)
                    return -1;
                else if(o1.visited_p < o2.visited_p)
                    return 1;
                if(o1.getPerformance() > o2.getPerformance())
                    return -1;
                else if(o1.getPerformance() < o2.getPerformance())
                    return 1;
                return 0;
            }
        });
        return candidates;
    }

   
    
  //目标函数，获得每个解得成本
    public int getCostNew(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        //server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }
        //寻找该解下的所有路径
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());

        //计算成本
        individual.gene = new boolean[individual.size];
        for (LinkedList<Step> walk : walks) {
            for (Step step : walk) {
                //确保当路径到达新增服务器时，停止计算成本
                /*
                例如：
                0->1->2->5->4
                其中，2号节点在之后被选择为服务器。则计算成本是，应当到达2号节点，停止。
                 */
                if (server_set.contains(step.node)) {
                    sets.add(step.node.id);
                    result += step.cost;
                    total_visited_num +=1;
                    break;
                }
            }
        }

        for(Integer i : sets){
            visited_num[i] +=1;
            individual.gene[i] = true;
        }
        result += G.getServerCost() * sets.size();
        return result;
    }

    
    public int anotherGetCostNew(Chromosome individual) {
        int result = 0;
        boolean[] gen = individual.getGene();
        //server_set：服务器集合
        HashSet<Vertex<Integer>> server_set = new HashSet<Vertex<Integer>>();
        HashSet<Integer> sets = new HashSet<Integer>();
        for (int idx = 0; idx < gen.length; idx++) {
            if (gen[idx])
                server_set.add(G.getNode(idx));
        }
        //寻找该解下的所有路径
        LinkedList<LinkedList<Step>> walks = findWalksNew(G, server_set, clients_init, performance_init, outputs_init,
                weights_init, G.getNumberOfVertexes());

        //计算成本
        for (LinkedList<Step> walk : walks) {
            for (Step step : walk) {
                if (server_set.contains(step.node)) {
                    sets.add(step.node.id);
                    result += step.cost;
                    break;
                }
            }
        }
        result += G.getServerCost() * sets.size();
        return result;
    }

    //寻找所有路径
    public static LinkedList<LinkedList<Step>> findWalksNew(Graph G, HashSet<Vertex<Integer>> server_set,
                                                         LinkedList<Vertex<Integer>> clients, double[] performance, int[] outputs, int[][] weights, int len) {

        LinkedList<LinkedList<Step>> walks = new LinkedList<LinkedList<Step>>();
        //outputs_neighbor_server，该节点的邻居节点有服务器，计算其与服务器的带宽和
        //neighbor_has_server：判断该节点的邻居节点是否有服务器
        boolean[] neighbor_has_server = new boolean[len];
        int[] outputs_neighbor_server = new int[len];
        //克隆信息
        double[] performance_tmp = performance;
        int[] outputs_tmp = outputs.clone();
        int[][] weights_tmp = new int[len][];
        for (int i = 0; i < len; i++) {
            weights_tmp[i] = weights[i].clone();
        }
        //根据服务器，更新网络中各个节点的性价比
        for (Vertex<Integer> server : server_set) {
            for (Vertex<Integer> neighbor : server.getNeighbors()) {
                if (neighbor.isNode() && !server_set.contains(neighbor)) {
                    neighbor_has_server[neighbor.id] = true;
                    outputs_neighbor_server[neighbor.id] += neighbor.getWeight(server);
                }
            }
        }
        //为每一个服务器，寻找路径
        SubWalks.weights = weights_tmp;
        for (Vertex<Integer> client : clients) {
            int demand = client.getDemand();
            int remain_demand = demand;
            //确保满足每一个服务器的需求带宽
            while (remain_demand != 0) {
                LinkedList<Step> walk = findNew(client, client.getDemand(), server_set, neighbor_has_server,
                        performance_tmp, weights_tmp, outputs_neighbor_server, outputs_tmp);

                walks.addLast(walk);
                remain_demand -= walk.getLast().demand;
            }
        }
        return walks;
    }
    //给定服务器和需要承担的带宽，寻找路径.每次只寻找一次路径
    public static LinkedList<Step> findNew(Vertex<Integer> client, int demand, final HashSet<Vertex<Integer>> server_set,
                                        final boolean[] neighbor_has_server, final double[] performance, int[][] weights,
                                        int[] outputs_neighbor_server, int[] outputs) {

        HashSet<Vertex<Integer>> selected_nodes = new HashSet<Vertex<Integer>>();

        LinkedList<Step> walk = new LinkedList<Step>();
        walk.addLast(new Step(client, demand, 0));
        boolean flag = true;
        //
        int cost = 0;
        int support;
        Step step_tmp;
        Vertex<Integer> last;

        while (flag) {
            Step current_step = walk.getLast();
            Vertex<Integer> current_node = current_step.node;
            int current_demand = current_step.demand;

            //生成新的邻居节点集
            LinkedList<SubWalks> raw_sub_walks_set = current_node.getSubWalksSet();
            LinkedList<SubWalks> sub_walks_set = new LinkedList<SubWalks>();

            //根据规则，排序
            Vertex<Integer> end;
            for (SubWalks sub_walk : raw_sub_walks_set) {
                end = sub_walk.getDestination();
                if (end.isNode() && sub_walk.getWeight() > 0
                        && !selected_nodes.contains(end)
                        && !selected_nodes.contains(sub_walk.get(0))){
                    sub_walks_set.addLast(sub_walk);
                }
            }

            Collections.sort(sub_walks_set, new Comparator<SubWalks>() {
                public int compare(SubWalks o1, SubWalks o2) {
                    Vertex<Integer> end1 = o1.getDestination();
                    Vertex<Integer> end2 = o2.getDestination();
                    if(server_set.contains(end1) && !server_set.contains(end2))
                        return -1;
                    else if((!server_set.contains(end1) && server_set.contains(end2)))
                        return 1;

                    if ((neighbor_has_server[end1.id] && !neighbor_has_server[end2.id]) )
                       return -1;
                    else if((!neighbor_has_server[end1.id] && neighbor_has_server[end2.id]))
                        return 1;

                    if(o1.getValue() > o2.getValue())
                        return 1;
                    else if(o1.getValue() < o2.getValue())
                        return -1;

                    if(performance[end1.id] > performance[end2.id])
                        return -1;
                    else if(performance[end1.id] < performance[end2.id])
                        return 1;
                    return 0;
                }
            });

            LinkedList<Step>  next_steps = findNextStepNew(current_node, current_demand, sub_walks_set, server_set, neighbor_has_server,
                    weights, outputs_neighbor_server, performance, outputs);
            if (next_steps != null) {
                for(Step next_step : next_steps){
                    walk.addLast(next_step);
                    selected_nodes.add(next_step.node);
                    if (server_set.contains(next_step.node)) {
                        flag = false;
                        last = client;
                        support = walk.getLast().demand;
                        for (int i = 1; i < walk.size(); i++) {
                            step_tmp = walk.get(i);
                            cost += support * step_tmp.value;
                            step_tmp.cost = cost;
                            weights[last.id][step_tmp.node.id] -= support;
                            if (neighbor_has_server[step_tmp.node.id]){
                                outputs_neighbor_server[step_tmp.node.id] -= support;
                            }
                            outputs[step_tmp.node.id] -= support;
                            last = step_tmp.node;
                        }
                    }
                    break;
                }
            } else {
                flag = false;
                last = client;
                support = walk.getLast().demand;
                for (int i = 1; i < walk.size(); i++) {
                    step_tmp = walk.get(i);
                    cost += support * step_tmp.value;
                    step_tmp.cost = cost;
                    weights[last.id][step_tmp.node.id] -= support;
                    if (neighbor_has_server[step_tmp.node.id]){
                        outputs_neighbor_server[step_tmp.node.id] -= support;
                    }
                    outputs[step_tmp.node.id] -= support;
                    last = step_tmp.node;
                }

                //更新，新增服务器
                Vertex<Integer> new_server = walk.getLast().node;
                server_set.add(new_server);
                //更新新增服务器的邻居节点的配置信息，如perforamnce，outputs_neighbor_server
                for (Vertex<Integer> neighbor : new_server.getNeighbors()) {
                    if (neighbor.isNode() && !server_set.contains(neighbor)) {
                        neighbor_has_server[neighbor.id] = true;
                        outputs_neighbor_server[neighbor.id] += weights[neighbor.id][new_server.id];
                    }
                }
            }

        }
        return walk;
    }
    //寻找一条路径中的下一步骤
    public static LinkedList<Step> findNextStepNew(Vertex<Integer> current_node, int current_demand,
                                    LinkedList<SubWalks> neighbors, final HashSet<Vertex<Integer>> server_set,
                                    boolean[] neighbor_has_server, int[][] weights, int[] outputs_neighbor_server, double[] performance,
                                    int[] outputs) {

        if (neighbors.isEmpty())
            return null;
        SubWalks sub_walk = neighbors.removeFirst();
        int sub_walk_support = sub_walk.getWeight();
        if(sub_walk_support > 0){
            LinkedList<Step> next_steps = new LinkedList<Step>();
            int support = Math.min(sub_walk_support,current_demand);
            /*
            for(Vertex<Integer> node : sub_walk.sub_walks){
                Step step = new Step(node,support,current_node.getValue(node));
                next_steps.addLast(step);
                current_node = node;
            }*/
            Vertex<Integer> node = sub_walk.get(0);
            Step step = new Step(node,support,current_node.getValue(node));
            next_steps.addLast(step);
            return next_steps;
        }
        return findNextStepNew(current_node, current_demand, neighbors, server_set, neighbor_has_server, weights,
                outputs_neighbor_server, performance, outputs);
    }
    
}
