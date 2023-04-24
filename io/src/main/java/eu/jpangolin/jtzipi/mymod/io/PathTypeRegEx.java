package eu.jpangolin.jtzipi.mymod.io;

import java.util.function.Supplier;

public enum PathTypeRegEx implements Supplier<String> {

    /**
     * Any file not matching other regular exp.
     */
    PLAIN(""),

    TEXT("^.*\\.(txt|dat|rtf)$"),
    /**
     * Java file.
     */
;
private final String regEx;
    PathTypeRegEx(final String regExStr) {
this.regEx = regExStr;
    }

    @Override
    public String get() {
        return regEx;
    }

}
