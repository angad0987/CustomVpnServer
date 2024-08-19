package route;

import java.util.*;

public class RoutingTable {
    private final Map<String, String> routes = new HashMap<>();

    public RoutingTable() {
        routes.put("service1", "localhost:8181");
        routes.put("service2", "localhost:8182");
        routes.put("service3", "localhost:8183");
    }

    public String getRoute(String service) {
        return routes.get(service);
    }

}
