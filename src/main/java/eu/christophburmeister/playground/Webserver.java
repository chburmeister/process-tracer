package eu.christophburmeister.playground;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Webserver extends AbstractHandler {

    private static Server server;

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hello World</h1>");
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (System.getProperty("serverport") != null){
            port = Integer.valueOf(System.getProperty("serverport"));
        } else {
            port = 8080;
        }

        server = new Server(port);
        server.setHandler(new Webserver());

        server.start();
        server.join();
    }
}
