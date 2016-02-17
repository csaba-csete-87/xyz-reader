package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class TestDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  private Cursor mCursor;
  private long mStartId;

  private long mSelectedItemId;
  private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;

  private SimpleImageLoadingListener articleHeaderImageLoadingListener = new SimpleImageLoadingListener() {

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
      ActivityCompat.startPostponedEnterTransition(TestDetailActivity.this);
    }

  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_detail);
    ActivityCompat.postponeEnterTransition(this);

    setupToolbar();
    setupFab();

    getLoaderManager().initLoader(0, null, this);
    if (savedInstanceState == null) {
      if (getIntent() != null && getIntent().getData() != null) {
        mStartId = ItemsContract.Items.getItemId(getIntent().getData());
        mSelectedItemId = mStartId;
      }
    }
  }

  private void setupFab() {
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.share_button);
    if (fab != null) {
      fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          shareArticle();
        }
      });
    }
  }

  private void setupToolbar() {
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.detail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_share) {
      shareArticle();
    }
    return super.onOptionsItemSelected(item);
  }

  private void shareArticle() {
    Intent shareIntent = getShareIntent();
    startActivity(shareIntent);
  }

  @NonNull
  private Intent getShareIntent() {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, getFullArticle());
    sendIntent.setType("text/plain");
    return sendIntent;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return ArticleLoader.newAllArticlesInstance(this);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    setCursor(cursor);

    setToolbarTitle();
    initArticle();
  }

  private void initArticle() {
    setHeaderImage();
    setArticleTitle();
    setArticleSubtitle();
    setArticleBody();
  }

  @Override
  public void onEnterAnimationComplete() {
    super.onEnterAnimationComplete();
    Animator animator = getUpwardScrollAnimator();
    animator.start();
  }

  @NonNull
  private ObjectAnimator getUpwardScrollAnimator() {
    return ObjectAnimator.ofInt(
        findViewById(R.id.article_container),
        "scrollY",
        getResources().getDimensionPixelSize(R.dimen.init_scroll_up_distance),
        0
    ).setDuration(500);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mCursor = null;
  }

  private void setToolbarTitle() {
    CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
    if (collapsingToolbarLayout != null) {
      collapsingToolbarLayout.setTitle(getArticleTitle());
    }
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

  private void setHeaderImage() {
    ImageView iv = (ImageView) findViewById(R.id.photo);
    ImageLoader.getInstance().displayImage(getArticlePhotoUrl(), iv, articleHeaderImageLoadingListener);
  }

  private void setArticleBody() {
    TextView body = (TextView) findViewById(R.id.article_body);
    body.setText(Html.fromHtml(getArticlePart(ArticleLoader.Query.BODY)));
  }

  private void setArticleSubtitle() {
    TextView t = (TextView) findViewById(R.id.article_subtitle);
    t.setText(Html.fromHtml(getArticleSubtitle()));
  }

  @NonNull
  private String getArticleSubtitle() {
    return getReadableDate() + getArticleAuthor();
  }

  @NonNull
  private String getReadableDate() {
    return DateUtils.getRelativeTimeSpanString(
        getArticleDate(),
        System.currentTimeMillis(),
        DateUtils.HOUR_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_ALL
    ).toString();
  }

  private long getArticleDate() {
    return mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE);
  }

  private void setArticleTitle() {
    TextView t = (TextView) findViewById(R.id.article_title);
    t.setText(getArticleTitle());
  }

  private String getArticleTitle() {
    return getArticlePart(ArticleLoader.Query.TITLE);
  }

  private String getArticleAuthor() {
    return getArticlePart(ArticleLoader.Query.AUTHOR);
  }

  private String getArticleBody() {
    return getArticlePart(ArticleLoader.Query.BODY);
  }

  private String getArticlePhotoUrl() {
    return getArticlePart(ArticleLoader.Query.PHOTO_URL);
  }

  private String getArticlePart(int part) {
    return mCursor.getString(part);
  }

  private String getFullArticle() {
    return getArticleTitle() + getNewLine() + getNewLine() + getArticleBody();
  }

  private String getNewLine() {
    return "//n";
  }

}
