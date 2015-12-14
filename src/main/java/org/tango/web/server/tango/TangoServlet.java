package org.tango.web.server.tango;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import org.tango.DeviceState;
import org.tango.client.ez.util.TangoUtils;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
@Device
public class TangoServlet implements Servlet {
    @State
    private DevState state;

    public void setState(DevState state){
        this.state = state;
    }

    public DevState getState(){
        return state;
    }

    /**
     * Called by servlet container
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //TODO init params to Tango
        ServerManager.getInstance().start(new String[]{"development"}, "TangoRestServer");
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void initTango(){

    }

    @Override
    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.getWriter().write(state.toString());
    }

    @Override
    public String getServletInfo() {
        return TangoServlet.class.getSimpleName() + " by Ingvord";
    }

    @Override
    public void destroy() {
        try {
            ServerManager.getInstance().stop();
        } catch (DevFailed devFailed) {
            throw new RuntimeException(TangoUtils.convertDevFailedToException(devFailed));
        }
    }

    @Delete
    @StateMachine(endState = DeviceState.OFF)
    public void delete(){
    }
}
