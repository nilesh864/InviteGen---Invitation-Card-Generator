package com.example.invitegen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class PDFGenerator {

    public static File generatePDF(Context context, String text) {
        PdfDocument pdfDocument = new PdfDocument();
        // Standard A4 size is approx 595x842 at 72dpi, but let's use a reasonable size
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(16);
        textPaint.setAntiAlias(true);

        int x = 50;
        int y = 50;
        int width = pageInfo.getPageWidth() - 100;

        StaticLayout staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0, 1.2f)
                .setIncludePad(false)
                .build();

        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();

        pdfDocument.finishPage(page);

        File file = new File(context.getExternalFilesDir(null), "Invitation.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            // Run on UI thread to show Toast
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> 
                    Toast.makeText(context, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }

        return file;
    }
}
