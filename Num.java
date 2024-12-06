public class Num extends Token{
    public final float value;
    public Num(float value, int tag){
        super (tag);
    this.value = value;
    }
    public String toString(){
        if(value % 1 == 0)
            return "" + (int)value;
        else
            return "" + value;
    }
}
   