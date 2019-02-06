package com.ludei.devapplib.android.auth.cocoon;

/**
 * Created by imanolmartin on 04/11/15.
 */
public interface AddAccountTaskListener {

    void AddCocoonAccountSuccess(AddAccountTask task, AddAccountResponse response);
    void AddCocoonAccountError(AddAccountTask task, int status, String msg);

}
