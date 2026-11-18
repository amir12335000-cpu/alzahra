
package ir.phonelink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var major: EditText
    private lateinit var status: TextView

    private val prefs by lazy {
        getSharedPreferences("alzahra_settings", MODE_PRIVATE)
    }

    companion object {
        private const val REQUEST_FOLDER = 1001
        private const val PREF_FOLDER_URI = "folder_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 40, 36, 36)
            setBackgroundColor(0xFFF5F7FA.toInt())
        }

        fun addText(text: String, size: Float, bold: Boolean = false) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(0xFF172033.toInt())
                if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 10, 0, 10)
            })
        }

        fun addField(hint: String, value: String = ""): EditText {
            val field = EditText(this).apply {
                this.hint = hint
                setText(value)
                textSize = 16f
                setSingleLine(true)
            }

            root.addView(field, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            })

            return field
        }

        fun addButton(label: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                text = label
                setOnClickListener { action() }
            })
        }

        addText("Alzahra University", 25f, true)
        addText("ثبت مشخصات دانشجو", 18f, true)

        firstName = addField(
            "نام",
            prefs.getString("first_name", "") ?: ""
        )

        lastName = addField(
            "نام خانوادگی",
            prefs.getString("last_name", "") ?: ""
        )

        major = addField(
            "رشته تحصیلی",
            prefs.getString("major", "") ?: ""
        )

        addButton("ذخیره مشخصات") {
            val first = firstName.text.toString().trim()
            val last = lastName.text.toString().trim()
            val field = major.text.toString().trim()

            if (first.isEmpty() || last.isEmpty() || field.isEmpty()) {
                status.text = "لطفاً هر سه قسمت را تکمیل کن."
                return@addButton
            }

            prefs.edit()
                .putString("first_name", first)
                .putString("last_name", last)
                .putString("major", field)
                .apply()

            status.text = "مشخصات ذخیره شد."
        }

        addText("دسترسی به فایل‌ها", 18f, true)

        addText(
            "پوشه‌ای را که می‌خواهی با دانشگاه به اشتراک بگذاری انتخاب کن."
        , 14f)

        addButton("انتخاب پوشه برای اشتراک‌گذاری") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_FOLDER)
        }
                }

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF315A80.toInt())
            setPadding(0, 20, 0, 10)
            text = if (prefs.contains(PREF_FOLDER_URI)) {
                "پوشه قبلاً انتخاب شده است."
            } else {
                "هنوز پوشه‌ای انتخاب نشده است."
            }
        }

        root.addView(status)
        setContentView(root)
    }

    @Deprecated("Deprecated in Android API, retained for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_FOLDER &&
            resultCode == RESULT_OK) {

            val uri: Uri = data?.data ?: return

            val flags = data.flags and (
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    flags
                )

                prefs.edit()
                    .putString(PREF_FOLDER_URI, uri.toString())
                    .apply()

                                     
            } catch (e: Exception) {
                status.text = "ذخیره دسترسی پوشه ناموفق بود: ${e.message}"
            }
        }
    }

    private fun revokeFolderAccess() {
        val savedUri = prefs.getString(PREF_FOLDER_URI, null)

        if (savedUri == null) {
            status.text = "پوشه‌ای برای لغو دسترسی وجود ندارد."
            return
        }

        try {
            val uri = Uri.parse(savedUri)

            contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            
        } catch (e: Exception) {
            status.text = "لغو دسترسی انجام نشد: ${e.message}"
        }
    }
}
