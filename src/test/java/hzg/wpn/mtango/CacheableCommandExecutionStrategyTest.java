package hzg.wpn.mtango;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.command.Command;
import org.tango.web.server.command.Commands;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 29.08.13
 */
public class CacheableCommandExecutionStrategyTest {
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private TangoProxy mockProxy;
    private Method readAttrMtd;

    @Before
    public void before() throws Exception {
        mockProxy = mock(TangoProxy.class);

        when(mockProxy.readAttribute("someAttr")).then(new Answer<Object>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return "Hello from mock Tango!";
            }
        });
        when(mockProxy.readAttribute("someOtherAttr")).then(new Answer<Object>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return "Another hello from mock Tango!";
            }
        });

        readAttrMtd = TangoProxy.class.getDeclaredMethod("readAttribute", String.class);
    }

    @Test
    public void testExecute_sameCmds() throws Exception {
        final Command cmd1 = new Command(mockProxy, readAttrMtd, "someAttr");

        final Command cmd2 = new Command(mockProxy, readAttrMtd, new String("someAttr"));//force different objects

        final CyclicBarrier startStop = new CyclicBarrier(3);
        Future<Object> fCmd1 = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                startStop.await();
                return Commands.execute(cmd1);
            }
        });

        Future<Object> fCmd2 = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                startStop.await();
                return Commands.execute(cmd2);
            }
        });

        startStop.await();
        long start = System.nanoTime();

        Object resCmd1 = fCmd1.get();
        Object resCmd2 = fCmd2.get();

        long end = System.nanoTime();
        long delta = end - start;

        //assert results are the same
        assertEquals("Hello from mock Tango!", resCmd1);
        assertEquals("Hello from mock Tango!", resCmd2);
        //assert total execution is less than double invocation time
        assertTrue(TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS) < 1500);
    }

    @Test
    public void testExecute_differentCmds() throws Exception {
        final Command cmd1 = new Command(mockProxy, readAttrMtd, "someAttr");

        final Command cmd2 = new Command(mockProxy, readAttrMtd, "someOtherAttr");

        final CyclicBarrier startStop = new CyclicBarrier(3);
        Future<Object> fCmd1 = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                startStop.await();
                return Commands.execute(cmd1);
            }
        });

        Future<Object> fCmd2 = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                startStop.await();
                return Commands.execute(cmd2);
            }
        });

        startStop.await();
        long start = System.nanoTime();

        Object resCmd1 = fCmd1.get();
        Object resCmd2 = fCmd2.get();

        long end = System.nanoTime();
        long delta = end - start;

        //assert results are the same
        assertEquals("Hello from mock Tango!", resCmd1);
        assertEquals("Another hello from mock Tango!", resCmd2);
        //assert total execution is almost equal to a single invocation time
        assertTrue(TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS) < 1100);
    }

    @Test
    public void testExecute_invalidatedCache() throws Exception {
        final Command cmd1 = new Command(mockProxy, readAttrMtd, "someAttr");

        final Command cmd2 = new Command(mockProxy, readAttrMtd, new String("someAttr"));//force different objects

        long start = System.nanoTime();
        Commands.execute(cmd1);

        Thread.sleep(500);

        Commands.execute(cmd2);
        long end = System.nanoTime();
        long delta = end - start;

        //assert total execution is greater than double invocation time
        assertTrue(TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS) > 2250);
    }
}
