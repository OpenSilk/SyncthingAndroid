/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package syncthing.android.ui.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 3/10/15.
 */
public class LoginScreenView extends RelativeLayout {

    @InjectView(R.id.alias) EditText serverAlias;
    @InjectView(R.id.server_url) EditText serverUrl;
    @InjectView(R.id.server_port) EditText serverPort;
    @InjectView(R.id.user) EditText userName;
    @InjectView(R.id.password) EditText userPass;
    @InjectView(R.id.use_tls) CheckBox useTls;

    @Inject LoginPresenter mPresenter;

    ProgressDialog loadingProgress;
    AlertDialog errorDialog;

    public LoginScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            LoginComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ButterKnife.inject(this);
            userPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    submit();
                    return true;
                }
            });
            mPresenter.takeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            serverUrl.requestFocus();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        dismissError();
        dismissLoginProgress();
        dismissKeyboard();
    }

    @OnClick(R.id.submit)
    void submit() {
        String alias = serverAlias.getText().toString();
        String host = serverUrl.getText().toString();
        String port = serverPort.getText().toString();
        String user = userName.getText().toString();
        String pass = userPass.getText().toString();
        boolean tls = useTls.isChecked();
        if (!LoginUtils.validateHost(host)) {
            showInputError("Invalid Host");
        } else if (!LoginUtils.validatePort(port)) {
            showInputError("Invalid Port");
        } else {
            mPresenter.fetchApiKey(alias, host, port, user, pass, tls);
            showLoginProgress();
            dismissKeyboard();
        }
    }

    @OnClick(R.id.cancel)
    void cancel() {
        mPresenter.exitCanceled();
    }

    void initWithCredentials(Credentials credentials) {
        serverAlias.setText(credentials.alias);
        serverUrl.setText(LoginUtils.extractHost(credentials.url));
        serverPort.setText(LoginUtils.extractPort(credentials.url));
        useTls.setChecked(LoginUtils.isHttps(credentials.url));
    }

    void showInputError(String error) {
        dismissError();
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.input_error)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void showLoginError(String error) {
        dismissError();
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.login_failure)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void dismissError(){
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
    }

    void showLoginProgress() {
        dismissLoginProgress();
        loadingProgress = new ProgressDialog(getContext());
        loadingProgress.setMessage(getResources().getString(R.string.fetching_api_key_dots));
        loadingProgress.setCancelable(false);
        loadingProgress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getContext().getString(android.R.string.cancel),
                (DialogInterface dialog, int which) -> {
                        mPresenter.cancelLogin();
                });
        loadingProgress.show();
    }

    void dismissLoginProgress() {
        if (loadingProgress != null && loadingProgress.isShowing()) {
            loadingProgress.dismiss();
        }
    }

    void dismissKeyboard() {
        final InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

}
