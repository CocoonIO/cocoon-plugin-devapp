package com.ludei.devapplib.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.io.IOException;


public class AccountUtils {

	public interface AddCocoonAccountCallback {
        void onCocoonAccountAdded();
        void onCocoonAccountAddError();
    }
	
	public static boolean checkUserHasAccount(Context context){
		AccountManager am = AccountManager.get(context);
		return am.getAccountsByType(CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE).length > 0;
	}
	
	public static void addAccount(Activity activity, AddCocoonAccountCallback callback){
		AccountManager accountManager = AccountManager.get(activity);
		accountManager.addAccount(
				CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE,
				CocoonAccountAuthenticatorService.COCOON_AUTHTOKEN_TYPE,
				null, 
				null, 
				activity, 
				new AddCocoonAccount(callback),
				null);
	}
	
	static class AddCocoonAccount implements AccountManagerCallback<Bundle>{
		
		private AddCocoonAccountCallback mCallback;
		
		public AddCocoonAccount(AddCocoonAccountCallback callback) {
			mCallback = callback;
		}
		
		@Override
		public void run(AccountManagerFuture<Bundle> future) {
			try {
				Bundle result = future.getResult();
				if(result != null){
					mCallback.onCocoonAccountAdded();
				}else{
					mCallback.onCocoonAccountAddError();
				}
				
			} catch (OperationCanceledException e) {
				mCallback.onCocoonAccountAddError();
				
			} catch (AuthenticatorException e) {
				mCallback.onCocoonAccountAddError();
				
			} catch (IOException e) {
				mCallback.onCocoonAccountAddError();
			}
		}
	}

	public static Account getAccount(Context context) {
		AccountManager am = AccountManager.get(context);
		Account result = null;
		Account[] availableAccounts = am.getAccountsByType(CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE);
		if(availableAccounts.length > 0){
			result =  availableAccounts[0];
		}
		
		return result;
	}
}
