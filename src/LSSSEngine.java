import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.field.z.ZrField;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LSSSEngine {
    public static final String OPS = "(|+&*)";

    public LSSSMatrix genMatrix(String policy, Field f){
        policy = policy.trim();
        List<Node> list = new ArrayList<>();
        String temps = "";
        char tc;
        int tmporder = 0;
        for(int i = 0; i < policy.length(); i++){
            tc = policy.charAt(i);
            if( OPS.indexOf(tc) < 0 ){
                temps += tc;
            }else{
                temps = temps.trim();
                if(temps != "") {
                    list.add(new Node(temps.trim(), 0, true, f));
                    temps = "";
                }
                    if(tc == '+' || tc == '|')
                        tmporder = 1;
                    else if(tc == '*' || tc == '&')
                        tmporder = 2;
                    else if(tc == '(')
                        tmporder = 3;
                    else if(tc == ')')
                        tmporder = 4;
                    list.add(new Node(tc+"", tmporder, false, f));
            }
        }
        if(temps != "") {
            list.add(new Node(temps.trim(), 0, true, f));
        }

        Stack<Node> leafStack = new Stack();
        Stack<Node> nodeStack = new Stack();
        Node prenode = null;
        Node tempnode = null;
        Node branchNode = null;


        for(int i = 0; i < list.size(); i++) {
            tempnode = list.get(i);
            if (tempnode.order == 3) {
                nodeStack.add(tempnode);
            } else if (tempnode.order > 0 && tempnode.order != 3) {
                if (i == 0) {
                    return null;
                }

                if (tempnode.order != 4) {
                    if (prenode == null) {
                        nodeStack.add(tempnode);
                    } else if (prenode != null && tempnode.order != 4) {
                        if (nodeStack.isEmpty()) {
                            leafStack.add(prenode);
                            nodeStack.add(tempnode);
                        } else {
                            switch((nodeStack.peek()).order) {
                                case 1:
                                    leafStack.add(prenode);
                                    nodeStack.add(tempnode);
                                    break;
                                case 2:
                                    if (!(leafStack.peek()).isfull) {
                                        branchNode = leafStack.peek();
                                        branchNode.push(prenode);
                                        leafStack.pop();
                                        leafStack.add(branchNode);
                                        nodeStack.pop();
                                    } else {
                                        branchNode = nodeStack.peek();
                                        branchNode.combine(prenode, leafStack.peek());
                                        leafStack.pop();
                                        leafStack.add(branchNode);
                                        nodeStack.pop();
                                    }

                                    nodeStack.add(tempnode);
                                    break;
                                case 3:
                                    leafStack.add(prenode);
                                    nodeStack.add(tempnode);
                                    break;
                                case 4:
                            }
                        }
                    }
                } else {
                    Node flag = null;
                    if (prenode == null && leafStack.peek().order > 0) {
                        flag = leafStack.pop();
                    } else {
                        flag = prenode;
                    }

                    if ((nodeStack.peek()).order == 2) {
                        if (!leafStack.peek().isfull) {
                            branchNode = leafStack.peek();
                            branchNode.push(flag);
                            leafStack.pop();
                            nodeStack.pop();
                        } else {
                            branchNode = nodeStack.peek();
                            branchNode.combine(flag, leafStack.peek());
                            leafStack.pop();
                            nodeStack.pop();
                        }

                        flag = branchNode;
                    }

                    Node temp = null;
                    if (leafStack.isEmpty()) {
                        leafStack.add(flag);
                        nodeStack.pop();
                    } else {
                        while(nodeStack.peek() != null && nodeStack.peek().order != 3) {
                            //System.out.println(nodeStack);
                            temp = nodeStack.peek();
                            temp.combine(flag, leafStack.peek());
                            flag = temp;
                            nodeStack.pop();
                            leafStack.pop();
                        }

                        nodeStack.pop();
                        if (!nodeStack.isEmpty() && nodeStack.peek().order != 3 && nodeStack.peek().order != 1) {
                            temp = nodeStack.peek();
                            temp.combine(flag, leafStack.peek());
                            nodeStack.pop();
                            leafStack.pop();
                            leafStack.add(temp);
                        } else {
                            leafStack.add(flag);
                        }
                    }

                    prenode = null;
                }
            } else if (tempnode.order == 0) {
                prenode = tempnode;
            }
        }

        Node res = null;
        if (tempnode != null && tempnode.order == 0) {
            leafStack.add(tempnode);
        }

        if (nodeStack.isEmpty()) {
            res = leafStack.pop();
            if (leafStack.isEmpty()) {
                return res.matrix;
            } else {
                return null;
            }
        } else {
            branchNode = null;

            while(!leafStack.isEmpty()) {
                if (nodeStack.isEmpty()) {
                    return leafStack.pop().matrix;
                }

                branchNode = nodeStack.peek();
                branchNode.combine(leafStack.pop(), leafStack.pop());
                nodeStack.pop();
                if (leafStack.isEmpty()) {
                    return branchNode.matrix;
                }

                leafStack.add(branchNode);
            }
            return null;
        }
    }

    public LSSSShares genSecretShares(LSSSMatrix matrix, Vector vector){
        return matrix.genShareVector(vector);
    }

    public int extract(LSSSMatrix matrix, LSSSShares shares, String[] attrs){
        LSSSMatrix matrixA = matrix.extract(attrs);
        return 0;
    }

    private class Node{
        public String name;
        public int order;
        // true -- attr; false -- operator
        public boolean flag;
        public LSSSMatrix matrix;
        public Node temp;
        public boolean isfull;
        public Node(String name, int order, boolean flag, Field f){
            this.name = name;
            this.order = order;
            this.flag = flag;
            if(flag) {
                this.matrix = new LSSSMatrix(name, f);
                this.isfull = true;
            }else
                this.isfull = false;
        }

        public void combine(Node node1, Node node2){
            if(order == 1) {
                matrix = node2.matrix.or(node1.matrix);
            }else if(order == 2) {
                matrix = node2.matrix.and(node1.matrix);
            }
            if(!node1.flag && this.order > node1.order){
                this.name = "(" + node1.name + ")" + this.name;
            }else
                this.name = node1.name + this.name;
            if(!node2.flag && this.order > node2.order){
                this.name += "(" + node2.name + ")";
            }else
                this.name += node2.name;
            this.isfull = true;
            this.matrix.setFormat(this.name);
            this.temp = null;
        }

        public void push(Node node){
            if(temp == null)
                this.temp = node;
            else {
                combine(node, this.temp);
            }
        }

        public String toString(){
            return this.name;
        }
    }

    public static void main(String[] args){
        ZrField zr = new ZrField(new BigInteger("101"));

        LSSSEngine engine = new LSSSEngine();
        String policy = "a1|a2|a3&(a4|a5&(a6|a7))|a8&(a9|a0)";
        String[] attrs = {"a0","a1","a2","a3","a4","a5","a6","a7","a8","a9"};

        LSSSMatrix matrix = engine.genMatrix(policy, zr);
        System.out.println(matrix.toString());
        LSSSMatrix statisfiedM = matrix.extract(attrs);
        System.out.println(statisfiedM);


        Element[] eles = new Element[]{zr.newElement().set(78), zr.newElement().set(54), zr.newElement().set(81),
                zr.newElement().set(78)};
        Vector secret = new Vector(false, eles);
        LSSSShares shares = matrix.genShareVector(secret);
        System.out.println(shares);
        LSSSShares ushares = shares.extract(attrs);
        System.out.println(ushares);

        Element s = statisfiedM.recover(ushares);
        System.out.println(s);
    }
}
