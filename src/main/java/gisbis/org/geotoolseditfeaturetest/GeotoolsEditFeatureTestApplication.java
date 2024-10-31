package gisbis.org.geotoolseditfeaturetest;

import org.geotools.api.filter.identity.FeatureId;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.UUID;

public class GeotoolsEditFeatureTestApplication {

    public static void main(String[] args) {
        UUID insertId = UUIDUtil.sqUUID();

        FeatureId featureId = GeotoolsUtils.createGeometry(getGeometry(), insertId);
        System.out.println("Added ID : " + featureId.getID() + ", but inserted id : " + insertId);

        GeotoolsUtils.updateGeometry(getGeometry(), featureId.getID());
    }

    public static Geometry getGeometry() {
        PrecisionModel pm = new PrecisionModel();
        GeometryFactory gf = new GeometryFactory(pm, 4326);
        CoordinateArraySequence cas =
                new CoordinateArraySequence(new Coordinate[]{
                        new Coordinate(30.262776196255125, 59.93954262728195),
                        new Coordinate(30.262907623301622, 59.93958105131431),
                        new Coordinate(30.262941029017526, 59.9395526213441),
                        new Coordinate(30.262956789606722, 59.93953962130284),
                        new Coordinate(30.263027460562363, 59.939488541678394),
                        new Coordinate(30.262897264629483, 59.93944742258056),
                        new Coordinate(30.262776196255125, 59.93954262728195)
                });

        LinearRing lr = new LinearRing(cas, gf);
        return new Polygon(lr, null, gf);
    }

}
