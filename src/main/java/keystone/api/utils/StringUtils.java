package keystone.api.utils;

/**
 * A collection of static functions for manipulating Strings
 */
public class StringUtils
{
    /**
     * Convert an enum name to title case. [e.g. TEST_VALUE -&gt; "Test Value"]
     * @param text The enum name to convert
     * @return The title cased string
     */
    public static final String enumCaseToTitleCase(String text)
    {
        return titleCase(text.toLowerCase().replace('_', ' '));
    }

    /**
     * Convert a snake case string to title case. [e.g. test_value -&gt; "Test Value"]
     * Functionally identical to enumCaseToTitleCase
     * @param text The snake case string to convert
     * @return The title cased string
     */
    public static final String snakeCaseToTitleCase(String text)
    {
        return enumCaseToTitleCase(text);
    }
    /**
     * Convert a string to title case. [e.g. "test string" -&gt; "Test String"]
     * @param text The text to convert
     * @return The title cased string
     */
    public static final String titleCase(String text)
    {
        if (text == null || text.isEmpty()) return text;

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++)
        {
            char ch = chars[i];

            if (Character.isSpaceChar(ch) || Character.isUpperCase(chars[i + 1])) convertNext = true;
            else if (convertNext)
            {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            }
            else ch = Character.toLowerCase(ch);

            converted.append(ch);
        }
        converted.append(chars[chars.length - 1]);

        return converted.toString();
    }
    /**
     * Add spaces to a sentence string. [e.g. "TestString" -&gt; "Test String"]
     * @param text The text to convert
     * @return The spaced string
     */
    public static final String addSpacesToSentence(String text)
    {
        if (text == null || text.isEmpty()) return text;

        StringBuilder converted = new StringBuilder();

        char[] chars = text.toCharArray();
        boolean isLowerCase = Character.isLowerCase(chars[0]);
        converted.append(chars[0]);

        for (int i = 1; i < chars.length; i++)
        {
            char ch = chars[i];
            boolean upper = Character.isUpperCase(ch);

            if (isLowerCase && upper) converted.append(' ');
            isLowerCase = Character.isLowerCase(ch);

            converted.append(ch);
        }

        return converted.toString();
    }
}
