package keystone.api.utils;

public class StringUtils
{
    public static final String titleCase(String text)
    {
        if (text == null || text.isEmpty()) return text;

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray())
        {
            if (Character.isSpaceChar(ch)) convertNext = true;
            else if (convertNext)
            {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            }
            else ch = Character.toLowerCase(ch);

            converted.append(ch);
        }

        return converted.toString();
    }
    public static final String addSpacesToSentence(String text)
    {
        return text.replaceAll("(\\B[A-Z]+?(?=[A-Z][^A-Z])|\\B[A-Z]+?(?=[^A-Z]))", text);
    }
}
