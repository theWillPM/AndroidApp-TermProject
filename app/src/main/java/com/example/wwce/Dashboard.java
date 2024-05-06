package com.example.wwce;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Dashboard extends AppCompatActivity {

    static Map<String, Integer> currencyToFlagMap;
//    private ImageView flag;
    private Spinner dropdown;
    private Spinner dropdown2;
    String username;
    // although it says not used, json is necessary for the API call
    private JsonObject json;
    private TextView txt_username;
    private RecyclerView recyclerView;
    private CustomAdapter c_adapter;
    private List<ItemRow> dataList = new ArrayList<>();
    private Button btn_add_currency;
    private String currentCurrency;
    private Double currentBaseRate = 1.0;
    private Map<String, Double> exchangeRates;
    private EditText amount;
    private TextView current_currency_text;
    private TextView conversion_result;
    private TextView conversion_helper_text;
    Button savePdfButton;
    TextView target_currency;

    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Intent i = getIntent();

        // Parse info from intent
        username = i.getStringExtra("user");

        // Load rates from local storage
        exchangeRates = UserPreferences.loadCurrencyMap();
        Log.d("Rates", "Rates from user: " + exchangeRates);

        // Get user
        currentUser = getUserByUsername(username);

        // Find our elements that we are going to use:
        txt_username = findViewById(R.id.txt_username);
        dropdown = findViewById(R.id.dropdown_currency);
        dropdown2 = findViewById(R.id.dropdown2);

        // flag is now only used as a support for the spinners
        // flag = findViewById(R.id.img_flag);

        // declare our views
        recyclerView = findViewById(R.id.recycler_rates);
        btn_add_currency = findViewById(R.id.btn_add_currency);
        current_currency_text = findViewById(R.id.textView_current_currency);
        conversion_result = findViewById(R.id.textView_conversion_result);
        conversion_helper_text = findViewById(R.id.txt_conversion_helper);
        target_currency = findViewById(R.id.textView_target_currency);
        savePdfButton = findViewById(R.id.btn_save_pdf);
        amount = findViewById(R.id.input_amount);

        // add listener to btn savePdf
        savePdfButton.setOnClickListener(v -> createPdf());

        // set event listener to amount input
        amount.addTextChangedListener(amountTextWatcher);

        // set the username's text value to match the logged user
        txt_username.setText(username);

        // the API key
        String apiUrl = "https://v6.exchangerate-api.com/v6/4e55bdd6c6676cc469062ae7/latest/USD";

        // Make an API request
        ExchangeRateAPI exchangeRateAPI = new ExchangeRateAPI(apiUrl,
                jsonObject -> {
                    if (jsonObject != null && jsonObject.has("result")) {
                        String result = jsonObject.get("result").getAsString();
                        // Check if the result is success and proceed accordingly
                        if (result.equals("success")) {
                            json = jsonObject;

                            // Save Exchange Rates:
                            exchangeRates = extractConversionRates(jsonObject);
                            Log.d("Rates", exchangeRates.toString());

                            // Save SharedPreferences
                            UserPreferences.saveUserList(User.userList);
                            UserPreferences.saveCurrencyMap(exchangeRates);
                            Toast.makeText(this, "Updated rates from API",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // I couldn't find any replacements for this (didn't try enough to be honest - sorry, Pedro)
        exchangeRateAPI.execute();

        // Initialize the options list (To be expanded in the future - maybe in 2059)
        String[] items = new String[]{
                "USD", "EUR", "JPY", "GBP", "CAD", "CNY", "INR", "RUB", "BRL"
        };

        // setting the spinner layout adapter
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_item,
                Arrays.asList(items));
        c_adapter = new CustomAdapter(this, dataList, recyclerView);

        // setup the list view (recyclerview)
        setupRecyclerView();

        // Populate dataList with user's rates
        updateDataListWithUserRates();

        setupItemTouchHelper();
        dropdown.setAdapter(adapter);
        dropdown2.setAdapter(adapter);
        target_currency.setText(dropdown2.getSelectedItem().toString());

        // Handle the dropdown selection
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected currency code
                currentCurrency = items[position];

                // Update the ImageView with the corresponding flag drawable resource
                int flagDrawable = currencyToFlagMap.get(currentCurrency);
//                flag.setImageResource(flagDrawable);

                // Change base calculation rate:
                currentBaseRate = getExchangeRateForCurrency(currentCurrency);

                // Update all items in dataList with the new exchange rate
                updateDataListWithNewRate();

                // update the currency texts
                current_currency_text.setText(currentCurrency);
                target_currency.setText(dropdown2.getSelectedItem().toString());

                // Update the converted amount
                String amountText = amount.getText().toString();
                double enteredAmount = TextUtils.isEmpty(amountText) ? 0.0 : Double.parseDouble(amountText);
                updateConvertedAmounts(enteredAmount);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected (optional)
            }
        });

        // same thing for the other dropdwn
        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected currency
                String selectedCurrency = dropdown2.getSelectedItem().toString();

                // Get the entered amount
                String amountText = amount.getText().toString();
                double enteredAmount = TextUtils.isEmpty(amountText) ? 0.0 : Double.parseDouble(amountText);

                // Update the converted amount and text
                updateConvertedAmounts(enteredAmount);
                target_currency.setText(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed for this implementation
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(c_adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(c_adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        btn_add_currency.setOnClickListener(v -> {
            // Create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);

            // Set title for the dialog
            builder.setTitle("Add Currency");

            // Create a new Spinner and populate it with currency options
            final Spinner currencySpinner = new Spinner(Dashboard.this);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(Dashboard.this, android.R.layout.simple_spinner_item, items);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(adapter1);
            builder.setView(currencySpinner);

            // Set positive button and define its behavior
            builder.setPositiveButton("Add", (dialog, which) -> {
                // Get the selected currency name from the Spinner
                String currencyName = currencySpinner.getSelectedItem().toString();

                // Check if the selected currency is already in currentUser.rates
                if (currentUser.rates.contains(currencyName)) {
                    // If currency already selected, show a Toast message
                    Toast.makeText(getApplicationContext(), "Currency already selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the exchange rate for the selected currency
                Double currencyRate = getExchangeRateForCurrency(currencyName);

                // add the new currency to the top dataList
                String value1 = String.format(Locale.CANADA, "%.3f", currentBaseRate / currencyRate);
                ItemRow newItem = new ItemRow(currencyName, value1, 1, currentCurrency);
                dataList.add(newItem);

                // Add the selected currency to currentUser.rates
                currentUser.rates.add(currencyName);
                updateRatesInUserList(currentUser.rates);
                UserPreferences.saveUserList(User.userList);
                // I couldn't implement the 'more-specific thing the documentation suggests'
                c_adapter.notifyDataSetChanged();
            });

            // Set negative button and define its behavior
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Dismiss the dialog
                dialog.cancel();
            });

            // Show the AlertDialog
            builder.show();
        });

        // Initialize the map with currency code to flag resource mappings
        initializeCurrencyToFlagMap();

//        flag.setImageResource(currencyToFlagMap.get("BRL"));

        View rootLayout = findViewById(R.id.root_layout);

        // Add touch listener to root layout
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard when touch event occurs outside of the input field
                hideKeyboard(Dashboard.this);

                // Clear focus from the input field
                clearFocusFromInputField(v);

                // Request focus for the root layout to ensure that the input field loses focus
                rootLayout.requestFocus();

                return false;
            }
        });
        txt_username.setOnClickListener(v -> {
            // Create a custom layout for the AlertDialog
            View dialogView =
                    LayoutInflater.from(Dashboard.this).inflate(R.layout.custom_dialog, null);

            // Initialize views in the custom layout
            TextView messageTextView = dialogView.findViewById(R.id.message_text_view);
            Button yesButton = dialogView.findViewById(R.id.yes_button);
            Button noButton = dialogView.findViewById(R.id.no_button);

            // Set message text
            messageTextView.setText(R.string.logout);

            // Create a AlertDialog.Builder instance
            AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);

            // Set the custom layout to the AlertDialog
            builder.setView(dialogView);

            // Create and show the AlertDialog
            final AlertDialog dialog = builder.create();
            dialog.show();

            // Set onClick listener for Yes button
            yesButton.setOnClickListener(v1 -> {
                // User clicked "Yes", so log out
                UserPreferences.saveCredentials("","");
                goToMain(v1);
                dialog.dismiss();
            });

            // Set onClick listener for No button
            noButton.setOnClickListener(v12 -> {
                // User clicked "No", so dismiss the dialog
                dialog.dismiss();
            });
        });
    }

    public void updateRatesInUserList(Set<String> newRates) {
        if (User.userList != null) {
            for (User user : User.userList) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    // Found the user with the desired username
                    user.setUserRates(newRates);
                    // update user list
                    UserPreferences.saveUserList(User.userList);
                    return;
                }
            }
        }
    }

    private void updateDataListWithUserRates() {
        // Clear existing data from dataList
        dataList.clear();

        // Check if currentUser is not null
        if (currentUser != null) {
            // Get user's rates
            Set<String> userRates = currentUser.rates;

            // Check if userRates is not null
            if (userRates != null) {
                // Populate dataList with user's rates
                for (String currency : userRates) {
                    // Get the exchange rate for the current currency
                    Double currencyRate = getExchangeRateForCurrency(currency);

                    // Check if currencyRate is not null and currentBaseRate is not null
                    if (currentBaseRate != null) {
                        // Calculate value1 based on the current currency rate
                        String value1 = String.format(Locale.CANADA, "%.3f",
                                currentBaseRate / currencyRate );

                        // Create a new ItemRow with the currency and calculated value
                        ItemRow newItem = new ItemRow(currency, value1, 1, currentCurrency);

                        // Add the new item to dataList
                        dataList.add(newItem);
                    }
                }
            }
        }

        // Notify the adapter that the dataset has changed
        c_adapter.notifyDataSetChanged();
    }


    private double getExchangeRateForCurrency(String currency) {
        if (exchangeRates != null && exchangeRates.containsKey(currency)) {
            Log.d("Rates", currency);
            return 1/exchangeRates.get(currency);
        } else {
            Log.d("Rates", "Not found: " + currency + " " + exchangeRates);
            return 0.0; // Or whatever default value you want to return
        }
    }

    public static Map<String, Double> extractConversionRates(JsonObject jsonObject) {
        Map<String, Double> conversionRatesMap = new HashMap<>();
        if (jsonObject.has("conversion_rates")) {
            JsonObject conversionRatesJson = jsonObject.getAsJsonObject("conversion_rates");

            for (Map.Entry<String, JsonElement> entry : conversionRatesJson.entrySet()) {
                String currency = entry.getKey();
                Double rate = conversionRatesJson.get(currency).getAsDouble();
                conversionRatesMap.put(currency, rate);
            }
        }
        return conversionRatesMap;
    }

    private void updateDataListWithNewRate() {
        for (ItemRow item : dataList) {
            // Get the exchange rate for the current item's currency
            Double currencyRate = getExchangeRateForCurrency(item.getName());

            // Calculate the new value1 for the item based on the current currency rate
            String value1 = String.format(Locale.CANADA, "%.3f", currentBaseRate/currencyRate);

            // Update the item's value1
            item.setValue1(value1);

            // Update the target currency to match currentCurrency:
            item.setName2(currentCurrency);
        }

        // Notify the adapter that the dataset has changed
        c_adapter.notifyDataSetChanged();
    }
    private User getUserByUsername(String username) {
        if (User.userList != null) {
            for (User user : User.userList) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    return user;
                }
            }
        }
        // User not found
        return null;
    }
    // Method to hide keyboard
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Method to clear focus from input field
    private void clearFocusFromInputField(View view) {
        if (view instanceof EditText) {
            view.clearFocus();
        }
    }

    private void initializeCurrencyToFlagMap() {
        currencyToFlagMap = new HashMap<>();


        // Add mappings for the 9 most popular currency codes to flag resources

        //<a href="https://www.flaticon.com/free-icons/flag" title="flag icons">Flag icons
        // created by Freepik - Flaticon</a>
        currencyToFlagMap.put("USD", R.drawable.us);

        //<a href="https://www.flaticon.com/free-icons/europe" title="europe icons">Europe icons
        // created by Indielogy - Flaticon</a>
        currencyToFlagMap.put("EUR", R.drawable.eur);

        //<a href="https://www.flaticon.com/free-icons/japan" title="japan icons">Japan icons created by Smashicons - Flaticon</a>
        currencyToFlagMap.put("JPY", R.drawable.japan);

        //<a href="https://www.flaticon.com/free-icons/uk-flag" title="uk flag icons">Uk flag icons created by Freepik - Flaticon</a>
        currencyToFlagMap.put("GBP", R.drawable.gb);

        //<a href="https://www.flaticon.com/free-icons/flags" title="flags icons">Flags icons
        // created by Freepik - Flaticon</a>
        currencyToFlagMap.put("CAD", R.drawable.canada);

        //<a href="https://www.flaticon.com/free-icons/china" title="china icons">China icons created by IconsBox - Flaticon</a>
        currencyToFlagMap.put("CNY", R.drawable.china);

        //<a href="https://www.flaticon.com/free-icons/india" title="india icons">India icons created by Freepik - Flaticon</a>
        currencyToFlagMap.put("INR", R.drawable.india);

        //<a href="https://www.flaticon.com/free-icons/russia" title="russia icons">Russia icons created by Freepik - Flaticon</a>
        currencyToFlagMap.put("RUB", R.drawable.russia);

        //<a href="https://www.flaticon.com/free-icons/brazil" title="brazil icons">Brazil icons created by Freepik - Flaticon</a>
        currencyToFlagMap.put("BRL", R.drawable.brazil);

    }
    private void setupRecyclerView() {
        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        c_adapter = new CustomAdapter(this, dataList, recyclerView);
        recyclerView.setAdapter(c_adapter);
    }

    /**
     * Handling the remove-on-swipe behavior that I suggested:
     * Paints a red rectangle on the item that is being swiped and then removes it.
     */
    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Remove the item from the dataset
                int position = viewHolder.getAdapterPosition();
                String name = dataList.get(position).getName();
                dataList.remove(position);
                Toast.makeText(Dashboard.this, "Removed " + name,
                        Toast.LENGTH_SHORT).show();
                // Notify the adapter that the item has been removed
                currentUser.rates.remove(name);
                UserPreferences.saveUserList(User.userList);
                c_adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Get the bounds of the itemView
                View itemView = viewHolder.itemView;
                int left = itemView.getLeft();
                int top = itemView.getTop();
                int right = itemView.getRight();
                int bottom = itemView.getBottom();

                // Define the paint for drawing the gradient
                Paint paint = new Paint();
                int colorWhite = getColor(R.color.white);
                Shader shader = new LinearGradient(left, top, right, bottom, colorWhite, Color.RED,
                        Shader.TileMode.CLAMP);
                paint.setShader(shader);

                // Draw the gradient background along the swipe path
                c.drawRect(left, top, right, bottom, paint);

                // Call super method for default behavior
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    // Define a TextWatcher for the amount EditText
    TextWatcher amountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed for this implementation
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not needed for this implementation
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Get the entered amount text
            String amountText = s.toString();

            // Remove any non-numeric characters from the amount text
            amountText = amountText.replaceAll("[^\\d.]", "");

            // Parse the cleaned amount text as a double
            double enteredAmount = TextUtils.isEmpty(amountText) ? 0.0 : Double.parseDouble(amountText);

            // Update the converted amounts and the helper text view
            updateConvertedAmounts(enteredAmount);
            conversion_helper_text.setText(String.valueOf(enteredAmount));
        }
    };
    private void updateConvertedAmounts(double enteredAmount) {
        // Get the selected currency from the dropdown spinner
        String selectedCurrency = dropdown2.getSelectedItem().toString();

        // Get the conversion rate for the selected currency
        double conversionRate = getExchangeRateForCurrency(selectedCurrency);

        // Calculate the converted amount
        double convertedAmount = (currentBaseRate / conversionRate) * enteredAmount;

        // Update the text view with the converted amount
        conversion_result.setText(String.format(Locale.CANADA, "%.3f", convertedAmount));
    }
    private void createPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        View content = findViewById(R.id.root_layout);

        // Get the dimensions of the content layout
        int contentWidth = content.getWidth();
        int contentHeight = content.getHeight();

        // Create a page info with the content dimensions
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(contentWidth, contentHeight, 1).create();
        // Start a page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Draw content on the page
        content.draw(page.getCanvas());

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the document
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy_MM_dd_HH_mm", Locale.getDefault());
            Date now = new Date();
            // Format the date and time
            String formattedDateTime = dateFormat.format(now);
            String filePath = username + formattedDateTime + ".pdf";
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, filePath);
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }

        // Close the document
        pdfDocument.close();
    }

    /**
     * return to main view and cancel loading user
     * @param v view
     */
    public void goToMain(View v) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("loadUser", false);
        startActivity(i);
    }
}


