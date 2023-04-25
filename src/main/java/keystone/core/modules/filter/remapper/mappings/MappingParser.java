package keystone.core.modules.filter.remapper.mappings;

import keystone.core.modules.filter.remapper.enums.MappingType;

import java.util.*;

public class MappingParser
{
    private final Scanner scanner;
    private String currentLine;
    private String[] currentTokens;
    private int currentIndent;
    private boolean done;
    
    private List<Mapping> parsed;
    private Mapping parent;
    
    private MappingParser(Scanner scanner)
    {
        this.scanner = scanner;
        this.parsed = new LinkedList<>();
    }
    public static List<Mapping> parse(Scanner scanner)
    {
        return new MappingParser(scanner).parse();
    }
    
    public List<Mapping> parse()
    {
        // Initialize
        done = false;
        parsed.clear();
        nextLine();
    
        // Parse Loop
        do { parseCurrent(); }
        while (!done);
        
        // Return Result
        return parsed;
    }
    
    private void parseCurrent()
    {
        switch (currentTokens[0].toUpperCase())
        {
            case "CLASS" -> parseClassMapping();
            case "METHOD" -> parseMethodMapping();
            case "FIELD" -> parseFieldMapping();
            default -> skipChildren();
        }
    }
    private void parseClassMapping()
    {
        // Parse Class Mapping
        Mapping mapping;
        if (currentTokens.length == 3) mapping = new Mapping(MappingType.CLASS, currentTokens[1], currentTokens[2]);
        else mapping = new Mapping(MappingType.CLASS, currentTokens[1], currentTokens[1]);
        add(mapping);
        
        // Parse Children
        parent = mapping;
        parseChildren();
        parent = null;
    }
    private void parseMethodMapping()
    {
        // Parse Method Mapping
        Mapping mapping;
        if (currentTokens.length == 4) mapping = new Mapping(MappingType.METHOD, currentTokens[1] + currentTokens[3], currentTokens[2] + currentTokens[3]);
        else mapping = new Mapping(MappingType.METHOD, currentTokens[1] + currentTokens[2], currentTokens[1] + currentTokens[2]);
        add(mapping);
        
        // Skip Parameter Mappings
        skipChildren();
    }
    private void parseFieldMapping()
    {
        // Parse Field Mapping
        Mapping mapping = new Mapping(MappingType.FIELD, currentTokens[1], currentTokens[2]);
        add(mapping);
    
        // Next Line
        nextLine();
    }
    
    private void skipChildren()
    {
        int baseIndent = this.currentIndent;
        
        while (!done)
        {
            nextLine();
            if (this.currentIndent <= baseIndent) break;
        }
    }
    private void parseChildren()
    {
        int baseIndent = this.currentIndent;
        
        nextLine();
        while (!done)
        {
            if (this.currentIndent > baseIndent)
            {
                // Process Child
                parseCurrent();
            }
            else break;
        }
    }
    
    //region Helpers
    private void nextLine()
    {
        if (!scanner.hasNextLine())
        {
            done = true;
            return;
        }
        
        currentLine = scanner.nextLine();
        currentIndent = getIndent();
        currentLine = currentLine.trim();
        
        if (currentLine.length() > 0) currentTokens = currentLine.split(" ");
        else nextLine();
    }
    private int getIndent()
    {
        int indent = 0;
        while (indent < currentLine.length() && currentLine.charAt(indent) == '\t') indent++;
        return indent;
    }
    private void add(Mapping mapping)
    {
        if (parent != null) parent.putMapping(mapping);
        else parsed.add(mapping);
    }
    //endregion
}
