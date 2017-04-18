package com.deveire.dev.deveiregeofindergolf;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by owenryan on 14/04/2017.
 */

public class GolfHole
{
    private int holeNumber; //<--- that incredibly bad joke was a complete accident, I swear
    private int timeSpentAtHole;//in millseconds.
    private Location teeLocation;//gps location of the hole's tee
    private Location greenLocation;//gps location of the centre of the hole's green
    private int greenRadius;//radius of the hole's green, in meters.
    private String golfCourseHoleIsPartOf; //the name of the golf course that the hole is part of.
    private ArrayList<Integer> averageShotDistances; //the adverage distance of each shot, with index 0 being the distance made from tee off.


    private static int intervalLength;//in millseconds, should always equal the interval of location updates.

    public GolfHole(int inHoleNumber, String owningGolfCourse, Location inTeeLocation, Location inGreenLocation, int greenRadiusInMeters)
    {
        holeNumber = inHoleNumber;
        golfCourseHoleIsPartOf = owningGolfCourse;
        teeLocation = inTeeLocation;
        greenLocation = inGreenLocation;
        greenRadius = greenRadiusInMeters;
        timeSpentAtHole = 0;
        averageShotDistances = new ArrayList<Integer>();
        if(intervalLength == 0)
        {
            Log.e("WARNING", "GolfHole class requires that static interval lenght be specificed and not 0");
        }
    }

    public GolfHole(int inHoleNumber, String owningGolfCourse, Location inTeeLocation, Location inGreenLocation, int greenRadiusInMeters, ArrayList<Integer> inAdverageDistances)
    {
        holeNumber = inHoleNumber;
        golfCourseHoleIsPartOf = owningGolfCourse;
        teeLocation = inTeeLocation;
        greenLocation = inGreenLocation;
        greenRadius = greenRadiusInMeters;
        timeSpentAtHole = 0;
        averageShotDistances = inAdverageDistances;
        if(intervalLength == 0)
        {
            Log.e("WARNING", "GolfHole class requires that static interval lenght be specificed and not 0");
        }
    }


    public boolean isSame(GolfHole inHole)
    {
        if(this.golfCourseHoleIsPartOf == inHole.golfCourseHoleIsPartOf && this.holeNumber == inHole.holeNumber)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static int getIntervalLength()
    {
        return intervalLength;
    }

    public static void setIntervalLength(int in)
    {
        intervalLength = in;
    }

    public int getTimeSpentAtHole()
    {
        return timeSpentAtHole;
    }

    public void addIntervalTimeToHole()
    {
        timeSpentAtHole += intervalLength;
    }

    public void addIntervalTimeToHole(int numberOfIntervals) { timeSpentAtHole += intervalLength * numberOfIntervals; }

    public void setTimeSpentAtHole(int in)
    {
        timeSpentAtHole = in;
    }

    public int getHoleNumber()
    {
        return holeNumber;
    }

    public void setHoleNumber(int in)
    {
        holeNumber = in;
    }

    public Location getTeeLocation()
    {
        return teeLocation;
    }

    public void setTeeLocation(Location in)
    {
        teeLocation = in;
    }

    public Location getGreenLocation()
    {
        return greenLocation;
    }

    public void setGreenLocation(Location in)
    {
        greenLocation = in;
    }

    public int getGreenRadius() { return greenRadius; }

    public void setGreenRadius(int in)
    {
        greenRadius = in;
    }

    public String getNameOfGolfCourse()
    {
        return golfCourseHoleIsPartOf;
    }

    public void setGolfCourseHoleIsPartOf(String in)
    {
        golfCourseHoleIsPartOf = in;
    }

    public ArrayList<Integer> getAverageShotDistances(){ return averageShotDistances;}

    public void setAverageShotDistances(ArrayList<Integer> inAverageDistances){ averageShotDistances = inAverageDistances;}

    public void addAverageShotDistances(int newDistance){ averageShotDistances.add(newDistance);}
}
