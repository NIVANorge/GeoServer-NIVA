# GeoServer-NIVA

NIVA is running Geoserver at three servers:

- kart.niva.no
- aquamonitor.niva.no
- test-aquamonitor.niva.no
- geoserver.t.niva.no
- geoserver.p.niva.no

They are running the same version of Geoserver which has it's own adaptions. Partly within a fork at [NIVANorge/geoserver](https://github.com/NIVANorge/geoserver), as plugin's from this repository, and also from [GeoTools-NIVA](https://github.com/NIVANorge/GeoTools-NIVA).

## Java and Tomcat

GeoServer is a Java servlet, and the preferred platform is Apache Tomcat.
At the moment we're running:

| Software      | Version       |
|---------------|---------------|
| OpenJDK       | 11.0.26       |
| Apache Tomcat | 9.0.98        |

## Accessing SAMPLE_POINTS from GeoServer

Use personal NIVA username / password, or a System account from NIVADATABASEN.USERS

```
curl -u "{username}:{password}" "https://aquamonitor.niva.no/geoserver/no.niva.aquamonitor/wfs?
           service=wfs&version=2.0.0&request=GetFeature&typeName=SAMPLE_POINTS&outputFormat=json&
            cql_filter=sample_point_id={sample_point_id}"
```

This will give this result:
```(json)
{
  "type":"FeatureCollection",
  "features":
    [{
       "type":"Feature",
       "id":"SAMPLE_POINTS.31601",
       "geometry": {
         "type":"Point","coordinates":[121.455947,16.826893]
       },
       "geometry_name":"the_geom",
       "properties": {
         "sample_point_id":71654
       }
     }],
   "totalFeatures":1,
   "numberMatched":1,
   "numberReturned":1,
   "timeStamp":"2023-09-11T06:42:28.338Z",
   "crs": {
     "type":"name",
     "properties":{
       "name":"urn:ogc:def:crs:EPSG::4326"
     }
   }
}
```
The interesting part would be the features[0].geometry.coordinates.

The result for a non-existing sample_point_id would be:
```(json)
{
  "type":"FeatureCollection",
  "features":[],
  "totalFeatures":0,
  "numberMatched":0,
  "numberReturned":0,
  "timeStamp":"2023-09-11T06:49:54.029Z",
  "crs":null
}
```

It's also possible to request several sample_point_id in one query:
```(json)
curl -u "{username}:{password}" "https://aquamonitor.niva.no/geoserver/no.niva.aquamonitor/wfs? \
                         service=wfs&version=2.0.0&request=GetFeature&typeName=SAMPLE_POINTS&outputFormat=json& \
                         cql_filter=sample_point_id+in+({sample_point_id_1},{sample_point_id_2},{sample_point_id_3})"
```

The result would be:
```(json)
{
  "type":"FeatureCollection",
  "features":[{
    "type":"Feature",
    "id":"SAMPLE_POINTS.1006",
    "geometry": {
      "type":"Point",
      "coordinates":[7.00166,58.06]
    },
    "geometry_name":"the_geom",
    "properties": {
      "sample_point_id":5593
    }
  }, {
    "type":"Feature",
    "id":"SAMPLE_POINTS.1007",
    "geometry": {
      "type":"Point",
      "coordinates":[6.96,58.04666]
    },
    "geometry_name":"the_geom",
    "properties": {
      "sample_point_id":5594
    }
  }, {
    "type":"Feature",
    "id":"SAMPLE_POINTS.1008",
    "geometry": {
      "type":"Point",
      "coordinates":[6.96666,58.04]
    },
    "geometry_name":"the_geom",
    "properties": {
      "sample_point_id":5595
    }
  }],
  "totalFeatures":3,
  "numberMatched":3,
  "numberReturned":3,
  "timeStamp":"2023-09-11T06:56:59.595Z",
  "crs": {
    "type":"name",
    "properties":{
      "name":"urn:ogc:def:crs:EPSG::4326"
    }
  }
}
```
