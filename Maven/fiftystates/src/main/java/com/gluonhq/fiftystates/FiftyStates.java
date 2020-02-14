/*
 * Copyright (c) 2016, 2019, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.fiftystates;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FiftyStates extends MobileApplication {

    private static final Logger LOG = Logger.getLogger(FiftyStates.class.getName());
    static {
        try {
            File root = StorageService.create().flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new IOException("Error: Storage is required"));
            Path securityPath = Path.of(root.getAbsolutePath(), "lib", "security");
            if (!Files.exists(securityPath)) {
                if (!Files.exists(Path.of(root.getAbsolutePath()))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath()));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib"))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib"));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib", "security"))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib", "security"));
                }
                copyFileFromResources("/security/blacklisted.certs", securityPath.resolve("blacklisted.certs").toString());
                copyFileFromResources("/security/cacerts.remove", securityPath.resolve("cacerts").toString());
                copyFileFromResources("/security/default.policy", securityPath.resolve("default.policy").toString());
                copyFileFromResources("/security/public_suffix_list.dat", securityPath.resolve("public_suffix_list.dat").toString());
            }
            LOG.log(Level.INFO, "securityPath = " + securityPath);
            System.setProperty("java.home", root.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStore", securityPath.resolve("cacerts").toString());
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Storage Service Error", e);
        }
    }

    @Override
    public void init() {
        addViewFactory(HOME_VIEW, BasicView::new);
    }

    @Override
    public void postInit(Scene scene) {

        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        ((Stage) scene.getWindow()).getIcons().add(new Image(FiftyStates.class.getResourceAsStream("/icon.png")));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static boolean copyFileFromResources(String pathIni, String pathEnd)  {
        try (InputStream myInput = FiftyStates.class.getResourceAsStream(pathIni)) {
            if (myInput == null) {
                LOG.log(Level.WARNING, "Error file " + pathIni + " not found");
                return false;
            }
            try (OutputStream myOutput = new FileOutputStream(pathEnd)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                LOG.log(Level.INFO, "File copied to " + pathEnd);
                return true;
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error copying file", ex);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Error copying file", ex);
        }
        return false;
    }
}
