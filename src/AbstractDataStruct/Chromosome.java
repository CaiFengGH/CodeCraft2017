package AbstractDataStruct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author Ethan
 * @desc 染色体（解）
 */
public class Chromosome {
	//boolean[] gen:存储服务器组合方式
	//gen[1] = {true,false,false,true,false}
	//则，候选一号和四号节点为服务器，其他为普通节点。
	//
	//fitness_score:该解得适应度值
	//size:gen的大小，其与candidate（候选集合）集合大小保持一致
	boolean[] gene;
    int fitness_score = 0;
    int size = 0;

    public Chromosome() {
    }

    public Chromosome(int size) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        for (int i = 0; i < size; i++) {
            gene[i] = Math.random() >= 0.5;
        }
    }

    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,
    		ArrayList<Vertex<Integer>> candidates, float rate) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        int len = (int)(size*0.6);
        int idx;
        Random random = new Random();
        for(Vertex<Integer> client : clients){
            idx = client.getNeighbors().getFirst().id;
            if(performance[idx] > Math.random()){
                gene[idx] = true;
            }else {
                gene[idx] = false;
                idx = candidates.get(random.nextInt(len)).id;
                gene[idx] = performance[idx] > Math.random();
            }
        }
    }

    public Chromosome(int size, double[] performance, LinkedList<Vertex<Integer>> clients,
    		ArrayList<Vertex<Integer>> candidates, float rate,
                      Chromosome sample) {
        if (size <= 0) {
            return;
        }
        initGeneSize(size);
        Random random = new Random();
        int len = (int)(candidates.size()*rate);
        int idx;
        for(int i = 0; i < sample.gene.length; i++){
            if(sample.gene[i]){
                if(Math.random() > 0.5)
                    this.gene[i] = true;
                else {
                    this.gene[i] = false;
                    idx = candidates.get(random.nextInt(len)).id;
                    this.gene[idx] = performance[idx] > Math.random();
                }
            }
        }
    }
    /**
     * @desc 输出化基因大小
     */
    void initGeneSize(int size) {
        if (size <= 0) {
            return;
        }
        this.size = size;
        gene = new boolean[size];
    }
    /**
     * @desc 获得基因组合
     */
    public boolean[] getGene() {
        return gene;
    }
    /**
     * @desc 对染色体克隆 
     */
    public static Chromosome clone(final Chromosome target) {
        if (target == null || target.gene == null)
            return null;
        Chromosome result = new Chromosome();
        result.initGeneSize(target.size);
        for (int i = 0; i < target.size; i++) {
            result.gene[i] = target.gene[i];
        }
        result.fitness_score = target.fitness_score;
        return result;
    }
}