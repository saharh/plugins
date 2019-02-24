package com.applaudsoft.wabi.virtual_number.fragments.dialog;

import android.text.TextUtils;

import java.text.Normalizer;

import androidx.annotation.Nullable;

/**
 * Created by Sahar on 26/02/2016.
 */
public class StringUtils {
    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     * <p>
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(CharSequence str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     * <p>
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is
     * not empty and not null and not whitespace
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    @Nullable
    public static String reduceSpaces(@Nullable String str) {
        return str != null ? str.replaceAll("\\s{2,}", " ").trim() : null;
    }

    public static String emptyStringIfNull(String str) {
        return str != null ? str : "";
    }

    public static String truncate(String str, int length) {
        if (str != null && str.length() > length) {
            return str.substring(0, length);
        }
        return str;
    }

    public static boolean isOnlyNumbers(String str) {
        return str != null && str.matches("[0-9]+");
    }

    public static String trimOrNull(String str) {
        return str != null ? str.trim() : null;
    }

    public static String flattenToAscii(String string) {
        if (isBlank(string)) {
            return string;
        }
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') {
                sb.append(c);
            } else if (c == '\u00DF') { // 'ß'
                sb.append("ss");
            }
        }
        return sb.toString();
    }
}
