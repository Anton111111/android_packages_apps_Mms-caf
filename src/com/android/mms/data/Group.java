/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.data;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Groups;
import android.provider.LocalGroups;
import android.util.Log;

import com.android.mms.LogTag;

public class Group {
    private static final String TAG = "Mms/Group";

    private static final String[] PROJECTION = new String[] {
        Groups._ID,
        Groups.TITLE,
        Groups.ACCOUNT_NAME,
        Groups.ACCOUNT_TYPE,
        Groups.DATA_SET,
        Groups.SUMMARY_COUNT,
    };

    private static final String[] LOCAL_PROJECTION = new String[] {
            LocalGroups.GroupColumns._ID,
            LocalGroups.GroupColumns.TITLE,
            LocalGroups.GroupColumns.COUNT,
    };

    private static final String SELECTION = Groups.ACCOUNT_TYPE + " NOT NULL AND "
            + Groups.ACCOUNT_NAME + " NOT NULL AND "
            + Groups.AUTO_ADD + "=0 AND "
            + Groups.DELETED + "=0 AND "
            + Groups.SUMMARY_COUNT + "!=0";

    private static final String LOCAL_SELECTION = LocalGroups.GroupColumns.TITLE + " NOT NULL ";

    private static final String SORT = Groups.ACCOUNT_TYPE + ", "
            + Groups.ACCOUNT_NAME + ", "
            + Groups.DATA_SET + ", "
            + Groups.TITLE + " COLLATE LOCALIZED ASC";

    private static final String LOCAL_SORT = LocalGroups.GroupColumns.TITLE +
            " COLLATE LOCALIZED ASC";

    private static final int COLUMN_ID            = 0;
    private static final int COLUMN_GROUP_TITLE   = 1;
    private static final int COLUMN_ACCOUNT_NAME  = 2;
    private static final int COLUMN_ACCOUNT_TYPE  = 3;
    private static final int COLUMN_DATA_SET      = 4;
    private static final int COLUMN_SUMMARY_COUNT = 5;

    private static final int LOCAL_COLUMN_COUNT = 2;

    private long mId;
    private String mTitle;
    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private int mSummaryCount;
    private ArrayList<PhoneNumber> mPhoneNumbers;
    private boolean mLocal = false;

    private boolean mIsChecked;

    private Group(Context context, Cursor c, boolean local) {
        mLocal = local;
        mId = c.getLong(COLUMN_ID);
        mTitle = c.getString(COLUMN_GROUP_TITLE);
        if (!local) {
            mAccountName = c.getString(COLUMN_ACCOUNT_NAME);
            mAccountType = c.getString(COLUMN_ACCOUNT_TYPE);
            mDataSet = c.getString(COLUMN_DATA_SET);
            mSummaryCount = c.getInt(COLUMN_SUMMARY_COUNT);
        } else {
            mSummaryCount = c.getInt(LOCAL_COLUMN_COUNT);
        }
        mPhoneNumbers = new ArrayList<PhoneNumber>();

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "Create Group: recipient=" + mTitle + ", groupId=" + mId);
        }
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getAccountType() {
        return mAccountType;
    }

    public String getDataSet() {
        return mDataSet;
    }

    public int getSummaryCount() {
        return mSummaryCount;
    }

    public ArrayList<PhoneNumber> getPhoneNumbers() {
        return mPhoneNumbers;
    }

    public boolean isLocal() {
        return mLocal;
    }

    public void addPhoneNumber(PhoneNumber phoneNumber) {
        if (!mPhoneNumbers.contains(phoneNumber)) {
            mPhoneNumbers.add(phoneNumber);
        }
    }

    /**
     * Returns true if this group is selected for a multi-operation.
     */
    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    /**
     * Get all groups
     */
    public static ArrayList<Group> getGroups(Context context) {
        final Cursor cursor = context.getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
                PROJECTION, SELECTION, null, SORT);

        if (cursor == null) {
            return null;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        ArrayList<Group> groups = new ArrayList<Group>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            groups.add(new Group(context, cursor, false));
        }
        cursor.close();

        //Check if LocalGroups is exists. We have to do it because Xperia don't have this class in framework base
        try {
            Class.forName("android.provider.LocalGroups");
        } catch( ClassNotFoundException e ) {
            Log.i(TAG, "LocalGroups isn't exist");
            return groups;
        }

        final Cursor localCursor = context.getContentResolver().query(LocalGroups.CONTENT_URI,
                LOCAL_PROJECTION, LOCAL_SELECTION, null, LOCAL_SORT);

        if (localCursor  == null) {
            return groups;
        }

        if (localCursor.getCount() == 0) {
            localCursor.close();
            return groups;
        }

        localCursor.moveToPosition(-1);
        while (localCursor.moveToNext()) {
            groups.add(new Group(context, localCursor, true));
        }
        localCursor.close();

        return groups;
    }

    
}
