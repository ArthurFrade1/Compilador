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

    public Syntax() throws FileNotFoundException {
        Scanner scan = new Scanner(System.in);
        symbolTable = new SymbolTable();
        System.out.println("Nome do arquivo a ser compilado: ");
        String filePath = scan.nextLine();
        lexer = new Lexer(filePath, symbolTable);
    }

    private Token bufferedToken = null;

    // Avança e anda
    private void check(int tag) throws IOException, AnaliseSintaticaException {

        try{

        Token token = null;

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
            System.out.println("\033[0;32mSyntax analysis completed successfully\033[0m");

        }catch (AnaliseLexicaException e) {
            System.out.println("\u001B[31mLexical error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }catch (AnaliseSintaticaException e) {
            System.out.println("\u001B[31mSyntax error - line: " + e.getLine() + " column: " + e.getColumn());
            System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
        }
        catch (IOException e) {
            System.out.println("Exceção de entrada e saída");
        }
    }

    

    // program ::= start [decl-list] stmt-list exit
    public void program() throws IOException, AnaliseSintaticaException {
        check(Tag.START);
        while (tagToken == Tag.INT || tagToken == Tag.FLOAT ||tagToken == Tag.STRING) {
            decl_list();
        }
        stmt_list();
        check(Tag.EXIT);
    }

    // decl_list ::= decl {decl}
    public void decl_list() throws IOException, AnaliseSintaticaException {
        decl();
        while (tagToken == Tag.INT || tagToken == Tag.FLOAT ||tagToken == Tag.STRING) {
            decl();
        }
    }

    // decl ::= type ident-list ";"
    public void decl() throws IOException, AnaliseSintaticaException {
        type();
        ident_list();
        check(Tag.SEMICOLON);
    }

    // ident_list ::= identifier {"," identifier}
    public void ident_list() throws IOException, AnaliseSintaticaException {
        check(Tag.ID);
        ////////////////////
        while (tagToken == Tag.COMMA) {
            check(Tag.COMMA);
            check(Tag.ID);
        }
    }

    // type ::= int | float | string
    public void type() throws IOException, AnaliseSintaticaException {
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
        }


    }

    // stmt-list ::= stmt {stmt}
    public void stmt_list() throws IOException, AnaliseSintaticaException {
        stmt();
        while (tagToken == Tag.ID || tagToken == Tag.IF || tagToken == Tag.DO || tagToken == Tag.SCAN || tagToken == Tag.PRINT) {
            stmt();
        }
    }

    // stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";
    public void stmt() throws IOException, AnaliseSintaticaException {
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
    public void assign_stmt() throws IOException, AnaliseSintaticaException {
        check(Tag.ID);
        check(Tag.ASSIGN);
        simple_expr();
    }

    // if_stmt2 ::= else stmt-list end | end
    public void if_stmt2() throws IOException, AnaliseSintaticaException {
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
    public void if_stmt() throws IOException, AnaliseSintaticaException {
        check(Tag.IF);
        condition();
        check(Tag.THEN);
        stmt_list();
        if_stmt2();
    }

    // condition ::= expression
    public void condition() throws IOException, AnaliseSintaticaException {
        expression();
    }

    // while_stmt ::= do stmt-list stmt-sufix
    public void while_stmt() throws IOException, AnaliseSintaticaException {
        check(Tag.DO);
        stmt_list();
        stmt_sufix();
    }

    // stmt_sufix ::= while condition end
    public void stmt_sufix() throws IOException, AnaliseSintaticaException {
        check(Tag.WHILE);
        condition();
        check(Tag.END);
    }

    // read_stmt ::= scan "(" identifier ")"
    public void read_stmt() throws IOException, AnaliseSintaticaException {
        check(Tag.SCAN);
        check(Tag.LPAREN);
        check(Tag.ID);
        check(Tag.RPAREN);
    
    }

    // write_stmt ::= print "(" writable ")"
    public void write_stmt() throws IOException, AnaliseSintaticaException {
        check(Tag.PRINT);
        check(Tag.LPAREN);
        writable();
        check(Tag.RPAREN);
    }
  
    // writable2 ::= simple-expr | literal
    public void writable2() throws IOException, AnaliseSintaticaException {
        check(Tag.LITERAL); 
        term2();
        simple_expr2();
    }

     // writable ::= simple-expr | writable2
    public void writable() throws IOException, AnaliseSintaticaException {
        switch (tagToken) {
            case Tag.NOT:
            case Tag.SUB:
            case Tag.ID:
            case Tag.INTEGER:
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
    public void expression2() throws IOException, AnaliseSintaticaException {
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
    public void expression() throws IOException, AnaliseSintaticaException {
        simple_expr();
        expression2();
    }

    // simple-expr ::= term simple-expr'
    public void simple_expr() throws IOException, AnaliseSintaticaException {
        term();
        simple_expr2();
    }

    // simple-expr2 ::= addop term simple-expr' | ε
    private void simple_expr2() throws IOException, AnaliseSintaticaException {
        switch (tagToken) {
            case Tag.ADD:   
            case Tag.SUB:
            case Tag.OR:
                addop();
                term();
                simple_expr2();
            break;
            default: //pode ser lambda
                break; 
        }
    }

    // term ::= factor-a term'
    public void term() throws IOException, AnaliseSintaticaException {
        factor_a();
        term2();
    }

    // term' ::= mulop factor-a term' | ε
    private void term2() throws IOException, AnaliseSintaticaException {
        if(tagToken == Tag.MUL || tagToken == Tag.DIV || tagToken == Tag.MOD || tagToken == Tag.AND){
            mulop();
            factor_a();
            term2();
            }
    }

    // fator-a ::= "!" factor | "-" factor | factor
    public void factor_a() throws IOException, AnaliseSintaticaException {
        switch (tagToken) {
            case Tag.NOT:
                check(Tag.NOT);
                factor();
                break;
            case Tag.SUB:
                check(Tag.SUB);
                factor();
                break;
            case Tag.ID:
            case Tag.INTEGER:
            case Tag.FLOAT:
            case Tag.LITERAL:
            case Tag.LPAREN:
                factor();
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "NOT | SUB | ID | INTEGER | FLOAT | LPAREN "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);
        }
    }

    // factor ::= identifier | constant | "(" expression ")"
    private void factor() throws IOException, AnaliseSintaticaException {
        switch (tagToken) {
            case Tag.ID:
                check(Tag.ID);
                break;
            case Tag.INTEGER:
            case Tag.FLOAT:
            case Tag.LITERAL:
                constant();
                break;
            case Tag.LPAREN:
            check(Tag.LPAREN);
            expression();
            check(Tag.RPAREN);
                break;
            default:
                throw new AnaliseSintaticaException("Tokens esperados: "+ "NOT | SUB | ID | INTEGER | FLOAT | LPAREN "+"\nToken recebido: "+TAG_NAMES.get(tagToken), linhaTokAnterior, colunaTokAnterior);

        }
    }

    // Padrões de tokens

    // relop ::= "==" | ">" | ">=" | "<" | "<=" | "!="
    public void relop() throws IOException, AnaliseSintaticaException {
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
    public void addop() throws IOException, AnaliseSintaticaException {
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
    }

    // mulop ::= "*" | "/" | “%” | "&&"
    private void mulop() throws IOException, AnaliseSintaticaException {
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
    }

    // constant ::= integer_const | float_const | literal
    public void constant() throws IOException, AnaliseSintaticaException {
        switch (tagToken) {
            case Tag.INTEGER:
                check(Tag.INTEGER);
                break;
            case Tag.FLOAT:
                check(Tag.FLOAT);
                break;
            case Tag.LITERAL:
                check(Tag.LITERAL);
                break;
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
        TAG_NAMES.put(Tag.INTEGER, "INTEGER");
        TAG_NAMES.put(Tag.FLOAT, "FLOAT");
        TAG_NAMES.put(Tag.ASSIGN, "ASSIGN"); 
        TAG_NAMES.put(Tag.DO, "DO");
        TAG_NAMES.put(Tag.WHILE, "WHILE"); 

    }
}
