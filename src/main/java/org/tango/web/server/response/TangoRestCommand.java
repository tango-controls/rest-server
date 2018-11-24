package org.tango.web.server.response;

import fr.esrf.Tango.DevError;
import fr.esrf.TangoApi.CommandInfo;
import fr.soleil.tango.clientapi.TangoCommand;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.rest.v10.entities.Command;

import java.net.URI;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoRestCommand extends Command {
    public CommandInfo info;
    @JsonIgnore
    public TangoCommand command;

    public TangoRestCommand(String name, String device, String host, CommandInfo info, URI href, TangoCommand command) {
        super(name, device, host, null, href);
        this.info = info;
        this.command = command;
    }

    public TangoRestCommand(DevError[] errors) {
        super();
        this.errors = errors;
    }
}
