<?xml version="1.0" encoding="iso-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>AquaMonitor_aggregation</Name>
    <UserStyle>
      <Title>Stasjoner</Title>
      <Abstract>Visning av stasjoner symbolisert etter stasjonstype, aggregrert etter antall i rutenett</Abstract>

      <FeatureTypeStyle>
        <Transformation>
          <ogc:Function name="niva:PointAggregateGrid">
            <ogc:Function name="parameter">
              <ogc:Literal>points</ogc:Literal>
            </ogc:Function>
            <ogc:Function name="parameter">
              <ogc:Literal>outputBBOX</ogc:Literal>
              <ogc:Function name="env">
                <ogc:Literal>wms_bbox</ogc:Literal>
              </ogc:Function>
            </ogc:Function>
            <ogc:Function name="parameter">
              <ogc:Literal>outputWidth</ogc:Literal>
              <ogc:Function name="env">
                <ogc:Literal>wms_width</ogc:Literal>
              </ogc:Function>
            </ogc:Function>
            <ogc:Function name="parameter">
              <ogc:Literal>outputHeight</ogc:Literal>
              <ogc:Function name="env">
                <ogc:Literal>wms_height</ogc:Literal>
              </ogc:Function>
            </ogc:Function>
            <ogc:Function name="parameter">
              <ogc:Literal>cellSize</ogc:Literal>
              <ogc:Literal>60</ogc:Literal>
            </ogc:Function>
            <ogc:Function name="parameter">
              <ogc:Literal>aggregateAttributes</ogc:Literal>
              <ogc:Literal>Air</ogc:Literal>
              <ogc:Literal>Begroing</ogc:Literal>
              <ogc:Literal>Biota</ogc:Literal>
              <ogc:Literal>Blotbunn</ogc:Literal> 
              <ogc:Literal>Bunndyr</ogc:Literal>
              <ogc:Literal>Fish</ogc:Literal>
              <ogc:Literal>Hardbunn</ogc:Literal>
              <ogc:Literal>Plankton</ogc:Literal>              
              <ogc:Literal>Sediment</ogc:Literal>
              <ogc:Literal>Sensor</ogc:Literal>
              <ogc:Literal>Sewage</ogc:Literal>
              <ogc:Literal>Vannplanter</ogc:Literal>
              <ogc:Literal>Water</ogc:Literal>
            </ogc:Function>
          </ogc:Function>
        </Transformation>

        <Rule>
          <Title>Deponi</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/deponi.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic> 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Deponi</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>       
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=D&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Elv</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/elv.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>  
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Elv</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=E&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Grunnvann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/grunnvann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic> 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Grunnvann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
            <PointSymbolizer>
              <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=G&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Gruvevann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/gruvevann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>  
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Gruvevann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=V&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Industri</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/industri.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Industri</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=N&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Innsj&#248;</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/innsjo.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Innsj&#248;</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=I&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Land</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/land.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>  
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Land</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=L&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Luft</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/luft.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic> 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Luft</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=U&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Marin</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/marin.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>  
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Marin</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=M&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>  
        </Rule>
        <Rule>
          <Title>Markvann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/markvann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Markvann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=A&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Myr</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/myr.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Myr</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=Y&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Overvann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/overvann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Overvann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=O&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Renseanlegg</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/renseanlegg.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Renseanlegg</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=R&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Sigevann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/sigevann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic> 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Sigevann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
        
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=S&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>

        <Rule>
          <Title>Akvakultur-R&#229;vann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/akvakultur_ravann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Akvakultur-R&#229;vann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="http://aquamonitor?typ=B&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title>Akvakultur-Driftsvann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/akvakultur_driftsvann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Akvakultur-Driftsvann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="http://aquamonitor?typ=C&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
            
        <Rule>
          <Title>Akvakultur-Karvann</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/akvakultur_karvann.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Akvakultur-Karvann</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="http://aquamonitor?typ=F&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
            
        <Rule>
          <Title>Ukjent</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/ukjent.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic> 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>STATION_TYPE</ogc:PropertyName>
              <ogc:Literal>Ukjent</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
     
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?typ=X&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title> &gt; 1 og &lt; 10 stasjoner</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/all_x.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>20</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsGreaterThan>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>1</ogc:Literal>
              </ogc:PropertyIsGreaterThan>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>10</ogc:Literal>
              </ogc:PropertyIsLessThan>
            </ogc:And>
          </ogc:Filter>

          <PointSymbolizer>
             <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?cnt=${COUNT}&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
                <Size>20</Size>
             </Graphic>
          </PointSymbolizer>
        </Rule>   
     
        <Rule>
          <Title> &gt;= 10 og &lt; 100 stasjoner</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/all_xx.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>30</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>10</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>100</ogc:Literal>
              </ogc:PropertyIsLessThan>
            </ogc:And>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?cnt=${COUNT}&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
                <Size>30</Size>
             </Graphic>
          </PointSymbolizer>
        </Rule>  
 
        <Rule>
          <Title> &gt;= 100 og &lt;1000</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/all_xxx.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>36</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>100</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>1000</ogc:Literal>
              </ogc:PropertyIsLessThan>
              </ogc:And>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?cnt=${COUNT}&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
                <Size>36</Size>
             </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Title> &gt; 1000 stasjoner</Title>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/all_xxxx.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>46</Size>
            </Graphic>
          </LegendGraphic>
          <ogc:Filter>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>COUNT</ogc:PropertyName>
                <ogc:Literal>1000</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
               <ExternalGraphic>
                 <OnlineResource xlink:href="http://aquamonitor?cnt=${COUNT}&amp;vals=${Water},${Sediment},${Biota},${Sewage},${Blotbunn},${Hardbunn},${Plankton},${Vannplanter},${Bunndyr},${Begroing},${Air},${EMPTY}&amp;cols=df0101,df7401,d7df01,74df00,01df01,01df74,01dfd7,0101df,7401df,df01d7,df0174,ffffff" />
                 <Format>application/chart</Format>
               </ExternalGraphic>
                <Size>46</Size>
             </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <LegendGraphic>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:href="file:/aqm_icons/all_keys.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>340</Size>
            </Graphic>
          </LegendGraphic>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
              </Mark>
              <Size>0</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>