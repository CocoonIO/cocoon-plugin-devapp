package com.ludei.devapplib.android.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * AbstractAccountAuthenticator simple implementation. Used for training.
 */
public class CocoonAccountAuthenticator extends AbstractAccountAuthenticator {

	private Context mContext;
	
	public CocoonAccountAuthenticator(Context context) {
		super(context);

		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		final Intent intent = new Intent(mContext, CocoonAccountAuthenticatorActivity.class);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        
        return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		if (!authTokenType.equals(CocoonAccountAuthenticatorService.COCOON_AUTHTOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "Invalid authTokenType");
            return result;
        }
        
        final AccountManager am = AccountManager.get(mContext);
        final String authToken = am.peekAuthToken(account, authTokenType);
        if (authToken != null){
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

		final Intent intent = new Intent(mContext, CocoonAccountAuthenticatorActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
		intent.putExtra(CocoonAccountAuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
		final Bundle result = new Bundle();
		result.putParcelable(AccountManager.KEY_INTENT, intent);
		return result;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}
}
