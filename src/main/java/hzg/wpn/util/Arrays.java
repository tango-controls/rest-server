package hzg.wpn.util;

/**
 * @author ingvord
 * @since 5/25/14@1:14 PM
 */
public class Arrays {
    private Arrays() {
    }

    private static ThreadLocal<StringBuilder> local_bld = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }
    };

    public static String join(Object[] arr, String delimiter) {
        StringBuilder bld = local_bld.get();
        int lastNdx = arr.length - 1;
        for (int i = 0; i < arr.length; i++) {
            Object el = arr[i];
            bld.append(String.valueOf(el));
            if (i < lastNdx) bld.append(delimiter);
        }
        try {
            return bld.toString();
        } finally {
            bld.setLength(0);
        }
    }
}
