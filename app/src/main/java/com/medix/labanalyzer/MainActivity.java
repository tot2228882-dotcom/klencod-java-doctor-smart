EditText nameInput, symptomsInput;
TextView resultText;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Main layout
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(32, 32, 32, 32);
    layout.setBackgroundColor(Color.rgb(240, 248, 248));

    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(layout);

    // App title
    TextView title = new TextView(this);
    title.setText("🩺 طبيبي الذكي\nSmart Doctor AI");
    title.setTextSize(27);
    title.setTextColor(Color.rgb(0, 105, 100));
    title.setGravity(Gravity.CENTER);
    title.setPadding(0, 20, 0, 30);
    layout.addView(title);

    // Patient name
    nameInput = new EditText(this);
    nameInput.setHint("أدخل اسم المريض");
    layout.addView(nameInput);

    // Symptoms
    symptomsInput = new EditText(this);
    symptomsInput.setHint(
        "اكتب الأعراض، مثال: صداع ودوخة"
    );
    symptomsInput.setMinLines(3);
    symptomsInput.setGravity(Gravity.TOP);
    layout.addView(symptomsInput);

    // Analyze button
    Button analyzeButton = new Button(this);
    analyzeButton.setText("تحليل الأعراض");
    layout.addView(analyzeButton);

    // Result area
    resultText = new TextView(this);
    resultText.setTextSize(18);
    resultText.setTextColor(Color.DKGRAY);
    resultText.setPadding(0, 25, 0, 25);
    layout.addView(resultText);

    // Safety message
    TextView warning = new TextView(this);
    warning.setText(
        "تنبيه: النتائج إرشادية وليست تشخيصاً طبياً."
    );
    warning.setTextSize(14);
    warning.setTextColor(Color.RED);
    layout.addView(warning);

    setContentView(scrollView);

    analyzeButton.setOnClickListener(v -> analyzeSymptoms());
}

private void analyzeSymptoms() {

    String name = nameInput.getText()
            .toString().trim();

    String symptoms = symptomsInput.getText()
            .toString().trim().toLowerCase();

    if (symptoms.isEmpty()) {
        resultText.setText("يرجى كتابة الأعراض أولاً.");
        return;
    }

    String result;

    // Simple educational rules, not an AI diagnosis
    if (symptoms.contains("ألم صدر")
            || symptoms.contains("الم صدر")
            || symptoms.contains("ضيق تنفس")
            || symptoms.contains("صعوبة التنفس")
            || symptoms.contains("ألم بالصدر")) {

        result = "قد تكون هذه أعراض حالة طارئة.\n"
                + "اتصل بالإسعاف أو اطلب الرعاية الطارئة الآن.";

    } else if (symptoms.contains("صداع")
            || symptoms.contains("دوخة")) {

        result = "الصداع والدوخة لهما أسباب متعددة.\n"
                + "استشر مختصاً إذا استمرت الأعراض أو تكررت، "
                + "واطلب المساعدة العاجلة إذا كانت شديدة أو مفاجئة.";

    } else if (symptoms.contains("حمى")
            || symptoms.contains("حرارة")
            || symptoms.contains("سعال")
            || symptoms.contains("كحة")) {

        result = "قد ترتبط هذه الأعراض بعدة أسباب.\n"
                + "راقب الأعراض واستشر الطبيب عند استمرارها "
                + "أو تفاقمها.";

    } else {
        result = "لا يمكن تحديد سبب الأعراض من هذه المعلومات وحدها.\n"
                + "يرجى استشارة مختص صحي للحصول على تقييم مناسب.";
    }

    String patientName = name.isEmpty()
            ? "المريض" : name;

    resultText.setText(
            "مرحباً " + patientName + "\n\n"
            + "الأعراض: " + symptoms + "\n\n"
            + "الإرشادات الأولية:\n" + result
    );
}