# GeoServer-NIVA

NIVA is running Geoserver at three servers:

- kart.niva.no
- aquamonitor.niva.no
- test-aquamonitor.niva.no
- geoserver.t.niva.no

They are running the same version of Geoserver which has it's own adaptions. Partly within a fork at [roarbra/geoserver](https://github.com/roarbra/geoserver), as plugin's from this repository, and also from [GeoTools-NIVA](https://github.com/NIVANorge/GeoTools-NIVA).

## Java and Tomcat

GeoServer is a Java servlet, and the preferred platform is Apache Tomcat.
At the moment we're running:

| Software      | Version       |
|---------------|---------------|
| OpenJDK       | 11.0.13       |
| Apache Tomcat | 9.0.56        |
