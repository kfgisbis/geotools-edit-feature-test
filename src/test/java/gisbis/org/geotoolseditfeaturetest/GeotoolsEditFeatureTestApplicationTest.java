package gisbis.org.geotoolseditfeaturetest;

import com.fasterxml.uuid.Generators;
import org.geotools.api.filter.identity.FeatureId;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GeotoolsEditFeatureTestApplicationTest {
    private final static DatabaseConnector database = new DatabaseConnector();
    private final static String TYPE_NAME = "bis:test";
    private static final List<String> INSERTED_ID_LIST = new ArrayList<>();
    private static final List<String> VERSIONS = List.of("1.1.0", "2.0.0");
    private static final Geometry GEOMETRY = getGeometry();

    @Order(1)
    @ParameterizedTest
    @MethodSource("getArgumentsForInsert")
    void insertGeometry(String wfsVersion) {
        String insertId = "test." + Generators.timeBasedEpochGenerator().generate();

        FeatureId featureId = GeotoolsUtils.createGeometry(TYPE_NAME, GEOMETRY, insertId, wfsVersion);
        INSERTED_ID_LIST.add(featureId.getID());

        assertEquals(featureId.getID(), insertId);
    }

    public static Stream<Arguments> getArgumentsForInsert() {
        return VERSIONS.stream().map(Arguments::of);
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("getArgumentsForUpdate")
    void updateGeometry(String fid, String version) throws SQLException {
        assertTrue(GeotoolsUtils.updateGeometry(TYPE_NAME, GEOMETRY, fid, version));

        String geom = database.getRowById(fid.split("\\.")[1]);

        assertEquals(geom, GEOMETRY.toText());
    }

    private static Stream<Arguments> getArgumentsForUpdate() {
        return INSERTED_ID_LIST.stream()
                .flatMap(fid -> VERSIONS.stream()
                        .map(version -> Arguments.of(fid, version)));
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