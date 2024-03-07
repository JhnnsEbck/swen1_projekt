package at.fhtw.mtcgapp.service.packages;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class PackageService implements Service {
    private final PackageController packageController;

    public PackageService()
    {
        this.packageController = new PackageController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST &&
                request.getPathParts().size() > 1 &&
                request.getPathParts().get(1).equals("packages")) {
            return this.packageController.acquireCardPackage(request);
        }
        else if (request.getMethod() == Method.POST) {
            return this.packageController.createPackage(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
