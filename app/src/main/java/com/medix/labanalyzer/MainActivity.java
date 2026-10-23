package com.medix.labanalyzer;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    private EditText nameInput, symptomsInput;
    private TextView resultText;
    private ScrollView scrollView;
    private Button searchButton;
    private String lastSymptoms = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        layout.setPadding(pad, pad, pad, pad);
        layout.setBackgroundColor(Color.rgb(240, 248, 248));

        scrollView = new ScrollView(this);
        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("🩺 طبيبي الذكي\nSmart Doctor AI");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(0, 105, 100));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(20), 0, dp(30));
        layout.addView(title);

        nameInput = new EditText(this);
        nameInput.setHint("أدخل اسم المريض");
        nameInput.setHintTextColor(Color.GRAY);
        nameInput.setTextColor(Color.BLACK);
        nameInput.setBackgroundColor(Color.WHITE);
        nameInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setSingleLine(true);
        layout.addView(nameInput);

        symptomsInput = new EditText(this);
        symptomsInput.setHint("اكتب الأعراض، مثال: صداع ودوخة");
        symptomsInput.setHintTextColor(Color.GRAY);
        symptomsInput.setTextColor(Color.BLACK);
        symptomsInput.setBackgroundColor(Color.WHITE);
        symptomsInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        symptomsInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        symptomsInput.setMinLines(3);
        symptomsInput.setGravity(Gravity.TOP | Gravity.START);
        layout.addView(symptomsInput);

        Button analyzeButton = new Button(this);
        analyzeButton.setText("تحليل الأعراض");
        layout.addView(analyzeButton);

        searchButton = new Button(this);
        searchButton.setText("🔍 البحث التلقائي عن العلاج");
        searchButton.setVisibility(Button.GONE);
        layout.addView(searchButton);

        resultText = new TextView(this);
        resultText.setTextSize(18);
        resultText.setTextColor(Color.DKGRAY);
        resultText.setPadding(0, dp(25), 0, dp(25));
        resultText.setTextIsSelectable(true);
        layout.addView(resultText);

        TextView warning = new TextView(this);
        warning.setText("⚠️ تنبيه: النتائج إرشادية وليست تشخيصاً طبياً.\n"
                + "في حالات الطوارئ اتصل بالإسعاف فوراً.");
        warning.setTextSize(14);
        warning.setTextColor(Color.RED);
        layout.addView(warning);

        setContentView(scrollView);

        analyzeButton.setOnClickListener(v -> analyzeSymptoms());

        // ✅ البحث التلقائي داخل التطبيق
        searchButton.setOnClickListener(v -> {
            if (lastSymptoms.isEmpty()) return;
            resultText.setText("جارٍ البحث عن العلاج...\nيرجى الانتظار");
            searchButton.setEnabled(false);
            new Thread(() -> searchTreatment(lastSymptoms)).start();
        });
    }

    private void analyzeSymptoms() {
        String name = nameInput.getText().toString().trim();
        String raw = symptomsInput.getText().toString().trim();
        String symptoms = normalize(raw);

        if (symptoms.isEmpty()) {
            resultText.setText("يرجى كتابة الأعراض أولاً.");
            return;
        }

        lastSymptoms = raw;
        String result = classify(symptoms);
        String patientName = name.isEmpty() ? "المريض" : name;

        resultText.setText("مرحباً " + patientName + "\n\n"
                + "الأعراض: " + raw + "\n\n"
                + "الإرشادات الأولية:\n" + result);

        searchButton.setVisibility(Button.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, resultText.getTop()));
    }

    // ✅ البحث عن العلاج عبر DuckDuckGo API (مجاني، لا يحتاج مفتاح)
    private void searchTreatment(String symptoms) {
        String answer = null;
        try {
            // DuckDuckGo Instant Answer API — مجاني بدون مفتاح
            String urlStr = "https://api.duckduckgo.com/?q="
                    + URLEncoder.encode("علاج " + symptoms, "UTF-8")
                    + "&format=json&no_html=1&skip_disambig=1";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();

            String json = sb.toString();

            // استخراج الحقلين Abstract و Definition يدوياً (بدون مكتبة JSON)
            answer = extract(json, "AbstractText");
            if (answer == null || answer.isEmpty()) {
                answer = extract(json, "Definition");
            }
            if (answer == null || answer.isEmpty()) {
                answer = extract(json, "Abstract");
            }
        } catch (Exception e) {
            answer = null;
        }

        final String finalAnswer = answer;
        new Handler(Looper.getMainLooper()).post(() -> {
            searchButton.setEnabled(true);
            if (finalAnswer == null || finalAnswer.isEmpty()) {
                showFallbackDialog(symptoms);
            } else {
                resultText.setText("🔍 نتيجة البحث عن: " + symptoms + "\n\n"
                        + finalAnswer
                        + "\n\n⚠️ استشر طبيباً قبل تناول أي دواء.");
                scrollView.post(() -> scrollView.smoothScrollTo(0, resultText.getTop()));
            }
        });
    }

    // بديل: عرض خيارات لفتح المواقع الطبية
    private void showFallbackDialog(String symptoms) {
        new AlertDialog.Builder(this)
                .setTitle("لم نجد نتيجة مباشرة")
                .setMessage("هل تريد فتح بحث Google؟")
                .setPositiveButton("نعم", (d, w) -> {
                    try {
                        android.content.Intent i = new android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(
                                        "https://www.google.com/search?q="
                                                + URLEncoder.encode("علاج " + symptoms, "UTF-8")));
                        startActivity(i);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // استخراج قيمة حقل نصي من JSON بشكل بسيط
    private String extract(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);
            if (start < 0) return "";
            start += pattern.length();
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char n = json.charAt(i + 1);
                    if (n == 'n') sb.append('\n');
                    else if (n == 't') sb.append('\t');
                    else if (n == '"') sb.append('"');
                    else if (n == '\\') sb.append('\\');
                    else if (n == 'u') {
                        if (i + 5 < json.length()) {
                            sb.append((char) Integer.parseInt(
                                    json.substring(i + 2, i + 6), 16));
                            i += 4;
                        }
                    }
                    i++;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String normalize(String s) {
        return s.toLowerCase()
                .replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
                .replace('ى', 'ي').replace('ة', 'ه')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String classify(String s) {
        if (matchesAny(s, "الم صدر", "الم في الصدر", "الم بالصدر",
                "ضيق تنفس", "صعوبه التنفس", "صعوبة التنفس",
                "ازرقاق", "فقدان الوعي", "اغماء")) {
            return "⚠️ قد تكون هذه أعراض حالة طارئة.\n"
                    + "اتصل بالإسعاف أو اطلب الرعاية الطارئة الآن.";
        }
        if (matchesAny(s, "صداع", "دوخه", "دوخة", "دوار")) {
            return "الصداع والدوخة لهما أسباب متعددة.\n"
                    + "استشر مختصاً إذا استمرت الأعراض أو تكررت، "
                    + "واطلب المساعدة العاجلة إذا كانت شديدة أو مفاجئة.";
        }
        if (matchesAny(s, "حمي", "حمى", "حراره", "حرارة", "سعال", "كحه", "كحة")) {
            return "قد ترتبط هذه الأعراض بعدة أسباب.\n"
                    + "راقب الأعراض واستشر الطبيب عند استمرارها أو تفاقمها.";
        }
        return "لا يمكن تحديد سبب الأعراض من هذه المعلومات وحدها.\n"
                + "يرجى استشارة مختص صحي للحصول على تقييم مناسب.";
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String k : keywords) if (text.contains(k)) return true;
        return false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}