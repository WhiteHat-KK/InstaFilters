package kk.techbytecare.instafilters;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.IOException;
import java.util.List;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import kk.techbytecare.instafilters.Adapter.ViewPagerAdapter;
import kk.techbytecare.instafilters.Fragments.AddTextFragment;
import kk.techbytecare.instafilters.Fragments.BrushFragment;
import kk.techbytecare.instafilters.Fragments.EditFragment;
import kk.techbytecare.instafilters.Fragments.EmojiFragment;
import kk.techbytecare.instafilters.Fragments.FilterListFragment;
import kk.techbytecare.instafilters.Fragments.FrameFragment;
import kk.techbytecare.instafilters.Interface.AddFrameListener;
import kk.techbytecare.instafilters.Interface.AddTextFragmentListener;
import kk.techbytecare.instafilters.Interface.BrushFragmentListener;
import kk.techbytecare.instafilters.Interface.EditImageFragmentListener;
import kk.techbytecare.instafilters.Interface.EmojiFragmentListener;
import kk.techbytecare.instafilters.Interface.FilterListFragmentListener;
import kk.techbytecare.instafilters.Utils.BitmapUtils;

public class MainActivity extends AppCompatActivity implements FilterListFragmentListener,
        EditImageFragmentListener, BrushFragmentListener, EmojiFragmentListener, AddTextFragmentListener, AddFrameListener {

    public static final String PIC_NAME = "flash.jpg";

    public static final int PERMISSION_PICK_IMAGE = 1000;
    public static final int PERMISSION_INSERT_IMAGE = 5152;

    PhotoEditorView photoEditorView;
    PhotoEditor photoEditor;

    CardView btn_filter_list,btn_edit,btn_brush,btn_emoji,btn_add_text,btn_add_image,btn_add_frame;

    CoordinatorLayout coordinatorLayout;

    Bitmap originalBitmap,filteredBitmap,finalBitmap;

    FilterListFragment filterListFragment;
    EditFragment editFragment;

    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float constraintFinal = 1.0f;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image Filters");

        photoEditorView = findViewById(R.id.image_preview);

        photoEditor = new PhotoEditor.Builder(this,photoEditorView)
                .setPinchTextScalable(true)
                .setDefaultEmojiTypeface(Typeface.createFromAsset(getAssets(),"emojione-android.ttf"))
                .build();

        coordinatorLayout = findViewById(R.id.coordinator);

        btn_filter_list = findViewById(R.id.btn_filter_list);
        btn_edit = findViewById(R.id.btn_edit);
        btn_brush = findViewById(R.id.btn_brush);
        btn_emoji = findViewById(R.id.btn_emoji);
        btn_add_text = findViewById(R.id.btn_add_text);
        btn_add_image = findViewById(R.id.btn_image);
        btn_add_frame = findViewById(R.id.btn_add_frame);

        btn_filter_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterListFragment filterListFragment = FilterListFragment.getInstance();
                filterListFragment.setListener(MainActivity.this);
                filterListFragment.show(getSupportFragmentManager(),filterListFragment.getTag());
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditFragment editFragment = EditFragment.getInstance();
                editFragment.setListener(MainActivity.this);
                editFragment.show(getSupportFragmentManager(),editFragment.getTag());
            }
        });

        btn_brush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                photoEditor.setBrushDrawingMode(true);

                BrushFragment brushFragment = BrushFragment.getInstance();
                brushFragment.setListener(MainActivity.this);
                brushFragment.show(getSupportFragmentManager(),brushFragment.getTag());
            }
        });

        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmojiFragment emojiFragment = EmojiFragment.getInstance();
                emojiFragment.setListener(MainActivity.this);
                emojiFragment.show(getSupportFragmentManager(),emojiFragment.getTag());
            }
        });
        btn_add_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTextFragment addTextFragment = AddTextFragment.getInstance();
                addTextFragment.setListener(MainActivity.this);
                addTextFragment.show(getSupportFragmentManager(),addTextFragment.getTag());
            }
        });
        btn_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImageToPicture();
            }
        });

        btn_add_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameFragment frameFragment = FrameFragment.getInstance();
                frameFragment.setListener(MainActivity.this);
                frameFragment.show(getSupportFragmentManager(),frameFragment.getTag());
            }
        });

        loadImage();
    }

    private void addImageToPicture() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted())  {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent,PERMISSION_INSERT_IMAGE);
                        }
                        else    {
                            Toast.makeText(MainActivity.this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void loadImage() {
        originalBitmap = BitmapUtils.getBitmapFromAssets(this,PIC_NAME,300,300);
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        photoEditorView.getSource().setImageBitmap(originalBitmap);
    }

    private void setUpViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filterListFragment = new FilterListFragment();
        filterListFragment.setListener(this);

        editFragment = new EditFragment();
        editFragment.setListener(this);

        adapter.addFragment(filterListFragment,"FILTERS");
        adapter.addFragment(editFragment,"EDIT");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFilerSelected(Filter filter) {
        resetControl();

        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        photoEditorView.getSource().setImageBitmap(filter.processFilter(filteredBitmap));

        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,true);
    }

    private void resetControl() {
        if (editFragment != null)   {
            editFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        constraintFinal = 1.0f;
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onContrastChanged(float contrast) {
        constraintFinal = contrast;

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        Bitmap bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        myFilter.addSubFilter(new ContrastSubFilter(constraintFinal));

        finalBitmap = myFilter.processFilter(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }
        else if (id == R.id.action_save)    {
            saveImageToGallery();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted())  {

                            photoEditor.saveAsBitmap(new OnSaveBitmap() {
                                @Override
                                public void onBitmapReady(Bitmap saveBitmap) {
                                    try {

                                        photoEditorView.getSource().setImageBitmap(saveBitmap);

                                        final String path = BitmapUtils.insertImage(getContentResolver(),
                                                saveBitmap,
                                                System.currentTimeMillis()+"_profile.jpg",
                                                null);

                                        if (!TextUtils.isEmpty(path))   {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                    "Image Saved to Gallery",
                                                    Snackbar.LENGTH_LONG)
                                                    .setAction("OPEN", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            openImage(path);
                                                        }
                                                    });
                                            snackbar.show();
                                        }
                                        else    {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                    "Unable to Save Image...",
                                                    Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else    {
                            Toast.makeText(MainActivity.this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openImage(String path) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path),"image/*");
        startActivity(intent);
    }

    private void openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted())  {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent,PERMISSION_PICK_IMAGE);
                        }
                        else    {
                            Toast.makeText(MainActivity.this, "Permission Denied....", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == PERMISSION_PICK_IMAGE) {

                Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

                originalBitmap.recycle();
                finalBitmap.recycle();
                filteredBitmap.recycle();

                originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

                photoEditorView.getSource().setImageBitmap(originalBitmap);
                bitmap.recycle();

                //filterListFragment.displayThumbnail(originalBitmap);
            }
            else if (requestCode == PERMISSION_INSERT_IMAGE)    {

                Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this,data.getData(),250,250);

                photoEditor.addImage(bitmap);
            }
        }
    }

    @Override
    public void onBrushSizeChanged(float size) {
        photoEditor.setBrushSize(size);
    }

    @Override
    public void onBrushOpacityChanged(int opacity) {
        photoEditor.setOpacity(opacity);
    }

    @Override
    public void onBrushColorChanged(int color) {
        photoEditor.setBrushColor(color);
    }

    @Override
    public void onBrushStateChanged(boolean isEraser) {
        if (isEraser)   {
            photoEditor.brushEraser();
        }
        else    {
            photoEditor.setBrushDrawingMode(true);
        }
    }

    @Override
    public void onEmojiSelected(String emoji) {
        photoEditor.addEmoji(emoji);
    }


    @Override
    public void onAddTextButtonClicked(Typeface typeface, String text, int color) {
        photoEditor.addText(typeface,text,color);
    }

    @Override
    public void onFrameAdd(int frame) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),frame);
        photoEditor.addImage(bitmap);
    }
}
