/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.persistence.android.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.yqman.persistence.file.FileAccessErrException;
import com.yqman.persistence.file.IDirectoryVisitor;
import com.yqman.persistence.file.IFileVisitor;
import com.yqman.persistence.android.FileTools;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

@TargetApi(Build.VERSION_CODES.N)
public class AndroidDirectory implements IDirectoryVisitor {
    private DocumentFile mDocumentFile;
    private Context mContext;

    public AndroidDirectory(Context context, Uri treeUri) throws FileAccessErrException {
        this(context, DocumentFile.fromTreeUri(context, treeUri));
    }

    public AndroidDirectory(Context context, DocumentFile file) throws FileAccessErrException {
        mContext = context;
        mDocumentFile = file;
        if (!mDocumentFile.isDirectory()) {
            throw new FileAccessErrException("file is not directory");
        }
    }

    @Override
    public IFileVisitor createNewFile(String displayName) throws FileAccessErrException {
        DocumentFile file = mDocumentFile.createFile(FileTools.getTypeForName(displayName), displayName);
        return new AndroidFile(mContext, file);
    }

    @Override
    public boolean copyFile(IFileVisitor sourceFile, IDirectoryVisitor targetDir) throws FileAccessErrException {
        IFileVisitor targetFile = targetDir.createNewFile(sourceFile.getIdentifier());
        copyData(sourceFile, targetFile);
        return true;
    }

    private void copyData(IFileVisitor sourceFile, IFileVisitor targetFile) throws FileAccessErrException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sourceFile.getOutputStream(false)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(targetFile.getInputStream()));
        try {
            int data = reader.read();
            while (data != -1) {
                writer.write(data);
                data = reader.read();
            }
        } catch (IOException e) {
            throw new FileAccessErrException(e.getMessage());
        } finally {
            try {
                writer.close();
                reader.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    @Override
    public boolean moveFile(IFileVisitor sourceFile, IDirectoryVisitor targetDir) throws FileAccessErrException {
        copyFile(sourceFile, targetDir);
        deleteFile(sourceFile);
        return true;
    }

    @Override
    public boolean deleteFile(IFileVisitor targetFile) {
        return mDocumentFile.findFile(targetFile.getIdentifier()).delete();
    }

    @Override
    public String getIdentifier() {
        return mDocumentFile.getUri().toString();
    }

    @Override
    public String getDisplayName() {
        return mDocumentFile.getName();
    }

    @Override
    public long getMTime() {
        return mDocumentFile.lastModified();
    }

    @Override
    public IDirectoryVisitor getParent() {
        try {
            return new AndroidDirectory(mContext, mDocumentFile.getParentFile());
        } catch (FileAccessErrException e) {
            return null;
        }
    }

    @Override
    public ArrayList<IFileVisitor> listFiles() {
        ArrayList<IFileVisitor> fileVisitors = new ArrayList<>();
        for(DocumentFile file: mDocumentFile.listFiles()) {
            if (!file.isDirectory()) {
                fileVisitors.add(new AndroidFile(mContext, file));
            }
        }
        return fileVisitors;
    }

    @Override
    public ArrayList<IDirectoryVisitor> listDirectories() {
        ArrayList<IDirectoryVisitor> dirVisitors = new ArrayList<>();
        for(DocumentFile file: mDocumentFile.listFiles()) {
            if (file.isDirectory()) {
                try {
                    dirVisitors.add(new AndroidDirectory(mContext, file));
                } catch (FileAccessErrException e) {
                    // do not handle
                }
            }
        }
        return dirVisitors;
    }

}
