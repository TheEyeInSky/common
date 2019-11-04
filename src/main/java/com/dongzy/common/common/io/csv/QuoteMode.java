package com.dongzy.common.common.io.csv;

/**
 * Defines quote behavior when printing.
 *
 * @version $Id: QuoteMode.java 1694977 2015-08-10 07:05:58Z ggregory $
 */
public enum QuoteMode {

    /**
     * Quotes all fields.
     */
    ALL,

    /**
     * Quotes fields which contain special characters such as a delimiter, quotes character or any of the characters in
     * line separator.
     */
    MINIMAL,

    /**
     * Quotes all non-numeric fields.
     */
    NON_NUMERIC,

    /**
     * Never quotes fields. When the delimiter occurs in data, the printer prefixes it with the current escape
     * character. If the escape character is not set, format validation throws an exception.
     */
    NONE
}
