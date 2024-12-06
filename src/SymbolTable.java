import java.util.HashMap;


public class SymbolTable {
    private final HashMap<String, Word> table;

    public SymbolTable(){
        table=new HashMap<>();
        initializeReservedWords();
    }

    private void initializeReservedWords() {
        table.put("start", new Word("start", Tag.START));   // Início do programa
        table.put("exit", new Word("exit", Tag.EXIT));      // Sair do programa
        table.put("int", new Word("int", Tag.INT));         // Números inteiros
        table.put("floating", new Word("floating", Tag.FLOATING)); // Números de ponto flutuante
        table.put("string", new Word("string", Tag.STRING)); // Literais (strings)
        table.put("if", new Word("if", Tag.IF));            // Palavra reservada "if"
        table.put("then", new Word("then", Tag.THEN));      // Palavra reservada "then"
        table.put("else", new Word("else", Tag.ELSE));      // Palavra reservada "else"
        table.put("end", new Word("end", Tag.END));         // Palavra reservada "end"
        table.put("scan", new Word("scan", Tag.SCAN));      // Entrada de dados
        table.put("print", new Word("print", Tag.PRINT));   // Saída de dados
        table.put("do", new Word("do", Tag.DO));      // Entrada de dados
        table.put("while", new Word("while", Tag.WHILE));   // Saída de dados

    }
    

    public void put(String lexeme, Word token){
        table.put(lexeme, token);
    }

    public Word get(String lexeme){
        return table.get(lexeme);
    }

    public HashMap<String, Word> getTable(){
        return table;
    }

    public boolean contains(String lexeme){
        return table.containsKey(lexeme);
    }
}
