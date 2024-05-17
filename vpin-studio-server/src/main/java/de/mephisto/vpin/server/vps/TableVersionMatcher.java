package de.mephisto.vpin.server.vps;

import org.apache.commons.lang3.StringUtils;

public class TableVersionMatcher {

    public static double versionDistance(String version1, String version2) {

        VersionTokenizer tokenizer1 = new VersionTokenizer(cleanVersion(version1));
        VersionTokenizer tokenizer2 = new VersionTokenizer(cleanVersion(version2));

        int number1 = 0, number2 = 0;

        while (tokenizer1.MoveNext()) {
            if (!tokenizer2.MoveNext()) {
                // Version1 is longer than version2
                return 0.3;
            }

            number1 = tokenizer1.getNumber();
            number2 = tokenizer2.getNumber();

            if (number1 != number2) {
                return Math.abs(number1 - number2);
            }
        }
        if (tokenizer2.MoveNext()) {
            // Version2 is longer than version1
            return 0.3;
        }

        String suffix1 = tokenizer1.getSuffix();
        String suffix2 = tokenizer2.getSuffix();
        return suffix1.equalsIgnoreCase(suffix2)? 0: 0.1;
    }

    private static String cleanVersion(String version) {
        if (StringUtils.startsWithIgnoreCase(version, "VP")) {
            int c, p = 2, l = version.length();
            while (p<l && StringUtils.indexOf("XS 0123456789.", c=version.charAt(p))>=0) {
                p++;
            }
            version = version.substring(p);
        }
        return version;
    }

    public static class VersionTokenizer {
        private final String _versionString;
        private final int _length;

        private int _position;
        private int _number;

        public int getNumber() {
            return _number;
        }

        public String getSuffix() {
            return _versionString.substring(_position);
        }

        public VersionTokenizer(String versionString) {
            if (versionString == null) {
                throw new IllegalArgumentException("versionString is null");
            }
            _versionString = versionString;
            _length = versionString.length();

            // remove all non first non numeric characters
            while (_position < _length) {
                char c = _versionString.charAt(_position);
                if (c >= '0' && c <= '9') break;
                _position++;
            }
        }

        public boolean MoveNext() {
            _number = 0;
            boolean hasValue = false;

            // No more characters ?
            while (_position < _length) {
                char c = _versionString.charAt(_position);
                if (c == '.') {
                    _position++;
                    break;
                }
                if (c < '0' || c > '9') { 
                    break;
                }
                _number = _number * 10 + (c - '0');
                _position++;
                hasValue = true;
            }
            return hasValue;
        }
    }
}