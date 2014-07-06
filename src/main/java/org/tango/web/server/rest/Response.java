package org.tango.web.server.rest;

import org.tango.client.ez.attribute.Quality;

import javax.annotation.Nullable;

/**
 * Represents server side json response.
 * <p/>
 * Designed to be thread confinement.
 */
public class Response {
    public Object argout;
    public String[] errors;
    public Quality quality;
    public long timestamp;

    /**
     * One of the argout or error messages should not be null
     *
     * @param argout
     * @param errors
     * @param quality
     * @param timestamp
     */
    public Response(@Nullable Object argout, @Nullable String[] errors, @Nullable Quality quality, long timestamp) {
        this.argout = argout;
        this.errors = errors;
        this.quality = quality;
        this.timestamp = timestamp;
    }
}