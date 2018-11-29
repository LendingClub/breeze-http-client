package org.lendingclub.http.breeze.util;

import org.lendingclub.http.breeze.exception.BreezeHttpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;

public class BreezeHttpUtil {
    public static final boolean IS_GSON_PRESENT = isPresent("com.google.gson.Gson");
    public static final boolean IS_JACKSON_PRESENT = isPresent("com.fasterxml.jackson.databind.ObjectMapper");
    public static final boolean IS_JSON_PATH_PRESENT = isPresent("com.jayway.jsonpath.DocumentContext");

    private BreezeHttpUtil() {
    }

    public static String readString(InputStream stream) {
        return readString(stream, "UTF-8");
    }

    public static String readString(InputStream stream, String charset) {
        if (stream == null) {
            return null;
        }

        try (Scanner scanner = new Scanner(stream, charset).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static byte[] readBytes(InputStream stream) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int read;
            byte[] data = new byte[1024];
            while ((read = stream.read(data, 0, data.length)) != -1) {
                output.write(data, 0, read);
            }

            output.flush();
            return output.toByteArray();
        } catch (IOException e) {
            throw new BreezeHttpException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String quote(Object o) {
        return o == null ? "null" : "\"" + o + "\"";
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String simpleName(Object o) {
        return o == null ? "null" : quote(o.getClass().getSimpleName());
    }
}
