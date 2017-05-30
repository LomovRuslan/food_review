import java.util.ArrayList;
import java.util.List;

public class SubList {
    //Method for splitting the string by certain delimiter. This algorithm splits the string by delimiter and finds the last occurrence of the dot
    //to have full sentence. Keep iterating with delimiter until the end of the string.
    static List<String> splitWithDelimiter(String stringToSplit, int delimiter) {
        List<String> subList = new ArrayList<>();
        int start = 0;
        int end = 0;
        String currentSubstring;
        int i = 0;
        String subString;
        for (; i < stringToSplit.length(); i += delimiter) {
            //first sequence
            if (i == 0) {
                end = countIndex(stringToSplit, stringToSplit.substring(0, start + delimiter));
                if (stringToSplit.charAt(end) == '.' || stringToSplit.charAt(end + 1) == '.')
                    end += 1;
                subList.add(stringToSplit.substring(0, end));
                if (stringToSplit.charAt(end) == '.')
                    end += 1;
                i = end;
                //middle sequence
            } else if (i < stringToSplit.length()) {
                start = end;
                currentSubstring = stringToSplit.substring(0, start + delimiter);
                end = countIndex(stringToSplit, currentSubstring);
                //case when there is no dot symbol in the sentence
                if (end + 1 == start)
                    end = start + delimiter;
                    //case when dot symbol comes before current start point than start from last space
                else if (start > end)
                    end = currentSubstring.lastIndexOf(" ");
                    //if last symbol is dot that include it
                else if (stringToSplit.charAt(end) == '.')
                    end += 1;
                //processing the substring
                if (start + delimiter == stringToSplit.length()) {
                    subList.add(stringToSplit.substring(start, stringToSplit.length()));
                    break;
                } else
                    subString = stringToSplit.substring(start, end);
                //start from the next symbol after substring processing
                if (subString.length() == delimiter && stringToSplit.charAt(end) == '.')
                    end += 1;
                subList.add(subString);
                i = end;
            }
        }
        //last sequence
        if (end <= stringToSplit.length())
            subList.add(stringToSplit.substring(end));
        return subList;
    }

    private static int countIndex(String stringToSplit, String subString) {
        if (subString.lastIndexOf(".") == -1)
            if (stringToSplit.charAt(subString.length()) == '.')
                return subString.length() - 1;
            else {
                return subString.lastIndexOf(" ");
            }
        return subString.lastIndexOf(".");
    }

}
