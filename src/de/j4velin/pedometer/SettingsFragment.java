package de.j4velin.pedometer;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {

	final static int DEFAULT_GOAL = 10000;
	final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 1.25f : 38.1f;
	final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		findPreference("about").setOnPreferenceClickListener(this);

		Preference account = findPreference("account");
		account.setOnPreferenceClickListener(this);
		if (((MainActivity) getActivity()).getGC().isConnected()) {
			account.setSummary(getString(R.string.signed_in, ((MainActivity) getActivity()).getGC().getCurrentPlayer()
					.getDisplayName()));
		}

		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

		Preference goal = findPreference("goal");
		goal.setOnPreferenceClickListener(this);
		goal.setSummary(getString(R.string.goal_summary, prefs.getInt("goal", DEFAULT_GOAL)));

		Preference stepsize = findPreference("stepsize");
		stepsize.setOnPreferenceClickListener(this);
		stepsize.setSummary(getString(R.string.step_size_summary, prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE),
				prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)));

		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_settings).setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return ((MainActivity) getActivity()).optionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		AlertDialog.Builder builder;
		View v;
		final SharedPreferences prefs;
		switch (preference.getTitleRes()) {
		case R.string.goal:
			builder = new AlertDialog.Builder(getActivity());
			final NumberPicker np = new NumberPicker(getActivity());
			prefs = getPreferenceManager().getSharedPreferences();
			np.setMinValue(1);
			np.setMaxValue(100000);
			np.setValue(prefs.getInt("goal", 10000));
			builder.setView(np);
			builder.setTitle("Set goal");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					np.clearFocus();
					prefs.edit().putInt("goal", np.getValue()).apply();
					preference.setSummary(getString(R.string.goal_summary, np.getValue()));
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
		case R.string.step_size:
			builder = new AlertDialog.Builder(getActivity());
			prefs = getPreferenceManager().getSharedPreferences();
			v = getActivity().getLayoutInflater().inflate(R.layout.stepsize, null);
			final RadioGroup unit = (RadioGroup) v.findViewById(R.id.unit);
			final EditText value = (EditText) v.findViewById(R.id.value);
			unit.check(prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm : R.id.ft);
			value.setText(String.valueOf(prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE)));
			builder.setView(v);
			builder.setTitle("Set step size");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					prefs.edit().putFloat("stepsize_value", Float.valueOf(value.getText().toString()))
							.putString("stepsize_unit", unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft").apply();
					preference.setSummary(getString(R.string.step_size_summary, Float.valueOf(value.getText().toString()),
							unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft"));
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
		case R.string.about:
			builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("About");
			builder.setMessage("This app was created by Thomas Hoffmann (www.j4velin-development.de) and uses the 'HoloGraphLibrary' by Daniel Nadeau");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
		case R.string.account:
			builder = new AlertDialog.Builder(getActivity());
			v = getActivity().getLayoutInflater().inflate(R.layout.signin, null);
			builder.setView(v);
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			if (((MainActivity) getActivity()).getGC().isConnected()) {
				((TextView) v.findViewById(R.id.signedin)).append(((MainActivity) getActivity()).getGC().getCurrentPlayer()
						.getDisplayName());
				v.findViewById(R.id.sign_in_button).setVisibility(View.GONE);
				builder.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((MainActivity) getActivity()).getGC().signOut();
						preference.setSummary(getString(R.string.sign_in));
						dialog.dismiss();
					}
				});
			}
			final Dialog d = builder.create();

			if (!((MainActivity) getActivity()).getGC().isConnected()) {
				v.findViewById(R.id.signedin).setVisibility(View.GONE);
				v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						// start the asynchronous sign in flow
						((MainActivity) getActivity()).beginSignIn();
						d.dismiss();
					}
				});
			}
			d.show();
			break;
		}
		return false;
	}

}
