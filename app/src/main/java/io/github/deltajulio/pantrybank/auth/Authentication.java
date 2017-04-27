package io.github.deltajulio.pantrybank.auth;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Created by Bryan on 19-Apr-17.
 */

public final class Authentication
{
    private Authentication() {}

    public static boolean IsEmailValid(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean IsPasswordValid(String password)
    {
        return password.length() > 4 && password.length() < 16;
    }
}
