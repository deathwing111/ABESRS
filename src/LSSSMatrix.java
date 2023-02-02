import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

public class LSSSMatrix {
    private Matrix matrix;
    private String[] map;
    private String format = null;

    public LSSSMatrix(String attr, Field f){
        this.matrix = Matrix.ONE(f);
        this.map = new String[]{attr};
        this.format = attr;
    }

    public LSSSMatrix(Matrix matrix, String[] map) {
        this.matrix = matrix;
        this.map = map;
    }

    public String[] getMap() {
        return map;
    }

    public int getCols(){
        return this.matrix.getCols();
    }

    public int getRows(){
        return this.matrix.getRows();
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public LSSSMatrix or(LSSSMatrix p){
        Matrix a = this.matrix;
        Matrix b = p.matrix;
        int cols = a.getCols() + b.getCols() - 1;
        int rows = a.getRows() + b.getRows();

        Matrix res = new Matrix(rows, cols);
        String[] resmap = new String[rows];
        int temp;
        Element zero = this.matrix.getValue(0,0).getField().newZeroElement();
        for(int i = 0; i < rows; i++) {
            if(i < a.getRows()){
                resmap[i] = this.map[i];
                for(int j = 0; j < a.getCols(); j++) {
                    res.setValue(i, j, a.getValue(i, j));
                }
                for(int j = a.getCols(); j < cols; j++){
                    res.setValue(i, j, zero);
                }
            }else{
                temp = i - a.getRows();
                resmap[i] = p.map[temp];
                res.setValue(i, 0, b.getValue(temp, 0));
                for(int j = 1; j < a.getCols(); j++) {
                    res.setValue(i, j, zero);
                }
                for(int j = a.getCols(); j < cols; j++)
                    res.setValue(i, j, b.getValue(temp, j-a.getCols() + 1));
            }
        }
        return new LSSSMatrix(res, resmap);
    }

    public LSSSMatrix and(LSSSMatrix p){
        Matrix a = this.matrix;
        Matrix b = p.matrix;
        int cols = a.getCols() + b.getCols();
        int rows = a.getRows() + b.getRows();

        Matrix res = new Matrix(rows, cols);
        String[] resmap = new String[rows];
        int temp;
        Element zero = this.matrix.getValue(0,0).getField().newZeroElement();
        for(int i = 0; i < rows; i++) {
            if(i < a.getRows()){
                resmap[i] = this.map[i];
                res.setValue(i, 0, a.getValue(i, 0));
                for(int j = 1; j <= a.getCols(); j++) {
                    res.setValue(i, j, a.getValue(i, j-1));
                }
                for(int j = a.getCols() + 1; j < cols; j++){
                    res.setValue(i, j, zero);
                }

            }else{
                temp = i - a.getRows();
                resmap[i] = p.map[temp];
                for(int j = 0; j <= a.getCols(); j++){
                    res.setValue(i, j, zero);
                }
                res.setValue(i, 1, b.getValue(temp, 0));
                for(int j = a.getCols()+1; j < cols; j++)
                    res.setValue(i, j, b.getValue(temp, j-a.getCols()));
            }
        }
        return new LSSSMatrix(res, resmap);
    }

    public LSSSShares genShareVector(Vector vector){
        Vector res = this.matrix.mul(vector);
        return new LSSSShares(res, this.map);
    }

    public LSSSMatrix extract(int[] indexes){
        Matrix matrix = this.matrix.extract(indexes);
        String[] map = new String[indexes.length];
        for(int i = 0; i < indexes.length; i++){
            map[i] = this.map[indexes[i]];
        }
        return new LSSSMatrix(matrix, map);
    }

    public LSSSMatrix extract(String[] attrs){
        int[] res = Utils.search(this.map, attrs)[0];
        return extract(res);
    }

    public Element recover(LSSSShares shares){
        Vector lambda = genLambda();
        if(lambda == null)
            return null;
        Element res = shares.recover(genLambda());
        return res;
    }

    public Vector genLambda(){
        Matrix tm = this.matrix.transform();
        Element init = this.matrix.getValue(0,0).getField().newZeroElement();
        Vector v = new Vector(tm.getRows(), false, init);
        v.setValue(0, init.set(1));
        Vector lambda = tm.GaussianElimination(v);
        if(!tm.mul(lambda).equals(v))
            return null;
        return lambda;
    }


    public String toString(){
        int cols = this.matrix.getCols();
        int rows = this.matrix.getRows();
        String res = this.format + " : {\n";
        for(int i = 0; i < rows; i++){
            res += "    ";
            for(int j = 0; j < cols; j++){
                res += this.matrix.getValue(i, j) + ", ";
            }
            res += "  -- " + this.map[i] + " " + i + "\n";
        }
        res += "}";
        return res;
    }


    public void setFormat(String name) {
        this.format = name;
    }
}
