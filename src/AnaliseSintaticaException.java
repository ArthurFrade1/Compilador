public class AnaliseSintaticaException extends CompilerException{
    public AnaliseSintaticaException(String message, int line, int column){
        super(message, line, column);
    }
}
