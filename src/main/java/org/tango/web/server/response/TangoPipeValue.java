package org.tango.web.server.response;

import fr.esrf.Tango.DevPipeDataElt;
import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeDataElement;
import org.tango.rest.entities.pipe.Pipe;
import org.tango.rest.entities.pipe.PipeValue;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/20/18
 */
public class TangoPipeValue extends PipeValue {
    public PipeBlob data;

    public TangoPipeValue() {
    }
}
