public class Token {
    private final int tag; //constante que representa o token
    public boolean idDecl = false;
    
    public int type = Tag.NULL;
    public Token (int t){
        tag = t;    
    }
    public String toString(){
        return "" + tag;
    }

    public int getTag(){
        return tag;
    }
   }