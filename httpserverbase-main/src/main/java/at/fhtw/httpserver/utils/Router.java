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

    public Service resolve(String route)
    {
        return this.serviceRegistry.get(route);
    }

    /*public Service resolve(String route) {
        System.out.println("DEBUG: Resolving route '" + route + "'");

        // Prüfe exakte Übereinstimmung
        Service service = this.serviceRegistry.get(route);
        if (service != null) {
            System.out.println("DEBUG: Exact match found for route '" + route + "'");
            return service;
        }

        // Prüfe dynamische Routen wie "/tradings/{id}"
        for (String key : this.serviceRegistry.keySet()) {
            if (route.startsWith(key + "/")) { // Erlaubt "/tradings/{id}"
                System.out.println("DEBUG: Found dynamic match for route '" + key + "'");
                return this.serviceRegistry.get(key);
            }
        }

        System.out.println("DEBUG: No exact or dynamic match found for route '" + route + "'");
        return null;
    }*/
}
