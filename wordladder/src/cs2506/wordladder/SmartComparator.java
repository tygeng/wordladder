/**
 *
 */
package cs2506.wordladder;

import java.util.Comparator;

/**
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class SmartComparator implements Comparator<String> {
    int omission;

    public SmartComparator(int omission) {
        this.omission = omission;
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public int compare(String arg0, String arg1) {

        char[] str0 = arg0.toCharArray();
        char[] str1 = arg1.toCharArray();
        char c0 = str0[omission];
        char c1 = str1[omission];
        for (int i = omission; i < str0.length - 1; i++) {
            str0[i] = str0[i + 1];
        }
        str0[str0.length - 1] = c0;
        for (int i = omission; i < str1.length - 1; i++) {
            str1[i] = str1[i + 1];
        }
        str1[str1.length - 1] = c1;
        for (int i = 0; i < (str0.length < str1.length ? str0.length
                : str1.length); i++) {
            if (str0[i] != str1[i]) {
                return str0[i] - str1[i];
            }
        }
        return str0.length < str1.length ? -1 : 1;
        // String str0;
        // String str1;
        // if (omission + 1 == arg0.length()) {
        // str0 = arg0.substring(0, omission) + arg0.charAt(omission);
        // }
        // else {
        //
        // str0 =
        // arg0.substring(0, omission)
        // + arg0.substring(omission + 1)
        // + arg0.charAt(omission);
        // }
        // if (omission + 1 == arg1.length()) {
        // str1 = arg1.substring(0, omission)
        //
        // + arg1.charAt(omission);
        // }
        // else {
        // str1 =
        // arg1.substring(0, omission)
        // + arg1.substring(omission + 1)
        // + arg1.charAt(omission);
        // }
        // return str0.compareTo(str1);
    }
}
