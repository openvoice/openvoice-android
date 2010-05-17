package org.openvoice;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.People.Phones;

/**
 * An implementation of {@link ContactAccessor} that uses legacy Contacts API.
 * These APIs are deprecated and should not be used unless we are running on a
 * pre-Eclair SDK.
 * <p>
 * There are several reasons why we wouldn't want to use this class on an Eclair device:
 * <ul>
 * <li>It would see at most one account, namely the first Google account created on the device.
 * <li>It would work through a compatibility layer, which would make it inherently less efficient.
 * <li>Not relevant to this particular example, but it would not have access to new kinds
 * of data available through current APIs.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class ContactAccessorSdk3_4 extends ContactAccessor {

    /**
     * Returns a Pick Contact intent using the pre-Eclair "people" URI.
     */
    @Override
    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
    }

    /**
     * Retrieves the contact information.
     */
    @Override
    public ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri) {
        ContactInfo contactInfo = new ContactInfo();
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{People.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactInfo.setDisplayName(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        Uri phoneUri = Uri.withAppendedPath(contactUri, Phones.CONTENT_DIRECTORY);
        cursor = contentResolver.query(phoneUri,
                new String[]{Phones.NUMBER}, null, null, Phones.ISPRIMARY + " DESC");

        try {
            if (cursor.moveToFirst()) {
                contactInfo.setPhoneNumber(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return contactInfo;
    }
}
