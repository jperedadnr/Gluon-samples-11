/*
 * Copyright (c) 2016, 2020, Gluon
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
package com.gluonhq.beacons.views;

import com.gluonhq.attach.ble.BleService;
import com.gluonhq.attach.ble.Configuration;
import com.gluonhq.attach.ble.Proximity;
import com.gluonhq.attach.ble.ScanDetection;
import com.gluonhq.beacons.Beacons;
import com.gluonhq.beacons.settings.Settings;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.inject.Inject;
import java.util.function.Consumer;

public class BeaconsPresenter extends GluonPresenter<Beacons> {

    @Inject Settings settings;
	
    @FXML
    private View beacons;

    @FXML
    private Label labelUUID;

    @FXML
    private Label labelMajor;

    @FXML
    private Label labelMinor;

    @FXML
    private Label labelRSSI;

    @FXML
    private Label labelDistance;

    @FXML
    private Label labelStatus;

    @FXML
    private Circle circleFar;

    @FXML
    private Circle circleNear;

    @FXML
    private Circle circleImmediate;
    
    private final ObjectProperty<ScanDetection> scanDetection = new SimpleObjectProperty<ScanDetection>() {
        @Override
        protected void invalidated() {
            labelUUID.setText(get().getUuid());
            labelMajor.setText(String.valueOf(get().getMajor()));
            labelMinor.setText(String.valueOf(get().getMinor()));
            labelRSSI.setText(String.valueOf(get().getRssi()));
            labelDistance.setText(get().getProximity().name());
        }
         
    };
    
    public void initialize() {
        Consumer<ScanDetection> callback = (ScanDetection t) -> {
            javafx.application.Platform.runLater(() -> scanDetection.set(t));
        };

        circleFar.setFill(null);
        circleFar.setStroke(Color.TRANSPARENT);
        circleFar.strokeProperty().bind(Bindings.createObjectBinding(() -> {
            if (scanDetection.get() != null && scanDetection.get().getProximity().equals(Proximity.FAR)) {
                circleFar.setEffect(new DropShadow(10, Color.GREEN));
                return Color.GREEN;
            }
            circleFar.setEffect(null);
            return Color.GRAY;
        }, labelDistance.textProperty()));
        circleNear.setFill(null);
        circleNear.setStroke(Color.TRANSPARENT);
        circleNear.strokeProperty().bind(Bindings.createObjectBinding(() -> {
            if (scanDetection.get() != null && scanDetection.get().getProximity().equals(Proximity.NEAR)) {
                circleNear.setEffect(new DropShadow(15, Color.GREEN));
                return Color.GREEN;
            }
            circleNear.setEffect(null);
            return Color.GRAY;
        }, labelDistance.textProperty()));
        circleImmediate.setFill(null);
        circleImmediate.setStroke(Color.TRANSPARENT);
        circleImmediate.strokeProperty().bind(Bindings.createObjectBinding(() -> {
            if (scanDetection.get() != null && scanDetection.get().getProximity().equals(Proximity.IMMEDIATE)) {
                circleImmediate.setEffect(new DropShadow(20, Color.GREEN));
                return Color.GREEN;
            }
            circleImmediate.setEffect(null);
            return Color.GRAY;
        }, labelDistance.textProperty()));

        beacons.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e ->
                        getApp().goHome()));
                appBar.setTitleText("Scan Beacons");
                ObservableList<Button> actions =
                    BleService.create()
                        .map(bleService -> {
                            final Button buttonScan = MaterialDesignIcon.BLUETOOTH_SEARCHING.button();
                            final Button buttonStop = MaterialDesignIcon.STOP.button();
                            buttonScan.setOnAction(e -> {
                                bleService.stopScanning();
                                Configuration conf = new Configuration(settings.getUuid());
                                bleService.startScanning(conf, callback);
                                buttonStop.setDisable(false);
                            });
                            buttonStop.setOnAction(e -> {
                                bleService.stopScanning();
                                buttonStop.setDisable(true);
                            });
                            labelStatus.textProperty().bind(new When(buttonScan.disableProperty())
                                    .then("Scanning for: " + settings.getUuid())
                                    .otherwise("Stopped"));
                            buttonScan.disableProperty().bind(buttonStop.disableProperty().not());
                            buttonStop.setDisable(true);

                            return FXCollections.observableArrayList(
                                    MaterialDesignIcon.SETTINGS.button(e ->
                                            AppViewManager.SETTINGS_VIEW.switchView()
                                                    .ifPresent(p -> ((SettingsPresenter) p).setupScanBeacon())),
                                    buttonScan, buttonStop);
                        })
                        .orElseGet(() -> FXCollections.singletonObservableList(
                                MaterialDesignIcon.SETTINGS.button(e ->
                                    AppViewManager.SETTINGS_VIEW.switchView()
                                            .ifPresent(p -> ((SettingsPresenter) p).setupScanBeacon()))));
                appBar.getActionItems().setAll(actions);
            }
        });
    }
    
}
