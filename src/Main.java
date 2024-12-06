import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    Tag tag;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Tag tag = new Tag();
        Token tok = null;
        SymbolTable symbolTable = new SymbolTable();
        Lexer lex = new Lexer(symbolTable);

        System.out.println("\033[0;32m------------ TOKENS ------------\033[0m");
        while (true) {
            try {
                tok = lex.scan();
                if (tok == null) {
                    System.out.println("\033[0;32mLexical analysis completed successfully\033[0m");
                    break;
                }

                int tg = tok.getTag();
                if (tok instanceof Word || tok instanceof Num) {
                    String lexema = tok.toString();
                    System.out.println("(\"" + lexema + "\", " + TAG_NAMES.get(tg) + ")");
                } else {
                    System.out.println("(" + TAG_NAMES.get(tg) + ")");
                }
            } catch (AnaliseLexicaException e) { 
                System.out.println("\u001B[31mSyntax error - line: " + e.getLinha() + " column: " + e.getColumn());
                System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
                break;
            }
        }

        // Impressao da tabela de simbolos
        System.out.println("\033[0;32m--------- SYMBOL TABLE ---------\033[0m");
        HashMap<String, Word> table = symbolTable.getTable();
        for (Word token : table.values()) {
            System.out.println(String.format("%-20s -\t %-10s", token.toString(), TAG_NAMES.get(token.getTag())));
        }
    }

    // Para impressão no terminal
    public static final Map<Integer, String> TAG_NAMES = new HashMap<>();

    static {
        // Palavras reservadas
        TAG_NAMES.put(Tag.START, "START"); // Início do programa
        TAG_NAMES.put(Tag.EXIT, "EXIT"); // Sair do programa
        TAG_NAMES.put(Tag.INT, "INT"); // Números inteiros
        TAG_NAMES.put(Tag.FLOATING, "FLOATING"); // Números de ponto flutuante
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
