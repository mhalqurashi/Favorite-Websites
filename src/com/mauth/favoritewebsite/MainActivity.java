/**
File name: MainAcitivty.java
Author: Muath Alqurashi
Email Address: mhalqurashi@suffolk.edu
Last day of modification: Mar 10, 2015
Description: This app let users save their favorite websites. 
*/
package com.mauth.favoritewebsite;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	
	private static final String SITES = "sites";
	
	private EditText urlEditText;
	private EditText tagEditText;
	private SharedPreferences savedSites;
	private ArrayList<String> names;
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		urlEditText = (EditText) findViewById(R.id.urlEditText);
		tagEditText = (EditText) findViewById(R.id.tagEditText);
		
		savedSites = getSharedPreferences(SITES, MODE_PRIVATE);
		names = new ArrayList<String>(savedSites.getAll().keySet());
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		
		adapter = new ArrayAdapter<String>(this, R.layout.list_item, names);
		setListAdapter(adapter);
		
		ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListener);
		getListView().setOnItemClickListener(itemClickListener);
		getListView().setOnItemLongClickListener(itemLongClickListener);
		
	}
	
	private void addFavoriteSite(String url, String name) {
		SharedPreferences.Editor preferencesEditor = savedSites.edit();
		preferencesEditor.putString(name, url);
		preferencesEditor.apply();
		
		if(! names.contains(name)) {
			names.add(name);
			Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
			adapter.notifyDataSetChanged();
		}
	}
	
	private void shareFavorite(String name) {
			String urlString = savedSites.getString(name, "");
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, 
					getString(R.string.shareSubject));
			shareIntent.putExtra(Intent.EXTRA_TEXT, 
					getString(R.string.shareMessage, urlString));
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent, 
					getString(R.string.shareSite)));
	}
	
	private void deleteFavoriteSite(final String site) 
	{
		AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
		
		confirmBuilder.setMessage(
				getString(R.string.confirmMessage, site));
		confirmBuilder.setNegativeButton(getString(R.string.cancel), 
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id) 
			{
				dialog.cancel();
			}
		});
		
		confirmBuilder.setPositiveButton(getString(R.string.delete),
				new DialogInterface.OnClickListener ()
		{
			public void onClick(DialogInterface dialog, int id) 
			{
				names.remove(site);
				
				SharedPreferences.Editor preferencesEditor = 
						savedSites.edit();
				preferencesEditor.remove(site);
				preferencesEditor.apply();
				adapter.notifyDataSetChanged();
			}
		});
		
		confirmBuilder.create().show();
	}
	
	public OnClickListener saveButtonListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (urlEditText.getText().length() > 0 && 
					tagEditText.getText().length() > 0)
			{
				addFavoriteSite(urlEditText.getText().toString(),
						tagEditText.getText().toString());
				urlEditText.setText("");
				tagEditText.setText("");
				
				((InputMethodManager) getSystemService(
						Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
								tagEditText.getWindowToken(), 0);
			}
			else
			{
				AlertDialog.Builder builder = 
						new AlertDialog.Builder(MainActivity.this);
				
				builder.setMessage(R.string.missingMessage);
				builder.setPositiveButton(R.string.OK, null);
				AlertDialog errorDialog = builder.create();
				errorDialog.show();
			}
		}
	};
	
	OnItemClickListener itemClickListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
		{
			String tag = ((TextView) view).getText().toString();
			String urlString = savedSites.getString(tag, "");
			//Makes sure that the URL is valid. 
			if (! urlString.startsWith("https://") && 
					! urlString.startsWith("http://")) {
			    urlString = getString(R.string.https) + urlString;
			}
			Intent webIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(urlString));
			startActivity(webIntent);
		}
	};
	
	OnItemLongClickListener itemLongClickListener = 
			new OnItemLongClickListener ()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, 
				int position, long id)
		{
			final String tag = ((TextView) view).getText().toString();
			
			AlertDialog.Builder builder = 
					new AlertDialog.Builder(MainActivity.this);
			
			builder.setTitle(
					getString(R.string.shareEditDeleteTitle, tag));
			builder.setItems(R.array.dialog_items, 
					new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which)
					{
						case 0: //share
							shareFavorite(tag);
							break;
						case 1:
							tagEditText.setText(tag);
							urlEditText.setText(
									savedSites.getString(tag, ""));
							break;
						case 2:
							deleteFavoriteSite(tag);
							break;
					}
				}
			});
			
			builder.setNegativeButton(getString(R.string.cancel),
					new DialogInterface.OnClickListener() 
					{	
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
					}
			});
			
			builder.create().show();
			return true;
		}
	};
	
}
