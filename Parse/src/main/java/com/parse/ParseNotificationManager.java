/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseIntArray;

/**
 * A utility class for building and showing notifications.
 */
/** package */ class ParseNotificationManager {
  public static final String TAG = "com.parse.ParseNotificationManager";
  
  public static class Singleton {
    private static final ParseNotificationManager INSTANCE = new ParseNotificationManager();
  }
  
  public static ParseNotificationManager getInstance() {
    return Singleton.INSTANCE;
  }
  
  private final Object lock = new Object();
  private final AtomicInteger notificationCount = new AtomicInteger(0);
  private volatile boolean shouldShowNotifications = true;
  
  // protected by object lock
  private SparseIntArray iconIds = new SparseIntArray();
  
  public void setShouldShowNotifications(boolean show) {
    shouldShowNotifications = show;
  }
  
  public int getNotificationCount() {
    return notificationCount.get();
  }
  
  /*
   * Notifications must be created with a valid drawable iconId resource, or NotificationManager
   * will silently discard the notification. To help our clients, we check on the validity of the
   * iconId before creating the Notification and log an error when provided an invalid id.
   */ 
  public boolean isValidIconId(Context context, int iconId) {
    int valid;
    
    synchronized (lock) {
      valid = iconIds.get(iconId, -1);
    }
    
    if (valid == -1) {
      Resources resources = context.getResources();
      Drawable drawable = null;
      
      try {
        drawable = resources.getDrawable(iconId);
      } catch (NotFoundException e) {
        // do nothing
      }
      
      synchronized (lock) {
        valid = (drawable == null) ? 0 : 1;
        iconIds.put(iconId, valid);
      }
    }
    
    return valid == 1;
  }
  
  public void showNotification(Context context, Notification notification) {
    if (context != null && notification != null) {
      notificationCount.incrementAndGet();
      
      if (shouldShowNotifications) {
        // Fire off the notification
        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Pick an id that probably won't overlap anything
        int notificationId = (int)System.currentTimeMillis();

        try {
          nm.notify(notificationId, notification);
        } catch (SecurityException e) {
          // Some phones throw an exception for unapproved vibration
          notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
          nm.notify(notificationId, notification);
        }
      }
    }
  }
}
