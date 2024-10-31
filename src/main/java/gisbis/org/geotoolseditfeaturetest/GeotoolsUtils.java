package gisbis.org.geotoolseditfeaturetest;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.identity.FeatureId;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.internal.TransactionRequest;
import org.geotools.data.wfs.internal.TransactionResponse;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class GeotoolsUtils {
    private final static FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    private final static String TYPE_NAME = "bis:test";
    public static WFSDataStore getDataStore() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put(WFSDataStoreFactory.URL.getName(), "http://localhost:8484/geoserver/ows?service=wfs&REQUEST=GetCapabilities&version=2.0.0");
        params.put(WFSDataStoreFactory.PASSWORD.getName(), "geoserver");
        params.put(WFSDataStoreFactory.USERNAME.getName(), "admin");

        return new WFSDataStoreFactory().createDataStore(params);
    }

    public static boolean updateGeometry(Geometry geometry, String sid) {
        try {
            WFSDataStore dataStore = getDataStore();

            SimpleFeatureType sft = dataStore.getSchema(TYPE_NAME);

            QName qName = dataStore.getRemoteTypeName(sft.getName());
            String geomColumn = getGeomColumn(dataStore, qName);
            Filter idFilter = ff.id(ff.featureId(sid));

            return updateTransaction(dataStore, qName, List.of(new QName(geomColumn)), List.of(geometry), idFilter);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static boolean updateTransaction(WFSDataStore dataStore, QName qName, List<QName> columns, List<Object> values, Filter filter) throws IOException {
        TransactionRequest transactionRequest = dataStore.getWfsClient().createTransaction();
        TransactionRequest.Update update = transactionRequest.createUpdate(qName, columns, values, filter);
        transactionRequest.add(update);

        return transaction(dataStore, transactionRequest, TransactionResponse::getUpdatedCount) > 0;
    }

    public static FeatureId createGeometry(Geometry geometry, UUID newId) {
        try {
            WFSDataStore dataStore = getDataStore();
            SimpleFeatureType sft = dataStore.getSchema(TYPE_NAME);
            QName qName = dataStore.getRemoteTypeName(sft.getName());

            var crs = sft.getCoordinateReferenceSystem();

            String geomColumn = getGeomColumn(dataStore, qName);

            SimpleFeatureTypeBuilder typeBuilder = getFeatureTypeBuilder(qName, crs);
            typeBuilder.add(geomColumn, Geometry.class);

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(typeBuilder.buildFeatureType());
            featureBuilder.set(geomColumn, geometry);
            featureBuilder.featureUserData(Hints.USE_PROVIDED_FID, Boolean.TRUE);
            featureBuilder.featureUserData(Hints.PROVIDED_FID, newId.toString());

            SimpleFeature feature = featureBuilder.buildFeature(newId.toString());
            feature.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
            feature.getUserData().put(Hints.PROVIDED_FID, newId.toString());

            return insertTransaction(dataStore, feature, qName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static SimpleFeatureTypeBuilder getFeatureTypeBuilder(QName qName, CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setCRS(crs);
        typeBuilder.setNamespaceURI(qName.getNamespaceURI());
        typeBuilder.setName(qName.getLocalPart());
        return typeBuilder;
    }

    private static FeatureId insertTransaction(WFSDataStore dataStore, SimpleFeature feature, QName qName) throws IOException {
        TransactionRequest transactionRequest = dataStore.getWfsClient().createTransaction();
        TransactionRequest.Insert insert = transactionRequest.createInsert(qName);
        insert.add(feature);
        transactionRequest.add(insert);

        Function<TransactionResponse, FeatureId> action = (response) ->
                response
                        .getInsertedFids()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Can not extract identifier!"));

        return transaction(dataStore, transactionRequest, action);
    }

    public static <T> T transaction(WFSDataStore dataStore, TransactionRequest transactionRequest, Function<TransactionResponse, T> action) throws IOException {
        TransactionResponse response =
                dataStore
                        .getWfsClient()
                        .issueTransaction(transactionRequest);

        return action.apply(response);
    }

    public static String getGeomColumn(WFSDataStore dataStore, QName qName) throws IOException {
        String geomColumn = dataStore
                .getRemoteSimpleFeatureType(qName)
                .getTypes()
                .stream()
                .filter(at -> at.getBinding().equals(Geometry.class))
                .map(AttributeType::getName)
                .map(Name::toString)
                .findFirst()
                .orElse(null);

        if (geomColumn == null)
            throw new RuntimeException("Geometry column not found!");

        return geomColumn;
    }
}