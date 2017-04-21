package org.tango.web.server.interceptors;

import org.tango.web.server.cache.CachedEntity;
import org.tango.web.server.cache.SimpleBinaryCache;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/21/17
 */
@Provider
public class ResponseCacheWriterInterceptor implements WriterInterceptor {
    @Context
    private UriInfo uriInfo;
    private SimpleBinaryCache cache;

    public ResponseCacheWriterInterceptor(SimpleBinaryCache cache) {
        this.cache = cache;
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {

        CachedResponse cachedResponse = new CachedResponse(context.getOutputStream());
        context.setOutputStream(cachedResponse);

        context.proceed();

        cache.add(
                new CachedEntity(
                        uriInfo.getAbsolutePath().toString(),
                        System.currentTimeMillis(),
                        cachedResponse.cached.toByteArray()));


    }

    private static class CachedResponse extends OutputStream {
        private final ByteArrayOutputStream cached;
        private final OutputStream actual;

        /**
         * Creates a ServletResponse adaptor wrapping the given response object.
         *
         * @param actual
         * @throws IllegalArgumentException if the response is null.
         */
        public CachedResponse(OutputStream actual) {
            this.cached = new ByteArrayOutputStream(/*super.getBufferSize()*/);
            this.actual = actual;
        }

        @Override
        public void write(int b) throws IOException {
            cached.write(b);
            actual.write(b);
        }
    }
}
