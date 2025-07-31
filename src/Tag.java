import java.util.HashMap;
import java.util.Map;

public class Tag {
    // Operadores relacionais
    public final static int NULL = 0;
    public final static int EQ = 288;    // "=="
    public final static int ASSIGN = 294;    // "=" (símbolo de atribuição)
    public final static int NE = 289;    // "!="
    public final static int GE = 290;    // ">="
    public final static int LE = 291;    // "<="
    public final static int GT = 292;    // ">"
    public final static int LT = 293;    // "<"

    // Operadores aritméticos
    public final static int ADD = 300;   // "+"
    public final static int SUB = 301;   // "-"
    public final static int MUL = 302;   // "*"
    public final static int DIV = 303;   // "/"
    public final static int MOD = 304;   // "%"
    public final static int NOT = 247;   // "-"

    // Operadores lógicos
    public final static int AND = 310;   // "&&"
    public final static int OR = 311;    // "||"

    // Pontuação e delimitadores
    public final static int SEMICOLON = 320; // ";"
    public final static int COMMA = 321;     // ","
    public final static int LPAREN = 322;    // "("
    public final static int RPAREN = 323;    // ")"

    //TIPOIS
    public final static int ID = 330;        // Identificadores
    public final static int LITERAL = 331;   // Literais (strings)
    public final static int INTEGER = 332;   // Números inteiros
    public final static int FLOAT = 333;     // Números de ponto flutuante
    
    // Palavras reservadas
    public final static int START = 334;   // Início do programa
    public final static int EXIT = 335;    // Sair do programa
    public final static int INT = 336;     // Números inteiros
    public final static int STRING = 338;  // Literais (strings)
    public final static int IF = 339;      // Palavra reservada "if"
    public final static int THEN = 340;    // Palavra reservada "then"
    public final static int ELSE = 341;    // Palavra reservada "else"
    public final static int END = 342;     // Palavra reservada "end"
    public final static int SCAN = 343;    // Palavra reservada "scan" (entrada de dados)
    public final static int PRINT = 344;   // Palavra reservada "print" (saída de dados)
    public final static int DO = 345;    // Palavra reservada "do"
    public final static int WHILE = 346;   // Palavra reservada "while"


    public final static int ERROR = 999;   // Palavra reservada "while"
}
