package com.deveire.dev.deveiregeofindergolf;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrackingService extends Service
{
    public TrackingService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
