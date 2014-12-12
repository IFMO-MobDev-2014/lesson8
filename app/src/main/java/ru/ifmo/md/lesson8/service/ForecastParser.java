package ru.ifmo.md.lesson8.service;

import android.content.ContentValues;
import android.location.Location;

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

import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.ForecastsTable;

/**
 * Created by flyingleafe on 01.12.14.
 */
public class ForecastParser {
    DocumentBuilder builder;

    public class Result {
        public ArrayList<ContentValues> forecasts = new ArrayList<>();
        public ContentValues condition = new ContentValues();
    }

    public class LocationResult {
        public String cityName;
        public long woeid;
    }

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
                    add(CitiesTable.COLUMN_NAME_CUR_TEMP);
                    add(CitiesTable.COLUMN_NAME_CUR_COND);
                    add(CitiesTable.COLUMN_NAME_СUR_DESC);
                }
            };
            for(String attr: attrs) {
                res.condition.put(attr, condition.getAttribute(attr));
            }

            NodeList forecasts = item.getElementsByTagName("yweather:forecast");
            attrs = new ArrayList<String>() {
                {
                    add(ForecastsTable.COLUMN_NAME_DAY);
                    add(ForecastsTable.COLUMN_NAME_DATE);
                    add(ForecastsTable.COLUMN_NAME_LOW_TEMP);
                    add(ForecastsTable.COLUMN_NAME_HIGH_TEMP);
                    add(ForecastsTable.COLUMN_NAME_COND);
                    add(ForecastsTable.COLUMN_NAME_TEXT);
                }
            };
            for(int i = 0; i < forecasts.getLength(); i++) {
                Element forecast = (Element) forecasts.item(i);
                ContentValues row = new ContentValues();
                for(String attr: attrs) {
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

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }
}
