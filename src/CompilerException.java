public class CompilerException extends Exception{
    private int line;
    private int column;
    public CompilerException(String message, int line, int column){
        super(message);
        this.line=line;
        this.column=column;
    }

    public int getLine(){
        return line;
    }
    public int getColumn(){
        return column;
    }
}
