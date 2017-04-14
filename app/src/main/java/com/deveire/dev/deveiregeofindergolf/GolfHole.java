package com.deveire.dev.deveiregeofindergolf;

import android.location.Location;
import android.util.Log;

/**
 * Created by owenryan on 14/04/2017.
 */

public class GolfHole
{
    private int holeNumber; //<--- that incredibly bad joke was a complete accident, I swear
    private int timeSpentAtHole;//in millseconds.
    private static int intervalLength;//in millseconds, should always equal the interval of location updates.
    private Location  holeLocation;
    private String golfCourseHoleIsPartOf; //the name of the golf course that the hole is part of.

    public GolfHole(int inHoleNumber, String owningGolfCourse, Location inHoleLocation)
    {
        holeNumber = inHoleNumber;
        golfCourseHoleIsPartOf = owningGolfCourse;
        holeLocation = inHoleLocation;
        timeSpentAtHole = 0;
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

    public void addIntervalTimeToHole(int numberOfIntervals)
    {
        timeSpentAtHole += intervalLength * numberOfIntervals;
    }

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

    public Location getHoleLocation()
    {
        return holeLocation;
    }

    public void setHoleLocation(Location in)
    {
        holeLocation = in;
    }

    public String getNameOfGolfCourse()
    {
        return golfCourseHoleIsPartOf;
    }

    public void setGolfCourseHoleIsPartOf(String in)
    {
        golfCourseHoleIsPartOf = in;
    }
}
