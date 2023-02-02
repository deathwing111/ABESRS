import it.unisa.dia.gas.jpbc.Element;


public class RABECipher {
    private LSSSMatrix policy;
    private long t;
    private Element[] c0;
    private Element[][] cs;
    private Element c_p;

    public RABECipher(LSSSMatrix policy,
                      long t,
                      Element[] c0,
                      Element[][] cs,
                      Element c_p) {
        this.policy = policy;
        this.t = t;
        this.c0 = c0;
        this.cs = cs;
        this.c_p = c_p;
    }

    public LSSSMatrix getPolicy() {
        return policy;
    }

    public long getT() {
        return t;
    }

    public Element[] getC0() {
        return c0;
    }

    public Element[][] getCs() {
        return cs;
    }

    public Element getC_p() {
        return c_p;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;
        if(!(obj instanceof RABECipher))
            return false;
        RABECipher target = (RABECipher) obj;
        if(this.t != target.t)
            return false;
        if(!this.c_p.isEqual(target.c_p))
            return false;
        if(!this.policy.toString().equals(target.policy.toString()))
            return false;
        if(this.c0.length != target.c0.length)
            return false;
        for (int i = 0; i < this.c0.length; i++) {
            if(!this.c0[i].isEqual(target.c0[i]))
                return false;
        }
        if(this.cs.length != target.cs.length)
            return false;
        for (int i = 0; i < this.cs.length; i++) {
            if(this.cs[i].length != target.cs[i].length)
                return false;
            for (int j = 0; j < this.cs[i].length; j++) {
                if(!this.cs[i][j].isEqual(target.cs[i][j]))
                    return false;
            }
        }
        return true;
    }
}
