package keystone.core.modules.filter.remapper.mappings;

import keystone.api.Keystone;
import keystone.core.modules.filter.remapper.enums.MappingType;

import java.util.*;

public class MappingParser
{
    private final Scanner scanner;
    private String currentLine;
    private String[] currentTokens;
    private int currentIndent;
    private boolean done;
    
    private final List<Mapping> parsed;
    private final Stack<Mapping> childStack;
    
    private MappingParser(Scanner scanner)
    {
        this.scanner = scanner;
        this.parsed = new LinkedList<>();
        this.childStack = new Stack<>();
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
        // Ignore Anonymous Classes
        try
        {
            // If the class name can be parsed into an int, ignore this class mapping
            Integer.parseInt(currentTokens[1]);
            skipChildren();
            return;
        }
        catch (NumberFormatException ignored) {}
    
        // Parse Class Mapping
        Mapping mapping;
        if (currentTokens.length == 3) mapping = new Mapping(MappingType.CLASS, currentTokens[1], currentTokens[2]);
        else mapping = new Mapping(MappingType.CLASS, currentTokens[1], currentTokens[1]);
        add(mapping);
        
        // Parse Children
        childStack.push(mapping);
        parseChildren();
        childStack.pop();
    }
    private void parseMethodMapping()
    {
        if (currentTokens.length == 4 && currentTokens[1].charAt(0) != '<')
        {
            // Parse Method Mapping
            String obfuscatedName = currentTokens[1];
            String deobfuscatedName = currentTokens[2];
            String descriptor = currentTokens[3];
            descriptor = descriptor.substring(0, descriptor.indexOf(')') + 1);
            add(new Mapping(MappingType.METHOD, obfuscatedName + descriptor, deobfuscatedName + descriptor));
        }
        
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
        if (childStack.size() > 0) childStack.peek().putMapping(mapping);
        else parsed.add(mapping);
    }
    //endregion
}
