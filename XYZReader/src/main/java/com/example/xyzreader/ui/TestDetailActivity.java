package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class TestDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_detail);
        ActivityCompat.postponeEnterTransition(this);

        getLoaderManager().initLoader(0, null, this);
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
        setResult(Activity.RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finish();
//        finishAfterTransition();
    }

    @Override
    public boolean onNavigateUp() {
//        finishAfterTransition();
        finish();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setCursor(cursor);

        setTitle();
        setHeaderImage();
        setBody();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Do Share", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void setCursor(Cursor cursor) {
        mCursor = cursor;

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mCursor.moveToPosition(position);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    private void setTitle() {
        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout)).setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
    }

    private void setHeaderImage() {
        ImageView iv = (ImageView) findViewById(R.id.photo);
        ImageLoader.getInstance().displayImage(mCursor.getString(ArticleLoader.Query.PHOTO_URL), iv, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ActivityCompat.startPostponedEnterTransition(TestDetailActivity.this);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    private void setBody() {
        TextView t = (TextView) findViewById(R.id.article_body);
        t.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

}
