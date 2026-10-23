package com.medix.labanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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

        // العنوان
        TextView title = new TextView(this);
        title.setText("🩺 طبيبي الذكي\nSmart Doctor AI");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(0, 105, 100));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(20), 0, dp(30));
        layout.addView(title);

        // ✅ حقل الاسم - خلفية داكنة وخط أبيض غامق
        nameInput = new EditText(this);
        nameInput.setHint("أدخل اسم المريض");
        nameInput.setHintTextColor(Color.LTGRAY);
        nameInput.setTextColor(Color.WHITE);
        nameInput.setTypeface(null, Typeface.BOLD); // خط غامق
        nameInput.setTextSize(18);
        nameInput.setBackgroundColor(Color.rgb(60, 60, 60)); // خلفية داكنة
        nameInput.setPadding(dp(16), dp(16), dp(16), dp(16));
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setSingleLine(true);
        layout.addView(nameInput);

        // ✅ حقل الأعراض - خلفية داكنة وخط أبيض غامق
        symptomsInput = new EditText(this);
        symptomsInput.setHint("اكتب الأعراض، مثال: صداع ودوخة");
        symptomsInput.setHintTextColor(Color.LTGRAY);
        symptomsInput.setTextColor(Color.WHITE);
        symptomsInput.setTypeface(null, Typeface.BOLD);
        symptomsInput.setTextSize(18);
        symptomsInput.setBackgroundColor(Color.rgb(60, 60, 60));
        symptomsInput.setPadding(dp(16), dp(16), dp(16), dp(16));
        symptomsInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        symptomsInput.setMinLines(3);
        symptomsInput.setGravity(Gravity.TOP | Gravity.START);
        layout.addView(symptomsInput);

        // زر التحليل
        Button analyzeButton = new Button(this);
        analyzeButton.setText("تحليل الأعراض");
        layout.addView(analyzeButton);

        // ✅ زر البحث في Google
        searchButton = new Button(this);
        searchButton.setText("🔍 ابحث عن العلاج في Google");
        searchButton.setVisibility(Button.GONE);
        layout.addView(searchButton);

        // النتيجة
        resultText = new TextView(this);
        resultText.setTextSize(18);
        resultText.setTextColor(Color.DKGRAY);
        resultText.setPadding(0, dp(25), 0, dp(25));
        resultText.setTextIsSelectable(true);
        layout.addView(resultText);

        // تحذير
        TextView warning = new TextView(this);
        warning.setText("⚠️ تنبيه: النتائج إرشادية وليست تشخيصاً طبياً.\n"
                + "في حالات الطوارئ اتصل بالإسعاف فوراً.");
        warning.setTextSize(14);
        warning.setTextColor(Color.RED);
        layout.addView(warning);

        setContentView(scrollView);

        analyzeButton.setOnClickListener(v -> analyzeSymptoms());

        // ✅ عند الضغط: يفتح Google للبحث عن العلاج
        searchButton.setOnClickListener(v -> {
            if (lastSymptoms.isEmpty()) return;
            
            // بحث Google عن "علاج + الأعراض"
            String query = "علاج " + lastSymptoms;
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q="
                            + Uri.encode(query)));
            startActivity(intent);
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

        // إظهار زر البحث عن العلاج
        searchButton.setVisibility(Button.VISIBLE);
        
        scrollView.post(() -> scrollView.smoothScrollTo(0, resultText.getTop()));
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