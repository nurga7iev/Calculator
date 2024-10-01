package com.example.calculator;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    TextView tvExp, tvRes;
    MaterialButton btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    MaterialButton btn_c, btnOpenBrackets, btnCloseBrackets, btnDivede;
    MaterialButton ymn, minus, plus, ac, to, raven;

    int openBracketsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvRes = findViewById(R.id.tvResult);
        tvExp = findViewById(R.id.tvExpression);

        initButton(btn0, R.id.btn0);
        initButton(btn1, R.id.btn1);
        initButton(btn2, R.id.btn2);
        initButton(btn3, R.id.btn3);
        initButton(btn4, R.id.btn4);
        initButton(btn5, R.id.btn5);
        initButton(btn6, R.id.btn6);
        initButton(btn7, R.id.btn7);
        initButton(btn8, R.id.btn8);
        initButton(btn9, R.id.btn9);
        initButton(btn_c, R.id.btn_c);
        initButton(btnOpenBrackets, R.id.btnOpenBrackets);
        initButton(btnCloseBrackets, R.id.btnCloseBrackets);
        initButton(btnDivede, R.id.btnDivede);
        initButton(ymn, R.id.ymn);
        initButton(minus, R.id.minus);
        initButton(plus, R.id.plus);
        initButton(ac, R.id.ac);
        initButton(to, R.id.to);
        initButton(raven, R.id.raven);

        tvExp.setText("");
    }

    void initButton(MaterialButton button, int id) {
        button = findViewById(id);
        button.setOnClickListener(this::onClick);
    }

    public void onClick(View v) {
        MaterialButton button = (MaterialButton) v;
        String btnText = button.getText().toString();
        String data = tvExp.getText().toString();

        if (btnText.equals("AC")) {
            tvExp.setText("");
            tvRes.setText("");
            openBracketsCount = 0; // сброс счетчика скобок
            return;
        }

        if (btnText.equals("C")) {
            if (data.length() != 0 && !data.equals("0")) {
                // Удаление последнего символа и обновление счетчика скобок
                char lastChar = data.charAt(data.length() - 1);
                if (lastChar == '(') openBracketsCount--;
                if (lastChar == ')') openBracketsCount++;
                data = data.substring(0, data.length() - 1);
            }
            tvExp.setText(data);
            return;
        }

        if (btnText.equals("=")) {
            if (validateExpression(data)) {
                tvExp.setText(tvRes.getText());
            } else {
                tvRes.setText("error");
            }
            return;
        }

        if (isOperator(btnText) && (data.isEmpty() || isOperator(String.valueOf(data.charAt(data.length() - 1))))) {
            // Запрещаем дублирование операторов
            return;
        }

        if (btnText.equals("0") && (data.isEmpty() || isOperator(String.valueOf(data.charAt(data.length() - 1))))) {
            // Запрещаем ведущие нули
            return;
        }

        if (btnText.equals("(")) {
            openBracketsCount++;
        }

        if (btnText.equals(")")) {
            if (openBracketsCount <= 0 || isOperator(String.valueOf(data.charAt(data.length() - 1)))) {
                // Не даем закрывать скобку без открывающей или после оператора
                return;
            }
            openBracketsCount--;
        }

        data += btnText;
        tvExp.setText(data);

        // Проверяем выражение на правильность и вычисляем результат
        String finalResult = evaluateExpression(data);
        if (!finalResult.equals("error"))
            tvRes.setText(finalResult);

        Log.i("result", finalResult);
    }

    private String evaluateExpression(String expression) {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();
            String result = rhino.evaluateString(scope, expression, "JavaScript", 1, null).toString();
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            return decimalFormat.format(Double.parseDouble(result));
        } catch (Exception e) {
            return "error";
        } finally {
            Context.exit();
        }
    }

    private boolean isOperator(String str) {
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/");
    }

    private boolean validateExpression(String expression) {
        // Проверка на парность скобок
        Stack<Character> stack = new Stack<>();
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false; // Несбалансированные скобки
                }
            }
        }
        if (!stack.isEmpty()) return false; // Непарные открывающие скобки

        // Проверка на наличие дублирующихся операторов и другие ошибки
        for (int i = 0; i < expression.length() - 1; i++) {
            char current = expression.charAt(i);
            char next = expression.charAt(i + 1);
            if (isOperator(String.valueOf(current)) && isOperator(String.valueOf(next))) {
                return false; // Дублирующиеся операторы
            }
        }

        return true; // Если все проверки пройдены
    }
}
