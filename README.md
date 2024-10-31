# Problems changing Feature

### RUN
```
docker compose up -d
```

and run GeotoolsEditFeatureTestApplication.class

### INSERT FEATURE

It is not clear how to set the user identifier. Setting the parameter idgen="UseExisting" 
is possible only in objects of the InsertElementTypeImpl type.

feature.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE); - does not work

This was achieved only by overriding the Strategy createInsert method and adding the line:
```
 insert.setIdgen(IdentifierGenerationOptionType.USE_EXISTING_LITERAL);
```

```java
protected InsertElementType createInsert(WfsFactory factory, TransactionRequest.Insert elem) throws Exception {
    InsertElementType insert = factory.createInsertElementType();

    insert.setIdgen(IdentifierGenerationOptionType.USE_EXISTING_LITERAL);

    String srsName = getFeatureTypeInfo(elem.getTypeName()).getDefaultSRS();
    insert.setSrsName(new URI(srsName));

    List<SimpleFeature> features = elem.getFeatures();

    insert.getFeature().addAll(features);

    return insert;
}
```

### UPDATE FEATURE
```java
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
```

#### WFS 1.1.0
When using WFS 1.1.0, the request is sent and updates are performed. 
The response comes that the line has been updated, but POLYGON EMPTY 
is saved in the geometry field

#### WFS 2.0.0
REQUEST:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<wfs:Transaction 
        xmlns:xs="http://www.w3.org/2001/XMLSchema" 
        xmlns:bis="https://bis.org/bis" 
        xmlns:fes="http://www.opengis.net/fes/2.0" 
        xmlns:wfs="http://www.opengis.net/wfs/2.0" 
        xmlns:gml="http://www.opengis.net/gml/3.2" 
        xmlns:ows="http://www.opengis.net/ows/1.1" 
        xmlns:xlink="http://www.w3.org/1999/xlink" 
        handle="GeoTools 32.0(1e7dfc3f76d52f53b7a7b0af9bc02d5a1c321535) WFS 2.0.0 DataStore @PC-0017#1" 
        releaseAction="ALL" 
        service="WFS" 
        version="2.0.0">
    <wfs:Update inputFormat="application/gml+xml; version=3.2" srsName="urn:ogc:def:crs:EPSG::4326" typeName="bis:test">
        <wfs:Property>
            <wfs:ValueReference action="replace">geometry</wfs:ValueReference>
            <wfs:Value>
                <gml:Polygon xmlns:gml="http://www.opengis.net/gml">
                    <gml:outerBoundaryIs>
                        <gml:LinearRing>
                            <gml:coordinates>30.262776196255125,59.93954262728195 30.262907623301622,59.93958105131431 30.262941029017526,59.9395526213441 30.262956789606722,59.93953962130284 30.263027460562363,59.939488541678394 30.262897264629483,59.93944742258056 30.262776196255125,59.93954262728195</gml:coordinates>
                        </gml:LinearRing>
                    </gml:outerBoundaryIs>
                </gml:Polygon>
            </wfs:Value>
        </wfs:Property>
        <fes:Filter>
            <fes:ResourceId rid="test.07b9c1b9-c134-4025-845e-9372e09d1e05"/>
        </fes:Filter>
    </wfs:Update>
</wfs:Transaction>
```

A response comes from the geoserver:
```xml
<?xml version="1.0" encoding="UTF-8"?><ows:ExceptionReport xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0.0" xsi:schemaLocation="http://www.opengis.net/ows/1.1 http://localhost:8484/geoserver/schemas/ows/1.1.0/owsAll.xsd">
  <ows:Exception exceptionCode="InvalidValue" locator="geometry">
    <ows:ExceptionText>Invalid value for property geometry</ows:ExceptionText>
  </ows:Exception>
</ows:ExceptionReport>
```

