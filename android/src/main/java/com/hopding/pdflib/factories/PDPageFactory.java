package com.hopding.pdflib.factories;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.RequiresPermission;
import android.content.Context;
import android.content.res.AssetManager;

import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;

/**
 * Create a PDPage object and applies actions described in JSON
 * to it, such as drawing text or images. The PDPage object can
 * be created anew, or from an existing document.
 */
public class PDPageFactory {
    protected PDDocument document;
    protected PDPage page;
    protected PDPageContentStream stream;
    private static AssetManager ASSET_MANAGER = null;

    public static void init(Context context) {
        if (ASSET_MANAGER == null) {
            ASSET_MANAGER = context.getApplicationContext().getAssets();
        }
    }

    private PDPageFactory(PDDocument document, PDPage page, boolean appendContent) throws IOException {
        this.document = document;
        this.page = page;
        this.stream = new PDPageContentStream(document, page, appendContent, true, true);
    }

    /* ----- Factory methods ----- */
    protected static PDPage create(PDDocument document, ReadableMap pageActions) throws IOException {
        PDPage page = new PDPage();
        PDPageFactory factory = new PDPageFactory(document, page, false);

        factory.setMediaBox(pageActions.getMap("mediaBox"));
        factory.applyActions(pageActions);
        factory.stream.close();
        return page;
    }

    protected static PDPage load(PDDocument document, ReadableMap pageActions) throws IOException {
        int pageIndex = pageActions.getInt("pageIndex");
        String filePath = pageActions.getString("filePath");

        if (!filePath.isEmpty()) {
            document = PDDocument.load(new File(filePath));
        }

        PDPage page = document.getPage(pageIndex);

        PDPageFactory factory = new PDPageFactory(document, page, true);

        factory.stream.close();

        return page;
    }

    protected static PDPage modify(PDDocument document, ReadableMap pageActions) throws IOException {
        int pageIndex = pageActions.getInt("pageIndex");
        PDPage page = document.getPage(pageIndex);
        PDPageFactory factory = new PDPageFactory(document, page, true);

        factory.applyActions(pageActions);
        factory.stream.close();
        return page;
    }

    /* ----- Page actions (based on JSON structures sent over bridge) ----- */
    private void applyActions(ReadableMap pageActions) throws IOException {
        HashMap<String, PDFont> fonts = new HashMap<String, PDFont>();

        ReadableArray actions = pageActions.getArray("actions");
        for (int i = 0; i < actions.size(); i++) {
            ReadableMap action = actions.getMap(i);
            String type = action.getString("type");

            if (type.equals("text")) {
                String value = action.getString("value");
                String fontName = action.getString("fontName");
                int fontSize = action.getInt("fontSize");
                String textAlign = action.hasKey("textAlign") ? action.getString("textAlign") : "left";
                int fieldSize = action.hasKey("fieldSize") ? action.getInt("fieldSize") : 0;

                Integer[] coords = getCoords(action, true);
                int[] rgbColor = hexStringToRGB(action.getString("color"));

                if (!fonts.containsKey(fontName)) {
                    PDFont font = PDType0Font.load(document, ASSET_MANAGER.open("fonts/" + fontName + ".ttf"));
                    fonts.put(fontName, font);
                }
                this.drawText(value, fontName, fontSize, textAlign, fieldSize, coords, rgbColor, fonts.get(fontName));
            } else if (type.equals("rectangle"))
                this.drawRectangle(action);
            else if (type.equals("image"))
                this.drawImage(action);
        }
    }

    private void setMediaBox(ReadableMap dimensions) {
        Integer[] coords = getCoords(dimensions, true);
        Integer[] dims = getDims(dimensions, true);
        page.setMediaBox(new PDRectangle(coords[0], coords[1], dims[0], dims[1]));
    }

    private void drawText(String value, String fontName, int fontSize, String textAlign, int fieldSize,
            Integer[] coords, int[] rgbColor, PDFont font) throws NoSuchKeyException, IOException {
        int offsetLeft = coords[0];

        if (fieldSize > 0 && textAlign != "left") {
            WritableMap textSize = PDPageFactory.getTextSize(value, font, fontSize);

            switch (textAlign) {
                case "center":
                    offsetLeft = offsetLeft + fieldSize / 2 - textSize.getInt("width") / 2;
                    break;

                case "right":
                    offsetLeft = offsetLeft + fieldSize - textSize.getInt("width");
                    break;

                // Default is "left"
                default:
                    break;
            }
        }

        stream.beginText();
        stream.setNonStrokingColor(rgbColor[0], rgbColor[1], rgbColor[2]);
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(offsetLeft, coords[1]);
        stream.showText(value);
        stream.endText();
    }

    private void drawRectangle(ReadableMap rectActions) throws NoSuchKeyException, IOException {
        Integer[] coords = getCoords(rectActions, true);
        Integer[] dims = getDims(rectActions, true);
        int[] rgbColor = hexStringToRGB(rectActions.getString("color"));

        stream.addRect(coords[0], coords[1], dims[0], dims[1]);
        stream.setNonStrokingColor(rgbColor[0], rgbColor[1], rgbColor[2]);
        stream.fill();
    }

    private void drawImage(ReadableMap imageActions) throws NoSuchKeyException, IOException {
        String imageType = imageActions.getString("imageType");
        String imagePath = imageActions.getString("imagePath");
        String imageSource = imageActions.getString("source");

        Integer[] coords = getCoords(imageActions, true);
        Integer[] dims = getDims(imageActions, false);

        if (imageType.equals("jpg") || imageType.equals("png")) {
            // Create PDImageXObject
            PDImageXObject image = null;

            if (imageSource.equals("path")) {
                if (imageType.equals("jpg")) {
                    Bitmap bmpImage = BitmapFactory.decodeFile(imagePath);
                    image = JPEGFactory.createFromImage(document, bmpImage);
                } else { // imageType.equals("png") == true
                    InputStream in = new FileInputStream(new File(imagePath));
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    image = LosslessFactory.createFromImage(document, bmp);
                }
            }

            if (imageSource.equals("assets")) {
                InputStream is = ASSET_MANAGER.open(imagePath);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                image = LosslessFactory.createFromImage(document, bmp);
            }

            // Draw the PDImageXObject to the stream
            if (dims[0] != null && dims[1] != null) {
                stream.drawImage(image, coords[0], coords[1], dims[0], dims[1]);
            } else {
                stream.drawImage(image, coords[0], coords[1]);
            }
        }
    }

    public static WritableMap getTextSize(String text, String fontName, int fontSize, AssetManager ASSET_MANAGER)
            throws IOException {
        PDDocument document = new PDDocument();
        PDFont font = PDType0Font.load(document,
                ASSET_MANAGER.open("fonts/" + fontName + ".ttf"));

        return getTextSize(text, font, fontSize);
    }

    public static WritableMap getTextSize(String text, PDFont font, int fontSize)
            throws IOException {
        float width = font.getStringWidth(text) / 1000 * fontSize;
        float height = (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;
        WritableMap map = Arguments.createMap();
        map.putInt("width", (int) width);
        map.putInt("height", (int) height);

        return map;
    }

    /* ----- Static utilities ----- */
    private static Integer[] getDims(ReadableMap dimsMap, boolean required) {
        return getIntegerKeyPair(dimsMap, "width", "height", required);
    }

    private static Integer[] getCoords(ReadableMap coordsMap, boolean required) {
        return getIntegerKeyPair(coordsMap, "x", "y", required);
    }

    private static Integer[] getIntegerKeyPair(ReadableMap map, String key1, String key2, boolean required) {
        Integer val1 = null;
        Integer val2 = null;
        try {
            val1 = map.getInt(key1);
            val2 = map.getInt(key2);
        } catch (NoSuchKeyException e) {
            if (required)
                throw e;
        }
        return new Integer[] { val1, val2 };
    }

    // We get a color as a hex string, e.g. "#F0F0F0" - so parse into RGB vals
    private static int[] hexStringToRGB(String hexString) {
        int colorR = Integer.valueOf(hexString.substring(1, 3), 16);
        int colorG = Integer.valueOf(hexString.substring(3, 5), 16);
        int colorB = Integer.valueOf(hexString.substring(5, 7), 16);
        return new int[] { colorR, colorG, colorB };
    }
}
