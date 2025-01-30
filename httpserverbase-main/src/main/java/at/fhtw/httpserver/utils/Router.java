package at.fhtw.httpserver.utils;

import at.fhtw.httpserver.server.Service;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private Map<String, Service> serviceRegistry = new HashMap<>();

    public void addService(String route, Service service)
    {
        this.serviceRegistry.put(route, service);
    }

    public void removeService(String route)
    {
        this.serviceRegistry.remove(route);
    }

    public Service resolve(String route) {
        if (this.serviceRegistry.containsKey(route)) {
            return this.serviceRegistry.get(route);
        }
        for (String registeredRoute : serviceRegistry.keySet()) {
            if (route.startsWith(registeredRoute)) {
                return serviceRegistry.get(registeredRoute);
            }
        }

        return null;
    }
}
