package bravo.mail.fairmail.extension

import java.util.regex.Pattern

val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "gmail" +
            "(" +
            "\\." +
            "com" +
            ")+"
)
fun String.isValidEmail(): Boolean{
    return EMAIL_ADDRESS_PATTERN.matcher(this).matches()
}