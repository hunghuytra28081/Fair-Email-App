package bravo.mail.fairmail.utils;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static bravo.mail.fairmail.utils.GmailState.TYPE_GOOGLE;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import org.json.JSONException;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.Objects;

import javax.mail.MessagingException;

public class ServiceAuthenticator extends Authenticator {
    private Context context;
    private int auth;
    private String provider;
    private long keep_alive;
    private String user;
    private String password;
    private IAuthenticated intf;

    public static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;
    static final int AUTH_TYPE_OAUTH = 3;

    ServiceAuthenticator(
            Context context,
            int auth, String provider, int keep_alive,
            String user, String password,
            IAuthenticated intf) {
        this.context = context.getApplicationContext();
        this.auth = auth;
        this.provider = provider;
        this.keep_alive = keep_alive * 60 * 1000L;
        this.user = user;
        this.password = password;
        this.intf = intf;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String token = password;
        try {
            token = refreshToken(false);
        } catch (Throwable ex) {
            if (ex.getCause() instanceof InterruptedException)
                Log.i("ServiceAuthenticator",ex.toString());
            else
                Log.e("ServiceAuthenticator",ex.toString());
        }

        Log.i("ServiceAuthenticator",user + " returning " + (auth == AUTH_TYPE_PASSWORD ? "password" : "token"));
        return new PasswordAuthentication(user,token.toCharArray());
    }

    String refreshToken(boolean expire) throws AuthenticatorException, OperationCanceledException, IOException, JSONException, MessagingException {
        if (auth == AUTH_TYPE_GMAIL) {
            GmailState authState = GmailState.jsonDeserialize(password);
            try {
                authState.refresh(context, user, expire, keep_alive);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            }
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                Log.e("ServiceAuthenticator", user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(password, newPassword)) {
                password = newPassword;
                if (intf != null)
                    intf.onPasswordChanged(context, password);
            }

            return authState.getAccessToken();
        } else if (auth == AUTH_TYPE_OAUTH && provider != null) {
            AuthState authState = AuthState.jsonDeserialize(password);
//            OAuthRefresh(context, provider, authState, expire, keep_alive);
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                Log.e("ServiceAuthenticator", user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(password, newPassword)) {
                password = newPassword;
                if (intf != null)
                    intf.onPasswordChanged(context, password);
            }

            return authState.getAccessToken();
        } else
            return password;
    }

    void checkToken() {
        Long expiration = null;

        try {
            if (auth == AUTH_TYPE_GMAIL) {
                GmailState authState = GmailState.jsonDeserialize(password);
                expiration = authState.getAccessTokenExpirationTime();
            } else if (auth == AUTH_TYPE_OAUTH) {
                AuthState authState = AuthState.jsonDeserialize(password);
                expiration = authState.getAccessTokenExpirationTime();
            }
        } catch (JSONException ex) {
            Log.e("ServiceAuthenticator",ex.toString());
        }

        long slack = Math.min(keep_alive, 30 * 60 * 1000L);
        if (expiration != null && expiration - slack < new Date().getTime())
            throw new IllegalStateException("ServiceAuthenticator null");
    }

    interface IAuthenticated {
        void onPasswordChanged(Context context, String newPassword);
    }

    /*private static void OAuthRefresh(Context context, String id, AuthState authState, boolean expire, long keep_alive)
            throws MessagingException {
        try {
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null && expiration - keep_alive < new Date().getTime()) {
                Log.e("ServiceAuthenticator", "OAuth force refresh" +
                        " expiration=" + new Date(expiration) +
                        " keep_alive=" + (keep_alive / 60 / 1000) + "m");
                authState.setNeedsTokenRefresh(true);
            }

            if (expire)
                authState.setNeedsTokenRefresh(true);

            ClientAuthentication clientAuth;
            EmailProvider provider = EmailProvider.getProvider(context, id);
            if (provider.oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

            ErrorHolder holder = new ErrorHolder();
            Semaphore semaphore = new Semaphore(0);

            Log.i("ServiceAuthenticator","OAuth refresh id=" + id);
            AuthorizationService authService = new AuthorizationService(context);
            authState.performActionWithFreshTokens(
                    authService,
                    clientAuth,
                    new AuthState.AuthStateAction() {
                        @Override
                        public void execute(String accessToken, String idToken, AuthorizationException error) {
                            if (error != null)
                                holder.error = error;
                            semaphore.release();
                        }
                    });

            semaphore.acquire();
            Log.i("ServiceAuthenticator","OAuth refreshed id=" + id);

            if (holder.error != null)
                throw holder.error;
        } catch (Exception ex) {
            throw new MessagingException("OAuth refresh id=" + id, ex);
        }
    }*/

    public static String getAuthTokenType(String type) {
        // https://developers.google.com/gmail/imap/xoauth2-protocol
        if (TYPE_GOOGLE.equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    private static class ErrorHolder {
        AuthorizationException error;
    }
}
