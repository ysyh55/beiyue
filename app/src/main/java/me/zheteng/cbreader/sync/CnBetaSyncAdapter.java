/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.sync;

import static me.zheteng.cbreader.data.CnBetaContract.CONTENT_AUTHORITY;
import static me.zheteng.cbreader.data.CnBetaContract.CommentEntry;
import static me.zheteng.cbreader.data.CnBetaContract.FeedMessageEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Comment;
import me.zheteng.cbreader.model.Feed;
import me.zheteng.cbreader.model.FeedMessage;
import me.zheteng.cbreader.utils.CatkeRSSFeedParser;
import me.zheteng.cbreader.utils.RSSFeedParser;

/**
 * TODO 记得添加注释
 */
public class CnBetaSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String FEED_URL = "http://cnbeta.catke.com/feed";

    public static final String ACCOUNT_TYPE = "cbreader.zheteng.me";

    private static final String LOG_TAG = "CnBetaSyncAdapter";

    ContentResolver mContentResolver;

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;


    public CnBetaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");



        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(Uri.parse(FEED_URL).toString());


            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android;async-http/1.4.1)");
            urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            RSSFeedParser parser = new CatkeRSSFeedParser();
            Feed feed = null;

            feed = parser.readFeed(reader);

            saveResult(feed);

            mContentResolver.notifyChange(FeedMessageEntry.CONTENT_URI, null);
            mContentResolver.notifyChange(CommentEntry.CONTENT_URI, null);
            Log.d(LOG_TAG, "Sync Success");
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void saveResult(Feed feed) {
        List<FeedMessage> entries = feed.getMessages();
        List<Comment> comments = feed.getComments();
        Vector<ContentValues> cVVectorMessages = new Vector<ContentValues>(entries.size());
        Vector<ContentValues> cVVectorComments = new Vector<ContentValues>(comments.size());

        for (FeedMessage entry : entries) {
            ContentValues cvMessage = new ContentValues();

            cvMessage.put(FeedMessageEntry.COLUMN_TITLE, entry.getTitle());
            cvMessage.put(FeedMessageEntry.COLUMN_DESCRIPTION, entry.getDescription());
            cvMessage.put(FeedMessageEntry.COLUMN_CONTENT, entry.getContent());
            cvMessage.put(FeedMessageEntry.COLUMN_GUID, entry.getGuid());
            cvMessage.put(FeedMessageEntry.COLUMN_LINK, entry.getLink());
            cvMessage.put(FeedMessageEntry.COLUMN_PUB_DATE, entry.getPubDate());
            cvMessage.put(FeedMessageEntry.COLUMN_COMMENT_COUNT, entry.getComments().size());
            cvMessage.put(FeedMessageEntry.COLUMN_READED, 0);

            cVVectorMessages.add(cvMessage);
        }

        for (Comment comment : comments) {
            ContentValues cvComment = new ContentValues();

            cvComment.put(CommentEntry.COLUMN_NAME, comment.getName());
            cvComment.put(CommentEntry.COLUMN_CONTENT, comment.getContent());
            cvComment.put(CommentEntry.COLUMN_PUB_DATE, comment.getPubDate());
            cvComment.put(CommentEntry.COLUMN_SUPPORT, comment.getSupport());
            cvComment.put(CommentEntry.COLUMN_OPPOSE, comment.getOppose());
            cvComment.put(CommentEntry.COLUMN_FEED_ID, comment.getFeedGuid());

            cVVectorComments.add(cvComment);
        }

        if ( cVVectorMessages.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVectorMessages.size()];
            cVVectorMessages.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(FeedMessageEntry.CONTENT_URI, cvArray);

//            // delete old data so we don't build up an endless history
//            getContext().getContentResolver().delete(FeedMessageEntry.CONTENT_URI,
//                    FeedMessageEntry.COLUMN_DATE + " <= ?",
//                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});
        }

        if ( cVVectorComments.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVectorComments.size()];
            cVVectorComments.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(CommentEntry.CONTENT_URI, cvArray);
        }

    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = CONTENT_AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, CONTENT_AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                CONTENT_AUTHORITY, bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
