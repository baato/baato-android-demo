package com.baato.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baato.baatoandroiddemo.adapters.SearchAdapter;
import com.example.baatoandroiddemo.R;
import com.baato.baatolibrary.models.SearchAPIResponse;
import com.baato.baatolibrary.services.BaatoSearch;

import static android.view.View.GONE;

/**
 * Type your query and then perform
 * search to retrieve the list of places
 */
public class SearchActivity extends AppCompatActivity {
    EditText etSearch;
    TextView errorMessage;
    ImageView btnClose;
    RecyclerView recyclerView;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearchQuery);
        errorMessage = findViewById(R.id.errorMessage);
        btnClose = findViewById(R.id.btnClose);
        recyclerView = findViewById(R.id.searchRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        setUpViews();
    }

    private void setUpViews() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (recyclerView.getItemDecorationCount() == 0)
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        btnClose.setOnClickListener(View -> {
            etSearch.setText("");
        });


        etSearch.requestFocus();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkEmpty(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void checkEmpty(CharSequence s) {
        if (!s.toString().isEmpty()) {
            btnClose.setVisibility(View.VISIBLE);
            searchTheQuery(s.toString());
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            btnClose.setVisibility(GONE);
            recyclerView.setVisibility(GONE);
            showKeyboard(etSearch);
        }
    }

    private void showKeyboard(EditText etSearch) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }


    /**
     * This method is used to perform search where the user inputs any query address.
     *
     * @param query The query to use for the search
     */
    private void searchTheQuery(String query) {
        recyclerView.setVisibility(GONE);
        progressBar.setVisibility(View.VISIBLE);
        new BaatoSearch(this)
                .setAccessToken(getString(R.string.baato_access_token))
                .setQuery(query)
                .withListener(new BaatoSearch.BaatoSearchRequestListener() {
                    @Override
                    public void onSuccess(SearchAPIResponse places) {
                        // get the list of search results and add it to the recycler view adapter
                        if (places.getData() != null && places.getData().size() > 0) {
                            hideErrorMessage();
                            Log.d("TAG", "onSuccess: "+places.getData());
                            recyclerView.setAdapter(new SearchAdapter(places.getData(), SearchActivity.this));
                        } else
                            showErrorMessage("Empty results!\nCouldn't find any matching results for the " + query);
                        progressBar.setVisibility(GONE);
                    }

                    @Override
                    public void onFailed(Throwable error) {
                        // get the error messages here
                        Toast.makeText(SearchActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .doRequest();
    }

    private void hideErrorMessage() {
        errorMessage.setVisibility(GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String s) {
        errorMessage.setText(s);
        errorMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(GONE);
    }
}
