package keystone.core.modules.filter.remapper;

import java.util.ArrayList;
import java.util.List;

public class StringSubstitutions
{
    private record Substitution(int start, int end, String replaceWith) {}
    
    private final List<Substitution> substitutions = new ArrayList<>();
    private int offset = 0;
    
    /**
     * Add a substitution operation to this substitution list.
     * @param start The starting index of the operation based on the source string, inclusive.
     * @param end The end index of the operation based on the source string, exclusive.
     * @param replaceWith The contents to replace the source substring with
     */
    public void add(int start, int end, String replaceWith)
    {
        substitutions.add(new Substitution(start + offset, end + offset, replaceWith));
        offset += replaceWith.length() - (end - start);
    }
    public void clear()
    {
        substitutions.clear();
        offset = 0;
    }
    
    public String perform(String source)
    {
        StringBuilder target = new StringBuilder(source);
        for (Substitution substitution : substitutions) target.replace(substitution.start, substitution.end, substitution.replaceWith);
        return target.toString();
    }
}
