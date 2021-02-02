package keystone.api.utils;

public class StringUtils
{
    public static final String enumCaseToTitleCase(String text)
    {
        return titleCase(text.toLowerCase().replace('_', ' '));
    }
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
