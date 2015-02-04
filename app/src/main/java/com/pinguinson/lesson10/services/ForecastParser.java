package com.pinguinson.lesson10.services;

import android.content.ContentValues;

import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.tables.ForecastsTable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by pinguinson.
 */
public class ForecastParser {
    DocumentBuilder builder;

    public ForecastParser() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Result parse(InputStream stream) throws SAXException {
        try {
            Result res = new Result();

            Document document = builder.parse(stream);
            Element item = (Element) document.getElementsByTagName("item").item(0);

            Element condition = (Element) item.getElementsByTagName("yweather:condition").item(0);
            ArrayList<String> attrs = new ArrayList<String>() {
                {
                    add(CitiesTable.COLUMN_NAME_CURRENT_TEMPERATURE);
                    add(CitiesTable.COLUMN_NAME_CURRENT_CONDITIONS);
                    add(CitiesTable.COLUMN_NAME_СURRENT_DESCRIPTION);
                }
            };
            for (String attr : attrs) {
                res.condition.put(attr, condition.getAttribute(attr));
            }

            NodeList forecasts = item.getElementsByTagName("yweather:forecast");
            attrs = new ArrayList<String>() {
                {
                    add(ForecastsTable.COLUMN_NAME_WEEKDAY);
                    add(ForecastsTable.COLUMN_NAME_DATE);
                    add(ForecastsTable.COLUMN_NAME_LOW_TEMPERATURE);
                    add(ForecastsTable.COLUMN_NAME_HIGH_TEMPERATURE);
                    add(ForecastsTable.COLUMN_NAME_CONDITIONS);
                    add(ForecastsTable.COLUMN_NAME_TEXT);
                }
            };
            for (int i = 0; i < forecasts.getLength(); i++) {
                Element forecast = (Element) forecasts.item(i);
                ContentValues row = new ContentValues();
                for (String attr : attrs) {
                    row.put(attr, forecast.getAttribute(attr));
                }
                res.forecasts.add(row);
            }

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public LocationResult parseLocation(InputStream stream) throws SAXException {
        try {
            LocationResult res = new LocationResult();

            Document document = builder.parse(stream);
            Element cityEl = (Element) document.getElementsByTagName("city").item(0);
            Element woeidEl = (Element) document.getElementsByTagName("woeid").item(0);

            res.cityName = cityEl.getTextContent();
            res.woeid = Long.parseLong(woeidEl.getTextContent());
            stream.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public class Result {
        public ArrayList<ContentValues> forecasts = new ArrayList<>();
        public ContentValues condition = new ContentValues();
    }

    public class LocationResult {
        public String cityName;
        public long woeid;
    }
}
