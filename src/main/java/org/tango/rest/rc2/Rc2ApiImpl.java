package org.tango.rest.rc2;

import org.tango.rest.rc1.Rc1ApiImpl;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author ingvord
 * @since 1/30/16
 */
@Path("/rc2")
@Produces("application/json")
public class Rc2ApiImpl extends Rc1ApiImpl {
}
