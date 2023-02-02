import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

public class Matrix {
    private static Element zero = null;

    public static Matrix ONE(Field f){
        Matrix res = new Matrix(1, 1);
        res.matrix[0][0] = f.newOneElement();
        if(zero == null)
            zero = f.newZeroElement();
        return res;
    }

    private int rows;
    private int cols;
    private Element[][] matrix;

    public Matrix(int rows, int cols, Element init){
        this.rows = rows;
        this.cols = cols;
        this.matrix = new Element[rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++)
                this.matrix[i][j] = init.duplicate();
        }
    }

    public Matrix(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        this.matrix = new Element[rows][cols];
    }

    public Element getValue(int i, int j){
        return matrix[i][j];
    }

    public void setValue(int i, int j, Element value){
        this.matrix[i][j] = value.duplicate();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Matrix add(Matrix a){
        if(this.rows != a.rows || this.cols != a.cols)
            return null;
        if(!this.matrix[0][0].getField().getOrder().equals(a.matrix[0][0].getField().getOrder()))
            return null;
        Matrix res = new Matrix(this.rows, this.cols);
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.cols; j++)
                res.matrix[i][j] = this.matrix[i][j].duplicate().add(a.matrix[i][j]);
        }
        return res;
    }

    public Matrix sub(Matrix a){
        if(this.rows != a.rows || this.cols != a.cols)
            return null;
        if(!this.matrix[0][0].getField().getOrder().equals(a.matrix[0][0].getField().getOrder()))
            return null;
        Matrix res = new Matrix(this.rows, this.cols);
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.cols; j++)
                res.matrix[i][j] = this.matrix[i][j].duplicate().sub(a.matrix[i][j]);
        }
        return res;
    }

    public Matrix mul(Matrix a){
        if(this.cols != a.rows)
            return null;
        if(!this.matrix[0][0].getField().getOrder().equals(a.matrix[0][0].getField().getOrder()))
            return null;
        Element init = this.matrix[0][0].getField().newElement();
        init.set(0);
        Matrix res = new Matrix(this.rows, a.cols, init);
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < a.cols; j++){
                for(int k = 0; k < this.cols; k++){
                    res.matrix[i][j].add(this.matrix[i][k].duplicate().mul(a.matrix[k][j]));
                }
            }
        }
        return res;
    }

    public Vector mul(Vector a){
        if(this.cols != a.getLen() || a.isFlag())
            return null;
        if(!this.matrix[0][0].getField().getOrder().equals(a.getValue(0).getField().getOrder()))
            return null;
        Element init = this.matrix[0][0].getField().newElement();
        init.set(0);
        Vector res = new Vector(this.rows, false, init);
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.cols; j++)
                res.getValue(i).add(this.matrix[i][j].duplicate().mul(a.getValue(j)));
        }
        return res;
    }

    public Matrix extract(int[] rows){
        Matrix res = new Matrix(rows.length, this.cols);
        for(int i = 0; i < res.rows; i++){
            for(int j = 0; j < res.cols; j++){
                if(rows[i] >= this.rows || rows[i] < 0)
                    return null;
                res.matrix[i][j] = this.matrix[rows[i]][j].duplicate();
            }
        }
        return res;
    }

    public Vector GaussianElimination(Vector res){
        if(this.rows != res.getLen() || res.isFlag())
            return null;
        if(!this.matrix[0][0].getField().getOrder().equals(res.getValue(0).getField().getOrder()))
            return null;
        Element tmp;
        Matrix tm = new Matrix(this.rows, this.cols+1);
        for(int i = 0 ;i < this.rows; i++){
            for(int j = 0; j < this.cols; j++){
                tm.setValue(i, j, this.getValue(i, j));
            }
            tm.setValue(i, this.getCols(), res.getValue(i));
        }
        int maxI;
        for(int i = 0; i < this.cols; i++) {
            if (i >= this.rows)
                break;
            maxI = i;
            for (int j = i; j < this.rows; j++) {
                if (!tm.getValue(j, i).isZero()) {
                    maxI = j;
                    break;
                }
            }
            if (i != maxI) {
//                tm.switchRow(i, maxI);
                for(int j = 0; j < this.cols; j++){
                    tmp = tm.getValue(maxI, j);
                    tm.setValue(i, j, tmp.add(tm.getValue(i, j)));
                    tm.setValue(i, j, tm.getValue(maxI, j).sub(tm.getValue(i, j)));
                }
            }
            for( int j = 0; j < this.rows; j++){
                tm.performRow(i, j);
            }
        }
        Vector resv = new Vector(this.cols, false);
        for(int i = 0; i < this.cols; i++){
            if(i < this.rows)
                resv.setValue(i, tm.getValue(i, tm.cols-1));
            else
                resv.setValue(i, this.zero.duplicate());

        }
        return resv;
    }

    private boolean switchRow(int row1, int row2){
        if(row1 < 0 || row1 >= this.rows || row2 < 0 || row2 >= this.rows )
            return false;
        Element tmp;
        for(int i = 0; i < this.cols; i++){
            tmp = this.getValue(row1, i);
            this.matrix[row1][i] = this.matrix[row2][i];
            this.matrix[row2][i] = tmp;
        }
        return true;
    }

    private boolean performRow(int fix, int target){
        if(fix < 0 || fix >= this.rows || target < 0 || target >= this.rows )
            return false;
        Element tmp;
        if(fix == target){
            tmp = this.matrix[fix][fix].duplicate();
            if(!tmp.isZero()){
                for (int i = 0; i < this.cols; i++) {
                    this.matrix[fix][i] = this.matrix[fix][i].mul(tmp.invert());
                }
            }
        }else{
            tmp = this.matrix[fix][fix];
            if(!tmp.isZero()){
                tmp = this.matrix[target][fix].duplicate().mul(tmp.invert());
                if(!tmp.isZero()) {
                    for (int i = 0; i < this.cols; i++) {
                        this.matrix[target][i].sub(this.matrix[fix][i].duplicate().mul(tmp));
                    }
                }
            }

        }
        return true;
    }

    public Matrix transform(){
        Matrix res = new Matrix(this.cols, this.rows);
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.cols; j++)
                res.matrix[j][i] = this.matrix[i][j].duplicate();
        }
        return res;
    }

    public String toString(){
        int cols = this.getCols();
        int rows = this.getRows();
        String res = "{\n";
        for(int i = 0; i < rows; i++){
            res += "    ";
            for(int j = 0; j < cols; j++){
                res += this.getValue(i, j) + ", ";
            }
            res += "\n";
        }
        res += "}";
        return res;
    }
}
