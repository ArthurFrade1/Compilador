import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;

public class Lexer {
    public char ch=' ';
    public int line=1;
    public int column=1;
    public int state=0;
    public FileReader file;
    SymbolTable symbolTable;

    public Lexer(SymbolTable symbolTable) throws FileNotFoundException{
        try{
            file = new FileReader ("./test.txt");
        }
            catch(FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
            throw e;
        }
        this.symbolTable=symbolTable;
    
    }

    private void readch() throws IOException {
        int data = file.read(); // Lê o próximo caractere como inteiro
        if (data == -1) { // Verifica se atingiu o final do arquivo
            ch = (char) -1; // Marca EOF
        } else {
            ch = (char) data; // Converte o dado lido para char
            column++; // Incrementa a coluna
        }
    }
    
    
    private boolean readch(char c) throws IOException{
        readch();
        if (ch != c) return false;
        ch = ' ';
        return true;
    }
    
    
    public Token scan() throws IOException, AnaliseLexicaException{
        state=0;
        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\b') continue;
            else if (ch == '\n'){
                line++; //conta linhas
                column=0;
            } 
            else break;
        }
        
        
        if(ch=='&'){
            if(readch('&'))
            return new Token( Tag.AND);
        }
        else if(ch=='+'){
            readch();
            return new Token( Tag.ADD); 
        }
        
        else if (ch == '-') {
            Token token = new Token(Tag.SUB); 
            readch();  
            return token;
        }
        
        else if (ch == ';') {
            Token token = new Token(Tag.SEMICOLON); 
            readch();  
            return token;
        }
        
        else if (ch == ',') {
            Token token = new Token(Tag.COMMA); 
            readch();  
            return token;
        }
        
        else if (ch == '(') {
            Token token = new Token(Tag.LPAREN); 
            readch();  
            return token;
        }
        
        else if (ch == ')') {
            Token token = new Token(Tag.RPAREN); 
            readch();  
            return token;
        }
        
        else if (ch == '*') {
            Token token = new Token(Tag.MUL); 
            readch();  
            return token;
        }

        else if (ch == '%') {
            Token token = new Token(Tag.MOD); 
            readch();  
            return token;
        }
        
        else if(ch=='>'){
            if(readch('='))
                return new Token( Tag.GE);
            else
                return new Token( Tag.GT);
        }

        else if(ch=='<'){
            if(readch('='))
                return new Token( Tag.LE);
            else
                return new Token( Tag.LT);
        }

        else if(ch=='='){      
            if(readch('='))
                return new Token( Tag.EQ);
            else  
                return new Token(Tag.ASSIGN);
        }

        else if(ch=='|')
            if(readch('|'))
            return new Token( Tag.OR);

        else if(ch=='!')
            if(readch('='))
                return new Token( Tag.NE);
        
        boolean isFloat=false;

        //Para literais
        if(ch=='{'){
            readch();
            if(ch == '}'){
                readch();
                return new Word("{}", Tag.LITERAL);
            }
            else if(ch<255 && ch>0){

                if(ch=='\n')
                    throw new AnaliseLexicaException( "Unexpected newline inside literal:",line, column);

                String var=ch+"";
                if(readch('}'))
                    return new Word("{"+var+"}", Tag.LITERAL);
                while(true){
                    if(ch=='\n')
                        throw new AnaliseLexicaException( "Unexpected newline inside literal:",line, column);
                    if(ch==(char) -1)
                        throw new AnaliseLexicaException("Unexpected end of file: missing closing '}' for literal.",line, column);
                    else if(ch=='}'){
                        readch();
                        return new Word("{"+var+"}", Tag.LITERAL);
                    }
                    var=var+ch+"";
                    readch();

                }
            }
            
            else if(ch==(char) -1)
                throw new AnaliseLexicaException("Unexpected end of file: missing closing '}' for literal.",line, column);

        }

        //Para identificadores
        String id="";
        if(Character.isLetter(ch) || ch=='_'){
            id=ch+"";
            while(true){
                readch();
                if(Character.isLetter(ch) || Character.isDigit(ch)){
                    id=id+ch+"";
                }
                else{
                    Word tok=new Word(id, Tag.ID); 
                    
                    //Se nao esta incluso
                    if(!symbolTable.contains(id))
                        symbolTable.put(id, tok);   
                    else 
                        tok=symbolTable.get(id);

                    return tok;  
                } 
            }
        }


        int decimalPlates=1;
        //Para inteiros e floats
        if(Character.isDigit(ch)){
            boolean lacksNumber=false;
            float val=Character.digit(ch, 10);
            while(true){
                readch();
                if(ch=='.'){ 
                    if(!isFloat){//Se enccontrou um ponto pela primeira vez
                        isFloat=true;
                        lacksNumber=true;
                        continue;
                    }
                    else{//Se encontrou 2 pontos no mesmo numero reporta erro
                        throw new AnaliseLexicaException("Invalid float format: multiple decimal points detected.", line, column);
                    }
                }
                if(!Character.isDigit(ch)){
                    if(isFloat){
                        if(lacksNumber)
                            throw new AnaliseLexicaException("Invalid float format: a number was expected after the decimal point.", line, column);
                        return new Num(val, Tag.FLOAT);
                    }
                    else
                        return new Num(val, Tag.INTEGER);
                }

                if(isFloat){
                    float temp = Character.digit(ch, 10);
                    temp=temp*(float)Math.pow(10, -decimalPlates);//for each digit after comma, come to 1 plate right
                    decimalPlates++;
                    val+=temp;
                }
                else{
                    int temp = Character.digit(ch, 10);
                    val*=10;
                    val+=temp;
                }
                lacksNumber=false;
            }
        }

        if(ch == '/'){
            readch();

            //Para o div
            if(ch!='/' && ch!='*'){                    
                return new Token(Tag.DIV); 
            }
            
        //Comentarios
            //comentario uma linha
            if(ch=='/'){
                while (true) {
                    if(!readch('\n')) continue;
                    else{
                        line++;
                        break;
                    }
                }
            }
            //comentario multiplas linhas
            if(ch=='*'){
                while (true) {
                    readch();
                    if(ch == (char)-1)
                        throw new AnaliseLexicaException("Unclosed comment detected!", line, column);
                    if(ch=='*'){
                        if(ch == (char)-1)
                            throw new AnaliseLexicaException("Unclosed comment detected!", line, column);
                        if(readch('/')){
                            break;
                        }
                    }
                }
            }
            
            return scan(); //Assim ignora os comentarios
        }
    
        else{
            if(ch != (char)-1) //Se nao é fim de arquivo reporta tokem mal formado
                throw new AnaliseLexicaException("Error: Malformed token detected!", line, column);
            return null;
        }
    }
}

