package pathfindead.ui;

/**
 * Convierte coordenadas de la cuadrícula del grafo (x, y) en píxeles de pantalla
 * con proyección isométrica: cada casilla se ve como un rombo de 64 × 32.
 * También hace la conversión inversa (pantalla -> cuadrícula).
 */
public final class IsoProjection {

    public static final double TILE_W = 64;   // ancho del rombo en píxeles
    public static final double TILE_H = 32;   // alto del rombo en píxeles

    // Origen usado por el mapa de la ciudad: píxel donde se dibuja la casilla (0, 0)
    public static final double CITY_ORIGIN_X = 502;
    public static final double CITY_ORIGIN_Y = 130;

    private final double originX;
    private final double originY;

    public IsoProjection(double originX, double originY) {
        this.originX = originX;
        this.originY = originY;
    }

    /** Proyección con el origen del mapa de la ciudad. */
    public static IsoProjection forCity() {
        return new IsoProjection(CITY_ORIGIN_X, CITY_ORIGIN_Y);
    }

    public double toScreenX(double gx, double gy) {
        return originX + (gx - gy) * TILE_W / 2;
    }

    public double toScreenY(double gx, double gy) {
        return originY + (gx + gy) * TILE_H / 2;
    }

    /** Conversión inversa: de píxel de pantalla a coordenada X de la cuadrícula. */
    public double toGridX(double screenX, double screenY) {
        double a = (screenX - originX) / (TILE_W / 2);   // = gx - gy
        double b = (screenY - originY) / (TILE_H / 2);   // = gx + gy
        return (a + b) / 2;
    }

    /** Conversión inversa: de píxel de pantalla a coordenada Y de la cuadrícula. */
    public double toGridY(double screenX, double screenY) {
        double a = (screenX - originX) / (TILE_W / 2);
        double b = (screenY - originY) / (TILE_H / 2);
        return (b - a) / 2;
    }
}