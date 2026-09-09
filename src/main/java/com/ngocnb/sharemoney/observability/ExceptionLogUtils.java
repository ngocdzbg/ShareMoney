package com.ngocnb.sharemoney.observability;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

public final class ExceptionLogUtils {

    private static final Pattern URL_CREDENTIALS = Pattern.compile(
            "([a-zA-Z][a-zA-Z0-9+.-]*://)([^:@\\s/?#]+):([^@\\s/?#]+)@"
    );
    private static final Pattern SENSITIVE_KEY_VALUE = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key|credential|authorization|cookie|database[_-]?url|redis[_-]?url)(\\s*[=:]\\s*)([^\\s,;&]+)"
    );

    private ExceptionLogUtils() {
    }

    public static Throwable rootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    public static String describe(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }

        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getName();
        }

        return throwable.getClass().getName() + ": " + sanitize(message);
    }

    public static String stackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return sanitize(stringWriter.toString());
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String sanitized = URL_CREDENTIALS.matcher(value).replaceAll("$1$2:****@");
        return SENSITIVE_KEY_VALUE.matcher(sanitized).replaceAll("$1$2****");
    }
}
