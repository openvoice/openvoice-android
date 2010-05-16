package org.openvoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Contacts.People;

public class ContactManager {
	private static ContactManager mInstance;
	private static Context mContext;
	private ArrayList<String> mPhoneNumbers = new ArrayList<String>();
	private HashMap mContactInfo = new HashMap();
	
	private ContactManager() {

		// Make the query. 
		Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
		                         null, 			// Which columns to return 
		                         null,      // Which rows to return (all rows)
		                         null, null);		
		if (cursor.moveToFirst()) {
			int phoneColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
			int idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
			do {
				String phoneNumber = cursor.getString(phoneColumn);
				String name = cursor.getString(nameColumn);
				long id = cursor.getLong(idColumn);
				if(phoneNumber != null) {
					Pattern p = Pattern.compile("[^0-9]");
					phoneNumber = phoneNumber.replaceAll(p.pattern(), "");
					// TODO mPhoneNumbers can be replaced by mContactInfo
					mPhoneNumbers.add(phoneNumber);
					mContactInfo.put(phoneNumber, new String[]{name, new Long(id).toString()});
				}
			} while (cursor.moveToNext());
		}
	}

	public static ContactManager getInstance(Context context) {
		mContext = context;
		if (mInstance == null) {
			mInstance = new ContactManager();
		}
		return mInstance;
	}
	
	public boolean isContact(String incomingNumber) {
	  String modifiedNumber = "";
	  if(incomingNumber == null || incomingNumber.length() == 0) {
	  	return false;
	  }
	  
	  if(incomingNumber.length() == 11) {
	    modifiedNumber = incomingNumber.substring(1);
	  }
	  
		return mPhoneNumbers.contains(incomingNumber) || mPhoneNumbers.contains(modifiedNumber);
	}

	public Uri getPhotoUriByPhoneNumber(String phoneNumber) {
	  String[] result = (String[])mContactInfo.get(phoneNumber);
	  if(result != null) {
      return ContentUris.withAppendedId(People.CONTENT_URI, Long.parseLong(result[1]));
	  }
	  return null;
	}
	
	public String getContactNameByPhoneNumber(String phoneNumber) {
	  String[] result = (String[]) mContactInfo.get(phoneNumber); 
	  if(result != null) {
	    return result[0];
	  } else {
	    return "Unknown sender";
	  }
	}
}
