package pathfindead.model;

public final class CityFactory {

    private CityFactory() { }

    public static Graph createCity() {
        Graph city = new Graph();

        // Centro de control
        city.addNode(new Node("C-01", "Parque Principal", NodeType.CONTROL_CENTER, "Central", 6, 7));

        // Refugios
        city.addNode(new Node("R-01", "Barrio Colonial", NodeType.SHELTER, "Central", 9, 4));
        city.addNode(new Node("R-02", "Barrio La Paz", NodeType.SHELTER, "Central", 10, 6));
        city.addNode(new Node("R-03", "Barrio Buenos Aires", NodeType.SHELTER, "Norte", 6, 1));
        city.addNode(new Node("R-04", "Barrio Metropolis", NodeType.SHELTER, "Sur", 6, 9));
        city.addNode(new Node("R-05", "Barrio Venecia", NodeType.SHELTER, "Sur", 10, 9));
        city.addNode(new Node("R-06", "Barrio Brisas del Soapaga", NodeType.SHELTER, "Occidental", 1, 4));

        // Hospitales abandonados
        city.addNode(new Node("H-01", "Polideportivo", NodeType.HOSPITAL, "Central", 13, 2));
        city.addNode(new Node("H-02", "Santa Teresa", NodeType.HOSPITAL, "Sur", 9, 10));

        // Torres de control
        city.addNode(new Node("T-01", "Piscina", NodeType.TOWER, "Occidental", 0, 1));
        city.addNode(new Node("T-02", "Malecon", NodeType.TOWER, "Central", 11, 0));

        // Puntos de aparición de hordas
        city.addNode(new Node("Z-01", "Planta Minera", NodeType.HORDE_SPAWN, "Oriental", 15, 0));
        city.addNode(new Node("Z-02", "Via Tasco", NodeType.HORDE_SPAWN, "Oriental", 14, 11));
        city.addNode(new Node("Z-03", "Cementerio", NodeType.HORDE_SPAWN, "Sur", 7, 12));

        // Corredor Occidental
        city.connect("C-01", "R-06");
        city.connect("R-06", "T-01");
        city.connect("T-01", "C-01");

        // Corredor Central
        city.connect("C-01", "R-01");
        city.connect("R-01", "R-02");
        city.connect("R-02", "H-01");
        city.connect("H-01", "C-01");

        // Corredor Nororiental
        city.connect("C-01", "R-03");
        city.connect("R-03", "T-02");
        city.connect("T-02", "Z-01");
        city.connect("Z-01", "C-01");

        // Corredor Sur
        city.connect("C-01", "R-04");
        city.connect("R-04", "R-05");
        city.connect("R-05", "H-02");
        city.connect("H-02", "Z-02");
        city.connect("Z-02", "Z-03");
        city.connect("Z-03", "C-01");

        // Enlaces entre corredores
        city.connect("R-03", "R-01");
        city.connect("H-01", "T-02");
        city.connect("H-01", "Z-01");
        city.connect("R-02", "R-05");
        city.connect("R-06", "R-04");
        city.connect("T-01", "R-03");
        city.connect("R-05", "Z-02");
        assignZombieDanger(city);
        return city;
    }
    /** Asigna el nivel de peligro zombi (0–5) a cada corredor. */
    private static void assignZombieDanger(Graph city) {
        // Nivel 5: entre dos puntos de horda
        setZombieDanger(city, "Z-02", "Z-03", 5);
        // Nivel 4: tocan un punto de horda
        setZombieDanger(city, "Z-01", "C-01", 4);
        setZombieDanger(city, "Z-03", "C-01", 4);
        setZombieDanger(city, "H-01", "Z-01", 4);
        setZombieDanger(city, "R-05", "Z-02", 4);
        setZombieDanger(city, "H-02", "Z-02", 4);
        // Nivel 3: horda junto a una torre vigilada
        setZombieDanger(city, "T-02", "Z-01", 3);
        // Nivel 2: tocan un hospital abandonado
        setZombieDanger(city, "H-01", "C-01", 2);
        setZombieDanger(city, "R-02", "H-01", 2);
        setZombieDanger(city, "R-05", "H-02", 2);
        setZombieDanger(city, "H-01", "T-02", 2);
        // Nivel 1: riesgo bajo
        setZombieDanger(city, "C-01", "R-06", 1);
        setZombieDanger(city, "T-01", "C-01", 1);
        setZombieDanger(city, "C-01", "R-03", 1);
        setZombieDanger(city, "R-03", "R-01", 1);
        setZombieDanger(city, "R-02", "R-05", 1);
        setZombieDanger(city, "R-03", "T-02", 1);
        setZombieDanger(city, "T-01", "R-03", 1);
        setZombieDanger(city, "R-04", "R-05", 1);
        setZombieDanger(city, "R-06", "R-04", 1);
        // Nivel 0: corredores seguros
        setZombieDanger(city, "C-01", "R-01", 0);
        setZombieDanger(city, "C-01", "R-04", 0);
        setZombieDanger(city, "R-01", "R-02", 0);
        setZombieDanger(city, "R-06", "T-01", 0);
    }

    /** Busca la conexión entre dos IDs y le asigna el nivel de peligro zombi. */
    private static void setZombieDanger(Graph city, String idA, String idB, int level) {
        boolean found = false;
        for (Node node : city.getNodes()) {
            for (Edge e : city.getEdges(node)) {
                String a = e.getA().getId();
                String b = e.getB().getId();
                if ((a.equals(idA) && b.equals(idB)) || (a.equals(idB) && b.equals(idA))) {
                    e.setZombieDanger(level);
                    found = true;
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("No existe la conexión " + idA + " - " + idB);
        }
    }
}