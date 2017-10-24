package AbstractDataStruct;

import java.util.LinkedList;

/**
 * @author Ethan
 * @desc 子路径
 */
public class SubWalks{
    static int[][] weights;
    private int value;
    LinkedList<Vertex<Integer>> sub_walks;
    private Vertex<Integer> start;
    //true:一个节点
    //false:多个节点
    private boolean one_two;
    
    public SubWalks(Vertex<Integer> start, int value,boolean flag){
        this.start = start;
        one_two = flag;
        this.value = value;
        sub_walks = new LinkedList<Vertex<Integer>>();
    }
    
    public int getWeight(){
        int support = Integer.MAX_VALUE;
        Vertex<Integer> last = start;
        for(Vertex<Integer> node : sub_walks){
            support = Math.min(support,weights[last.id][node.id]);
            last = node;
        }
        return support;
    }
    
    public int getValue(){
        return value;
    }
    
    public void setValue(int value){
        this.value = value;
    }
    
    public void setStart(Vertex<Integer> start){
        this.start = start;
    }
    
    public Vertex<Integer> getStart(){
        return start;
    }
    
    public boolean isOneTwo(){
        return one_two;
    }
    
    public void add(Vertex<Integer> node){
        sub_walks.add(node);
    }
    
    public Vertex<Integer> getDestination(){
        return sub_walks.getLast();
    }
    
    public Vertex<Integer> get(int idx){
        return sub_walks.get(idx);
    }
    
    public String toString(){
        String result = ""+start.id+"->";
        for(Vertex<Integer> walk : sub_walks){
            result +=(walk.id+"->");
        }
        result += ("  "+"价格为"+value);
        result += ("  "+"终点为"+getDestination());
        result += ("  "+"判断"+isOneTwo());
        result += ("  "+"权值"+getWeight());
        return result;
    }
}

