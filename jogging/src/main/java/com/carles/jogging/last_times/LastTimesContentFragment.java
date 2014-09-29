package com.carles.jogging.last_times;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.util.PrefUtil;

import java.util.List;

/**
 * Created by carles1 on 10/09/14.
 */
public class LastTimesContentFragment extends BaseFragment {

    private Context ctx;

    private TextView txtNoResults;
    private ListView list;
    private ProgressBar progress;
    private LastTimesAdapter adapter;

    // Manage Contextual Action Bar
    private ActionMode actionMode;

    public static LastTimesContentFragment newInstance() {
        LastTimesContentFragment fragment = new LastTimesContentFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_last_times_content, container, false);

        txtNoResults = (TextView) view.findViewById(R.id.txt_no_results);
        list = (ListView) view.findViewById(R.id.list);
        progress = (ProgressBar) view.findViewById(R.id.progress);

        loadData();

        return view;
    }

    private void loadData() {
        // obtain the selected distance by the user in the actionBar navigation list
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().
                setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        int position = ((SherlockFragmentActivity) getActivity()).getSupportActionBar().
                getSelectedNavigationIndex();
        if (position == -1) {
            position = 0;
        }
        final int meters = (position + 1)*1000;

        new QueryLastTimesAsyncTask().execute(meters);
    }

    private void updateView(final List<JoggingModel> joggings) {
        if (joggings == null || joggings.isEmpty()) {
            txtNoResults.setVisibility(View.VISIBLE);

        } else {
            adapter = new LastTimesAdapter(ctx, joggings);
            list.setAdapter(adapter);
            list.setItemsCanFocus(true);
            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    if (actionMode == null) {
                        // ActionMode off. If user clicks a jogging, open its details
                        JoggingModel selectedJogging = joggings.get(position);
                        List<JoggingModel> partials = JoggingSQLiteHelper.getInstance(ctx).queryPartials(selectedJogging);
                        selectedJogging.setPartialsForKilometer(partials);
                        selectedJogging.setPartials(partials);

                        Intent intent = new Intent(ctx, ResultDetailActivity.class);
                        intent.putExtra(C.EXTRA_JOGGING_TOTAL, selectedJogging);
                        intent.putExtra(C.EXTRA_FOOTING_RESULT, FootingResult.SUCCESS);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);

                    } else {
                        // ActionMode on. If user clicks a jogging select/unselect it
                        onItemClickInActionMode(position);

                    }
                }
            });

            // when user long-presses a jogging, open the CAB to allow deleting joggings
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (actionMode == null) {
                        list.setItemChecked(position, true);
                    }
                    onItemClickInActionMode(position);
                    return true;
                }
            });
        }}

    private void onItemClickInActionMode(int position) {
        SparseBooleanArray checked = list.getCheckedItemPositions();

        int checkedCount = countChecked(checked);
        if (checkedCount > 0 && actionMode == null) {
            // there are selected items, so start CAB
            actionMode = getSherlockActivity().startActionMode(new ActionModeCallback());
        } else if (checkedCount == 0 && actionMode != null) {
            // no selected items remaining, hide CAB
            actionMode.finish();
        }

        if (actionMode != null) {
            // show number of items selected in the action bar title
            String suffix;
            if (checkedCount == 1) {
                suffix = getString(R.string.title_last_contextual_suffix);
            } else {
                suffix = getString(R.string.title_last_contextual_suffix_pl);
            }
            actionMode.setTitle(String.valueOf(checkedCount) + suffix);
            adapter.setChecked(checked);
            adapter.notifyDataSetChanged();
        }
    }

    private int countChecked(SparseBooleanArray checked) {
        int count = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                count++;
            }
        }
        return count;
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_last_times_contextual, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                new ConfirmDeleteDialog().show(getFragmentManager(), C.TAG_CONFIRM_DELETE_DIALOG);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (int i = 0; i < list.getAdapter().getCount(); i++) {
                list.setItemChecked(i, false);
            }

            if (mode == actionMode) {
                actionMode = null;
            }
        }

    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    public class ConfirmDeleteDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(getActivity());

            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_alert_custom, null);
            final TextView title = (TextView) view.findViewById(R.id.dlg_title);
            final TextView msg = (TextView) view.findViewById(R.id.dlg_msg);
            final Button btnOk = (Button) view.findViewById(R.id.btn_yes);
            final Button btnNo = (Button) view.findViewById(R.id.btn_no);

            title.setText(getString(R.string.confirm_delete_title));
            msg.setText(getString(R.string.confirm_delete_msg));
            btnOk.setText(getString(R.string.yes));
            btnNo.setText(getString(R.string.no));

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DeleteSelectedAsyncTask().execute();
                    dismiss();
                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            // should invoke setCancelable in the DialogFragment directly, not in the inner Dialog
            // in order to avoid dismissing the dialog when user presses back button
            this.setCancelable(false);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);
            dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
            return dialog;
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class QueryLastTimesAsyncTask extends AsyncTask<Integer, Void, List<JoggingModel>> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<JoggingModel> doInBackground(Integer... params) {
            return JoggingSQLiteHelper.getInstance(ctx).queryLastTimes(PrefUtil.getLoggedUser(ctx), params[0]);
        }

        @Override
        protected void onPostExecute(List<JoggingModel> joggings) {
            progress.setVisibility(View.GONE);
            updateView(joggings);
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class DeleteSelectedAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            SparseBooleanArray checked = list.getCheckedItemPositions();
            long id;
            for (int i = checked.size() - 1; i >= 0; i--) {
                if (checked.get(i)) {
                    id = adapter.getItem(i).getId();
                    adapter.remove(i);
                    JoggingSQLiteHelper.getInstance(ctx).deleteJogging(id);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            actionMode.finish();

            if (adapter.getCount() == 0) {
                txtNoResults.setVisibility(View.VISIBLE);
            }

            progress.setVisibility(View.GONE);
        }
    }
}
