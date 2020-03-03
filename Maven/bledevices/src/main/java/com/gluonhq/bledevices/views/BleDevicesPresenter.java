/*
 * Copyright (c) 2020, Gluon
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
package com.gluonhq.bledevices.views;

import com.gluonhq.attach.ble.BleDevice;
import com.gluonhq.attach.ble.BleDevice.State;
import com.gluonhq.attach.ble.BleService;
import com.gluonhq.bledevices.BleDevices;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BleDevicesPresenter extends GluonPresenter<BleDevices> {


    @FXML
    private View bleDevices;

    @FXML
    private CharmListView<BleDevice, String> deviceList;


    public void initialize() {

        deviceList.setPlaceholder(new Label("No BLE devices"));
        deviceList.setCellFactory(p -> new CharmListCell<>() {

            private BleDevice device;
            private final Label connectButton;
            private final ListTile tile;
            private final ChangeListener<State> stateListener = (obs, ov, nv) -> updateState(nv);

            {
                tile = new ListTile();
                tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH.graphic());
                connectButton = new Label("", MaterialDesignIcon.CAST.graphic());
                tile.setSecondaryGraphic(connectButton);

                tile.setOnMouseClicked(e -> {
                    if (device.getState().equals(State.STATE_UNKNOWN) || device.getState().equals(State.STATE_DISCONNECTED)) {
                        BleService.create().ifPresent(ble -> {
                            device.stateProperty().addListener(new InvalidationListener() {
                                @Override
                                public void invalidated(Observable observable) {
                                    if (State.STATE_CONNECTED.equals(device.getState())) {
                                        AppViewManager.BLE_SERVICES_VIEW.switchView().ifPresent(presenter ->
                                                ((BleServicesPresenter) presenter).setDevice(device));
                                        device.stateProperty().removeListener(this);
                                    }
                                }
                            });
                            ble.connect(device);
                        });
                    } else if (device.getState().equals(State.STATE_CONNECTED)) {
                        BleService.create().ifPresent(ble -> ble.disconnect(device));
                    }
                });
            }

            @Override
            public void updateItem(BleDevice item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    if (device != null) {
                        device.stateProperty().removeListener(stateListener);
                    }
                    device = item;
                    device.stateProperty().addListener(stateListener);
                    updateState(device.getState());
                    tile.setTextLine(0, item.getName() == null ? "Unknown" : item.getName());
                    tile.setTextLine(1, item.getAddress());
                    setGraphic(tile);
                } else {
                    setGraphic(null);
                }
            }

            private void updateState(BleDevice.State state) {
                switch (state) {
                    case STATE_CONNECTED:
                        tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH_CONNECTED.graphic());
                        connectButton.setGraphic(MaterialDesignIcon.CLOSE.graphic());
                        break;
                    case STATE_DISCONNECTED:
                    case STATE_UNKNOWN:
                        tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH.graphic());
                        connectButton.setGraphic(MaterialDesignIcon.CAST.graphic());
                        if (!bleDevices.isShowing()) {
                            getApp().goHome();
                        }
                        break;
                    case STATE_CONNECTING:
                    case STATE_DISCONNECTING:
                        tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH_SEARCHING.graphic());
                        break;

                }
            }

        });

        bleDevices.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        getApp().getDrawer().open()));
                appBar.setTitleText("BLE Devices");
                appBar.getActionItems().add(MaterialDesignIcon.SCANNER.button(e -> {
                    BleService.create().ifPresent(ble ->
                            deviceList.setItems(ble.startScanningDevices()));
                }));
            }
        });
    }
    
}
