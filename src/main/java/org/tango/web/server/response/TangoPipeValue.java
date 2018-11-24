package org.tango.web.server.response;

import fr.esrf.TangoApi.PipeBlob;
import org.tango.rest.v10.entities.pipe.Pipe;
import org.tango.rest.v10.entities.pipe.PipeValue;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/20/18
 */
public class TangoPipeValue extends PipeValue {
    public PipeBlob data;

    public TangoPipeValue() {
    }
}
