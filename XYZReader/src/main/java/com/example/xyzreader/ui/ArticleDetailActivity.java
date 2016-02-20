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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final long FAB_ANIMATION_DURATION = 200;
    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;

    private long mArticleId;
    private FloatingActionButton fab;
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ActivityCompat.postponeEnterTransition(this);

        setupToolbar();
        setupFab();

        imageView = (ImageView) findViewById(R.id.photo);
        mArticleId = getIntent().getLongExtra("articleId", 0);

        setupFullBleedIfNecessary();

        getLoaderManager().initLoader(0, null, this);
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }

        AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (collapsingToolbarLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbarLayout)) {
                    fab.animate().scaleX(0).setDuration(FAB_ANIMATION_DURATION);
                    fab.animate().scaleY(0).setDuration(FAB_ANIMATION_DURATION);
                } else {
                    fab.animate().scaleX(1).setDuration(FAB_ANIMATION_DURATION);
                    fab.animate().scaleY(1).setDuration(FAB_ANIMATION_DURATION);
                }
            }
        };
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(mListener);
    }

    @Override
    public void onBackPressed() {
//        finishAfterTransition();
//        supportFinishAfterTransition();
        finish();
    }

    private void setupFullBleedIfNecessary() {
        if (getResources().getBoolean(R.bool.shouldShowFullBleedImage)) {
            NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.article_scroll_container);
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) imageView.getLayoutParams();

                    setNewImageHeight(scrollY, oldScrollY, layoutParams);

                    imageView.setLayoutParams(layoutParams);
                }
            });
        }
    }

    private void setNewImageHeight(int scrollY, int oldScrollY, CoordinatorLayout.LayoutParams layoutParams) {
        float dy = (float) (oldScrollY - scrollY) / 2;
        int height = layoutParams.height;
        height += dy;
        if (height <= getActionBarHeight())
            layoutParams.height = getActionBarHeight();
        else
            layoutParams.height = height;
    }

    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private void setupFab() {
        fab = (FloatingActionButton) findViewById(R.id.share_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareArticle();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
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
        return ArticleLoader.newInstanceForItemId(this, mArticleId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setCursor(cursor);

        setToolbarTitle();
        initArticle();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
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

    private void setToolbarTitle() {
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(getArticleTitle());
        }
    }

    private void setCursor(Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e("TAG", "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
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

    private String getArticleSubtitle() {
        return String.format("%s %s", getReadableDate(), getArticleAuthor());
    }

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

    private SimpleImageLoadingListener articleHeaderImageLoadingListener = new SimpleImageLoadingListener() {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ActivityCompat.startPostponedEnterTransition(ArticleDetailActivity.this);
        }

    };

}
