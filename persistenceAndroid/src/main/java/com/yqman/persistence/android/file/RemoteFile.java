package com.yqman.persistence.android.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.yqman.persistence.file.FileAccessErrException;
import com.yqman.persistence.file.IDirectoryVisitor;
import com.yqman.persistence.file.IFileVisitor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

@TargetApi(Build.VERSION_CODES.N)
public class RemoteFile implements IFileVisitor {

    private DocumentFile mDocumentFile;
    private Context mContext;

    public RemoteFile(Context context, DocumentFile documentFile) {
        mContext = context;
        mDocumentFile = documentFile;
    }

    @Override
    public void writeString(String value, boolean isAppendMode) throws FileAccessErrException {
        try {
            OutputStream outputStream = getOutputStream(isAppendMode);
            if (outputStream == null) {
                throw new FileAccessErrException("can not find file");
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(value);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new FileAccessErrException(e.getMessage());
        } catch (IOException e) {
            throw new FileAccessErrException(e.getMessage());
        }
    }

    @Override
    public void writeStringNewLine(String value, boolean isAppendMode) throws FileAccessErrException {
        try {
            OutputStream outputStream = getOutputStream(isAppendMode);
            if (outputStream == null) {
                throw new FileAccessErrException("can not find file");
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.newLine();
            writer.write(value);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new FileAccessErrException(e.getMessage());
        } catch (IOException e) {
            throw new FileAccessErrException(e.getMessage());
        }
    }

    @Override
    public String readNewLine() throws FileAccessErrException {
        try {
            InputStream inputStream = getInputStream();
            if (inputStream == null) {
                throw new FileAccessErrException("can not find file");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String value = reader.readLine();
            reader.close();
            return value;
        } catch (FileNotFoundException e) {
            throw new FileAccessErrException(e.getMessage());
        } catch (IOException e) {
            throw new FileAccessErrException(e.getMessage());
        }
    }

    @Override
    public OutputStream getOutputStream(boolean isAppendMode) throws FileAccessErrException {
        try {
            return mContext.getContentResolver().openOutputStream(mDocumentFile.getUri());
        } catch (FileNotFoundException e) {
            throw new FileAccessErrException(e.getMessage());
        }
    }

    @Override
    public InputStream getInputStream() throws FileAccessErrException {
        try {
            return mContext.getContentResolver().openInputStream(mDocumentFile.getUri());
        } catch (FileNotFoundException e) {
            throw new FileAccessErrException(e.getMessage());
        }
    }

    @Override
    public String getIdentifier() {
        return mDocumentFile.getName();
    }

    @Override
    public String getDisplayName() {
        return mDocumentFile.getName();
    }

    @Override
    public long getSize() {
        return mDocumentFile.length();
    }

    @Override
    public long getMTime() {
        return mDocumentFile.lastModified();
    }

    @Override
    public IDirectoryVisitor getDirectoryVisitor() {
        try {
            return new RemoteDirectory(mContext, mDocumentFile.getParentFile());
        } catch (FileAccessErrException e) {
            return null;
        }
    }
}
