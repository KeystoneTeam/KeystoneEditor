package keystone.core.modules.filter.remapper;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import keystone.api.Keystone;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptor;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FilterRemapper
{
    //region Static
    private static final ParserConfiguration PARSER_CONFIGURATION;
    private static final MappingTree MAPPINGS;
    static
    {
        MAPPINGS = MappingTree.builtin();
    
        TypeSolver typeSolver = new ClassLoaderTypeSolver(new RemappingClassLoader(MAPPINGS, KeystoneMod.class.getClassLoader()));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        
        PARSER_CONFIGURATION = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setSymbolResolver(symbolSolver);
    }
    //endregion
    
    private final String fileName;
    private final List<Integer> lineStarts;
    private final StringSubstitutions substitutions;
    private final String sourceCode;
    
    public FilterRemapper(File filterFile) throws IOException
    {
        this.fileName = filterFile.getName();
        this.lineStarts = new ArrayList<>();
        this.substitutions = new StringSubstitutions();
    
        StringBuilder sourceBuilder = new StringBuilder();
        int lastLineStart = 0;
        try (Stream<String> lineStream = Files.lines(filterFile.toPath()))
        {
            List<String> lines = lineStream.toList();
            for (String line : lines)
            {
                sourceBuilder.append(line).append(System.lineSeparator());
                lineStarts.add(lastLineStart);
                lastLineStart += line.length() + System.lineSeparator().length();
            }
        }
        this.sourceCode = sourceBuilder.substring(0, sourceBuilder.length() - System.lineSeparator().length());
    }
    
    //region Core
    public String remap() throws IOException
    {
        if (!fileName.equalsIgnoreCase("Vanilla Test.java")) return sourceCode;
        
        // Calculate Remapping Operations
        substitutions.clear();
        try
        {
            // Configure Parser
            StaticJavaParser.setConfiguration(PARSER_CONFIGURATION);
            
            // Parse Code
            CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);
    
            // Header
            Keystone.LOGGER.info(fileName);
            Keystone.LOGGER.info("============================================================");
    
            // Print Nodes
            Keystone.LOGGER.info("Nodes:");
            List<Node> nodes = compilationUnit.findAll(Node.class);
            nodes.forEach(node ->
            {
                Keystone.LOGGER.info(node.getClass().getName());
                Keystone.LOGGER.info(node);
                Keystone.LOGGER.info("");
            });
            
            // Remap Imports
            Keystone.LOGGER.info("Remappers:");
            compilationUnit.findAll(MethodCallExpr.class).forEach(this::remapMethodCall);
    
            // Footer
            Keystone.LOGGER.info("============================================================");
        }
        catch (Exception e)
        {
            Keystone.LOGGER.error("Parse failed on " + fileName + "!");
            e.printStackTrace();
            return this.sourceCode;
        }
        
        // Perform Remapping Operations
        String remapped = substitutions.perform(this.sourceCode);
        return this.sourceCode;
    }
    
    //endregion
    //region Remappers
    private void remapNameExpression(NameExpr node)
    {
        ResolvedType resolved = node.getSymbolResolver().calculateType(node);
        if (resolved instanceof ResolvedReferenceType referenceType)
        {
            ClassDescriptor classDescriptor = ClassDescriptor.fromName(referenceType.getQualifiedName());
            if (node.getNameAsString().equals(classDescriptor.getSimpleName()))
            {
                ClassDescriptor remapped = classDescriptor.remap(RemappingDirection.OBFUSCATING, MAPPINGS);
                if (!remapped.equals(classDescriptor)) replaceNode(node, remapped.getQualifiedName());
            }
        }
    }
    private void remapMethodCall(MethodCallExpr node)
    {
        MethodDescriptor descriptor = MethodDescriptor.fromMethodCall(node);
        if (node.getScope().isPresent() && node.getScope().get() instanceof NameExpr name)
        {
            ResolvedType type = node.getSymbolResolver().calculateType(name);
            Keystone.LOGGER.info(type.getClass().getName());
        }
    }
    //endregion
    //region Remap Helpers
    private void replaceNode(Node node, String replaceWith) throws IllegalArgumentException
    {
        Optional<Position> begin = node.getBegin();
        Optional<Position> end = node.getEnd();
        if (begin.isEmpty() || end.isEmpty()) throw new IllegalArgumentException("Node '" + node.getClass().getSimpleName() + "' doesn't have a beginning or ending position!");
        substitutions.add(positionToIndex(begin.get()), positionToIndex(end.get()) + 1, replaceWith);
    }
    private int positionToIndex(Position position)
    {
        return lineStarts.get(position.line - 1) + position.column - 1;
    }
    //endregion
}
