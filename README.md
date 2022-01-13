# GeoServer-NIVA

NIVA is running Geoserver at three servers:

- kart.niva.no
- aquamonitor.niva.no
- test-aquamonitor.niva.no

They are running the same version of Geoserver which has it's own adaptions. Partly within a fork at roarbra/geoserver, as plugin's from this repositoy, and also from geotools-niva.

## Java and Tomcat

Geoserver is a Java servlet, and the preferred platform is Apache Tomcat.
At the moment we're running:

| Software      | Version       |
|---------------|---------------|
| AdoptOpenJDK  | 8.0.275       |
| Apache Tomcat | 9.0.37        |
