import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Syntax {

    private Lexer lexer;
    private SymbolTable symbolTable;
    static int tagToken = 0;
    public int linhaTokAnterior = 0;
    public int colunaTokAnterior = 0;
    Token tok;
    Token token;

    public Syntax() throws FileNotFoundException {
        Scanner scan = new Scanner(System.in);
        symbolTable = new SymbolTable();
        System.out.println("Nome do arquivo a ser compilado: ");
        // String filePath = scan.nextLine();
        String filePath = "oi.txt";
        lexer = new Lexer(filePath, symbolTable);
    }

    private Token bufferedToken = null;

    // Avança e anda
    private void check(int tag) throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {

        try{

        token = null;

        if (tag != tagToken)
            throw new AnaliseSintaticaException("Token esperado: "+ TAG_NAMES.get(tag)+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        

        // Se tem algo no buffer isso será utilizado como token atual
        if (bufferedToken != null) {
            token = bufferedToken;
            bufferedToken = null; // Consumir o token do buffer
        } else
            // antes de ler o token salva sua linha e coluna pra que se occorra um erro sintatico como o proximo token se possa identificar onde ocorreu a falta
            linhaTokAnterior = lexer.line;
            colunaTokAnterior = lexer.column;

            token = lexer.scan();
        if (token != null)
            tagToken = token.getTag();
        }
        
         catch (AnaliseLexicaException e) {
            System.out.println("\u001B[31mLexical error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }
    }

    public Token lookahead() throws IOException {
        
        if (bufferedToken == null) {
            try{
                bufferedToken = lexer.scan(); // Armazena o próximo token no buffer
            }catch(AnaliseLexicaException e){
                System.out.println("\u001B[31mLexical error - line: " + e.getLine() + " column: " + e.getColumn());
                System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
            }
        }
        return bufferedToken;
    }

    // Inicializa o processo
    public static void start() {
try{
            Syntax instance = new Syntax();
            Token tok = instance.lexer.scan();

            tagToken = tok.getTag();

            instance.program();
            System.out.println("\033[0;32mSemantic analysis completed successfully\033[0m");

        }catch (AnaliseLexicaException e) {
            System.out.println("\u001B[31mLexical error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }catch (AnaliseSintaticaException e) {
            System.out.println("\u001B[31mSyntax error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }catch (AnaliseSemanticaException e) {
            System.out.println("\u001B[31mSemantic error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }
        catch (IOException e) {
            System.out.println("Exceção de entrada e saída");
        }
    }

    // program ::= start [decl-list] stmt-list exit
    public void program() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.START);
        while (tagToken == Tag.INT || tagToken == Tag.FLOAT ||tagToken == Tag.STRING) {
            decl_list();
        }
        stmt_list();
        check(Tag.EXIT);
    }

    // decl_list ::= decl {decl}
    public void decl_list() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        decl();
        while (tagToken == Tag.INT || tagToken == Tag.FLOAT ||tagToken == Tag.STRING) {
            decl();
        }
    }

    // decl ::= type ident-list ";"
    public int decl() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type = 0;
        type = type();
        ident_list(type);
        check(Tag.SEMICOLON);
        return 0;
    }

    // ident_list ::= identifier {"," identifier}
    public int ident_list(int type) throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        token.type = type; //Atribui o tipo herdado ao token atual
        token.idDecl = true;
        check(Tag.ID);
        ////////////////////
        while (tagToken == Tag.COMMA) {
            check(Tag.COMMA);
            token.idDecl = true;
            token.type = type; //Atribui o tipo herdado ao token atual
            check(Tag.ID);
        }
        return 0;
    }

    // type ::= int | float | string
    public int type() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type = tagToken;
        switch (tagToken) {
            case Tag.INT:
                check(Tag.INT);
            break;
            case Tag.FLOAT:
                check(Tag.FLOAT);
            break;
            case Tag.STRING:
                check(Tag.STRING);
            break;
            default:
                return 999;
        }
        return type;

    }

    // stmt-list ::= stmt {stmt}
    public void stmt_list() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException, AnaliseSemanticaException {
        stmt();
        while (tagToken == Tag.ID || tagToken == Tag.IF || tagToken == Tag.DO || tagToken == Tag.SCAN || tagToken == Tag.PRINT) {
            stmt();
        }
    }

    // stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";
    public void stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException, AnaliseSemanticaException {
        switch (tagToken) {
            case Tag.ID:
                assign_stmt();
                check(Tag.SEMICOLON);
            break;
            case Tag.IF:
                if_stmt();
            break;
            case Tag.DO:
                while_stmt();
            break;
            case Tag.SCAN:
                read_stmt();
                check(Tag.SEMICOLON);
            break;
            case Tag.PRINT:
                write_stmt();
                check(Tag.SEMICOLON);
            break;     
        }
    }

    // assign-stmt ::= identifier "=" simple_expr
    public int assign_stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException, AnaliseSemanticaException {
        int type = 0;
        Token temp = token;
        //Verifica se a variável está declarada
        Word word = (Word) token; //Casting pra usar o Token como seu filho Word
        if(!token.idDecl)
            throw new AnaliseSemanticaException("Variável não declarada", linhaTokAnterior, colunaTokAnterior);

        check(Tag.ID);
        check(Tag.ASSIGN);
        type = simple_expr();

        if (type != temp.type) 
            throw new AnaliseSemanticaException("O tipo da variável não corresponde com o resultado da expressão", linhaTokAnterior, colunaTokAnterior);
        else
            temp.type = type;
        return 0;
    }

    // if_stmt2 ::= else stmt-list end | end
    public void if_stmt2() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        switch (tagToken) {
            case Tag.END:
                check(Tag.END);
            break;
            case Tag.ELSE:
                check(Tag.ELSE);
                stmt_list();
                check(Tag.END);
            break;  
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "END | ELSE "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        }
    }

    // if_stmt ::= if condition then stmt-list if_stmt2 
    public void if_stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.IF);
        condition();
        check(Tag.THEN);
        stmt_list();
        if_stmt2();
    }

    // condition ::= expression
    public void condition() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        expression();
    }

    // while_stmt ::= do stmt-list stmt-sufix
    public void while_stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.DO);
        stmt_list();
        stmt_sufix();
    }

    // stmt_sufix ::= while condition end
    public void stmt_sufix() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.WHILE);
        condition();
        check(Tag.END);
    }

    // read_stmt ::= scan "(" identifier ")"
    public void read_stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.SCAN);
        check(Tag.LPAREN);

        Word word = (Word) token; //Casting pra usar o Token como seu filho Word
        if(!token.idDecl)
            throw new AnaliseSemanticaException("Variável não declarada", linhaTokAnterior, colunaTokAnterior);
        check(Tag.ID);
        check(Tag.RPAREN);
    
    }

    // write_stmt ::= print "(" writable ")"
    public void write_stmt() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.PRINT);
        check(Tag.LPAREN);
        writable();
        check(Tag.RPAREN);
    }
  
    // writable2 ::= simple-expr | literal
    public void writable2() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        check(Tag.LITERAL); 
        term2();
        simple_expr2();
    }

     // writable ::= simple-expr | writable2
    public void writable() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        switch (tagToken) {
            case Tag.NOT:
            case Tag.SUB:
            case Tag.ID:
            case Tag.INT:
            case Tag.FLOAT:
            case Tag.LPAREN:
                simple_expr();
                break;

            case Tag.LITERAL:
                writable2();
                break;

            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "NOT | SUB | ID | INTEGER | FLOAT | LPAREN "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);

        }
    }

    // expression2 ::= relop simple-expr | ε
    public void expression2() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        switch (tagToken) {
            case Tag.EQ: 
            case Tag.GT:
            case Tag.GE:
            case Tag.LT: 
            case Tag.LE: 
            case Tag.NE: 
                relop();
                simple_expr();
            break;
            default: //Também pode ser lambda
                break; 
        }
    }

    // expression ::= simple-expr expression2
    public void expression() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        simple_expr();
        expression2();
    }

    // simple-expr ::= term simple-expr2
    public int simple_expr() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type1, type2;
        type1 = term();
        type2 = simple_expr2();
        if(type1 != type2 && type2 != 0)
            return 999;
        else{
            int type = Tag.INT;
            if(type1 == Tag.FLOAT || type2 == Tag.FLOAT)
                type = Tag.FLOAT;

            return type;
        }
    }

    // simple-expr2 ::= addop term simple-expr' | ε
    private int simple_expr2() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type1 = 0, type2 = 0;
        int typeAddop=0;
        switch (tagToken) {
            case Tag.ADD:   
            case Tag.SUB:
            case Tag.OR:
                typeAddop = addop();
                type1 = term();
                type2 = simple_expr2();
            break;
            default: //pode ser lambda
                break; 
        }
        if(type1 == 0 && type2 ==0)
            return 0;
        if(type1 != type2 && type2 != 0)
            return 999;
        else{
            int type = Tag.INT;
            if(type1 == Tag.FLOAT || type2 == Tag.FLOAT)
                type = Tag.FLOAT;
            else if(type1 == Tag.LITERAL || type2 == Tag.LITERAL){
                if(type1 == Tag.LITERAL && type2 == Tag.LITERAL)
                    type = Tag.LITERAL;
                else    
                    type = 999;
            }

            return type;
        }
    }

    // term ::= factor-a term2
    public int term() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type1=0, type2=0;
        type1 = factor_a();
        type2 = term2();

        if(type1 != type2)
            return Tag.FLOAT;
        else   
            return type1;
    }

    // term' ::= mulop factor-a term' | ε
    private int term2() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type1=0, type2=0;
        int typeMulop=0;
        if(tagToken == Tag.MUL || tagToken == Tag.DIV || tagToken == Tag.MOD || tagToken == Tag.AND){
            
            typeMulop = mulop();
            type1 = factor_a();
            type2 = term2();
        }
        else
            return 0;

            if(type1!= 999 && type2==0)
            return type1;

        if(typeMulop == Tag.DIV){
            if((type1 != Tag.INT && type1 != Tag.FLOAT) || (type2 != Tag.INT && type2 != Tag.FLOAT)){
                System.out.println("Divisão deve envolver somente inteiros e floats");
                return 999;
            }
            else{    
                int type = Tag.FLOAT;
                if(type1 == Tag.INT || type2 == Tag.INT){
                    System.out.println("Os dois são inteiros");
                    type = Tag.INT;
                }
                return type1;
            }
        }
        else if(typeMulop == Tag.MOD){
            if(type1 == Tag.INT && type2 == Tag.INT)
                return Tag.INT;
            else
                throw new AnaliseSemanticaException("O operador % deve ter os dois termos inteiros", linhaTokAnterior, colunaTokAnterior);
        }
        
        return 0;
    }

    // fator-a ::= "!" factor | "-" factor | factor
    public int factor_a() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type;
        switch (tagToken) {
            case Tag.NOT:
                check(Tag.NOT);
                type = factor();
                break;
            case Tag.SUB:
                check(Tag.SUB);
                type = factor();
                break;
            case Tag.ID:
            case Tag.INT:
            case Tag.FLOAT:
            case Tag.LITERAL:
            case Tag.LPAREN:
                type = factor();
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "NOT | SUB | ID | INTEGER | FLOAT | LPAREN "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        }
        return type;
    }

    // factor ::= identifier | constant | "(" expression ")"
    private int factor() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type=0;
        switch (tagToken) {
            case Tag.ID:
            
                Word word = (Word) token; //Casting pra usar o Token como seu filho Word
                if(!token.idDecl)
                    throw new AnaliseSemanticaException("Variável não declarada", linhaTokAnterior, colunaTokAnterior);
                check(Tag.ID);
                break;
            case Tag.INT:
            case Tag.FLOAT:
            case Tag.LITERAL:
                type = constant();
                break;
            case Tag.LPAREN:
            check(Tag.LPAREN);
            expression();
            check(Tag.RPAREN);
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "NOT | SUB | ID | INTEGER | FLOAT | LPAREN "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);

        }
        return type;
    }

    // Padrões de tokens

    // relop ::= "==" | ">" | ">=" | "<" | "<=" | "!="
    public void relop() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        switch (tagToken) {
            case Tag.EQ:
                check(Tag.EQ);
                break;
            case Tag.GT:
                check(Tag.GT);
                break;
            case Tag.GE:
                check(Tag.GE);
                break;
            case Tag.LT:
                check(Tag.LT);
                break;
            case Tag.LE:
                check(Tag.LE);
                break;
            case Tag.NE:
                check(Tag.NE);
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "EQ | GT | GE | LT| LE | NE "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);

        }
    }

    // addop ::= "+" | "-" | "||"
    public int addop() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type = tagToken;
        switch (tagToken) {
            case Tag.ADD:
                check(Tag.ADD);
                break;
            case Tag.SUB:
                check(Tag.SUB);
                break;
            case Tag.OR:
                check(Tag.OR);
                break;
                default:
                    throw new AnaliseSintaticaException("Tokens esperados: "+ "AND | SUB | OR "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        }
        return type;
    }

    // mulop ::= "*" | "/" | “%” | "&&"
    private int mulop() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type = tagToken;
        switch (tagToken) {
            case Tag.MUL:
                check(Tag.MUL);
                break;
            case Tag.DIV:
                check(Tag.DIV);
                break;
            case Tag.MOD:
                check(Tag.MOD);
                break;
            case Tag.AND:
                check(Tag.AND);
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "MUL | DIV | MOD | AND "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        }
        return type;
    }

    // constant ::= integer_const | float_const | literal
    public int constant() throws IOException, AnaliseSintaticaException, AnaliseSemanticaException {
        int type = token.getTag();
        switch (tagToken) {
            case Tag.INT:
                
                check(Tag.INT);
                return type;

            case Tag.FLOAT:
                check(Tag.FLOAT);
                return type;

            case Tag.LITERAL:
                check(Tag.LITERAL);
                return type;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "INTEGER | FLOAT | LITERAL "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);

        }
    }

    public static void main(String[] args) {
        start();
    }

    // Para impressão no terminal
    public static final Map<Integer, String> TAG_NAMES = new HashMap<>();

    static {
        // Palavras reservadas
        TAG_NAMES.put(Tag.START, "START"); // Início do programa
        TAG_NAMES.put(Tag.EXIT, "EXIT"); // Sair do programa
        TAG_NAMES.put(Tag.INT, "INT"); // Números inteiros
        TAG_NAMES.put(Tag.FLOAT, "FLOAT"); // Números de ponto flutuante
        TAG_NAMES.put(Tag.STRING, "STRING"); // Literais (strings)
        TAG_NAMES.put(Tag.IF, "IF"); // Palavra reservada "if"
        TAG_NAMES.put(Tag.THEN, "THEN"); // Palavra reservada "then"
        TAG_NAMES.put(Tag.ELSE, "ELSE"); // Palavra reservada "else"
        TAG_NAMES.put(Tag.END, "END"); // Palavra reservada "end"
        TAG_NAMES.put(Tag.SCAN, "SCAN"); // Entrada de dados
        TAG_NAMES.put(Tag.PRINT, "PRINT"); // Saída de dados

        // Operadores relacionais
        TAG_NAMES.put(Tag.EQ, "EQ");
        TAG_NAMES.put(Tag.NE, "NE");
        TAG_NAMES.put(Tag.GE, "GE");
        TAG_NAMES.put(Tag.LE, "LE");
        TAG_NAMES.put(Tag.GT, "GT");
        TAG_NAMES.put(Tag.LT, "LT");

        // Operadores aritméticos
        TAG_NAMES.put(Tag.ADD, "ADD");
        TAG_NAMES.put(Tag.SUB, "SUB");
        TAG_NAMES.put(Tag.MUL, "MUL");
        TAG_NAMES.put(Tag.DIV, "DIV");
        TAG_NAMES.put(Tag.MOD, "MOD");

        // Operadores lógicos
        TAG_NAMES.put(Tag.AND, "AND");
        TAG_NAMES.put(Tag.OR, "OR");

        // Pontuação e delimitadores
        TAG_NAMES.put(Tag.SEMICOLON, "SEMICOLON");
        TAG_NAMES.put(Tag.COMMA, "COMMA");
        TAG_NAMES.put(Tag.LPAREN, "LPAREN");
        TAG_NAMES.put(Tag.RPAREN, "RPAREN");

        // Outros tokens
        TAG_NAMES.put(Tag.ID, "ID");
        TAG_NAMES.put(Tag.LITERAL, "LITERAL");
        TAG_NAMES.put(Tag.INT, "INTEGER");
        TAG_NAMES.put(Tag.FLOAT, "FLOAT");
        TAG_NAMES.put(Tag.ASSIGN, "ASSIGN"); 
        TAG_NAMES.put(Tag.DO, "DO");
        TAG_NAMES.put(Tag.WHILE, "WHILE"); 

    }
}
