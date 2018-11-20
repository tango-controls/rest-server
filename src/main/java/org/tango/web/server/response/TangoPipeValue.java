package org.tango.web.server.response;

import fr.esrf.TangoApi.PipeBlob;
import org.tango.rest.entities.pipe.Pipe;
import org.tango.rest.entities.pipe.PipeValue;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/20/18
 */
public class TangoPipeValue extends PipeValue {
    public PipeBlob data;

    public TangoPipeValue() {
    }

    public TangoPipeValue(Pipe pipe, long timestamp, PipeBlob data) {
        super(pipe, timestamp, null);
        this.data = data;
    }
}
