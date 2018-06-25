package com.dhis2.data.qr;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.reactivex.Observable;
import timber.log.Timber;

import static com.dhis2.data.qr.QRjson.ATTR_JSON;
import static com.dhis2.data.qr.QRjson.ENROLLMENT_JSON;
import static com.dhis2.data.qr.QRjson.TEI_JSON;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QRCodeGenerator implements QRInterface {



    private static final String TEI = "SELECT * FROM TrackedEntityInstance WHERE TrackedEntityInstance.uid = ?";
    private final BriteDatabase briteDatabase;
    private static final String TEI_ATTR = "SELECT * FROM " + TrackedEntityAttributeValueModel.TABLE +
            " WHERE " + TrackedEntityAttributeValueModel.TABLE + "." + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE + " = ?";
    private static final String TEI_ENROLLMENTS = "SELECT * FROM Enrollment WHERE Enrollment." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " =?";
    private final Gson gson;

    QRCodeGenerator(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        gson = new Gson();
    }

    @Override
    public Observable<List<Bitmap>> teiQRs(String teiUid) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();


        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, TEI, teiUid)
                .mapToOne(TrackedEntityInstanceModel::create)
                .map(data -> bitmaps.add(transform(TEI_JSON, gson.toJson(data))))
                .flatMap(data -> briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, TEI_ATTR, teiUid)
                        .mapToList(TrackedEntityAttributeValueModel::create))
                .map(data -> bitmaps.add(transform(ATTR_JSON, gson.toJson(data))))
                .flatMap(data -> briteDatabase.createQuery(EnrollmentModel.TABLE, TEI_ENROLLMENTS, teiUid)
                        .mapToList(EnrollmentModel::create))
                .map(data -> bitmaps.add(transform(ENROLLMENT_JSON, gson.toJson(data))))
                .flatMap(data -> Observable.just(bitmaps))
                ;
    }

    private Bitmap transform(String type, String info) {

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(gson.toJson(new QRjson(type, info)), BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


}
