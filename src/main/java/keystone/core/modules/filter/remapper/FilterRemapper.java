package keystone.core.modules.filter.remapper;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import keystone.api.Keystone;
import keystone.core.DebugFlags;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptor;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.mappings.MappingTree;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

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
    public static final MappingTree MAPPINGS;
    public static final RemappingClassLoader REMAPPING_CLASS_LOADER;
    
    private static final ParserConfiguration PARSER_CONFIGURATION;
    static
    {
        MAPPINGS = MappingTree.builtin();
        REMAPPING_CLASS_LOADER = new RemappingClassLoader(MAPPINGS, KeystoneMod.class.getClassLoader());
        PARSER_CONFIGURATION = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(REMAPPING_CLASS_LOADER)));
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
        // If the game is running in a development environment, don't remap
        if (FabricLauncherBase.getLauncher().isDevelopment())
        {
            if (DebugFlags.isFlagSet("debugRemapper") && fileName.equalsIgnoreCase("Vanilla Test.java")) remapInternal();
            return sourceCode;
        }
        else return remapInternal();
    }
    private String remapInternal()
    {
        boolean debug = DebugFlags.isFlagSet("debugRemapper");
        
        // Calculate Remapping Operations
        substitutions.clear();
        try
        {
            // Configure Parser
            StaticJavaParser.setConfiguration(PARSER_CONFIGURATION);
        
            // Parse Code
            CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);
        
            // Debug Printing
            if (debug)
            {
                // Print Header
                Keystone.LOGGER.info("==================== REMAPPING " + fileName + " ====================");
                
                if (DebugFlags.isFlagSet("debugRemapper.astNodes"))
                {
                    // Print Nodes
                    Keystone.LOGGER.info("Nodes:");
                    List<Node> nodes = compilationUnit.findAll(Node.class);
                    nodes.forEach(node ->
                    {
                        Keystone.LOGGER.info(node.getClass().getName());
                        Keystone.LOGGER.info(node);
                        Keystone.LOGGER.info("");
                    });
                }
    
                // Print Remappers Section
                Keystone.LOGGER.info("Remappers:");
            }
        
            // Remap Imports
            compilationUnit.findAll(ImportDeclaration.class).forEach(this::remapImport);
            compilationUnit.findAll(ClassOrInterfaceType.class).forEach(this::remapClassType);
            compilationUnit.findAll(MethodCallExpr.class).forEach(this::remapMethodCall);
            compilationUnit.findAll(FieldAccessExpr.class).forEach(this::remapFieldAccess);
        }
        catch (Exception e)
        {
            Keystone.LOGGER.error("Parse failed on " + fileName + "!");
            e.printStackTrace();
            return this.sourceCode;
        }
    
        // Perform Remapping Operations
        String remapped = substitutions.perform(this.sourceCode);
        if (debug)
        {
            // Print Remapped Code
            Keystone.LOGGER.info("");
            Keystone.LOGGER.info("Remapped Code:" + System.lineSeparator() + remapped);
            
            // Print Debug Footer
            Keystone.LOGGER.info("=".repeat(52 + fileName.length()));
        }
        return remapped;
    }
    //endregion
    //region Remappers
    private void remapImport(ImportDeclaration node)
    {
        // Remap the Import Node
        ClassDescriptor importClass = ClassDescriptor.fromName(node.getNameAsString());
        ClassDescriptor remapped = importClass.remap(RemappingDirection.OBFUSCATING, MAPPINGS);
        if (!remapped.equals(importClass)) replaceNode(node.getName(), remapped.getQualifiedName());
    }
    private void remapClassType(ClassOrInterfaceType node)
    {
        ClassDescriptor classType = ClassDescriptor.fromName(node.resolve().asReferenceType().getQualifiedName());
        ClassDescriptor remapped = classType.remap(RemappingDirection.OBFUSCATING, MAPPINGS);
        if (!remapped.equals(classType)) renameClassName(node.getNameAsExpression(), remapped);
    }
    private void remapMethodCall(MethodCallExpr node)
    {
        Optional<MethodDescriptor> methodDescriptorOptional = MethodDescriptor.fromMethodCall(node, MAPPINGS);
        methodDescriptorOptional.ifPresent(methodDescriptor ->
        {
            MethodDescriptor remapped = methodDescriptor.remap(RemappingDirection.OBFUSCATING, MAPPINGS);
            if (!remapped.equals(methodDescriptor))
            {
                // Remap Method Name
                replaceNode(node.getName(), remapped.getName());
        
                // Remap Static Scope
                if (methodDescriptor.isStatic() && node.getScope().isPresent())
                {
                    Node scope = node.getScope().get();
                    if (scope instanceof NameExpr className) renameClassName(className, remapped.getDeclaringClass());
                    else Keystone.LOGGER.info("Unknown static method scope node type: " + scope.getClass().getName());
                }
            }
        });
    }
    private void remapFieldAccess(FieldAccessExpr node)
    {
        try
        {
            ResolvedValueDeclaration resolved = node.resolve();
            if (resolved.isField())
            {
                ResolvedFieldDeclaration fieldDeclaration = resolved.asField();
                ClassDescriptor owner = ClassDescriptor.fromName(fieldDeclaration.declaringType().asReferenceType().getQualifiedName());
                
                MAPPINGS.lookupMapping(RemappingDirection.OBFUSCATING, owner).ifPresent(mapping ->
                {
                    mapping.lookup(RemappingDirection.OBFUSCATING, MappingType.FIELD, node.getNameAsString()).ifPresent(field ->
                    {
                        replaceNode(node.getName(), field);
                    });
                });
            }
        }
        catch (Exception ignored) {}
    }
    //endregion
    //region Remap Helpers
    private void renameClassName(NameExpr name, ClassDescriptor remapped) throws IllegalArgumentException
    {
        if (name.isQualified()) replaceNode(name, remapped.getQualifiedName());
        else replaceNode(name, remapped.getSimpleName());
    }
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
