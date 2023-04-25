package keystone.core.modules.filter.remapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StringSubstitutions
{
    private record Substitution(int start, int end, String replaceWith) {}
    
    private final List<Substitution> substitutions = new ArrayList<>();
    
    /**
     * Add a substitution operation to this substitution list.
     * @param start The starting index of the operation based on the source string, inclusive.
     * @param end The end index of the operation based on the source string, exclusive.
     * @param replaceWith The contents to replace the source substring with
     */
    public void add(int start, int end, String replaceWith)
    {
        substitutions.add(new Substitution(start, end, replaceWith));
    }
    public void clear()
    {
        substitutions.clear();
    }
    
    public String perform(String source)
    {
        substitutions.sort(Comparator.comparingInt(o -> o.start));
        
        StringBuilder target = new StringBuilder(source);
        int offset = 0;
        for (Substitution substitution : substitutions)
        {
            target.replace(substitution.start + offset, substitution.end + offset, substitution.replaceWith);
            offset += substitution.replaceWith.length() - (substitution.end - substitution.start);
        }
        
        return target.toString();
    }
}
