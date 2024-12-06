public class AnaliseLexicaException extends Exception{
    private int linha;
    private int column;
    public AnaliseLexicaException(String message, int linha, int column){
        super(message);
        this.linha=linha;
        this.column=column;
    }

    public int getLinha(){
        return linha;
    }
    public int getColumn(){
        return column;
    }
}
