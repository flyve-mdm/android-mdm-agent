package org.fusioninventory.categories;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class StringUtils {

    private StringUtils() {
        // Utility class.
    }

    public static String join(Collection<String> collection, String delimiter, boolean reversed) {
        if (collection != null) {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> iter = collection.iterator();
            while (iter.hasNext()) {
                if (!reversed) {
                    buffer.append(iter.next());
                    if (iter.hasNext()) {
                        buffer.append(delimiter);

                    }
                } else {
                    buffer.insert(0, iter.next());
                    if (iter.hasNext()) {
                        buffer.insert(0, delimiter);

                    }
                }
            }
            return buffer.toString();
        } else {
            return null;
        }
    }

    public static String join(Collection<String> collection, String delimiter) {

        return StringUtils.join(collection, delimiter, false);

    }

    public static byte[] int_to_byte(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    public static String int_to_ip(int value) {
        byte[] b = int_to_byte(value);
        Stack<String> stack = new Stack<String>();
        for (byte c : b) {
            stack.push(String.valueOf(0xFF & c));
        }

        return (StringUtils.join(stack, ".", true));
    }
}