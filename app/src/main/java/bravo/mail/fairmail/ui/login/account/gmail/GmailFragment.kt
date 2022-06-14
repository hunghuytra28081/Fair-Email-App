package bravo.mail.fairmail.ui.login.account.gmail

import android.accounts.AccountManager.newChooseAccountIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import bravo.mail.fairmail.R
import bravo.mail.fairmail.ui.login.account.ChooseAccountActivity.Companion.REQUEST_CHOOSE_ACCOUNT
import bravo.mail.fairmail.utils.GmailState
import bravo.mail.fairmail.utils.Helper
import kotlinx.android.synthetic.main.fragment_gmail.*

class GmailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gmail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initHandles()
    }

    private fun initHandles() {
        btn_login_gmail.setOnClickListener {

            val intent = newChooseAccountIntent(
                null,
                null, arrayOf(GmailState.TYPE_GOOGLE),
                false,
                null,
                null,
                null,
                null
            )
            val pm = requireContext().packageManager
            if (intent.resolveActivity(pm) == null) // system whitelisted
                Log.e("GmailFragment", "newChooseAccountIntent unavailable")
            startActivityForResult(intent, REQUEST_CHOOSE_ACCOUNT)
        }
    }

    companion object {
        fun newInstance() = GmailFragment()
    }
}
