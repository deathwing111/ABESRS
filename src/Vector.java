import it.unisa.dia.gas.jpbc.Element;

public class Vector {
    // true - row vector; false - col vector
    private boolean flag;
    private int len;
    private Element[] vector;

    public Vector(int len, boolean flag){
        this.len = len;
        this.flag = flag;
        this.vector = new Element[len];
    }

    public Vector(int len, boolean flag, Element init){
        this.len = len;
        this.flag = flag;
        this.vector = new Element[len];
        for(int i = 0; i < len; i++)
            this.vector[i] = init.duplicate();
    }

    public Vector(boolean flag, Element[] vector){
        this.len = vector.length;
        this.flag = flag;
        this.vector = vector;
    }

    public Vector(int len){
        this(len, true);
    }

    public Element getValue(int i){
        return this.vector[i];
    }

    public void setValue(int i, Element value){
        this.vector[i] = value.duplicate();
    }

    public boolean isFlag() {
        return flag;
    }

    public int getLen() {
        return len;
    }

    public Vector transform(){
        Vector res = new Vector(this.len, !this.flag);
        for(int i = 0; i < this.len; i++)
            res.vector[i] = this.vector[i];
        return res;
    }

    public Vector add(Vector a){
        if(this.flag != a.flag || this.len != a.len)
            return null;
        Vector res = new Vector(this.len, this.flag);
        for(int i = 0; i < this.len; i++)
            res.vector[i] = this.vector[i].duplicate().add(a.vector[i]);
        return res;

    }

    public Vector sub(Vector a){
        if(this.flag != a.flag || this.len != a.len)
            return null;
        if(!this.vector[0].getField().getOrder().equals(a.vector[0].getField().getOrder()))
            return null;
        Vector res = new Vector(this.len, this.flag);
        for(int i = 0; i < this.len; i++)
            res.vector[i] = this.vector[i].duplicate().sub(a.vector[i]);
        return res;
    }

    public Vector mul(Matrix a){
        if(this.len != a.getRows() || !this.flag)
            return null;
        if(!this.vector[0].getField().getOrder().equals(a.getValue(0,0).getField().getOrder()))
            return null;
        Element init = this.vector[0].getField().newElement();
        init.set(0);
        Vector res = new Vector(a.getCols(), this.flag, init);
        for(int i = 0; i < a.getCols(); i++){
            for(int j = 0; j < this.len; j++)
                res.vector[i].add(this.vector[j].duplicate().mul(a.getValue(j, i)));
        }
        return res;
    }

    public Matrix mul(Vector a){
        if(this.flag || !a.flag)
            return null;
        if(!this.vector[0].getField().getOrder().equals(a.vector[0].getField().getOrder()))
            return null;
        Matrix res = new Matrix(this.len, a.len);
        for(int i = 0; i < this.len; i++){
            for(int j = 0 ; j < this.len ; j++){
                res.setValue( i, j, this.vector[i].duplicate().mul(a.vector[j]));
            }
        }
        return res;
    }

    public Element mul1(Vector a){
        if(!this.flag || a.flag || this.len != a.len)
            return null;
        if(!this.vector[0].getField().getOrder().equals(a.vector[0].getField().getOrder()))
            return null;
        Element res = a.vector[0].getField().newZeroElement();
        for(int i = 0; i < this.len; i++)
                res.add(this.vector[i].duplicate().mul(a.vector[i]));
        return res;
    }

    public Vector extract(int[] indexes) {
        Vector res = new Vector(indexes.length, this.flag);
        for(int i = 0; i < indexes.length; i++){
            res.vector[i] = this.vector[indexes[i]].duplicate();
        }
        return res;
    }

    public String toString(){
        int len = this.getLen();
        String res = "{";
        for(int i = 0; i < len; i++){
            res += " " + this.getValue(i) + ", ";
        }
        res += "} -- " + this.isFlag();
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(this == obj)
            return true;
        if(! (obj instanceof Vector))
            return false;
        if(((Vector)obj).isFlag() != this.isFlag())
            return false;
        if(((Vector)obj).len != this.len)
            return false;
        if(((Vector)obj).vector == this.vector)
            return true;
        if(((Vector)obj).vector == null)
            return false;
        for(int i = 0; i < this.len; i++){
            if(!((Vector)obj).vector[i].isEqual(this.vector[i]))
                return false;
        }
        return true;
    }
}
